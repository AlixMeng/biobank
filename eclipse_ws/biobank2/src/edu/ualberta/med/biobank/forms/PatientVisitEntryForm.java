package edu.ualberta.med.biobank.forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.formatters.DateFormatter;
import edu.ualberta.med.biobank.common.wrappers.PatientVisitWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import edu.ualberta.med.biobank.common.wrappers.ShipmentWrapper;
import edu.ualberta.med.biobank.model.PvCustomInfo;
import edu.ualberta.med.biobank.model.Shipment;
import edu.ualberta.med.biobank.treeview.PatientAdapter;
import edu.ualberta.med.biobank.treeview.PatientVisitAdapter;
import edu.ualberta.med.biobank.validators.DoubleNumberValidator;
import edu.ualberta.med.biobank.widgets.ComboAndQuantityWidget;
import edu.ualberta.med.biobank.widgets.DateTimeWidget;
import edu.ualberta.med.biobank.widgets.PvSampleSourceEntryWidget;
import edu.ualberta.med.biobank.widgets.SelectMultipleWidget;
import edu.ualberta.med.biobank.widgets.listeners.BiobankEntryFormWidgetListener;
import edu.ualberta.med.biobank.widgets.listeners.MultiSelectEvent;

public class PatientVisitEntryForm extends BiobankEntryForm {

    private static Logger LOGGER = Logger.getLogger(PatientVisitEntryForm.class
        .getName());

    public static final String ID = "edu.ualberta.med.biobank.forms.PatientVisitEntryForm";

    public static final String MSG_NEW_PATIENT_VISIT_OK = "Creating a new patient visit record.";

    public static final String MSG_PATIENT_VISIT_OK = "Editing an existing patient visit record.";

    public static final String MSG_NO_VISIT_NUMBER = "Visit must have a number";

    private PatientVisitAdapter patientVisitAdapter;

    private PatientVisitWrapper patientVisitWrapper;

    private DateTimeWidget dateProcessed;

    private PatientWrapper patientWrapper;

    private class FormPvCustomInfo extends PvCustomInfo {
        Control control;
    }

    private List<FormPvCustomInfo> pvCustomInfoList;

    private ComboViewer shipmentsComboViewer;

    private PvSampleSourceEntryWidget pvSampleSourceEntryWidget;

    @Override
    public void init() {
        Assert.isTrue(adapter instanceof PatientVisitAdapter,
            "Invalid editor input: object of type "
                + adapter.getClass().getName());

        patientVisitAdapter = (PatientVisitAdapter) adapter;
        patientVisitWrapper = patientVisitAdapter.getWrapper();
        patientWrapper = ((PatientAdapter) patientVisitAdapter.getParent())
            .getWrapper();
        retrieve();
        String tabName;
        if (patientVisitWrapper.isNew()) {
            tabName = "New Patient Visit";
        } else {
            tabName = "Visit "
                + patientVisitWrapper.getFormattedDateProcessed();
        }
        setPartName(tabName);
    }

    private void retrieve() {
        try {
            patientVisitWrapper.reload();
            patientWrapper.reload();
        } catch (Exception e) {
            LOGGER.error("Error while retrieving patient visit "
                + patientVisitAdapter.getWrapper().getFormattedDateProcessed()
                + " (Patient " + patientVisitWrapper.getPatient() + ")", e);
        }
    }

    @Override
    protected void createFormContent() throws Exception {
        form.setText("Patient Visit Information");
        form.setMessage(getOkMessage(), IMessageProvider.NONE);
        form.getBody().setLayout(new GridLayout(1, false));
        form.setImage(BioBankPlugin.getDefault().getImageRegistry().get(
            BioBankPlugin.IMG_PATIENT_VISIT));
        createMainSection();
        createSourcesSection();
        if (patientVisitWrapper.isNew()) {
            setDirty(true);
        }
    }

