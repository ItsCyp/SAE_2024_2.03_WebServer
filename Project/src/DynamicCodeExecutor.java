import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe exécute du code dynamique en utilisant un interpréteur spécifié.
 * Elle envoie le code à l'interpréteur via un processus et récupère la sortie résultante.
 */
public class DynamicCodeExecutor {

    /**
     * Exécute le code spécifié avec l'interpréteur donné.
     *
     * @param interpreter L'interpréteur à utiliser pour exécuter le code (par exemple, python, bash, etc.)
     * @param code        Le code à exécuter
     * @return La sortie résultante de l'exécution du code, ou un message d'erreur en cas d'échec
     */
    public static String execute(String interpreter, String code) throws IOException {
        List<String> command = new ArrayList<>();
        command.add(interpreter);
        command.add("-c");
        command.add(code);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        // Lire la sortie du processus
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
            output.append("\n");
        }

        return output.toString();
    }
}
