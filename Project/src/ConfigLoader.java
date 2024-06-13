import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigLoader {

    private Map<String, String> config;
    private Set<String> acceptIPs;
    private Set<String> rejectIPs;

    public ConfigLoader(String path){
        config = new HashMap<>();
        acceptIPs = new HashSet<>();
        rejectIPs = new HashSet<>();
        loadConfig(path);
    }

    public void loadConfig(String path){
        try{
            File configFile = new File(path);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(configFile);
            doc.getDocumentElement().normalize();

            config.put("port", getTagValue("port", doc, "80"));
            config.put("root", getTagValue("root", doc, "."));
            config.put("accesslog", getTagValue("accesslog", doc, "src/logs/access.log"));
            config.put("errorlog", getTagValue("errorlog", doc, "src/logs/error.log"));

            NodeList acceptNodes = doc.getElementsByTagName("accept");
            for (int i = 0; i < acceptNodes.getLength(); i++) {
                String[] ips = acceptNodes.item(i).getTextContent().trim().split("\\s+");
                for (String ip : ips) {
                    acceptIPs.add(ip);
                }
            }

            NodeList rejectNodes = doc.getElementsByTagName("reject");
            for (int i = 0; i < rejectNodes.getLength(); i++) {
                String[] ips = rejectNodes.item(i).getTextContent().trim().split("\\s+");
                for (String ip : ips) {
                    rejectIPs.add(ip);
                }
            }
        }catch (Exception e) {
            System.out.println("Erreur de chargement de la configuration : " + e.getMessage());
        }
    }

    private String getTagValue(String tag, Document doc, String defaultValue) {
        NodeList nodeList = doc.getElementsByTagName(tag);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        } else {
            return defaultValue;
        }
    }

    public int getPort() { return Integer.parseInt(config.get("port")); }
    public String getRootDirectory() { return config.get("root"); }
    public String getAccessLog() { return config.get("accesslog"); }
    public String getErrorLog() { return config.get("errorlog"); }
    public Set<String> getAccept() { return acceptIPs; }
    public Set<String> getReject() { return rejectIPs; }
}