    private void createMainSection() throws Exception {
        Composite client = toolkit.createComposite(form.getBody());
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        Label siteLabel = (Label) createWidget(client, Label.class, SWT.NONE,
            "Site");
        FormUtils.setTextValue(siteLabel, patientWrapper.getStudy().getSite()
            .getName());

        List<ShipmentWrapper> patientShipments = patientWrapper
            .getShipmentCollection();
        ShipmentWrapper selectedShip = patientVisitWrapper.getShipment();
        if (patientShipments.size() == 1) {
            selectedShip = patientShipments.get(0);
        }
        shipmentsComboViewer = createComboViewerWithNoSelectionValidator(
            client, "Shipment", patientShipments, selectedShip,
            "A shipment should be selected");

        if (patientVisitWrapper.getDateProcessed() == null) {
            patientVisitWrapper.setDateProcessed(new Date());
        }
        dateProcessed = createDateTimeWidget(client, "Date Processed",
            patientVisitWrapper.getDateProcessed(), patientVisitWrapper,
            "dateProcessed", "Date processed should be set");
        firstControl = dateProcessed;

        createPvDataSection(client);

        createBoundWidgetWithLabel(client, Text.class, SWT.MULTI, "Comments",
            null,
            BeansObservables.observeValue(patientVisitWrapper, "comment"), null);
    }

    private void createSourcesSection() {
        Composite client = createSectionWithClient("Source Vessels");

        GridLayout layout = new GridLayout(1, false);
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        pvSampleSourceEntryWidget = new PvSampleSourceEntryWidget(client,
            SWT.NONE, patientVisitWrapper.getPvSampleSourceCollection(),
            toolkit);
        pvSampleSourceEntryWidget
            .addSelectionChangedListener(new BiobankEntryFormWidgetListener() {
                @Override
                public void selectionChanged(MultiSelectEvent event) {
                    setDirty(true);
                }
            });
        pvSampleSourceEntryWidget.addBinding(widgetCreator);

    }

    private void createPvDataSection(Composite client) throws Exception {
        String[] labels = patientVisitWrapper.getPvInfoLabels();
        if (labels == null)
            return;

        pvCustomInfoList = new ArrayList<FormPvCustomInfo>();

        for (String label : labels) {
            FormPvCustomInfo pvCustomInfo = new FormPvCustomInfo();
            pvCustomInfo.label = label;
            pvCustomInfo.type = patientWrapper.getStudy().getPvInfoType(label);
            pvCustomInfo.allowedValues = patientWrapper.getStudy()
                .getPvInfoAllowedValues(label);
            Control control = getControlForLabel(client, pvCustomInfo);
            pvCustomInfoList.add(pvCustomInfo);
            if (control != null) {
                GridData gd = new GridData(GridData.FILL_HORIZONTAL);
                control.setLayoutData(gd);
                controls.put(label, control);
            }
        }
    }

    private Control getControlForLabel(Composite client,
        FormPvCustomInfo pvCustomInfo) {
        switch (pvCustomInfo.type) {
        case 1: // number
            return createBoundWidgetWithLabel(client, Text.class, SWT.NONE,
                pvCustomInfo.label, null, PojoObservables.observeValue(
                    pvCustomInfo, "value"), new DoubleNumberValidator(
                    "You should select a valid number"));
        case 2: // text
            return createBoundWidgetWithLabel(client, Text.class, SWT.NONE,
                pvCustomInfo.label, null, PojoObservables.observeValue(
                    pvCustomInfo, "value"), null);
        case 3: // date_time
            return createDateTimeWidget(client, pvCustomInfo.label,
                DateFormatter.parseToDateTime(pvCustomInfo.value), null, null,
                null);
        case 4: // select_single
            return createBoundWidgetWithLabel(client, Combo.class, SWT.NONE,
                pvCustomInfo.label, pvCustomInfo.allowedValues, PojoObservables
                    .observeValue(pvCustomInfo, "value"), null);
        case 5: // select_multiple
            createFieldLabel(client, pvCustomInfo.label);
            SelectMultipleWidget s = new SelectMultipleWidget(client,
                SWT.BORDER, pvCustomInfo.allowedValues, selectionListener);
            s.adaptToToolkit(toolkit, true);
            if (pvCustomInfo.value != null) {
                s.setSelections(pvCustomInfo.value.split(";"));
            }
            return s;
        case 6: // select_single_and_quantity_1_5_1
            createFieldLabel(client, pvCustomInfo.label);
            ComboAndQuantityWidget c = new ComboAndQuantityWidget(client,
                SWT.BORDER);
            c.adaptToToolkit(toolkit, true);
            if (pvCustomInfo.allowedValues != null) {
                c.addValues(pvCustomInfo.allowedValues);
            }
            if (pvCustomInfo.value != null) {
                String[] values = pvCustomInfo.value.split(" ");
                Assert.isTrue(values.length == 2);
                c.setText(values[0], Integer.parseInt(values[1]));
            }
            return c;
        default:
            Assert.isTrue(false, "Invalid pvInfo type: " + pvCustomInfo.type);
        }
        return null;
    }

