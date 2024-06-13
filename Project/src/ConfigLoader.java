import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {

    public static Map<String, String> loadConfig(String path) throws Exception{
        Map<String, String> config = new HashMap<>();
        File configFile = new File(path);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(configFile);
        doc.getDocumentElement().normalize();

        config.put("port", doc.getElementsByTagName("port").item(0).getTextContent());
        config.put("rootDirectory", doc.getElementsByTagName("root").item(0).getTextContent());
        config.put("accessLogPath", doc.getElementsByTagName("acceslog").item(0).getTextContent());
        config.put("errorLogPath", doc.getElementsByTagName("errorlog").item(0).getTextContent());

        NodeList acceptIPs = doc.getElementsByTagName("acceptIP");
        for (int i = 0; i < acceptIPs.getLength(); i++) {
            config.put("acceptIP" + i, acceptIPs.item(i).getTextContent());
        }

        NodeList rejectIPs = doc.getElementsByTagName("rejectIP");
        for (int i = 0; i < rejectIPs.getLength(); i++) {
            config.put("rejectIP" + i, rejectIPs.item(i).getTextContent());
        }

        return config;
    }
}
