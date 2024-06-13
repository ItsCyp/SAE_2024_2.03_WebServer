import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Cette classe charge la configuration à partir d'un fichier XML spécifié.
 * Elle récupère les paramètres tels que le port, le répertoire racine, les fichiers de log,
 * et les adresses IP acceptées ou rejetées.
 */
public class ConfigLoader {

    private Map<String, String> config; // Map pour stocker les paramètres de configuration
    private Set<String> acceptIPs;      // Ensemble pour les adresses IP acceptées
    private Set<String> rejectIPs;      // Ensemble pour les adresses IP rejetées

    /**
     * Constructeur de la classe ConfigLoader.
     * Initialise les structures de données et charge la configuration à partir du fichier spécifié.
     *
     * @param path Chemin vers le fichier de configuration XML
     */
    public ConfigLoader(String path) {
        config = new HashMap<>();
        acceptIPs = new HashSet<>();
        rejectIPs = new HashSet<>();
        loadConfig(path);
    }

    /**
     * Charge la configuration à partir du fichier XML spécifié.
     *
     * @param path Chemin vers le fichier de configuration XML
     */
    public void loadConfig(String path) {
        try {
            File configFile = new File(path);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(configFile);
            doc.getDocumentElement().normalize();

            // Récupération des valeurs des balises spécifiées dans le fichier de configuration
            config.put("port", getTagValue("port", doc, "80"));
            config.put("root", getTagValue("root", doc, "."));
            config.put("accesslog", getTagValue("accesslog", doc, "src/logs/access.log"));
            config.put("errorlog", getTagValue("errorlog", doc, "src/logs/error.log"));

            // Traitement des balises <accept> pour récupérer les adresses IP acceptées
            NodeList acceptNodes = doc.getElementsByTagName("accept");
            for (int i = 0; i < acceptNodes.getLength(); i++) {
                String[] ips = acceptNodes.item(i).getTextContent().trim().split("\\s+");
                for (String ip : ips) {
                    acceptIPs.add(ip);
                }
            }

            // Traitement des balises <reject> pour récupérer les adresses IP rejetées
            NodeList rejectNodes = doc.getElementsByTagName("reject");
            for (int i = 0; i < rejectNodes.getLength(); i++) {
                String[] ips = rejectNodes.item(i).getTextContent().trim().split("\\s+");
                for (String ip : ips) {
                    rejectIPs.add(ip);
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur de chargement de la configuration : " + e.getMessage());
        }
    }

    /**
     * Récupère la valeur d'une balise XML à partir du document.
     *
     * @param tag          Nom de la balise XML
     * @param doc          Document XML
     * @param defaultValue Valeur par défaut si la balise n'est pas trouvée
     * @return La valeur de la balise XML ou la valeur par défaut si non trouvée
     */
    private String getTagValue(String tag, Document doc, String defaultValue) {
        NodeList nodeList = doc.getElementsByTagName(tag); // Obtention de la liste des noeuds correspondant au tag spécifié
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent(); // Récupération du contenu texte du premier noeud s'il existe
        } else {
            return defaultValue; // Retourne la valeur par défaut si la balise n'est pas trouvée
        }
    }

    /**
     * Retourne le port configuré pour le serveur.
     *
     * @return Le port configuré
     */
    public int getPort() {
        return Integer.parseInt(config.get("port")); // Conversion de la valeur du port en entier et retour
    }

    /**
     * Retourne le répertoire racine configuré pour le serveur.
     *
     * @return Le répertoire racine configuré
     */
    public String getRootDirectory() {
        return config.get("root");
    }

    /**
     * Retourne le chemin du fichier de log d'accès configuré.
     *
     * @return Le chemin du fichier de log d'accès
     */
    public String getAccessLog() {
        return config.get("accesslog");
    }

    /**
     * Retourne le chemin du fichier de log d'erreur configuré.
     *
     * @return Le chemin du fichier de log d'erreur
     */
    public String getErrorLog() {
        return config.get("errorlog");
    }

    /**
     * Retourne l'ensemble des adresses IP acceptées configurées.
     *
     * @return L'ensemble des adresses IP acceptées
     */
    public Set<String> getAccept() {
        return acceptIPs;
    }

    /**
     * Retourne l'ensemble des adresses IP rejetées configurées.
     *
     * @return L'ensemble des adresses IP rejetées
     */
    public Set<String> getReject() {
        return rejectIPs;
    }
}
