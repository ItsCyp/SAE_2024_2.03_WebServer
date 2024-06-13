import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class MachineStatus {

    public static String getStatusHtml(int connectionCount) {
        Runtime runtime = Runtime.getRuntime();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;
        double systemLoad = osBean.getSystemLoadAverage();

        File[] roots = File.listRoots();
        long freeSpace = 0;
        for (File root : roots) {
            freeSpace += root.getFreeSpace();
        }

        int processCount = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();

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
