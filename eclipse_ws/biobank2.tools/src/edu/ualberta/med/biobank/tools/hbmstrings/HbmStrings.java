package edu.ualberta.med.biobank.tools.hbmstrings;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.Option;
import jargs.gnu.CmdLineParser.OptionException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.ualberta.med.biobank.tools.modelumlparser.Attribute;
import edu.ualberta.med.biobank.tools.modelumlparser.ModelUmlParser;
import edu.ualberta.med.biobank.tools.utils.CamelCase;

public class HbmStrings {
    private static final String NOT_EMPTY_STEREOTYPE = "not-empty";

    private static final String NOT_NULL_STEREOTYPE = "not-null";

    private static final String UNIQUE_STEREOTYPE = "unique";

    private static final Logger LOGGER = Logger.getLogger(HbmStrings.class
        .getName());

    private static String USAGE = "Usage: strfields [options] UMLFILE HBMDIR TEMPLATE\n\n"
        + "Options\n" + "  -v, --verbose    Shows verbose output";

    private static HbmStrings instance = null;

    private static String HBM_FILE_EXTENSION = ".hbm.xml";

    private static Pattern VARCHAR_LEN = Pattern.compile("VARCHAR\\((\\d+)\\)");

    private AppArgs appArgs = null;

    private HbmStrings() {

    }

    public static HbmStrings getInstance() {
        if (instance == null) {
            instance = new HbmStrings();
        }
        return instance;
    }

    public void doWork(AppArgs appArgs) {
        this.appArgs = appArgs;

        if (appArgs.verbose) {
            LOGGER.info("  UML file: " + appArgs.modelFileName);
            LOGGER.info("  HBM dir:  " + appArgs.hbmDir);
            LOGGER.info("  Template: " + appArgs.template);
        }

        try {
            ModelUmlParser.getInstance().geDataModel(appArgs.modelFileName);

            if (appArgs.verbose) {
                for (String className : ModelUmlParser.getInstance()
                    .getDmTableSet()) {
                    Map<String, Attribute> attrMap = ModelUmlParser
                        .getInstance().getDmTableAttrMap(className);
                    for (String attrName : attrMap.keySet()) {
                        String type = attrMap.get(attrName).getType();
                        if (!type.startsWith("VARCHAR")
                            && !type.startsWith("TEXT"))
                            continue;

                        LOGGER.info("  " + className + "." + attrName + ": "
                            + type);
                    }
                }
            }

            for (String file : getHbmFiles(appArgs.hbmDir)) {
                updateHbmFile(file);
            }

            createVarCharLengthsSourceCode();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getVerbose() throws Exception {
        if (appArgs == null) {
            throw new Exception("invalid state");
        }
        return appArgs.verbose;
    }

    /*
     * Returns all "*.hbm.xml" files found in directory hbmDir
     */
    private List<String> getHbmFiles(String hbmDir) {
        File dir = new File(hbmDir);
        String[] files = dir.list();
        if (files == null) {
            LOGGER.info("Error: no files found in directory " + hbmDir);
            System.exit(-1);
        }

        List<String> hbmFiles = new ArrayList<String>();
        for (String file : files) {
            if (!file.endsWith(HBM_FILE_EXTENSION))
                continue;
            hbmFiles.add(file);
        }
        return hbmFiles;
    }

    /*
     * Creates a new HBM file with lengths derived from UML file for all string
     * fields
     */
    private void updateHbmFile(String hbmFileName) throws Exception {
        String hbmFilePath = appArgs.hbmDir + "/" + hbmFileName;
        String className = hbmFileName.replace(HBM_FILE_EXTENSION, "");
        String tableName = CamelCase.toTitleCase(className);

        Map<String, Attribute> attrMap = ModelUmlParser.getInstance()
            .getDmTableAttrMap(tableName);
        Map<String, Integer> attrLengthMap = new HashMap<String, Integer>();

        Set<String> uniqueList = new HashSet<String>();
        Set<String> notNullList = new HashSet<String>();

        for (String attrName : attrMap.keySet()) {
            Attribute attr = attrMap.get(attrName);

            String attrType = attr.getType();
            if (attrType.startsWith("VARCHAR")) {
                Matcher varcharMatcher = VARCHAR_LEN.matcher(attrType);

                if (varcharMatcher.find()) {
                    attrLengthMap.put(attrName,
                        Integer.valueOf(varcharMatcher.group(1)));
                }
            } else if (attrType.startsWith("TEXT")) {
                attrLengthMap.put(attrName, 500);
            }
            if (attr.hasStereotype(UNIQUE_STEREOTYPE))
                uniqueList.add(attrName);
            if (attr.hasStereotype(NOT_NULL_STEREOTYPE))
                notNullList.add(attrName);
            if (attr.hasStereotype(NOT_EMPTY_STEREOTYPE))
                notNullList.add(attrName);
        }

        HbmModifier.getInstance().alterMapping(hbmFilePath, className,
            tableName, attrLengthMap, uniqueList, notNullList);
    }

    private void createVarCharLengthsSourceCode() throws Exception {
        String newLine = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();

        for (String className : ModelUmlParser.getInstance().getDmTableSet()) {
            Map<String, Attribute> attrMap = ModelUmlParser.getInstance()
                .getDmTableAttrMap(className);
            for (String attrName : attrMap.keySet()) {
                String attrType = attrMap.get(attrName).getType();
                if (!attrType.startsWith("VARCHAR"))
                    continue;

                if (attrType.startsWith("VARCHAR")) {
                    Matcher varcharMatcher = VARCHAR_LEN.matcher(attrType);

                    if (varcharMatcher.find()) {
                        attrType = varcharMatcher.group(1);

                        sb.append("\t\taMap.put(\"edu.ualberta.med.biobank.model."
                            + CamelCase.toCamelCase(className + "." + attrName,
                                true) + "\", " + attrType + ");");
                        sb.append(newLine);
                    }
                }
            }
        }

        String content = sb.toString();

        BufferedReader reader = new BufferedReader(new FileReader(
            appArgs.template));
        BufferedWriter writer = new BufferedWriter(new FileWriter(
            appArgs.template.replace(".template", "")));

        String line = reader.readLine();
        while (line != null) {
            line = line.replace("{attrMapContents}", content);
            writer.write(line);
            writer.newLine();
            line = reader.readLine();
        }

        writer.flush();
        writer.close();
        reader.close();
    }

    public static void main(String argv[]) throws URISyntaxException {
        HbmStrings.getInstance().doWork(parseCommandLine(argv));
    }

    /*
     * Parses the command line arguments and returns them in an AppArgs object.
     */
    private static AppArgs parseCommandLine(String argv[])
        throws URISyntaxException {
        AppArgs appArgs = new AppArgs();

        CmdLineParser parser = new CmdLineParser();
        Option verboseOpt = parser.addBooleanOption('v', "verbose");

        try {
            parser.parse(argv);
        } catch (OptionException e) {
            LOGGER.info(e.getMessage());
            System.exit(-1);
        }

        Boolean verbose = (Boolean) parser.getOptionValue(verboseOpt);
        if (verbose != null) {
            appArgs.verbose = verbose.booleanValue();
        }

        String[] args = parser.getRemainingArgs();
        if (args.length != 3) {
            LOGGER.info("Error: invalid arguments\n" + USAGE);
            System.exit(-1);
        }

        appArgs.modelFileName = args[0];
        appArgs.hbmDir = args[1];
        appArgs.template = args[2];

        return appArgs;
    }
}

class AppArgs {
    boolean verbose = false;
    String modelFileName = null;
    String hbmDir = null;
    String template = null;
}
