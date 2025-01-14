package proforma2moodle.logic;

import proforma2moodle.Entity.TaskXMLData;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse ist dafuer verantwortlich, ein Moodle-kompatibles XML-Dokument aus einer Liste von TaskXMLData-Objekten zu erzeugen.
 * Sie ermoeglicht die Erstellung strukturierter Moodle-Fragen in einem XML-Format, das direkt in Moodle importiert werden kann.
 */
public class MoodleXMLConverter {
    private List<TaskXMLData> taskXMLDataList;

    /**
     * Konstruktor fuer die MoodleXMLConverter-Klasse.
     *
     * @param dataList Die Liste von TaskXMLData-Objekten, die in ein Moodle XML-Dokument umgewandelt werden sollen.
     * @throws ParserConfigurationException falls ein Fehler bei der Konfiguration des XML-Parsers auftritt.
     */
    public MoodleXMLConverter(List<TaskXMLData> dataList) throws ParserConfigurationException {
        this.taskXMLDataList = dataList;
    }
    /**
     * Erstellt ein Moodle-kompatibles XML-Dokument aus der bereitgestellten Liste von TaskXMLData-Objekten.
     * Jedes TaskXMLData-Objekt wird in ein <question>-Element im XML-Dokument konvertiert.
     *
     * @return Ein String, der das Moodle XML-Dokument darstellt. Gibt null zurueck, falls ein Fehler auftritt.
     */
    public String createMoodleXML() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element quiz = doc.createElement("quiz");
            doc.appendChild(quiz);
            addCategoryQuestionIfNeeded(doc, quiz);

