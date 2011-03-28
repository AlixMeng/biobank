package edu.ualberta.med.biobank.treeview.admin;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

import edu.ualberta.med.biobank.BiobankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.security.User;
import edu.ualberta.med.biobank.common.wrappers.CenterWrapper;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.server.applicationservice.BiobankApplicationService;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.treeview.util.AdapterFactory;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class SessionAdapter extends AdapterBase {

    private static final String LOGOUT_COMMAND_ID = "edu.ualberta.med.biobank.commands.logout";

    public static final int CLINICS_BASE_NODE_ID = 0;

    public static final int SITES_NODE_ID = 1;

    public static final int STUDIES_NODE_ID = 2;

    private BiobankApplicationService appService;

    private User user;
    private String serverName;

    public SessionAdapter(AdapterBase parent,
        BiobankApplicationService appService, int sessionId, String serverName,
        User user) {
        super(parent, null, false);
        this.appService = appService;
        setId(sessionId);
        if (user.getLogin().isEmpty()) {
            setName(serverName);
        } else {
            setName(serverName + " [" + user.getLogin() + "]");
        }
        this.serverName = serverName;
        this.user = user;

        addSubNodes();
    }

    private void addSubNodes() {
        if (SessionManager.getInstance().isConnected()) {
            if (SessionManager.isSuperAdminMode()) {
                addChild(new StudyMasterGroup(this, STUDIES_NODE_ID));
                addChild(new ClinicMasterGroup(this, CLINICS_BASE_NODE_ID));
                SiteGroup siteGroup = new SiteGroup(this, SITES_NODE_ID);
                addChild(siteGroup);
                siteGroup.performExpand();
            } else {
                CenterWrapper<?> currentCenter = SessionManager.getUser()
                    .getCurrentWorkingCenter();
                CenterWrapper<?> clonedCenter;
                try {
                    clonedCenter = (CenterWrapper<?>) currentCenter
                        .getDatabaseClone();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (clonedCenter != null) {
                    AdapterBase child = AdapterFactory.getAdapter(clonedCenter);
                    addChild(child);
                    child.performExpand();
                }
            }
        }
    }

    @Override
    public void rebuild() {
        removeAll();
        addSubNodes();
    }

    @Override
    public BiobankApplicationService getAppService() {
        return appService;
    }

    public void resetAppService() {
        appService = null;
    }

    @Override
    protected String getLabelInternal() {
        return "";
    }

    @Override
    public String getTooltipText() {
        if (appService != null) {
            return "Current server version: " + appService.getServerVersion();
        }
        return "";
    }

    private SiteGroup getSitesGroupNode() {
        AdapterBase adapter = getChild(SITES_NODE_ID);
        Assert.isNotNull(adapter);
        return (SiteGroup) adapter;
    }

    private StudyMasterGroup getStudiesGroupNode() {
        AdapterBase adapter = getChild(STUDIES_NODE_ID);
        Assert.isNotNull(adapter);
        return (StudyMasterGroup) adapter;
    }

    private ClinicMasterGroup getClinicGroupNode() {
        AdapterBase adapter = getChild(CLINICS_BASE_NODE_ID);
        Assert.isNotNull(adapter);
        return (ClinicMasterGroup) adapter;
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Logout");
        mi.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                IHandlerService handlerService = (IHandlerService) PlatformUI
                    .getWorkbench().getService(IHandlerService.class);

                try {
                    handlerService.executeCommand(LOGOUT_COMMAND_ID, null);
                } catch (Exception ex) {
                    throw new RuntimeException(LOGOUT_COMMAND_ID + " not found");
                }
            }
        });
    }

    public User getUser() {
        return user;
    }

    public String getServerName() {
        return serverName;
    }

    @Override
    public List<AdapterBase> search(Object searchedObject) {
        return searchChildren(searchedObject);
    }

    @Override
    protected AdapterBase createChildNode() {
        return new SiteAdapter(this, null);
    }

    @Override
    protected AdapterBase createChildNode(ModelWrapper<?> child) {
        return null;
    }

    @Override
    protected Collection<? extends ModelWrapper<?>> getWrapperChildren() {
        return null;
    }

    @Override
    protected int getWrapperChildCount() {
        return 0;
    }

    @Override
    public String getEntryFormId() {
        return null;
    }

    @Override
    public String getViewFormId() {
        return null;
    }

    @SuppressWarnings("unused")
    public List<ClinicWrapper> getClinicCollection(boolean sort) {
        try {
            return ClinicWrapper.getAllClinics(appService);
        } catch (ApplicationException e) {
            BiobankPlugin.openAsyncError(
                "Unable to load clinics from database", e);
        }
        return null;
    }

    public void addStudy() {
        StudyMasterGroup g = getStudiesGroupNode();
        if (g != null) {
            g.addStudy();
        }
    }

    public void addClinic() {
        ClinicMasterGroup g = getClinicGroupNode();
        if (g != null) {
            g.addClinic();
        }
    }

    public void addSite() {
        SiteGroup s = getSitesGroupNode();
        if (s != null)
            s.addSite();
    }

}
