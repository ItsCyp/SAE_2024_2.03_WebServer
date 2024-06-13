import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
    private static Logger accessLogger = Logger.getLogger("AccessLog");
    private static Logger errorLogger = Logger.getLogger("ErrorLog");

    public static void setupLog(String accessLogPath, String errorLogPath) throws IOException {
        try {
            File accessLogFile = new File(accessLogPath);
            if (!accessLogFile.exists()) {
                accessLogFile.createNewFile();
            }

            File errorLogFile = new File(errorLogPath);
            if (!errorLogFile.exists()) {
                errorLogFile.createNewFile();
            }

            FileHandler accessHandler = new FileHandler(accessLogPath, true);
            FileHandler errorHandler = new FileHandler(errorLogPath, true);
            accessHandler.setFormatter(new SimpleFormatter());
            errorHandler.setFormatter(new SimpleFormatter());
            accessLogger.addHandler(accessHandler);
            errorLogger.addHandler(errorHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logAccess(String message) {
        accessLogger.info(message);
    }

    public static void logError(String message) {
        errorLogger.severe(message);
    }

}
