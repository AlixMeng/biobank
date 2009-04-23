package edu.ualberta.med.biobank.forms;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.ListOrderedMap;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.springframework.remoting.RemoteConnectFailureException;

import com.gface.date.DatePickerCombo;
import com.gface.date.DatePickerStyle;

import edu.ualberta.med.biobank.forms.input.FormInput;
import edu.ualberta.med.biobank.model.PatientVisit;
import edu.ualberta.med.biobank.model.PatientVisitData;
import edu.ualberta.med.biobank.model.Sdata;
import edu.ualberta.med.biobank.model.Study;
import edu.ualberta.med.biobank.treeview.Node;
import edu.ualberta.med.biobank.treeview.PatientAdapter;
import edu.ualberta.med.biobank.treeview.PatientVisitAdapter;
import edu.ualberta.med.biobank.treeview.StudyAdapter;
import edu.ualberta.med.biobank.validators.NonEmptyString;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.query.SDKQuery;
import gov.nih.nci.system.query.SDKQueryResult;
import gov.nih.nci.system.query.example.InsertExampleQuery;
import gov.nih.nci.system.query.example.UpdateExampleQuery;

public class PatientVisitEntryForm extends BiobankEntryForm {
    public static final String ID =
        "edu.ualberta.med.biobank.forms.PatientVisitEntryForm";

    public static final String NEW_PATIENT_VISIT_OK_MESSAGE =
        "Creating a new patient visit record.";

    public static final String PATIENT_VISIT_OK_MESSAGE =
        "Editing an existing patient visit record.";
    
    public static final String NO_VISIT_NUMBER_MESSAGE =
        "Visit must have a number";
    
    public static final String DATE_FORMAT = 
        "yyyy-MM-dd";

    private PatientVisitAdapter patientVisitAdapter;

    private PatientVisit patientVisit;

    private Study study;

    private ListOrderedMap pvInfoMap;

    private Button submit;

    public PatientVisitEntryForm() {
        super();
        pvInfoMap = new ListOrderedMap();
    }
    
    class PatientVisitInfo {
        Sdata sdata;
        PatientVisitData pvData;
        
        public PatientVisitInfo() {
            sdata = null;
            pvData = null;
        }
    }

    @Override
    public void init(IEditorSite editorSite, IEditorInput input)
            throws PartInitException {
        super.init(editorSite, input);

        Node node = ((FormInput) input).getNode();
        Assert.isNotNull(node, "Null editor input");

        patientVisitAdapter = (PatientVisitAdapter) node;
        appService = patientVisitAdapter.getAppService();
        patientVisit = patientVisitAdapter.getPatientVisit();

        if (patientVisit.getId() == null) {
            setPartName("New Patient Visit");
        }
        else {
            setPartName("Patient Visit" + patientVisit.getId());
        }
    }

    @Override
    protected void createFormContent() {
        form.setText("Patient Visit Information");
        form.setMessage(getOkMessage(), IMessageProvider.NONE);
        form.getBody().setLayout(new GridLayout(1, false));

        createPvSection();
        createButtonsSection();
    }

    private void createPvSection() {
        Composite client = toolkit.createComposite(form.getBody());
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        createBoundWidget(client, Text.class, SWT.NONE, "Visit Number", null,
            PojoObservables.observeValue(patientVisit, "number"),
            NonEmptyString.class, NO_VISIT_NUMBER_MESSAGE);

        study = (Study) ((StudyAdapter)
            patientVisitAdapter.getParent().getParent().getParent()).getStudy();

        for (Sdata sdata : study.getSdataCollection()) {
            PatientVisitInfo pvInfo = new PatientVisitInfo();
            pvInfo.sdata = sdata;
            pvInfoMap.put(sdata.getSdataType().getType(), pvInfo);
        }

        Collection<PatientVisitData> pvDataCollection =
            patientVisit.getPatientVisitDataCollection();
        if (pvDataCollection != null) {
            for (PatientVisitData pvData : pvDataCollection) {
                String key = pvData.getSdata().getSdataType().getType();
                PatientVisitInfo pvInfo = (PatientVisitInfo) pvInfoMap.get(key);
                pvInfo.pvData = pvData;
            }
        }

        Control control;
        MapIterator it = pvInfoMap.mapIterator();
        while (it.hasNext()) {
            control = null;
            String label = (String) it.next();
            PatientVisitInfo pvInfo = (PatientVisitInfo) it.getValue();
            String value = null;
            int typeId = pvInfo.sdata.getSdataType().getId();
            
            if (pvInfo.pvData != null) {
                value = pvInfo.pvData.getValue();
            }
            
            Label labelWidget = toolkit.createLabel(client, label + ":", SWT.LEFT);
            labelWidget.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

            switch (typeId) {
                case 1: // Date Drawn
                case 2: // Date Received
                case 3: // Date Processed
                case 4: // Shipped Date
                    control = createDatePickerSection(client, value);
                    break;
                    
                case 5: // Aliquot Volume
                case 6: // Blood Received
                case 7: // Visit
                    control = createComboSection(client, 
                        pvInfo.sdata.getValue().split(";"), 
                        value);
                    break;
                    
                case  8: // WBC Count
                case  9: // Time Arrived
                case  10: // Biopsy Length
                    control = toolkit.createText(client, value, 
                        SWT.LEFT);
                    break;
                    
                case 11: // Comments
                    control = toolkit.createText(client, value, 
                        SWT.LEFT | SWT.MULTI);
                    break;
                    
                default:
                    Assert.isTrue(false, "Invalid sdata type: " + typeId);
            }
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            if (typeId == 11) {
                gd.heightHint = 40;
            }
            control.setLayoutData(gd);
            controls.put(label, control);
        }
    }

