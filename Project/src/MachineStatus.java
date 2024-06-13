import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Cette classe fournit des informations sur l'état de la machine serveur.
 * Elle génère une page HTML affichant la mémoire disponible, l'espace disque disponible,
 * le nombre de processus et le nombre de connexions actives.
 */
public class MachineStatus {

    /**
     * Génère une représentation HTML de l'état actuel de la machine serveur.
     *
     * @param connectionCount Le nombre de connexions actives
     * @return Une chaîne HTML représentant l'état du serveur
     */
    public static String getStatusHtml(int connectionCount) {
        // Obtient l'instance de Runtime pour récupérer des informations sur la mémoire
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();

        // Calcule l'espace disque total disponible sur toutes les partitions
        File[] roots = File.listRoots();
        long freeSpace = 0;
        for (File root : roots) {
            freeSpace += root.getFreeSpace();
        }

        // Obtient l'instance d'OperatingSystemMXBean pour récupérer le nombre de processeurs disponibles
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        int processCount = osBean.getAvailableProcessors();

        // Construit la réponse HTML contenant les informations sur l'état du serveur
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset=\"UTF-8\"></head><body>");
        html.append("<h1>État du serveur</h1>");
        html.append("<p>Mémoire disponible: ").append(freeMemory).append(" bytes</p>");
        html.append("<p>Espace disque disponible: ").append(freeSpace).append(" bytes</p>");
        html.append("<p>Nombre de processus: ").append(processCount).append("</p>");
        html.append("<p>Nombre de connexions actives: ").append(connectionCount).append("</p>");
        html.append("</body></html>");

        return html.toString();
    }

}
