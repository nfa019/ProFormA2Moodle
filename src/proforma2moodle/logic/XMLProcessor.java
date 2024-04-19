package proforma2moodle.logic;

import proforma2moodle.Entity.TaskXMLData;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.of;

/**
 * Die XMLProcessor-Klasse dient zum Parsen und Extrahieren von Informationen aus XML-Dateien.
 * Sie verarbeitet eine Liste von TaskXMLData-Objekten, die XML-Dateien repraesentieren,
 * extrahiert relevante Informationen aus diesen Dateien und speichert sie in den TaskXMLData-Objekten.
 */
public class XMLProcessor {
    private static final String[] PROFORMA_TASK_XML_NAMESPACES = {
            "urn:proforma:v2.1"
    };

    private static final String[] META_XML_NAMESPACES = {
            "urn:proforma:lmsinputfields:v0.1"
    };
    private static final Map<String, List<String>> GRADER_VERSIONS = of(
            "Graflap", List.of("1.0"),
            "Graja", List.of("2.2", "2.3"),
            "Asqlg", List.of("2.0")
    );
    private String prefix;
    private String metaPrefix;

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
            this.prefix = detectProformaNamespacePrefix(document.getDocumentElement().getAttributes(), PROFORMA_TASK_XML_NAMESPACES);
            this.metaPrefix = detectProformaNamespacePrefix(document.getDocumentElement().getAttributes(), META_XML_NAMESPACES);
            extractTaskXMLData(document, data);
        }

        MoodleXMLConverter converter = new MoodleXMLConverter(taskXMLDataList);
        String MoodleXML = converter.createMoodleXML();
        // System.out.println(MoodleXML);
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
        String taskTitle = getElementContent(document, this.prefix + "title");
        String taskDescription = getElementContent(document, this.prefix + "description");
        taskDescription = "<h3>"+taskTitle+"</h3>"+System.lineSeparator()+taskDescription;
        extractedData.setQuestionName(taskTitle);
        extractedData.setQuestionText(taskDescription);
        extractedData.setGeneralFeedback(extractGeneralFeedback(document));
        extractedData.setInternalDescription(getElementContent(document, this.prefix + "internal-description"));
        extractGradernameAndGraderversion(document, extractedData);
        extractedData.setTaskuuid(document.getDocumentElement().getAttribute("uuid"));
        extractTaskGrade(document, extractedData);
        extractTaskXMLMetaData(document, extractedData);
    }

    /**
     * Extrahiert den defaultgrade aus der Task
     * und setzt die entsprechenden Eigenschaften im TaskXMLData-Objekt.
     *
     * @param document Das XML-Dokument, aus dem Daten extrahiert werden.
     * @param extractedData Das TaskXMLData-Objekt, in dem die extrahierten Daten gespeichert werden sollen.
     */
    private void extractTaskGrade(Document document, TaskXMLData extractedData) {
        double points=0.0d;
        HashMap<String,String> combine=new HashMap<String,String>();
        HashMap<String,String> combine11=new HashMap<String,String>();
        HashMap<String,String> combine12=new HashMap<String,String>();
         HashMap<String,Double> combine13=new HashMap<String,Double>();
        NodeList nodeList = document.getElementsByTagName(prefix+"grading-hints");
        if (nodeList != null && nodeList.getLength() > 0) {
            Node node = nodeList.item(0); // root
            NodeList nodeList1 = node.getChildNodes();
            if (nodeList1 != null && nodeList1.getLength() > 0) {
                for (int i = 0; i < nodeList1.getLength(); i++) {
                    Node node1 = nodeList1.item(i);
                    if (node1.getNodeName().equals(prefix + "root")) {
                        NamedNodeMap att = node1.getAttributes();
                        if (att.getNamedItem("function").getTextContent().equals("sum")) {
                            NodeList nol = node1.getChildNodes();
                            for (int j = 0; j < nol.getLength(); j++) {
                                Node n = nol.item(j);
                                if (n.getNodeName().equals(prefix + "test-ref")) {
                                    NamedNodeMap natt = n.getAttributes();
                                    String ptString = natt.getNamedItem("weight").getTextContent();
                                    points += Double.parseDouble(ptString);
                                }
                                if (n.getNodeName().equals(prefix + "combine-ref")) {
                                    NamedNodeMap natt = n.getAttributes();
                                    String nameCom = natt.getNamedItem("ref").getTextContent();
                                    String ptString = natt.getNamedItem("weight").getTextContent();
                                    combine.put(nameCom, ptString);
                                }
                            }
                        }
                    }
                }
            }
            if (nodeList1 != null && nodeList1.getLength() > 0) {
                for (int i = 0; i < nodeList1.getLength(); i++) {
                    Node node1 = nodeList1.item(i); // combined tests
                    if (node1.getNodeName().equals(prefix+"combine")){
                        double ptcom = 0.0d;
                        NamedNodeMap natt= node1.getAttributes();
                        String nameCom = natt.getNamedItem("id").getTextContent();
                        if (natt.getNamedItem("function").getTextContent().equals("sum")) {
                            NodeList nol1 = node1.getChildNodes();
                            for (int k = 0; k < nol1.getLength(); k++) {
                                Node nk = nol1.item(k);
                                if (nk.getNodeName().equals(prefix + "test-ref")) {
                                    NamedNodeMap nattk = nk.getAttributes();
                                    String ptkString = nattk.getNamedItem("weight").getTextContent();
                                    ptcom += Double.parseDouble(ptkString);
                                }
                                if (nk.getNodeName().equals(prefix + "combine-ref")) {
                                    NamedNodeMap nattk = nk.getAttributes();
                                    String nameCom1 = nattk.getNamedItem("ref").getTextContent();
                                    String ptString1 = nattk.getNamedItem("weight").getTextContent();
                                    combine11.put(nameCom1,nameCom);
                                    combine12.put(nameCom1, ptString1);
                                }
                            }
                        }
                        if (natt.getNamedItem("function").getTextContent().equals("min")) {
                            NodeList nol1 = node1.getChildNodes(); // combined in combined
                            double ptcom1 = 0.0d;
                            for (int k = 0; k < nol1.getLength(); k++) {
                                Node nk = nol1.item(k);
                                if (nk.getNodeName().equals(prefix + "test-ref")) {
                                    NamedNodeMap nattk = nk.getAttributes();
                                    String ptkString = nattk.getNamedItem("weight").getTextContent();
                                    ptcom1 = Math.max(ptcom1, (double) (Double.parseDouble(ptkString)));
                                }
                            }
                            combine13.put(nameCom,ptcom1);
                        }
                        if (combine.containsKey(nameCom)){
                            // combined in root
                            points += ptcom * Double.parseDouble(combine.get(nameCom));}
                    }
                }
            }

        }

        for (String k : combine11.keySet()){
            // combined in combined
            points += Double.parseDouble(combine12.get(k)) * combine13.get(k);
        }
        points = Math.round(points);
        extractedData.setDefaultgrade(String.valueOf(points));

    }

    /**
     * Extrahiert spezifische Daten aus einem XML-Dokument und speichert sie in einem TaskXMLData-Objekt.
     * Diese Methode liest verschiedene XML-Elemente aus den Metadaten
     * hier: Freitextfelder und Filenamen
     * und setzt die entsprechenden Eigenschaften im TaskXMLData-Objekt.
     *
     * @param document Das XML-Dokument, aus dem Daten extrahiert werden.
     * @param extractedData Das TaskXMLData-Objekt, in dem die extrahierten Daten gespeichert werden sollen.
     */
    private void extractTaskXMLMetaData(Document document, TaskXMLData extractedData) {
        NodeList nodeList = document.getElementsByTagName(this.metaPrefix+"textfield");
        int nrFields = nodeList.getLength();
        if (nrFields>0){
            extractedData.setEnableFreeTextSubmissions("1");
            extractedData.setFtsNumInitialFields(String.valueOf(nrFields));
            extractedData.setFtsMaxnumFields(String.valueOf(nrFields));
            for (int i=0; i<nrFields; i++){
                Node el = nodeList.item(i);
                NamedNodeMap namedNodeMap = el.getAttributes();
                if (namedNodeMap.getNamedItem("fixedfilename").equals("true")){
                    extractedData.setFtsAutoGenerateFileNames("0");
                    break;
                }
            }
        }
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
        String freetextlang ="txt";
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
                freetextlang = "java";
                break;
            case "SQL":
                graderName = "Asqlg";
                freetextlang = "SQL";
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
        extractedData.setFtsStandardLang(freetextlang);
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
     * @param nameSpaces für ProformA selbst oder Metadaten
     * @return Das Namensraum-Praefix inklusive Doppelpunkt (z.B. "p:"), oder ein leerer String, falls nicht gefunden.
     */
    private static String detectProformaNamespacePrefix(NamedNodeMap attributes, String[] nameSpaces) {
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            if (attribute.getNodeName().startsWith("xmlns:")) {
                String namespaceUri = attribute.getNodeValue();
                for (String knownNamespace : nameSpaces) {
                    if (knownNamespace.equals(namespaceUri)) {
                        return attribute.getNodeName().substring(6) + ":";
                    }
                }
            }
        }
        return "";
    }

}
