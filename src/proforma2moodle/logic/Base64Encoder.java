package proforma2moodle.logic;

import java.util.Base64;
import java.nio.file.Files;
import java.io.File;
import java.io.IOException;
/**
 * Diese Klasse bietet eine Methode zur Konvertierung von Dateiinhalten in einen Base64-kodierten String.
 *
 */
public class Base64Encoder {
    /**
     *
     * Diese Methode liest die Datei, konvertiert ihren Inhalt in einen Byte-Array und
     * kodiert diesen anschließend in einen Base64-String.
     *
     * @param file Die Datei, deren Inhalt kodiert werden soll.
     * @return Ein Base64-kodierter String, der den Inhalt der Datei darstellt.
     * Gibt null zurück, falls beim Lesen der Datei ein Fehler auftritt.
     */
    public static String encodeFileToBase64(File file) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