    private Control createDatePickerSection(Composite client, String value) {        
        DatePickerCombo datePicker = new DatePickerCombo(client, SWT.BORDER,
            DatePickerStyle.BUTTONS_ON_BOTTOM | DatePickerStyle.YEAR_BUTTONS
            | DatePickerStyle.HIDE_WHEN_NOT_IN_FOCUS);
        datePicker.setLayout(new GridLayout(1, false));
        datePicker.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        datePicker.setDateFormat(new SimpleDateFormat(DATE_FORMAT));

        if ((value != null) && (value.length() > 0)) {
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
            try {
                datePicker.setDate(df.parse(value));
            }
            catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
        return datePicker;
    }
    
    private Control createComboSection(Composite client, String [] values, 
        String selected) {
        
        Combo combo = new Combo(client, SWT.READ_ONLY);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        combo.setItems(values);
        
        if (selected != null) {
            int count = 0;       
            for (String value : values) {
                if (selected.equals(value)) {
                    combo.select(count);
                    break;
                }
                ++count;
            }
        }
        
        toolkit.adapt(combo, true, true);
        
        return combo;
    }

    private void createButtonsSection() {
        Composite client = toolkit.createComposite(form.getBody());
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        submit = toolkit.createButton(client, "Submit", SWT.PUSH);
        submit.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                doSaveInternal();
            }
        });
    }

    private String getOkMessage() {
        if (patientVisit.getId() == null) {
            return NEW_PATIENT_VISIT_OK_MESSAGE;
        }
        return PATIENT_VISIT_OK_MESSAGE;
    }

    @Override
    protected void handleStatusChanged(IStatus status) {   
        if (status.getSeverity() == IStatus.OK) {
            form.setMessage(getOkMessage(), IMessageProvider.NONE);
            submit.setEnabled(true);
        }
        else {
            form.setMessage(status.getMessage(), IMessageProvider.ERROR);
            submit.setEnabled(false);
        }          
    }

    @Override
    protected void saveForm() {
        try {
            SDKQuery query;
            SDKQueryResult result;
            
            PatientAdapter patientAdapter = 
                (PatientAdapter) patientVisitAdapter.getParent();
            
            patientVisit.setPatient(patientAdapter.getPatient()); 

            if ((patientVisit.getId() == null) || (patientVisit.getId() == 0)) {
                query = new InsertExampleQuery(patientVisit);
            }
            else { 
                query = new UpdateExampleQuery(patientVisit);
            }

            result = appService.executeQuery(query);
            patientVisit = (PatientVisit) result.getObjectResult();     
            
            savePatientVisitData();
            
            patientAdapter.performExpand();       
            getSite().getPage().closeEditor(this, false);    
        }
        catch (final RemoteConnectFailureException exp) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    MessageDialog.openError(
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
                            "Connection Attempt Failed", 
                    "Could not connect to server. Make sure server is running.");
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Set<PatientVisitData> savePatientVisitData() throws ApplicationException {
        SDKQuery query;
        SDKQueryResult result;
        Set<PatientVisitData> pvDataCollection = new HashSet<PatientVisitData>();
        
        for (String key : controls.keySet()) {
            PatientVisitInfo pvInfo = (PatientVisitInfo) pvInfoMap.get(key);
            PatientVisitData pvData = new PatientVisitData();

            if ((patientVisit.getId() != null) && (patientVisit.getId() != 0)) {
                pvData.setPatientVisit(patientVisit);
            }
            pvData.setSdata(pvInfo.sdata);
            
            if (pvInfo.pvData != null) {
                pvData.setId(pvInfo.pvData.getId());
            }
            Control control = controls.get(key);
            
            if (control instanceof Text) {
                pvData.setValue(((Text) control).getText());
                System.out.println(key + ": " + ((Text) control).getText());
            }
            else if (control instanceof Combo) {
                String [] options = pvInfo.sdata.getValue().split(";");
                int index = ((Combo) control).getSelectionIndex();
                
                Assert.isTrue(index < options.length,
                    "Invalid combo box selection " + index);
                pvData.setValue(options[index]);
                System.out.println(key + ": " + options[index]);
            }
            else if (control instanceof DatePickerCombo) {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                Date date = ((DatePickerCombo) control).getDate();
                if (date != null) {
                    System.out.println(key + ": " +  sdf.format(date));
                    pvData.setValue(sdf.format(date));
                }
            }

            if (pvInfo.pvData == null) {
                query = new InsertExampleQuery(pvData);
            }
            else {
                query = new UpdateExampleQuery(pvData);
            }                  

            result = patientVisitAdapter.getAppService().executeQuery(query);
            pvDataCollection.add((PatientVisitData) result.getObjectResult());
        }
        
        return pvDataCollection;
    }
}
