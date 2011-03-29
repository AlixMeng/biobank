package edu.ualberta.med.biobank.views;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.BiobankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.treeview.patient.PatientAdapter;
import edu.ualberta.med.biobank.treeview.patient.PatientSearchedNode;
import edu.ualberta.med.biobank.treeview.patient.StudyWithPatientAdapter;

public class CollectionView extends AbstractAdministrationView {

    public static final String ID = "edu.ualberta.med.biobank.views.CollectionView";

    private static CollectionView currentInstance;

    private PatientSearchedNode searchedNode;

    public CollectionView() {
        super();
        currentInstance = this;
        SessionManager.addView(this);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        createNodes();
    }

    protected List<? extends ModelWrapper<?>> search(String text)
        throws Exception {
        PatientWrapper patient = PatientWrapper.getPatient(
            SessionManager.getAppService(), text.trim(),
            SessionManager.getUser());
        if (patient != null) {
            return Arrays.asList(patient);
        }
        return null;
    }

    protected void notFound(String text) {
        boolean create = BiobankPlugin.openConfirm("Patient not found",
            "Do you want to create this patient ?");
        if (create) {
            PatientWrapper patient = new PatientWrapper(
                SessionManager.getAppService());
            patient.setPnumber(text);
            PatientAdapter adapter = new PatientAdapter(searchedNode, patient);
            adapter.openEntryForm();
        }
    }

    protected PatientSearchedNode createSearchedNode() {
        if (searchedNode == null)
            return new PatientSearchedNode(rootNode, 0);
        else
            return searchedNode;
    }

    public static CollectionView getCurrent() {
        return currentInstance;
    }

    public static PatientAdapter getCurrentPatient() {
        AdapterBase selectedNode = currentInstance.getSelectedNode();
        if (selectedNode != null && selectedNode instanceof PatientAdapter) {
            return (PatientAdapter) selectedNode;
        }
        return null;
    }

    public static void reloadCurrent() {
        if (currentInstance != null)
            currentInstance.reload();
    }

    public static void showPatient(PatientWrapper patient) {
        if (currentInstance != null) {
            currentInstance.showSearchedObjectsInTree(Arrays.asList(patient),
                false);
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected String getTreeTextToolTip() {
        return "Enter a patient number";
    }

    protected void showSearchedObjectsInTree(
        List<? extends ModelWrapper<?>> searchedObjects, boolean doubleClick) {
        for (ModelWrapper<?> searchedObject : searchedObjects) {
            List<AdapterBase> nodeRes = rootNode.search(searchedObject);
            if (nodeRes.size() == 0) {
                searchedNode.addSearchObject(searchedObject);
                searchedNode.performExpand();
                nodeRes = searchedNode.search(searchedObject);
            }
            if (nodeRes.size() > 0) {
                // FIXME: why do this here when BiobankFormBase.setFocus() sets
                // sets the selected node also?
                // setSelectedNodeAsync(nodeRes.get(0));
                if (doubleClick) {
                    nodeRes.get(0).performDoubleClick();
                }
            }
        }
    }

    public static AdapterBase addToNode(AdapterBase parentNode,
        ModelWrapper<?> wrapper) {
        if (wrapper instanceof PatientWrapper) {
            PatientWrapper patient = (PatientWrapper) wrapper;
            List<AdapterBase> res = parentNode.search(patient.getStudy());
            StudyWithPatientAdapter studyAdapter = null;
            if (res.size() > 0)
                studyAdapter = (StudyWithPatientAdapter) res.get(0);
            if (studyAdapter == null) {
                studyAdapter = new StudyWithPatientAdapter(parentNode,
                    patient.getStudy());
                studyAdapter.setEditable(false);
                studyAdapter.setLoadChildrenInBackground(false);
                parentNode.addChild(studyAdapter);
            }
            List<AdapterBase> patientAdapterList = studyAdapter.search(patient);
            PatientAdapter patientAdapter = null;
            if (patientAdapterList.size() > 0)
                patientAdapter = (PatientAdapter) patientAdapterList.get(0);
            else {
                patientAdapter = new PatientAdapter(studyAdapter, patient);
                studyAdapter.addChild(patientAdapter);
            }
            return patientAdapter;
        }
        return null;
    }

    @Override
    protected void internalSearch() {
        String text = treeText.getText();
        try {
            List<? extends ModelWrapper<?>> searchedObject = search(text);
            if (searchedObject == null || searchedObject.size() == 0) {
                notFound(text);
            } else {
                showSearchedObjectsInTree(searchedObject, true);
                getTreeViewer().expandToLevel(searchedNode, 3);
            }
        } catch (Exception e) {
            BiobankPlugin.openAsyncError("Search error", e);
        }
    }

    private void createNodes() {
        searchedNode = createSearchedNode();
        rootNode.addChild(searchedNode);
        searchedNode.setParent(rootNode);
    }

    public AdapterBase getSearchedNode() {
        return searchedNode;
    }

    @Override
    public void reload() {
        rootNode.removeAll();
        createNodes();
        for (AdapterBase adapter : rootNode.getChildren())
            adapter.rebuild();
        super.reload();
    }

    @Override
    public void clear() {
        searchedNode.clear();
        setSearchFieldsEnablement(false);
    }

}
