package logic;

import java.io.*;
import javax.swing.*;
import java.util.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import Entity.TaskXMLData;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;


/**
 * Die Klasse FileChooser ist verantwortlich für das Auswaehlen und Verarbeiten von Dateien.
 * Sie ermoeglicht das Oeffnen von XML- und ZIP-Dateien sowie das Durchsuchen von Verzeichnissen.
 * Nach dem Auswaehlen der Dateien verarbeitet sie diese und speichert sie im gewuenschten Speicherort ab.
 */
public class FileChooser {
    private JFrame frame;
    private List<TaskXMLData> inputData;

    /**
     * Konstruktor der FileChooser-Klasse.
     *
     * @param frame Das Hauptfenster, in dem der FileChooser eingebettet ist.
     */
    public FileChooser(JFrame frame) {
        this.frame = frame;
        inputData = new ArrayList<>();
    }

    /**
     * Default Constructor.
     */
    public FileChooser() {
        inputData = new ArrayList<>();
    }


    /**
     * Waehlt eine Datei aus
     * @throws ParserConfigurationException Bei Fehlern im XML-Parsing-Prozess.
     * @throws IOException Bei Lese-/Schreibfehlern.
     * @throws SAXException Bei Fehlern in der XML-Verarbeitung.
     */
    public void chooseAndProcessFile() throws ParserConfigurationException, IOException, SAXException {
        File chosenFile = chooseFile();
        chooseAndProcessFile(chosenFile);
    }
    /**
     * Verarbeitet eine Datei entsprechend ihrem Typ (XML, ZIP, Verzeichnis).
     *
     * @throws ParserConfigurationException Bei Fehlern im XML-Parsing-Prozess.
     * @throws IOException Bei Lese-/Schreibfehlern.
     * @throws SAXException Bei Fehlern in der XML-Verarbeitung.
     */
    public void chooseAndProcessFile(File chosenFile) throws ParserConfigurationException, IOException, SAXException {
        if (chosenFile != null) {
            if (chosenFile.isDirectory()) {
                processDirectory(chosenFile);
            } else if (chosenFile.getName().toLowerCase().endsWith(".zip")) {
                processZipFile(chosenFile);
            } else if (chosenFile.getName().toLowerCase().endsWith("task.xml")) {
                TaskXMLData data = new TaskXMLData();
                data.setTaskXMLDatei(chosenFile);
                inputData.add(data);
            }
        }
        processAllXMLFiles();
        System.out.println("Anzahl der bearbeiteten XML-Dateien: " + inputData.size());
    }

    /**
     * Verarbeitet eine ZIP-Datei, indem sie alle darin enthaltenen XML-Dateien extrahiert und verarbeitet.
     *
     * @param zipFile Die zu verarbeitende ZIP-Datei.
     */
    private void processZipFile(File zipFile) {
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                System.out.println("Processing ZIP entry: " + entry.getName());
                if (entry.getName().toLowerCase().endsWith("task.xml")) {
                    File tempFile = File.createTempFile("task", ".xml");
                    tempFile.deleteOnExit();
                    extractEntryToFile(zip, entry, tempFile);
                    TaskXMLData data = new TaskXMLData();
                    data.setTaskXMLDatei(tempFile);
                    data.setZipFile(zipFile);
                    inputData.add(data);
                } else if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".zip")) {
                    File nestedZipFile = File.createTempFile("nested", ".zip");
                    nestedZipFile.deleteOnExit();
                    extractEntryToFile(zip, entry, nestedZipFile);
                    processZipFile(nestedZipFile);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error processing ZIP file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     *
     * Diese Methode liest einen spezifischen Eintrag aus einem geoeffneten ZIP-Archiv und
     * schreibt dessen Inhalt in eine Ausgabedatei.
     *
     * @param zip Das ZIP-Archiv, aus dem der Eintrag extrahiert wird.
     * @param entry Der spezifische Eintrag im ZIP-Archiv, der extrahiert werden soll.
     * @param outputFile Die Datei, in die der Inhalt des Eintrags geschrieben wird.
     * @throws IOException Bei Lese-/Schreibfehlern waehrend der Extraktion.
     */
    private void extractEntryToFile(ZipFile zip, ZipEntry entry, File outputFile) throws IOException {
        try (InputStream inputStream = zip.getInputStream(entry);
             OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }


    /**
     * Durchlaeuft ein Verzeichnis rekursiv und verarbeitet alle darin enthaltenen XML- und ZIP-Dateien.
     *
     * @param directory Das zu durchlaufende Verzeichnis.
     * @throws ParserConfigurationException Bei Fehlern im XML-Parsing-Prozess.
     * @throws IOException Bei Lese-/Schreibfehlern.
     * @throws SAXException Bei Fehlern in der XML-Verarbeitung.
     */
    public void processDirectory(File directory) throws ParserConfigurationException, IOException, SAXException {

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    processDirectory(file);
                } else if (file.getName().toLowerCase().endsWith("task.xml")) {
                    TaskXMLData data = new TaskXMLData();
                    data.setTaskXMLDatei(file);
                    inputData.add(data);
                } else if (file.getName().toLowerCase().endsWith(".zip")) {
                    processZipFile(file);
                }
            }
        }
    }

    /**
     * Oeffnet einen Dateiauswahldialog, um eine XML-Datei, eine ZIP-Datei oder ein Verzeichnis auszuwaehlen.
     *
     * @return Die ausgewaehlte Datei oder null, falls keine Auswahl getroffen wurde.
     */
    private File chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose an XML, ZIP file or Directory");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml") || f.getName().toLowerCase().endsWith(".zip");
            }

            public String getDescription() {
                return "XML Files, ZIP Archives, and Directories";
            }
        });

        int returnVal = fileChooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Verarbeitet alle in der Liste inputData gespeicherten XML-Dateien.
     *
     * @throws ParserConfigurationException Bei Fehlern im XML-Parsing-Prozess.
     * @throws IOException Bei Lese-/Schreibfehlern.
     * @throws SAXException Bei Fehlern in der XML-Verarbeitung.
     */
    private void processAllXMLFiles() throws ParserConfigurationException, IOException, SAXException {
        XMLProcessor processor = new XMLProcessor();
        String moodleXML =  processor.convertToMoodleXML(inputData);
    }
}
