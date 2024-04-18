package proforma2moodle.gui;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import proforma2moodle.Entity.TaskXMLData;
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
            launchGUI(args[0], args[1]);
        } else if (args.length == 3) {
            // alles über Konsole
            convertToMoodleXML(args[0], args[1],args[2]);
        } else {
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
    public static void convertToMoodleXML(String categoryPath, String outputPath, String filePath) {
        TaskXMLData.setCategoryPath(categoryPath);
        TaskXMLData.setOutputPath(outputPath);
        File chosenFile = new File(filePath);
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.chooseAndProcessFile(chosenFile);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            throw new RuntimeException("Fehler bei der Verarbeitung: " + e.getMessage(), e);
        }
    }
    /**
     * Startet die grafische Benutzeroberflaeche (GUI) der Anwendung.
     *
     * @param categoryPath Der Pfad der Kategorie für alle Fragen.
     * @param outputPath Der Pfad, in dem die Moodle-XML-Datei gespeichert werden soll.
     */
    private static void launchGUI(String categoryPath, String outputPath) {
        TaskXMLData.setCategoryPath(categoryPath);
        TaskXMLData.setOutputPath(outputPath);
        javax.swing.SwingUtilities.invokeLater(proforma2moodle.gui.ProFormA2Moodle::createAndShowGUI);
    }

    /**
     * Erstellt und zeigt das GUI der Anwendung.
     * Diese Methode initialisiert das Hauptfenster,
     * um XML-Dateien, ZIP-Dateien oder ein Verzeichnis zu oeffnen und zu bearbeiten.
     */
    private static void createAndShowGUI() {
        JFrame frame = new JFrame(APPLICATION_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(45, 45, 45));

        JButton openButton = new JButton("Open XML File");
        openButton.setForeground(Color.WHITE);
        openButton.setBackground(new Color(100, 100, 100));
        openButton.setFocusPainted(false);

        FileChooser fileChooser = new FileChooser(frame);
        openButton.addActionListener(e -> {
            try {
                fileChooser.chooseAndProcessFile();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.add(openButton, BorderLayout.NORTH);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
