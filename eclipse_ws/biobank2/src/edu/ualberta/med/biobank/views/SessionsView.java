package edu.ualberta.med.biobank.views;

import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.treeview.admin.SessionAdapter;
import edu.ualberta.med.biobank.widgets.AdapterTreeWidget;

/**
 * This view contains a tree view that represents the link to the server and the
 * ORM model objects in the database.
 */
public class SessionsView extends AbstractViewWithAdapterTree {

    public static final String ID = "edu.ualberta.med.biobank.views.SessionsView";

    public SessionsView() {
        SessionManager.getInstance().setSessionsView(this);
    }

    @Override
    public void createPartControl(Composite parent) {
        adaptersTree = new AdapterTreeWidget(parent, false);
        getSite().setSelectionProvider(getTreeViewer());
        rootNode = SessionManager.getInstance().getRootNode();
        getTreeViewer().setInput(rootNode);
        rootNode.setTreeViewer(getTreeViewer());
        if (rootNode.hasChildren()) {
            getTreeViewer().expandToLevel(3);
        }
        // will refresh and expand sites if open application on another view
        getTreeViewer().refresh();
        SessionAdapter session = (SessionAdapter) rootNode.getChild(0);
        if (session != null && SessionManager.getInstance().isConnected())
            session.rebuild();
    }

    @Override
    public void reload() {
        SessionAdapter session = (SessionAdapter) rootNode.getChild(0);
        if (session != null) {
            session.rebuild();
        }
        if (SessionManager.isSuperAdminMode())
            setPartName("Administration");
        else
            setPartName("Site Administration");
    }

    @Override
    public String getId() {
        return ID;
    }
};
