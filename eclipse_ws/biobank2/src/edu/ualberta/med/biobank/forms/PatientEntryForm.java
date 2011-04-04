package edu.ualberta.med.biobank.forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import edu.ualberta.med.biobank.Messages;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.peer.PatientPeer;
import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.logs.BiobankLogger;
import edu.ualberta.med.biobank.treeview.patient.PatientAdapter;
import edu.ualberta.med.biobank.validators.NonEmptyStringValidator;
import edu.ualberta.med.biobank.validators.NotNullValidator;
import edu.ualberta.med.biobank.views.CollectionView;
import edu.ualberta.med.biobank.widgets.BiobankText;
import edu.ualberta.med.biobank.widgets.DateTimeWidget;
import edu.ualberta.med.biobank.widgets.utils.ComboSelectionUpdate;

public class PatientEntryForm extends BiobankEntryForm {

    private static BiobankLogger logger = BiobankLogger
        .getLogger(PatientEntryForm.class.getName());

    public static final String ID = "edu.ualberta.med.biobank.forms.PatientEntryForm";

    private static final String CREATED_AT_BINDING = "patient-created-at-binding";

    public static final String MSG_NEW_PATIENT_OK = Messages
        .getString("PatientEntryForm.creation.msg");

    public static final String MSG_PATIENT_OK = Messages
        .getString("PatientEntryForm.edition.msg");

    private PatientWrapper patient;

    private ComboViewer studiesViewer;

    private Label createdAtLabel;

    private NotNullValidator createdAtValidator;

    private DateTimeWidget createdAtWidget;

    private NonEmptyStringValidator pnumberNonEmptyValidator = new NonEmptyStringValidator(
        Messages.getString("PatientEntryForm.patientNumber.validation.msg"));

    @Override
    public void init() throws Exception {
        Assert.isTrue((adapter instanceof PatientAdapter),
            "Invalid editor input: object of type "
                + adapter.getClass().getName());

        if (adapter.getModelObject().isNew())
            patient = (PatientWrapper) adapter.getModelObject();
        else
            try {
                patient = (PatientWrapper) adapter.getModelObjectClone();
            } catch (Exception e1) {
                logger.error("Error getting patient clone", e1);
            }
        retrievePatient();
        SessionManager.logEdit(patient);
        String tabName;
        if (patient.isNew()) {
            tabName = Messages.getString("PatientEntryForm.new.title");
        } else {
            tabName = Messages.getString("PatientEntryForm.edit.title",
                patient.getPnumber());
        }
        setPartName(tabName);
    }

    @Override
    protected void createFormContent() throws Exception {
        form.setText(Messages.getString("PatientEntryForm.main.title"));
        form.setMessage(getOkMessage(), IMessageProvider.NONE);
        page.setLayout(new GridLayout(1, false));

        createPatientSection();

        if (patient.isNew()) {
            setDirty(true);
        }
    }

    private void createPatientSection() throws Exception {
        Composite client = toolkit.createComposite(page);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        List<StudyWrapper> studies = new ArrayList<StudyWrapper>(
            StudyWrapper.getAllStudies(appService));
        StudyWrapper selectedStudy = null;
        if (patient.isNew()) {
            if (studies.size() == 1) {
                selectedStudy = studies.get(0);
            }
        } else {
            selectedStudy = patient.getStudy();
        }

        studiesViewer = createComboViewer(client,
            Messages.getString("PatientEntryForm.field.study.label"), studies,
            selectedStudy,
            Messages.getString("PatientEntryForm.field.study.validation.msg"),
            new ComboSelectionUpdate() {
                @Override
                public void doSelection(Object selectedObject) {
                    patient.setStudy((StudyWrapper) selectedObject);
                }
            });
        setFirstControl(studiesViewer.getControl());

        createBoundWidgetWithLabel(client, BiobankText.class, SWT.NONE,
            Messages.getString("PatientEntryForm.field.pNumber.label"), null,
            patient, PatientPeer.PNUMBER.getName(), pnumberNonEmptyValidator);

        createdAtLabel = widgetCreator.createLabel(client, "Created At");
        createdAtLabel.setLayoutData(new GridData(
            GridData.VERTICAL_ALIGN_BEGINNING));
        createdAtValidator = new NotNullValidator("Created At should be set");

        createdAtWidget = createDateTimeWidget(client, createdAtLabel,
            patient.getCreatedAt(), patient, "createdAt", createdAtValidator,
            SWT.DATE | SWT.TIME, CREATED_AT_BINDING);
    }

    @Override
    protected String getOkMessage() {
        if (patient.isNew()) {
            return MSG_NEW_PATIENT_OK;
        }
        return MSG_PATIENT_OK;
    }

    @Override
    protected void saveForm() throws Exception {
        patient.persist();
        // to update patient view:
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                CollectionView.reloadCurrent();
                CollectionView.showPatient(patient);
            }
        });
    }

    @Override
    public String getNextOpenedFormID() {
        return PatientViewForm.ID;
    }

    private void retrievePatient() {
        try {
            patient.reload();
        } catch (Exception e) {
            logger
                .error(
                    Messages.getString("PatientEntryForm.retrieve.error.msg",
                        patient.getPnumber()), e);
        }
    }

    @Override
    public void reset() throws Exception {
        super.reset();
        StudyWrapper study = patient.getStudy();
        if (study != null) {
            studiesViewer.setSelection(new StructuredSelection(study));
        }
        createdAtWidget.setDate(new Date());
        studiesViewer.setSelection(null);
        patient.reset();
        pnumberNonEmptyValidator.validate(null);
    }
}
