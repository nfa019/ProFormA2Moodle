package logic;

import Entity.TaskXMLData;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.net.StandardSocketOptions;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Die XMLProcessor-Klasse dient zum Parsen und Extrahieren von Informationen aus XML-Dateien.
 * Sie verarbeitet eine Liste von TaskXMLData-Objekten, die XML-Dateien repraesentieren,
 * extrahiert relevante Informationen aus diesen Dateien und speichert sie in den TaskXMLData-Objekten.
 */
public class XMLProcessor {
    private static final String[] PROFORMA_TASK_XML_NAMESPACES = {
            "urn:proforma:v2.1"
    };
    private static final Map<String, List<String>> GRADER_VERSIONS = Map.of(
            "Graflap", List.of("1.0"),
            "Graja", List.of("2.2", "2.3"),
            "Asqlg", List.of("2.0")
    );
    private String prefix;
    /**
     * Verarbeitet eine Liste von TaskXMLData-Objekten, indem sie die zugehoerigen XML-Dateien liest und analysiert.
     * Extrahiert spezifische Informationen aus den XML-Dateien und speichert diese in den entsprechenden TaskXMLData-Objekten.
     *
     * @param taskXMLDataList Die Liste von TaskXMLData-Objekten.
     * @return Ein String, der alle extrahierten XML-Tags enthält.
     * @throws ParserConfigurationException wenn ein Konfigurationsfehler beim Initialisieren des XML-Parsers auftritt.
     * @throws SAXException wenn ein Fehler beim Parsen der XML-Dokumente auftritt.
     * @throws IOException wenn ein Ein-/Ausgabefehler auftritt.
     */
    public String convertToMoodleXML(List<TaskXMLData> taskXMLDataList) throws ParserConfigurationException, SAXException, IOException {
        StringBuilder allTags = new StringBuilder();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        for (TaskXMLData data : taskXMLDataList) {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(data.getTaskXMLDatei());
            this.prefix = detectProformaNamespacePrefix(document.getDocumentElement().getAttributes());
            extractTaskXMLData(document, data);
        }

        MoodleXMLConverter converter = new MoodleXMLConverter(taskXMLDataList);
        System.out.println(converter.createMoodleXML());
        return allTags.toString();
    }
    /**
     * Extrahiert spezifische Daten aus einem XML-Dokument und speichert sie in einem TaskXMLData-Objekt.
     * Diese Methode liest verschiedene XML-Elemente und setzt die entsprechenden Eigenschaften im TaskXMLData-Objekt.
     *
     * @param document Das XML-Dokument, aus dem Daten extrahiert werden.
     * @param extractedData Das TaskXMLData-Objekt, in dem die extrahierten Daten gespeichert werden sollen.
     */
    private void extractTaskXMLData(Document document, TaskXMLData extractedData) {
        extractedData.setQuestionName(getElementContent(document, this.prefix + "title"));
        extractedData.setQuestionText(getElementContent(document, this.prefix + "description"));
        extractedData.setGeneralFeedback(extractGeneralFeedback(document));
        extractedData.setInternalDescription(getElementContent(document, this.prefix + "internal-description"));
        extractGradernameAndGraderversion(document, extractedData);
        extractedData.setTaskuuid(document.getDocumentElement().getAttribute("uuid"));
    }

    /**
     * Ermittelt den Namen und die Version des Bewertungssystems (Grader) basierend auf der Programmiersprache in der task.xml.
     * Wenn eine Version fuer den Grader definiert ist, wird die aktuellste Version ausgewaehlt und im TaskXMLData-Objekt gespeichert.
     *
     * @param document Das XML-Dokument, aus dem die Programmiersprache gelesen wird.
     * @param extractedData Das TaskXMLData-Objekt, in dem der Name und die Version des Graders gespeichert werden sollen.
     */
    public void extractGradernameAndGraderversion(Document document, TaskXMLData extractedData){
        String proglang = getElementContent(document, this.prefix + "proglang");

        String graderName;
        String graderVersion = "1.0";
        switch (proglang) {
            case "plaintext":
                graderName = "Graflap";
                break;
            case "GraFLAP":
                graderName = "Graflap";
                break;
            case "java":
                graderName = "Graja";
                break;
            case "SQL":
                graderName = "Asqlg";
                break;
            default:
                graderName = "DummyGrader";
                break;
        }

        List<String> versions = GRADER_VERSIONS.get(graderName);
        if (versions != null && !versions.isEmpty()) {
            graderVersion = VersionComparator.getLatestVersion(versions);
        }

        extractedData.setGraderName(graderName);
        extractedData.setGraderVersion(graderVersion);
    }

    /**
     * Extrahiert den Inhalt des 'general-feedback'-Elements aus einem XML-Dokument.
     * Diese Methode sucht nach dem 'general-feedback'-Element und extrahiert dessen Textinhalt.
     *
     * @param document Das XML-Dokument, aus dem der 'general-feedback'-Inhalt extrahiert wird.
     * @return Ein String, der den Inhalt des 'general-feedback'-Elements darstellt.
     *         Gibt einen leeren String zurueck, wenn das Element nicht gefunden wird.
     */
    private String extractGeneralFeedback(Document document) {
        NodeList fileList = document.getElementsByTagName( this.prefix + "file");
        for (int i = 0; i < fileList.getLength(); i++) {
            Node fileNode = fileList.item(i);
            if (fileNode.getNodeType() == Node.ELEMENT_NODE) {
                Element fileElement = (Element) fileNode;
                if ("general-feedback".equals(fileElement.getAttribute("id"))) {
                    NodeList embeddedFiles = fileElement.getElementsByTagName(this.prefix + "embedded-txt-file");
                    if (embeddedFiles.getLength() > 0) {
                        Element embeddedFile = (Element) embeddedFiles.item(0);
                        return embeddedFile.getTextContent().trim();
                    }
                }
            }
        }
        return "";
    }

    /**
     * Ermittelt und gibt den Textinhalt eines spezifischen Elements im XML-Dokument zurueck.
     * Diese Methode sucht nach dem ersten Vorkommen eines Elements mit dem angegebenen Tag-Namen und gibt dessen Textinhalt zurueck.
     *
     * @param document Das XML-Dokument, das durchsucht wird.
     * @param tagName Der Name des XML-Tags.
     * @return Ein String, der den Inhalt des gesuchten Elements darstellt.
     *         Gibt einen leeren String zurueck, wenn das Element nicht gefunden wird.
     */
    private static String getElementContent(Document document, String tagName) {
        NodeList nodeList = document.getElementsByTagName(tagName);

        if (nodeList != null && nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return node.getTextContent().trim();
            }
        }
        return "";
    }

    /**
     * Ermittelt das Praefix des Proforma-Namensraums im XML-Dokument.
     * @param attributes Die Attribute des Root-Elements des XML-Dokuments.
     * @return Das Namensraum-Praefix inklusive Doppelpunkt (z.B. "p:"), oder ein leerer String, falls nicht gefunden.
     */
    private static String detectProformaNamespacePrefix(NamedNodeMap attributes) {
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            if (attribute.getNodeName().startsWith("xmlns:")) {
                String namespaceUri = attribute.getNodeValue();
                for (String knownNamespace : PROFORMA_TASK_XML_NAMESPACES) {
                    if (knownNamespace.equals(namespaceUri)) {
                        return attribute.getNodeName().substring(6) + ":";
                    }
                }
            }
        }
        return "";
    }

}
