import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class HttpServer {
    private static final int DEFAULT_PORT = 80;
    private static final String ROOT_DIRECTORY = "src/site";

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server connecter au port " + port);

            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                         OutputStream out = socket.getOutputStream();
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))
                    ) {
                        String requestLine = reader.readLine();
                        if (requestLine == null || requestLine.isEmpty()) {
                            return;
                        }
                        System.out.println("Requete : " + requestLine);

                        String[] requestLineParts = requestLine.split(" ");
                        if (requestLineParts.length < 2) {
                            System.out.println("Requete invalide");
                            continue;
                        }

                        String filePath = requestLineParts[1];
                        if (filePath.equals("/")) {
                            filePath = "/index.html";
                        }

                        filePath = ROOT_DIRECTORY + filePath;

                        File file = new File(filePath);
                        if (file.exists() && !file.isDirectory()) {
                            sendFileResponse(writer, out, file);
                        } else {
                            sendErrorResponse(writer, 404, "Not Found");
                        }
                    } catch (IOException e) {
                        System.out.println("Erreur de traitement de la requête : " + e.getMessage());
                    }
                } catch (IOException e) {
                    System.out.println("Erreur de connection client : " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur server : " + e.getMessage());
        }
    }

    private static void sendFileResponse(BufferedWriter writer, OutputStream out, File file) throws IOException {
        String mimeType = Files.probeContentType(file.toPath());
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
}
