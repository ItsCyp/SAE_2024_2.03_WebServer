import java.lang.management.ManagementFactory;

public class MachineStatus {

    public static String getStatusHtml(int connectionCount) {
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        double systemLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();

        // Déterminer l'état général
        String status;
        if (freeMemory < 100 * 1024 * 1024) { // Exemple : moins de 100 Mo de mémoire libre
            status = "Mauvais";
        } else if (systemLoad > 0.8) { // Exemple : charge système supérieure à 80%
            status = "Mauvais";
        } else {
            status = "Bon";
        }

        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset=\"UTF-8\"></head><body>");
        html.append("<h1>État de la machine</h1>");
        html.append("<p>État général: ").append(status).append("</p>");
        html.append("<p>Mémoire libre: ").append(freeMemory).append(" bytes</p>");
        html.append("<p>Charge système moyenne: ").append(systemLoad).append("</p>");
        html.append("<p>Nombre de connexions actives: ").append(connectionCount).append("</p>");
        html.append("</body></html>");

        return html.toString();
    }



}
