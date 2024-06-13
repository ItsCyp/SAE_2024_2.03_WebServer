import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HttpServer {
    private static int port;
    private static String rootDirectory;
    private static String accessLogPath;
    private static String errorLogPath;
    private static Set<String> acceptIPs = new HashSet<>();
    private static Set<String> rejectIPs = new HashSet<>();
    private static final int DEFAULT_PORT = 80;
    private static int connectionCount = 0;

    public static void main(String[] args) {
        try {
            Map<String, String> config = ConfigLoader.loadConfig("src/config.xml");
            port = Integer.parseInt(config.get("port"));
            rootDirectory = config.get("rootDirectory");
            accessLogPath = config.get("accessLogPath");
            errorLogPath = config.get("errorLogPath");

            for (int i = 0; config.containsKey("acceptIP" + i); i++) {
                acceptIPs.add(config.get("acceptIP" + i));
            }

            for (int i = 0; config.containsKey("rejectIP" + i); i++) {
                rejectIPs.add(config.get("rejectIP" + i));
            }
            Logs.setupLog(accessLogPath, errorLogPath);
        } catch (Exception e) {
            Logs.logError("Erreur de chargement de la configuration : " + e.getMessage());
            port = DEFAULT_PORT;
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Logs.logAccess("Serveur démarré sur le port " + port);

            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    connectionCount++;
                    if (!isAccepted(socket.getInetAddress())) {
                        socket.close();
                        connectionCount--;
                        continue;
                    }

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                         OutputStream out = socket.getOutputStream();
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))
                    ) {
                        String requestLine = reader.readLine();
                        if (requestLine == null || requestLine.isEmpty()) {
                            return;
                        }
                        Logs.logAccess(requestLine); //mettre dans un fichier /access.log

                        String[] requestLineParts = requestLine.split(" ");
                        if (requestLineParts.length < 2) {
                            Logs.logError("Requête invalide : " + requestLine);
                            continue;
                        }

                        String filePath = requestLineParts[1];
                        if (filePath.equals("/status")) {
                            sendStatusResponse(writer);
                        } else {
                            if (filePath.equals("/")) {
                                filePath = "/index.html";
                            }

                            filePath = rootDirectory + filePath;

                            File file = new File(filePath);
                            if (file.exists() && !file.isDirectory()) {
                                sendFileResponse(writer, out, file);
                            } else {
                                sendErrorResponse(writer, 404, "Not Found");
                                Logs.logError("Fichier non trouvé : " + filePath);
                            }
                        }

                    } catch (IOException e) {
                        Logs.logError("Erreur de lecture de la requête : " + e.getMessage());
                        if(e instanceof SocketException){
                            Logs.logError("Information supplémentaire : " + e);
                        }
                    } finally {
                        connectionCount--;
                    }
                } catch (IOException e) {
                    Logs.logError("Erreur de connection client : " + e.getMessage());
                }
            }
        } catch (IOException e) {
            Logs.logError("Erreur de connection serveur : " + e.getMessage());
        }
    }

    private static void sendFileResponse(BufferedWriter writer, OutputStream out, File file) throws IOException {
        String mimeType = getMimeType(file);
        byte[] fileData = Files.readAllBytes(file.toPath());

        if (mimeType.startsWith("image/") || mimeType.startsWith("video/") || mimeType.startsWith("audio/")) {
            fileData = Base64.getEncoder().encode(fileData);
        }

        writer.write("HTTP/1.1 200 OK\r\n");
        writer.write("Content-Type: " + mimeType + "\r\n");
        writer.write("Content-Length: " + fileData.length + "\r\n");
        writer.write("\r\n");
        writer.flush();

        out.write(fileData);
        out.flush();
    }

    private static String getMimeType(File file) {
        String extension = "";

        String fileName = file.getName();
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }

        switch (extension) {
            case "html":
                return "text/html";
            case "css":
                return "text/css";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "mp4":
                return "video/mp4";
            case "mp3":
                return "audio/mpeg";
            // Add more cases as needed
            default:
                return "application/octet-stream";
        }
    }

    private static void sendErrorResponse(BufferedWriter writer, int statusCode, String statusMessage) throws IOException {
        writer.write("HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n");
        writer.write("Content-Type: text/html\r\n");
        writer.write("\r\n");
        writer.write("<html><body><h1>" + statusCode + " " + statusMessage + "</h1></body></html>");
        writer.flush();
    }

    private static boolean isAccepted(InetAddress clientAddress) {
        String clientIP = clientAddress.getHostAddress();
        if (rejectIPs.contains(clientIP)) {
            return false;
        }
        if (acceptIPs.isEmpty() || acceptIPs.contains(clientIP)) {
            return true;
        }
        return false;
    }

    private static void sendStatusResponse(BufferedWriter writer) throws IOException {
        String statusHtml = MachineStatus.getStatusHtml(connectionCount);
        writer.write("HTTP/1.1 200 OK\r\n");
        writer.write("Content-Type: text/html\r\n");
        writer.write("\r\n");
        writer.write(statusHtml);
        writer.flush();
    }
}
