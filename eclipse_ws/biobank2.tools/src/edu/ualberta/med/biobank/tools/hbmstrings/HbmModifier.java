package edu.ualberta.med.biobank.tools.hbmstrings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import edu.ualberta.med.biobank.tools.modelumlparser.Attribute;

public class HbmModifier {

    private static Pattern HBM_STRING_ATTR = Pattern.compile(
        "<property.*type=\"string\"\\s*column=\"([^\"]*)\"/>",
        Pattern.CASE_INSENSITIVE);

    private static Pattern HBM_ATTR = Pattern.compile(
        "<property.*column=\"([^\"]*)\"/>", Pattern.CASE_INSENSITIVE);

    private static String HBM_FILE_EXTENSION = ".hbm.xml";

    private static HbmModifier instance = null;

    private boolean documentChanged = false;

    private HbmModifier() {

    }

    public static HbmModifier getInstance() {
        if (instance == null) {
            instance = new HbmModifier();
        }
        return instance;
    }

    public void alterMapping(String filename, String className,
        String tableName, Map<String, Attribute> columnTypeMap,
        Set<String> uniqueList, Set<String> notNullList) throws Exception {
        if (!filename.contains(className)) {
            throw new Exception(
                "HBM file name does not contain class name: filename "
                    + filename + ", classname " + className);
        }

        try {
            File outFile = File.createTempFile(className, HBM_FILE_EXTENSION);

            BufferedReader reader = new BufferedReader(new FileReader(filename));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

            String line = reader.readLine();
            while (line != null) {
                Matcher stringAttrMatcher = HBM_STRING_ATTR.matcher(line);
                Matcher attrMatcher = HBM_ATTR.matcher(line);
                if (stringAttrMatcher.find() && !line.contains("length=\"")) {
                    String attrName = stringAttrMatcher.group(1);
                    Attribute attr = columnTypeMap.get(attrName);

                    if (attr == null) {
                        throw new Exception("column not found in column map: "
                            + attrName);
                    }

                    Integer attrLen = attr.getLength();

                    if (attrLen != null) {
                        line = line.replace(
                            "type=\"string\"",
                            "type=\"" + attr.getType() + "\" length=\""
                                + attr.getLength() + "\"");
                    } else {
                        line = line.replace("type=\"string\"",
                            "type=\"" + attr.getType() + "\"");
                    }
                    documentChanged = true;
                    line = addContraints(line, attrName, uniqueList,
                        notNullList);
                    documentChanged = true;
                } else if (attrMatcher.find()) {
                    String attrName = attrMatcher.group(1);
                    line = addContraints(line, attrName, uniqueList,
                        notNullList);
                }

                writer.write(line);
                writer.newLine();
                line = reader.readLine();
            }

            reader.close();
            writer.flush();
            writer.close();

            if (documentChanged) {
                FileUtils.copyFile(outFile, new File(filename));
                if (HbmStrings.getInstance().getVerbose()) {
                    System.out.println("HBM Modified: " + filename);
                }
            }

            outFile.deleteOnExit();
        } catch (IOException e) {
            System.out.println("class " + className
                + " does not have a corresponding HBM file");
        }
    }

    private String addContraints(String line, String attrName,
        Set<String> uniqueList, Set<String> notNullList) {
        String s = "";
        if (uniqueList.contains(attrName) && !s.contains("unique="))
            s += " unique=\"true\"";
        if (notNullList.contains(attrName) && !s.contains("not-null="))
            s += " not-null=\"true\"";
        if (s.length() > 0) {
            documentChanged = true;
            return line.replace("/>", s + "/>");
        }
        return line;
    }
}
