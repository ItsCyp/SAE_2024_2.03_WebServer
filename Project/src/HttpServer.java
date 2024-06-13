import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cette classe implémente un serveur HTTP simple capable de servir des fichiers statiques
 * et de renvoyer des informations sur l'état du serveur.
 */
public class HttpServer {
    private static ConfigLoader config;
    private static int connectionCount;

    /**
     * Méthode principale qui lance le serveur HTTP.
     * Charge la configuration, configure les logs, et attend les connexions entrantes.
     *
     * @param args Les arguments de ligne de commande (non utilisés dans cette application)
     */
    public static void main(String[] args) {
        // Chargement de la configuration à partir du fichier de configuration spécifié
        config = new ConfigLoader("src/myweb.conf");
        // Configuration des logs d'accès et d'erreur
        Logs.setupLog(config.getAccessLog(), config.getErrorLog());
        try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
            // Logs pour indiquer que le serveur a démarré avec les paramètres configurés
            Logs.logAccess("Serveur démarré sur le port " + config.getPort() + " avec le répertoire racine " + config.getRootDirectory());

            // Boucle principale pour accepter les connexions entrantes des clients
            while (true) {
                Socket socket = serverSocket.accept();
                // Démarrage d'un nouveau thread pour gérer chaque client
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            // En cas d'erreur lors du démarrage du serveur
            Logs.logError("Erreur de connection serveur : " + e.getMessage());
        }
    }

    /**
     * Classe interne qui gère les connexions des clients.
     */
    private static class ClientHandler implements Runnable {
        private Socket socket;

        /**
         * Constructeur de la classe ClientHandler.
         *
         * @param socket Le socket représentant la connexion avec le client
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Méthode principale exécutée lorsqu'un client se connecte.
         * Gère la lecture de la requête HTTP, la validation de l'adresse IP,
         * la gestion des fichiers demandés, et l'envoi des réponses appropriées.
         */
        @Override
        public void run() {
            connectionCount++;
            try {
                if (!isAccepted(socket.getInetAddress())) {
                    // Logs pour indiquer le refus de connexion pour une adresse IP non autorisée
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
                        // Logs pour indiquer la réception d'une requête vide ou nulle
                        Logs.logError("Requête vide ou nulle reçue");
                        return;
                    }
                    // Logs pour enregistrer la requête HTTP reçue
                    Logs.logAccess(requestLine);

                    String[] requestLineParts = requestLine.split(" ");
                    if (requestLineParts.length < 2) {
                        // Logs pour indiquer une requête HTTP invalide
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
                            // Logs pour indiquer qu'un fichier demandé n'a pas été trouvé
                            Logs.logError("Fichier non trouvé : " + filePath);
                        }
                    }
                } catch (IOException e) {
                    // Logs pour indiquer une erreur lors de la lecture de la requête
                    Logs.logError("Erreur de lecture de la requête : " + e.getMessage());
                    if (e instanceof SocketException) {
                        // Logs pour indiquer une exception de socket
                        Logs.logError("SocketException : " + e.getMessage());
                    }
                } finally {
                    connectionCount--;
                }
            } catch (IOException e) {
                // Logs pour indiquer une erreur de connexion avec le client
                Logs.logError("Erreur de connection client : " + e.getMessage());
            }
        }
    }

    /**
     * Méthode qui envoie le contenu d'un fichier en réponse à une requête.
     * Gère différents types MIME et traite les fichiers spéciaux HTML avec des sections de code dynamique.
     *
     * @param writer Le writer pour écrire la réponse HTTP
     * @param out    Le flux de sortie pour écrire le contenu binaire (pour les fichiers binaires)
     * @param file   Le fichier à envoyer en réponse
     * @throws IOException En cas d'erreur d'entrée/sortie lors de la lecture ou de l'écriture
     */
    private static void sendFileResponse(BufferedWriter writer, OutputStream out, File file) throws IOException {
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        byte[] fileData = Files.readAllBytes(file.toPath());

        if (mimeType.startsWith("image") || mimeType.startsWith("video") || mimeType.startsWith("audio")) {
            String base64File = encodeFileBase64(fileData);
            writer.write("HTTP/1.1 200 OK\r\n");
            writer.write("Content-Type: " + mimeType + "\r\n");
            writer.write("Content-Length: " + base64File.length() + "\r\n");
            writer.write("\r\n");
            writer.write(base64File);
            writer.flush();
        } else if (mimeType.equals("text/html")) {
            Pattern pattern = Pattern.compile("<code\\s+interpreteur=\"(.*?)\">(.*?)</code>");
            Matcher matcher = pattern.matcher(new String(fileData));

            StringBuilder result = new StringBuilder();
            while (matcher.find()) {
                String interpreter = matcher.group(1);
                String code = matcher.group(2);

                String output = DynamicCodeExecutor.execute(interpreter, code);
                matcher.appendReplacement(result, output);
            }
            matcher.appendTail(result);

            byte[] modifiedContent = result.toString().getBytes(StandardCharsets.UTF_8);
            writer.write("HTTP/1.1 200 OK\r\n");
            writer.write("Content-Type: text/html\r\n");
            writer.write("Content-Length: " + modifiedContent.length + "\r\n");
            writer.write("\r\n");
            writer.flush();

            out.write(modifiedContent);
        } else {
            writer.write("HTTP/1.1 200 OK\r\n");
            writer.write("Content-Type: " + mimeType + "\r\n");
            writer.write("Content-Length: " + fileData.length + "\r\n");
            writer.write("\r\n");
            writer.flush();

            out.write(fileData);
        }

        out.flush();
    }

    /**
     * Méthode qui encode un tableau de bytes en Base64.
     *
     * @param fileData Les données à encoder
     * @return La représentation Base64 des données
     */
    private static String encodeFileBase64(byte[] fileData) {
        return Base64.getEncoder().encodeToString(fileData);
    }

    /**
     * Méthode qui envoie une réponse d'erreur HTTP.
     *
     * @param writer        Le writer pour écrire la réponse HTTP
     * @param statusCode    Le code d'état HTTP (par exemple, 404 pour Not Found)
     * @param statusMessage Le message d'état associé au code
     * @throws IOException En cas d'erreur d'entrée/sortie lors de l'écriture
     */
    private static void sendErrorResponse(BufferedWriter writer, int statusCode, String statusMessage) throws IOException {
        writer.write("HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n");
        writer.write("Content-Type: text/html\r\n");
        writer.write("\r\n");
        writer.write("<html><body><h1>" + statusCode + " " + statusMessage + "</h1></body></html>");
        writer.flush();
    }

    /**
     * Méthode qui vérifie si une adresse IP est autorisée à se connecter au serveur.
     *
     * @param clientAddress L'adresse IP du client
     * @return true si l'adresse IP est autorisée, false sinon
     */
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

    /**
     * Méthode qui envoie une réponse de statut contenant les informations actuelles sur le serveur.
     *
     * @param writer Le writer pour écrire la réponse HTTP
     * @throws IOException En cas d'erreur d'entrée/sortie lors de l'écriture
     */
    private static void sendStatusResponse(BufferedWriter writer) throws IOException {
        String statusHtml = MachineStatus.getStatusHtml(connectionCount);
        writer.write("HTTP/1.1 200 OK\r\n");
        writer.write("Content-Type: text/html\r\n");
        writer.write("\r\n");
        writer.write(statusHtml);
        writer.flush();
    }
}
