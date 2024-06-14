import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Cette classe gère les logs d'accès et d'erreur pour le serveur HTTP.
 */
public class Logs {

    private static String accessLogPath;
    private static String errorLogPath;

    /**
     * Configure les fichiers de log pour les logs d'accès et d'erreur.
     * Crée les fichiers s'ils n'existent pas et configure les FileHandlers et formatters.
     *
     * @param accessLogPath Chemin vers le fichier de log d'accès
     * @param errorLogPath  Chemin vers le fichier de log d'erreur
     */
    public static void setupLog(String accessLogPath, String errorLogPath) {
        Logs.accessLogPath = accessLogPath;
        Logs.errorLogPath = errorLogPath;
    }

    /**
     * Log un message d'accès.
     *
     * @param message Message à logger
     */
    public static void logAccess(String message, String ip) {
        log(accessLogPath, message, ip); // Log un message d'accès
    }

    /**
     * Log un message d'erreur.
     *
     * @param message Message à logger
     */
    public static void logError(String message) {
        log(errorLogPath, message, null); // Log un message d'erreur
    }

    private static void log(String logPath, String message, String ip) {
        try(FileWriter fw = new FileWriter(logPath, true)) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            if (ip != null) {
                fw.write("[" + timestamp + "] " + ip + " - " + message + "\n");
            } else {
                fw.write("[" + timestamp + "] " + message + "\n");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier de log : " + e.getMessage());
        }
    }

}
