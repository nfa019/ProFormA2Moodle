package main.java.proforma2moodle.logic;

import java.util.Comparator;
import java.util.List;
/**
 * Vergleicht Versionsnummern, um die Sortierung oder Identifizierung der neuesten Version zu ermoeglichen.
 *
 * Diese Klasse implementiert das Comparator-Interface fuer Strings, um Versionsnummern, die als Strings
 * vorliegen, zu vergleichen.
 */
public class VersionComparator implements Comparator<String> {
    @Override
    public int compare(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");
        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int part1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int part2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (part1 != part2) {
                return part1 - part2;
            }
        }
        return 0;
    }
    /**
     * Ermittelt die neueste (hoechste) Version aus einer Liste von Versionsnummern.
     *
     * @param versions Eine Liste von Versionsnummern als Strings.
     * @return Die hoechste Versionsnummer als String oder null, falls die Liste leer ist.
     */
    public static String getLatestVersion(List<String> versions) {
        return versions.stream().max(new VersionComparator()).orElse(null);
    }
}

