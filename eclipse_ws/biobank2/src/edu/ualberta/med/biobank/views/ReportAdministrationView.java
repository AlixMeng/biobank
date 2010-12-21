package edu.ualberta.med.biobank.views;

import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.rcp.perspective.ReportAdministrationPerspective;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.treeview.report.AbstractReportGroup;
import edu.ualberta.med.biobank.treeview.report.PrivateReportsGroup;
import edu.ualberta.med.biobank.treeview.report.SharedReportsGroup;

public class ReportAdministrationView extends AbstractAdministrationView {
    public static final String ID = "edu.ualberta.med.biobank.views.ReportAdministrationView";

    private static ReportAdministrationView currentView;

    public ReportAdministrationView() {
        currentView = this;
        SessionManager.addView(ReportAdministrationPerspective.ID, this);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        createNodes();
    }

    @Override
    public void reload() {
        rootNode.removeAll();
        createNodes();

        for (AdapterBase adapter : rootNode.getChildren()) {
            adapter.rebuild();
        }

        super.reload();
    }

    public static ReportAdministrationView getCurrent() {
        return currentView;
    }

    private void createNodes() {
        if (SessionManager.getInstance().isConnected()) {
            AbstractReportGroup adapter = new PrivateReportsGroup(rootNode, 0);
            adapter.setParent(rootNode);
            adapter.setModifiable(true);
            rootNode.addChild(adapter);

            adapter = new SharedReportsGroup(rootNode, 1);
            adapter.setParent(rootNode);
            rootNode.addChild(adapter);
        }
    }

    @Override
    protected void internalSearch() {
        // TODO Auto-generated method stub
    }

    @Override
    protected String getTreeTextToolTip() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }
}
