package proforma2moodle;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import proforma2moodle.Entity.TaskXMLData;
import proforma2moodle.gui.GuiPFA2M;
import proforma2moodle.logic.FileChooser;
import org.xml.sax.SAXException;

/**
 * Die Main-Klasse für die ProFormATaskConverter Anwendung.
 * Diese Klasse erstellt und zeigt das Hauptfenster der Anwendung an,
 * das für das Oeffnen und Bearbeiten von XML-Dateien verwendet wird.
 */
public class ProFormA2Moodle {
    private static final String APPLICATION_TITLE = "ProFormA2Moodle";

    /**
     * Der Einstiegspunkt der Anwendung.
     *
     * @param args Kategoriepfad für alle Fragen
     * @param args Pfad, in dem die Moodle-XML gespeichert werden soll
     *
     */
    public static void main(String[] args) {

        if(args.length == 2){
            // Gui für XML Datei Auswahl
            TaskXMLData.setCategoryPath(args[0]);
            TaskXMLData.setOutputPath(args[1]);
            GuiPFA2M.launchGUI(args[0], args[1]);
        } else if (args.length == 3) {
            // alles über Konsole
            String message = convertToMoodleXML(args[0], args[1],args[2]);
            System.out.println(message);
        } else if (args.length == 0) {
            GuiPFA2M.launchGUI();
        }
          else
        {
            System.err.println("Ungueltige Anzahl von Argumenten.\n");
        }
    }

    /**
     * Diese Methode kann in anderen Java-Programmen aufgerufen werden, um die Konvertierung direkt durchzuführen.
     *
     * @param categoryPath Der Pfad der Kategorie für alle Fragen.
     * @param outputPath Der Pfad, in dem die Moodle-XML gespeichert werden soll.
     * @param filePath Der Pfad der Eingabedatei.
     */
    public static String convertToMoodleXML(String categoryPath, String outputPath, String filePath) {
        TaskXMLData.setCategoryPath(categoryPath);
        TaskXMLData.setOutputPath(outputPath);
        File chosenFile = new File(filePath);
        String message ="";
        try {
            FileChooser fileChooser = new FileChooser();
            message = fileChooser.chooseAndProcessFile(chosenFile);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            throw new RuntimeException("Fehler bei der Verarbeitung: " + e.getMessage(), e);
        }
        return message;
    }

}
