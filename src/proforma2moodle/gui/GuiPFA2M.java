package proforma2moodle.gui;

import proforma2moodle.Entity.TaskXMLData;
import proforma2moodle.logic.FileChooser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Enumeration;

public class GuiPFA2M  {
    private static final String APPLICATION_TITLE = "ProFormA2Moodle";
    private static File fromFile=null;
    public GuiPFA2M() {
            }

    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, f);
        }
    }
    /**
     * Startet die grafische Benutzeroberflaeche (GUI) der Anwendung.
     *
     * @param categoryPath Der Pfad der Kategorie für alle Fragen.
     * @param outputPath Der Pfad, in dem die Moodle-XML-Datei gespeichert werden soll.
     */
    public static void launchGUI(String categoryPath, String outputPath) {
        TaskXMLData.setCategoryPath(categoryPath);
        TaskXMLData.setOutputPath(outputPath);
        javax.swing.SwingUtilities.invokeLater(proforma2moodle.gui.GuiPFA2M::createAndShowGUI);
    }

    /**
     * Startet die grafische Benutzeroberflaeche (GUI) der Anwendung.
     *
     *
     */
    public static void launchGUI() {
        javax.swing.SwingUtilities.invokeLater(proforma2moodle.gui.GuiPFA2M::createAndShowGUIforAll);
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

    /**
     * Erstellt und zeigt das GUI der Anwendung.
     * Diese Methode initialisiert das Hauptfenster,
     * um XML-Dateien, ZIP-Dateien oder ein Verzeichnis zu oeffnen und zu bearbeiten.
     */
    private static void createAndShowGUIforAll() {
        setUIFont(new javax.swing.plaf.FontUIResource("Sans", Font.PLAIN, 24));

        JFrame frame = new JFrame(APPLICATION_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FileChooser fileChooser = new FileChooser(frame);
        frame.setSize(1000, 300);
        frame.setLocationRelativeTo(null);
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(200, 200, 200));

        GridLayout gl = new GridLayout(0,3);
        mainPanel.setLayout(gl);

        JLabel labelCat = new JLabel();
        labelCat.setVerticalAlignment(JLabel.TOP);
        labelCat.setHorizontalAlignment(JLabel.LEFT);
        labelCat.setHorizontalTextPosition(JLabel.LEFT);
        labelCat.setText("Moodle Question Category");
        mainPanel.add(labelCat);

        JTextField textFieldCat = new JTextField(20);
        textFieldCat.setSize(600, 80);
        mainPanel.add(textFieldCat);

        JSeparator separator1 = new JSeparator();
        separator1.setOrientation(JSeparator.HORIZONTAL);
        mainPanel.add(separator1);

        JLabel labelStore = new JLabel();
        labelStore.setVerticalAlignment(JLabel.TOP);
        labelStore.setHorizontalAlignment(JLabel.LEFT);
        labelStore.setHorizontalTextPosition(JLabel.LEFT);
        labelStore.setText("Directory to store");
        mainPanel.add(labelStore);

        JTextField textFieldStore = new JTextField(20);
        textFieldStore.setSize(600, 80);
        mainPanel.add(textFieldStore);

        JButton buttonStore = new JButton("Choose from directory");
        buttonStore.addActionListener(e -> {
            try {
                File dir = fileChooser.chooseDirectory();
                String path = dir.getAbsolutePath();
                TaskXMLData.setOutputPath(path);
                textFieldStore.setText(path);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        mainPanel.add(buttonStore);

        JLabel labelRead = new JLabel();
        labelRead.setVerticalAlignment(JLabel.TOP);
        labelRead.setHorizontalAlignment(JLabel.LEFT);
        labelRead.setHorizontalTextPosition(JLabel.LEFT);
        labelRead.setText("Read from");
        mainPanel.add(labelRead);

        JTextField textFieldRead = new JTextField(20);
        textFieldRead.setSize(600, 80);
        mainPanel.add(textFieldRead);

        JButton buttonRead = new JButton("Choose from directory");
        buttonRead.addActionListener(e -> {
            try {
                fromFile = fileChooser.chooseFile();
                TaskXMLData.setInputPath(fromFile.getAbsolutePath());
                textFieldRead.setText(TaskXMLData.getInputPath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        mainPanel.add(buttonRead);

        JLabel labelNix = new JLabel();
        mainPanel.add(labelNix);
        JButton buttonAction = new JButton("Start transform");
        buttonAction.addActionListener(e->{
             try {
                TaskXMLData.setCategoryPath(textFieldCat.getText());
                if (TaskXMLData.getInputPath().isEmpty()){
                    TaskXMLData.setInputPath(textFieldRead.getText());
                    fromFile = new File(textFieldRead.getText());
                }
                if (TaskXMLData.getOutputPath().isEmpty()){
                    TaskXMLData.setOutputPath(textFieldStore.getText());
                }
                String message = fileChooser.chooseAndProcessFile(fromFile);
                JOptionPane.showMessageDialog(frame,message,"Success",JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        mainPanel.add(buttonAction);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }
}
