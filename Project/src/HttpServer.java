import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HttpServer {
    private static ConfigLoader config;
    private static int connectionCount;

    public static void main(String[] args) {
        config = new ConfigLoader("src/myweb.conf");
        Logs.setupLog(config.getAccessLog(), config.getErrorLog());
        try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
            Logs.logAccess("Serveur démarré sur le port " + config.getPort() + " avec le répertoire racine " + config.getRootDirectory());

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            Logs.logError("Erreur de connection serveur : " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            connectionCount++;
            try {
                if (!isAccepted(socket.getInetAddress())) {
                    Logs.logError("Connection refusée pour l'adresse IP : " + socket.getInetAddress().getHostAddress());
                    socket.close();
                    connectionCount--;
                    return;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     OutputStream out = socket.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))
                ) {
                    String requestLine = reader.readLine();
                    if (requestLine == null || requestLine.isEmpty()) {
                        Logs.logError("Requête vide ou nulle reçue");
                        return;
                    }
                    Logs.logAccess(requestLine);

                    String[] requestLineParts = requestLine.split(" ");
                    if (requestLineParts.length < 2) {
                        Logs.logError("Requête invalide : " + requestLine);
                        return;
                    }

                    String filePath = requestLineParts[1];
                    if (filePath.equals("/status")) {
                        sendStatusResponse(writer);
                    } else {
                        if (filePath.equals("/")) {
                            filePath = "/index.html";
                        }

                        filePath = config.getRootDirectory() + filePath;

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
                    if (e instanceof SocketException) {
                        Logs.logError("SocketException : " + e.getMessage());
                    }
                } finally {
                    connectionCount--;
                }
            } catch (IOException e) {
                Logs.logError("Erreur de connection client : " + e.getMessage());
            }
        }
    }

    private static void sendFileResponse(BufferedWriter writer, OutputStream out, File file) throws IOException {
        String mimeType = Files.probeContentType(file.toPath());
        System.out.println(mimeType);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        byte[] fileData = Files.readAllBytes(file.toPath());

        writer.write("HTTP/1.1 200 OK\r\n");
        writer.write("Content-Type: " + mimeType + "\r\n");
        writer.write("Content-Length: " + fileData.length + "\r\n");
        writer.write("\r\n");
        writer.flush();

        out.write(fileData);
        out.flush();
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
        if (config.getReject().contains(clientIP)) {
            return false;
        }
        if (config.getAccept().isEmpty() || config.getAccept().contains(clientIP)) {
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