    private void createFieldLabel(Composite parent, String label) {
        Label labelWidget = toolkit.createLabel(parent, label + ":", SWT.LEFT);
        labelWidget.setLayoutData(new GridData(
            GridData.VERTICAL_ALIGN_BEGINNING));
    }

    @Override
    protected String getOkMessage() {
        return (patientVisitWrapper.isNew()) ? MSG_NEW_PATIENT_VISIT_OK
            : MSG_PATIENT_VISIT_OK;
    }

    @Override
    protected void saveForm() throws Exception {
        PatientAdapter patientAdapter = (PatientAdapter) patientVisitAdapter
            .getParent();
        patientVisitWrapper.setPatient(patientAdapter.getWrapper());

        IStructuredSelection shipSelection = (IStructuredSelection) shipmentsComboViewer
            .getSelection();
        if ((shipSelection != null) && (shipSelection.size() > 0)) {
            patientVisitWrapper.setShipment((ShipmentWrapper) shipSelection
                .getFirstElement());
        } else {
            patientVisitWrapper.setShipment((Shipment) null);
        }

        patientVisitWrapper
            .setPvSampleSourceCollection(pvSampleSourceEntryWidget
                .getPvSampleSources());

        setPvCustomInfo();

        if (patientVisitWrapper.isNew()) {
            patientVisitWrapper.setUsername(SessionManager.getInstance()
                .getSession().getUserName());
        }
        patientVisitWrapper.persist();

        patientAdapter.performExpand();
    }

    private void setPvCustomInfo() throws Exception {
        for (FormPvCustomInfo combinedPvInfo : pvCustomInfoList) {
            setPvInfoValueFromControlType(combinedPvInfo);
            if ((combinedPvInfo.value == null)
                || (combinedPvInfo.value.length() == 0))
                continue;

            patientVisitWrapper.setPvInfo(combinedPvInfo.label,
                combinedPvInfo.value);
        }
    }

    private void setPvInfoValueFromControlType(FormPvCustomInfo pvCustomInfo) {
        // for text and combo, the databinding is used
        if (pvCustomInfo.control instanceof DateTimeWidget) {
            pvCustomInfo.value = ((DateTimeWidget) pvCustomInfo.control)
                .getText();
        } else if (pvCustomInfo.control instanceof ComboAndQuantityWidget) {
            pvCustomInfo.value = ((ComboAndQuantityWidget) pvCustomInfo.control)
                .getText();
        } else if (pvCustomInfo.control instanceof SelectMultipleWidget) {
            String[] values = ((SelectMultipleWidget) pvCustomInfo.control)
                .getSelections();
            pvCustomInfo.value = StringUtils.join(values, ";");
        }
    }

    @Override
    public String getNextOpenedFormID() {
        return PatientVisitViewForm.ID;
    }

    @Override
    public void reset() {
        super.reset();

        if (patientVisitWrapper.getDateProcessed() == null) {
            patientVisitWrapper.setDateProcessed(new Date());
        }
        pvSampleSourceEntryWidget
            .setSelectedPvSampleSources(patientVisitWrapper
                .getPvSampleSourceCollection());
        // TODO reset for optional values
    }
}
