import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Cette classe gère les logs d'accès et d'erreur pour le serveur HTTP.
 */
public class Logs {
    private static Logger accessLogger = Logger.getLogger("AccessLog");
    private static Logger errorLogger = Logger.getLogger("ErrorLog");

    /**
     * Configure les fichiers de log pour les logs d'accès et d'erreur.
     * Crée les fichiers s'ils n'existent pas et configure les FileHandlers et formatters.
     *
     * @param accessLogPath Chemin vers le fichier de log d'accès
     * @param errorLogPath  Chemin vers le fichier de log d'erreur
     */
    public static void setupLog(String accessLogPath, String errorLogPath) {
        try {
            // Vérifie et crée les fichiers de log s'ils n'existent pas
            File accessLogFile = new File(accessLogPath);
            if (!accessLogFile.exists()) {
                accessLogFile.createNewFile();
            }

            File errorLogFile = new File(errorLogPath);
            if (!errorLogFile.exists()) {
                errorLogFile.createNewFile();
            }

            // Initialise les FileHandlers pour les logs d'accès et d'erreur
            FileHandler accessHandler = new FileHandler(accessLogPath, true);
            FileHandler errorHandler = new FileHandler(errorLogPath, true);
            accessHandler.setFormatter(new SimpleFormatter());
            errorHandler.setFormatter(new SimpleFormatter());
            accessLogger.addHandler(accessHandler); // Ajoute le FileHandler au logger d'accès
            errorLogger.addHandler(errorHandler); // Ajoute le FileHandler au logger d'erreur
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Log un message d'accès.
     *
     * @param message Message à logger
     */
    public static void logAccess(String message) {
        accessLogger.info(message); // Log un message d'accès avec niveau INFO
    }

    /**
     * Log un message d'erreur.
     *
     * @param message Message à logger
     */
    public static void logError(String message) {
        errorLogger.severe(message); // Log un message d'erreur avec niveau SEVERE
    }

}
