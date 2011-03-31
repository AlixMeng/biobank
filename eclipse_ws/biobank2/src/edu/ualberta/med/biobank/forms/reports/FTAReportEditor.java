package edu.ualberta.med.biobank.forms.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.widgets.BiobankLabelProvider;
import edu.ualberta.med.biobank.widgets.DateTimeWidget;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class FTAReportEditor extends ReportsEditor {

    public static String ID = "edu.ualberta.med.biobank.editors.FTAReportEditor";

    private ComboViewer studyCombo;
    private DateTimeWidget afterDate;

    @Override
    protected void createOptionSection(Composite parent) throws Exception {
        studyCombo = createStudyComboOption("Study", parent);
        afterDate = widgetCreator.createDateTimeWidget(parent,
            "After Date (Drawn)", null, null, null, SWT.DATE);
    }

    @Override
    protected void initReport() {
        List<Object> params = new ArrayList<Object>();
        params.add(((StudyWrapper) ((IStructuredSelection) studyCombo
            .getSelection()).getFirstElement()).getNameShort());
        params.add(ReportsEditor.processDate(afterDate.getDate(), true));
        report.setParams(params);
    }

    protected ComboViewer createStudyComboOption(String labelText,
        Composite parent) throws ApplicationException {
        Collection<StudyWrapper> studyWrappers;
        studyWrappers = StudyWrapper.getAllStudies(SessionManager
            .getAppService());
        ComboViewer combo = widgetCreator.createComboViewer(parent, labelText,
            studyWrappers, null);
        combo.setLabelProvider(new BiobankLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((StudyWrapper) element).getNameShort();
            }
        });
        combo.getCombo().select(0);
        return combo;
    }

    @Override
    protected String[] getColumnNames() {
        return new String[] { "Patient Number", "Date Processed",
            "Inventory Id", "Sample Type", "Site", "Location" };
    }

    @Override
    protected List<String> getParamNames() {
        List<String> names = new ArrayList<String>();
        names.add("Study");
        names.add("After Date (Processed)");
        return names;
    }

    @Override
    protected List<Object> getPrintParams() throws Exception {
        List<Object> params = new ArrayList<Object>();
        params.add(((StudyWrapper) ((IStructuredSelection) studyCombo
            .getSelection()).getFirstElement()).getNameShort());
        params.add(ReportsEditor.processDate(afterDate.getDate(), true));
        return params;
    }
}
