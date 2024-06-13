import java.io.*;

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
    public static String execute(String interpreter, String code) {
        String output = "";

        try {
            // Démarrage du processus avec l'interpréteur spécifié
            Process process = new ProcessBuilder(interpreter.trim()).start();

            // Écriture du code à exécuter dans le flux de sortie du processus
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writer.write(code);
            writer.flush();
            writer.close();

            // Lecture de la sortie du processus (résultat de l'exécution du code)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output += line + "\n";
            }
            reader.close();

            // Attente de la fin de l'exécution du processus
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            // En cas d'erreur lors de l'exécution du processus
            output = "Erreur d'exécution : " + e.getMessage();
        }

        return output; // Retourne la sortie résultante de l'exécution du code ou un message d'erreur
    }
}