            for (TaskXMLData data : taskXMLDataList) {
                Element question = doc.createElement("question");
                question.setAttribute("type", "moopt");
                quiz.appendChild(question);

                Element name = addElement(doc, question, "name", null);
                addElement(doc, name, "text", data.getQuestionName());

                Element questionText = addElement(doc, question, "questiontext", null);
                questionText.setAttribute("format", "html");
                question.appendChild(questionText);
                addCDATAElement(doc, questionText, "text", data.getQuestionText());

                Element generalFeedback = doc.createElement("generalfeedback");
                generalFeedback.setAttribute("format", "html");
                question.appendChild(generalFeedback);
                addCDATAElement(doc, generalFeedback, "text", data.getGeneralFeedback());

                Element defaultGrade = addElement(doc, question, "defaultgrade", data.getDefaultgrade());

                Element penalty = addElement(doc, question, "penalty", data.getPenalty());

                Element hidden = addElement(doc, question, "hidden", "0"); // DEFAULT

                Element idNumber = addElement(doc, question, "idnumber", null); //DEFAULT IST EIN LEERES FELD

                Element answer = doc.createElement("answer");
                answer.setAttribute("fraction","0");
                question.appendChild(answer);
                addCDATAElement(doc,answer,"text",data.getAnswer());

                Element taskfile = addElement(doc, question, "taskfile", Base64Encoder.encodeFileToBase64(data.getFile()));
                taskfile.setAttribute("filearea","taskfile");
                taskfile.setAttribute("name", data.getFileName());
                taskfile.setAttribute("path", "/");
                taskfile.setAttribute("encoding", "base64");

                Element customfts = doc.createElement("customsettingsforfreetextinputfields");
                question.appendChild(customfts);
                ArrayList<TaskXMLData.FreeInputField> listFTS = data.ftsList;
                if (!listFTS.isEmpty()){
                    int nrEl = listFTS.size();
                    for (int i=0; i<nrEl; i++){
                        TaskXMLData.FreeInputField fts = listFTS.get(i);
                        Element field = doc.createElement("field");
                        customfts.appendChild(field);
                        String iString = ""+i;
                        field.setAttribute("index",iString);
                        Element el = addElement(doc,field,"namesettingsforfreetextinput", fts.getNameSettingsForFreeTextInput());
                        el = addElement(doc,field,"freetextinputfieldname",fts.getFreeTextInputFieldName());
                        el = addElement(doc, field, "ftsoverwrittenlang",fts.getFtsOverWrittenLanguage());
                        el = addElement(doc, field, "ftsinitialdisplayrows",fts.getFtsInitialDisplayRows());
                        el = addCDATAElement(doc, field, "freetextinputfieldtemplate", fts.getFreeTextInputFieldTemplate());
                        field.appendChild(el);
                    }
                }

                Element internalDescription = addCDATAElement(doc, question, "internaldescription", data.getInternalDescription());
                question.appendChild(internalDescription);

                Element graderName = addElement(doc, question, "gradername", data.getGraderName());

                Element graderVersion = addElement(doc, question, "graderversion", data.getGraderVersion());

                Element taskUuid = addElement(doc, question, "taskuuid", data.getTaskuuid());

                Element showStudGradingScheme = addElement(doc, question, "showstudgradingscheme", "1"); //DEFAULT

                Element showStudScoreCalcScheme = addElement(doc, question, "showstudscorecalcscheme", "1"); //DEFAULT

                Element enableFileSubmissions = addElement(doc, question, "enablefilesubmissions",  data.getEnableFileSubmissions()); //DEFAULT

                Element enableFreeTextSubmissions = addElement(doc, question, "enablefreetextsubmissions", data.getEnableFreeTextSubmissions()); //DEFAULT

                Element ftsNumInitialFields = addElement(doc, question, "ftsnuminitialfields", data.getFtsNumInitialFields()); // DEFAULT

                Element ftsMaxnumFields = addElement(doc, question, "ftsmaxnumfields", data.getFtsMaxnumFields()); // DEFAULT NACH settings.php

                Element ftsAutoGenerateFileNames = addElement(doc, question, "ftsautogeneratefilenames", data.getFtsAutoGenerateFileNames()); // DEFAULT

                Element ftsStandardLang = addElement(doc, question, "ftsstandardlang", data.getFtsStandardLang()); //DEFAULT

                Element resultSpecFormat = addElement(doc, question, "resultspecformat", "zip"); //DEFAULT

                Element resultSpecStructure = addElement(doc, question, "resultspecstructure", "separate-test-feedback"); // DEFAULT

                Element studentFeedbackLevel = addElement(doc, question, "studentfeedbacklevel", "info"); //DEFAULT

                Element teacherFeedbackLevel = addElement(doc, question, "teacherfeedbacklevel", "debug"); //DEFAULT

                Element tags = doc.createElement("tags");
                question.appendChild(tags);
                Element tagMoopt = doc.createElement("tag");
                tags.appendChild(tagMoopt);
                Element textTagMoopt = addElement(doc,tagMoopt,"text","MooPT");
                Element tagGrader = doc.createElement("tag");
                tags.appendChild(tagGrader);
                Element textTagGrader = addElement(doc,tagGrader,"text",data.getGraderName());


            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            StreamResult result = new StreamResult(writer);
            transformer.transform(domSource, result);
            String xmlString = writer.toString();

            String[] lines = xmlString.split("\n");
            StringBuilder cleanedXml = new StringBuilder();
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    cleanedXml.append(line).append("\n");
                }
            }

            String outputname = ".xml";
            if (!(TaskXMLData.getCategoryName().equals(" ")||TaskXMLData.getCategoryName().isEmpty())) {
                outputname = "_"+TaskXMLData.getCategoryName()+outputname;
            }
            outputname = "MoodleXML"+outputname;

            try (FileWriter fileWriter = new FileWriter(TaskXMLData.getOutputPath() + File.separator
                    + outputname)) {
                fileWriter.write(cleanedXml.toString());
            }

            return cleanedXml.toString();


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    /**
     * Fuegt ein neues Element zum uebergebenen Elternelement hinzu.
     *
     * @param doc Das Dokument, zu dem das Element hinzugefuegt wird.
     * @param parent Das Elternelement, zu dem das neue Element hinzugefuegt wird.
     * @param tagName Der Tag-Name des neuen Elements.
     * @param textContent Der Textinhalt des neuen Elements. Kann null sein.
     * @return Das neu erstellte Element.
     */
    private Element addElement(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        if (textContent != null && !textContent.trim().isEmpty()) {
            element.appendChild(doc.createTextNode(textContent));
        } else {
            element.appendChild(doc.createTextNode(" "));
        }
        parent.appendChild(element);
        return element;
    }
    /**
     * Fuegt ein neues Element mit einem CDATA-Abschnitt zum übergebenen Elternelement hinzu.
     *
     * @param doc Das Dokument, zu dem das Element hinzugefuegt wird.
     * @param parent Das Elternelement, zu dem das neue Element hinzugefuegt wird.
     * @param tagName Der Tag-Name des neuen Elements.
     * @param cdataContent Der CDATA-Inhalt des neuen Elements. Kann null sein.
     * @return Das neu erstellte Element.
     */
    private Element addCDATAElement(Document doc, Element parent, String tagName, String cdataContent) {
        Element element = doc.createElement(tagName);
        CDATASection cdataSection;
        if (cdataContent != null && !cdataContent.trim().isEmpty()) {
            cdataSection = doc.createCDATASection(cdataContent);
            element.appendChild(cdataSection);
        } else {
            element.appendChild(doc.createTextNode(" "));
        }
        parent.appendChild(element);
        return element;
    }
    /**
     * Fuegt eine Kategorienfrage zum Quiz hinzu, wenn dies aufgrund der Kategorienpfadangabe noetig ist.
     *
     * Diese Methode prueft, ob ein Kategorienpfad fuer die Aufgabe vorhanden ist. Falls ja, wird eine neue
     * Kategorienfrage im Quiz-Dokument erstellt.
     *
     * @param doc Das XML-Dokument, das das Moodle-Quiz repraesentiert.
     * @param quiz Das XML-Element, das das Quiz repraesentiert, zu dem die Kategorienfrage hinzugefuegt werden soll.
     */
    private void addCategoryQuestionIfNeeded(Document doc, Element quiz) {
        String categoryPath = TaskXMLData.getCategoryPath();
        if (categoryPath != null && !categoryPath.isEmpty()) {
            Element categoryQuestion = doc.createElement("question");
            categoryQuestion.setAttribute("type", "category");
            quiz.appendChild(categoryQuestion);

            Element categoryElement = addElement(doc, categoryQuestion, "category", null);
            addElement(doc, categoryElement, "text", "$course$/" + categoryPath);

            Element info = addElement(doc, categoryQuestion, "info", null);
            info.setAttribute("format", "moodle_auto_format");
            addElement(doc, info, "text", "The Default category:  " + categoryPath + "'.");
            addElement(doc, categoryQuestion, "idnumber", null);
        }
    }

}
