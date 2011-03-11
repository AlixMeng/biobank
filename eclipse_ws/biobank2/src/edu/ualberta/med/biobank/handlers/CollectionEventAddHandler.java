package edu.ualberta.med.biobank.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.CollectionEventWrapper;
import edu.ualberta.med.biobank.common.wrappers.ProcessingEventWrapper;
import edu.ualberta.med.biobank.logs.BiobankLogger;
import edu.ualberta.med.biobank.treeview.patient.CollectionEventAdapter;
import edu.ualberta.med.biobank.treeview.patient.PatientAdapter;
import edu.ualberta.med.biobank.views.CollectionView;

public class CollectionEventAddHandler extends AbstractHandler {

    private static BiobankLogger logger = BiobankLogger
        .getLogger(CollectionEventAddHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            PatientAdapter patientAdapter = CollectionView.getCurrentPatient();
            CollectionEventWrapper ceWrapper = new CollectionEventWrapper(
                SessionManager.getAppService());
            ceWrapper.setPatient(patientAdapter.getWrapper());
            CollectionEventAdapter adapter = new CollectionEventAdapter(
                patientAdapter, ceWrapper);
            adapter.openEntryForm();
        } catch (Exception exp) {
            logger.error("Error while opening the patient visit entry form",
                exp);
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        return SessionManager.canCreate(ProcessingEventWrapper.class, null);
    }
}