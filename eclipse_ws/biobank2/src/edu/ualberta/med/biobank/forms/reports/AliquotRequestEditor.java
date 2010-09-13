package edu.ualberta.med.biobank.forms.reports;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCSVException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import edu.ualberta.med.biobank.server.reports.AliquotRequest;
import edu.ualberta.med.biobank.widgets.FileBrowser;

public class AliquotRequestEditor extends ReportsEditor {

    public static String ID = "edu.ualberta.med.biobank.editors.AliquotRequestEditor";

    private FileBrowser csvSelector;

    @Override
    protected int[] getColumnWidths() {
        return new int[] { 100, 100, 100, 100, 100, 100 };
    }

    @Override
    protected void createOptionSection(Composite parent) {
        csvSelector = createFileBrowserOption("CSV File", parent);
    }

    @Override
    protected List<Object> getParams() throws Exception {
        return parseCSV();
    }

    @Override
    protected List<Object> getPrintParams() {
        return Arrays.asList(new Object[] { csvSelector.getFilePath() });
    }

    protected FileBrowser createFileBrowserOption(String fieldLabel,
        Composite parent) {
        widgetCreator.createLabel(parent, fieldLabel);
        FileBrowser widget = new FileBrowser(parent, SWT.NONE);
        toolkit.adapt(widget, true, true);
        return widget;
    }

    protected List<Object> parseCSV() throws Exception {
        ICsvBeanReader reader = new CsvBeanReader(new FileReader(
            csvSelector.getFilePath()), CsvPreference.EXCEL_PREFERENCE);

        final CellProcessor[] processors = new CellProcessor[] {
            new StrNotNullOrEmpty(), new ParseDate("yyyy-MM-dd"),
            new StrNotNullOrEmpty(), new ParseInt() };

        List<Object> requests = new ArrayList<Object>();

        try {
            String[] header = new String[] { "pnumber", "dateDrawn",
                "sampleTypeNameShort", "maxAliquots" };
            AliquotRequest request;
            while ((request = reader.read(AliquotRequest.class, header,
                processors)) != null) {
                requests.add(request);
            }
        } catch (SuperCSVException e) {
            throw new Exception("Parse error at line " + reader.getLineNumber()
                + "\n" + e.getCsvContext());
        } finally {
            reader.close();
        }
        return requests;
    }

    @Override
    protected String[] getColumnNames() {
        return new String[] { "Patient", "Inventory ID", "Date Drawn", "Type",
            "Location", "Activity Status" };
    }

    @Override
    protected List<String> getParamNames() {
        List<String> paramName = new ArrayList<String>();
        paramName.add("CSV File");
        return paramName;
    }
}