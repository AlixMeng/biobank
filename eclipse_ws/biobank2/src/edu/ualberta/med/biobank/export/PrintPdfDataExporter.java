package edu.ualberta.med.biobank.export;

import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JasperPrint;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ITableLabelProvider;

import edu.ualberta.med.biobank.BiobankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.reporting.ReportingUtils;

public class PrintPdfDataExporter extends PdfDataExporter {
    public PrintPdfDataExporter() {
        super("Print");
    }

    @Override
    protected void export(Data data, ITableLabelProvider labelProvider,
        IProgressMonitor monitor) throws DataExportException {
        canExport(data);

        List<Map<String, String>> maps = getPropertyMaps(data, labelProvider,
            monitor);

        try {
            JasperPrint jasperPrint = ReportingUtils.createDynamicReport(
                data.getTitle(), data.getDescription(), data.getColumnNames(),
                maps);
            ReportingUtils.printReport(jasperPrint);
        } catch (Exception e) {
            BiobankPlugin.openAsyncError("Error printing PDF", e);
            return;
        }
        try {
            SessionManager.log("print", data.getTitle(), "data");
        } catch (Exception e) {
            BiobankPlugin.openAsyncError("Error Logging Print", e);
        }
    }
}
