package proforma2moodle.Entity;

import java.io.File;
/**
 *
 * Die TaskXMLData-Klasse repraesentiert die Daten einer einzelnen Aufgabe, die aus einer XML-Datei extrahiert wurden.
 *
 */
public class TaskXMLData {
    private static String categoryPath;
    private static String categoryName;
    private static String outputPath;
    private File taskXMLDatei;
    private String questionName;
    private String questionText;
    private String generalFeedback;
    private String internalDescription;
    private File zipFile;
    private String graderName;
    private String graderVersion;
    private String Taskuuid;
    private String enableFileSubmissions = "1"; //DEFAULT
    private String enableFreeTextSubmissions = "0"; //DEFAULT
    private String ftsNumInitialFields = "1"; // DEFAULT
    private String ftsMaxnumFields = "10"; // DEFAULT NACH settings.php
    private String ftsAutoGenerateFileNames = "0"; // DEFAULT
    private String penalty = "0";
    private String defaultgrade= "1";

    public String getPenalty() {
        return penalty;
    }

    public void setPenalty(String penalty) {
        this.penalty = penalty;
    }

    public String getDefaultgrade() {
        return defaultgrade;
    }

    public void setDefaultgrade(String defaultgrade) {
        this.defaultgrade = defaultgrade;
    }

    public String getEnableFileSubmissions() {
        return enableFileSubmissions;
    }

    public void setEnableFileSubmissions(String enableFileSubmissions) {
        this.enableFileSubmissions = enableFileSubmissions;
    }

    public String getEnableFreeTextSubmissions() {
        return enableFreeTextSubmissions;
    }

    public void setEnableFreeTextSubmissions(String enableFreeTextSubmissions) {
        this.enableFreeTextSubmissions = enableFreeTextSubmissions;
    }

    public String getFtsNumInitialFields() {
        return ftsNumInitialFields;
    }

    public void setFtsNumInitialFields(String ftsNumInitialFields) {
        this.ftsNumInitialFields = ftsNumInitialFields;
    }

    public String getFtsMaxnumFields() {
        return ftsMaxnumFields;
    }

    public void setFtsMaxnumFields(String ftsMaxnumFields) {
        this.ftsMaxnumFields = ftsMaxnumFields;
    }

    public String getFtsAutoGenerateFileNames() {
        return ftsAutoGenerateFileNames;
    }

    public void setFtsAutoGenerateFileNames(String ftsAutoGenerateFileNames) {
        this.ftsAutoGenerateFileNames = ftsAutoGenerateFileNames;
    }

    public TaskXMLData(){
    }

    /**
     * Gibt den Namen der Datei zurück, entweder der ZIP-Datei, falls vorhanden, oder der direkten XML-Datei.
     * @return Den Namen der Datei.
     */
    public String getFileName() {
        if (zipFile != null) {
            return zipFile.getName();
        } else {
            return taskXMLDatei.getName();
        }
    }
    /**
     * Gibt die Datei (entweder ZIP oder XML) zurueck, die mit diesem TaskXMLData-Objekt verknuepft ist.
     * @return Die verknuepfte Datei.
     */
    public File getFile() {
        if (zipFile != null) {
            return zipFile;
        } else {
            return taskXMLDatei;
        }
    }

    public String getGraderVersion() {
        return graderVersion;
    }

    public void setGraderVersion(String graderVersion) {
        this.graderVersion = graderVersion;
    }

    public String getQuestionName() {
        return questionName;
    }

    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }
    public String getGeneralFeedback() {
        return generalFeedback;
    }

    public void setGeneralFeedback(String generalFeedback) {
        this.generalFeedback = generalFeedback;
    }
    public String getQuestionText() {
        return questionText;
    }

    public String getGraderName() {
        return graderName;
    }

    public String getTaskuuid() {
        return Taskuuid;
    }

    public void setTaskuuid(String taskuuid) {
        Taskuuid = taskuuid;
    }

    public void setGraderName(String graderName) {
        this.graderName = graderName;
    }

    public String getInternalDescription() {
        return internalDescription;
    }

    public void setInternalDescription(String internalDescription) {
        this.internalDescription = internalDescription;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public File getTaskXMLDatei() {
        return taskXMLDatei;
    }

    public void setTaskXMLDatei(File taskXMLDatei) {
        this.taskXMLDatei = taskXMLDatei;
    }

    public void setZipFile(File zipFile) {
        this.zipFile = zipFile;
    }

    public static String getCategoryPath() {
        return categoryPath;
    }

    public static String getCategoryName() { return categoryName; }

    public static void setCategoryPath(String categoryPath) {
        TaskXMLData.categoryPath = categoryPath;
        String[] s = categoryPath.split("/");
        categoryName = s[s.length-1];
    }

    public static String getOutputPath() {
        return outputPath;
    }

    public static void setOutputPath(String outputPath) {
        TaskXMLData.outputPath = outputPath;
        File outputfile = new File(outputPath);
        if(!outputfile.exists()){
				//System.out.println("Create folder: "+outputfile.getAbsolutePath());
				outputfile.mkdirs();
		}
    }
}
