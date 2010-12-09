package edu.ualberta.med.biobank.rcp;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.client.util.ServiceConnection;
import edu.ualberta.med.biobank.rcp.perspective.AliquotManagementPerspective;
import edu.ualberta.med.biobank.rcp.perspective.MainPerspective;
import edu.ualberta.med.biobank.rcp.perspective.ReportsPerspective;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
        IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

    @Override
    public String getInitialWindowPerspectiveId() {
        return MainPerspective.ID;
    }

    @Override
    public void initialize(IWorkbenchConfigurer configurer) {
        configurer.setSaveAndRestore(true);
    }

    @Override
    public boolean preShutdown() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        if (page.getPerspective().getId().equals(ReportsPerspective.ID)) {
            IPerspectiveDescriptor main = workbench.getPerspectiveRegistry()
                .findPerspectiveWithId(MainPerspective.ID);
            page.setPerspective(main);
        }
        if (BioBankPlugin.isAskPrintActivityLog()
            && page.getPerspective().getId()
                .equals(AliquotManagementPerspective.ID)) {
            BioBankPlugin.openInformation("Can't close",
                "Please end aliquot management session before closing");
            return false;
        }
        if (SessionManager.getInstance().isConnected()) {
            try {
                ServiceConnection.logout(SessionManager.getAppService());
            } catch (Exception e) {
                return true;
            }
        }
        return true;
    }

    @Override
    public void postStartup() {
        PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
        // a 'Hidden General' preference page is added in the plugin.xml with id
        // 'org.eclipse.ui.preferencePages.Workbench'
        // It will prevent the security preference page warning about its
        // missing category
        pm.remove("org.eclipse.equinox.security.ui.category");
        pm.remove("org.eclipse.ui.preferencePages.Workbench");
        // this preference page is deactivated because it send a warning about
        // its missing parent. We have our own preference page definition that
        // is using the exact same
        pm.remove("org.eclipse.equinox.internal.p2.ui.sdk.scheduler.AutomaticUpdatesPreferencePage");
        pm.remove("org.eclipse.equinox.internal.p2.ui.sdk.ProvisioningPreferencePage");
    }

}
