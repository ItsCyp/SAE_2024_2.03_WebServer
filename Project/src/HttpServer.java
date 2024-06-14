import java.io.*;
import java.net.*;

/**
 * Cette classe implémente un serveur HTTP simple capable de servir des fichiers statiques
 * et de renvoyer des informations sur l'état du serveur.
 */
public class HttpServer {


    /**
     * L'objet ConfigLoader qui charge la configuration du serveur à partir du fichier de configuration.
     */
    private static ConfigLoader config;

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

    public static ConfigLoader getConfig() {
        return config;
    }
}
