package edu.ualberta.med.biobank.rcp;

import java.util.Map;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.services.ISourceProviderService;

import edu.ualberta.med.biobank.BiobankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.security.User;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.logs.BiobankLogger;
import edu.ualberta.med.biobank.rcp.perspective.LinkAssignPerspective;
import edu.ualberta.med.biobank.rcp.perspective.MainPerspective;
import edu.ualberta.med.biobank.rcp.perspective.ProcessingPerspective;
import edu.ualberta.med.biobank.rcp.perspective.ReportsPerspective;
import edu.ualberta.med.biobank.sourceproviders.SessionState;
import edu.ualberta.med.biobank.utils.BindingContextHelper;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    private static BiobankLogger logger = BiobankLogger
        .getLogger(ApplicationWorkbenchWindowAdvisor.class.getName());

    private IPropertyChangeListener propertyListener;

    private String server = null;

    private String username = null;

    private String currentCentre = null;

    public ApplicationWorkbenchWindowAdvisor(
        IWorkbenchWindowConfigurer configurer) {
        super(configurer);
        addBiobankPreferencesPropertyListener();
    }

    @Override
    public ActionBarAdvisor createActionBarAdvisor(
        IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }

    private String getWindowTitle() {
        IProduct product = Platform.getProduct();
        String windowTitle = product.getName();

        if (BiobankPlugin.getDefault().windowTitleShowVersionEnabled()) {
            windowTitle += " " + product.getDefiningBundle().getVersion();
        }
        return windowTitle;
    }

    @Override
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(800, 700));
        configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(true);

        /*
         * configurer.setShowPerspectiveBar(true);
         * PlatformUI.getPreferenceStore().setDefault("DOCK_PERSPECTIVE_BAR",
         * "left"); PlatformUI.getPreferenceStore().setDefault(
         * "SHOW_TEXT_ON_PERSPECTIVE_BAR", false);
         */
        configurer.setShowProgressIndicator(true);
        getWindowConfigurer().setTitle(getWindowTitle());
    }

    @Override
    public void postWindowOpen() {
        IStatusLineManager statusline = getWindowConfigurer()
            .getActionBarConfigurer().getStatusLineManager();
        statusline.setMessage(null, "Application ready");

        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
        IWorkbenchPage page = activeWindow.getActivePage();
        if (page.getPerspective().getId().equals(LinkAssignPerspective.ID)) {
            // can't start on this perspective: switch to patient perspective
            try {
                workbench.showPerspective(ProcessingPerspective.ID,
                    activeWindow);
            } catch (WorkbenchException e) {
                logger.error("Error while opening patients perpective", e);
            }
        }
        page.addPartListener(new BiobankPartListener());
        activeWindow.addPerspectiveListener(new BiobankPerspectiveListener());

        // to activate correct key bindings
        String currentPerspectiveId = activeWindow.getActivePage()
            .getPerspective().getId();
        activateIfNotInPerspective(currentPerspectiveId, MainPerspective.ID);
        activateIfNotInPerspective(currentPerspectiveId,
            ProcessingPerspective.ID);
        activateIfNotInPerspective(currentPerspectiveId, ReportsPerspective.ID);

        BindingContextHelper.activateContextInWorkbench(currentPerspectiveId);

        ISourceProviderService service = (ISourceProviderService) activeWindow
            .getService(ISourceProviderService.class);
        SessionState sessionSourceProvider = (SessionState) service
            .getSourceProvider(SessionState.LOGIN_STATE_SOURCE_NAME);
        sessionSourceProvider
            .addSourceProviderListener(new ISourceProviderListener() {
                @Override
                public void sourceChanged(int sourcePriority,
                    String sourceName, Object sourceValue) {
                    if (sourceValue != null) {
                        if (sourceValue.equals(SessionState.LOGGED_IN))
                            mainWindowUpdateTitle(SessionManager.getServer(),
                                SessionManager.getUser());
                        else if (sourceValue.equals(SessionState.LOGGED_OUT))
                            mainWindowResetTitle();
                    }
                }

                @Override
                public void sourceChanged(int sourcePriority,
                    @SuppressWarnings("rawtypes") Map sourceValuesByName) {
                }
            });

        BindingContextHelper
            .activateContextInWorkbench(SessionManager.BIOBANK2_CONTEXT_LOGGED_OUT);
    }

    private void activateIfNotInPerspective(String currentPerspectiveId,
        String notId) {
        if (!currentPerspectiveId.equals(notId))
            BindingContextHelper.activateContextInWorkbench("not." + notId);
    }

    private void mainWindowResetTitle() {
        mainWindowUpdateTitle(null, null);
    }

    private void mainWindowUpdateTitle(String server, User user) {
        this.server = server;
        if (user == null) {
            this.username = null;
            this.currentCentre = null;
        } else {
            this.username = user.getLogin();
            SiteWrapper site = user.getCurrentWorkingCentre();
            this.currentCentre = site == null ? null : site.getNameShort();
        }
        mainWindowUpdateTitle();
    }

    private void mainWindowUpdateTitle() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        String oldTitle = configurer.getTitle();
        String newTitle = getWindowTitle();

        if ((server != null) && (username != null)) {
            newTitle += " - " + server + " [" + username + "]";
        }
        if (currentCentre != null) {
            newTitle += " - Centre " + currentCentre;
        }

        if (!newTitle.equals(oldTitle)) {
            configurer.setTitle(newTitle);
        }
    }

    private void addBiobankPreferencesPropertyListener() {
        propertyListener = new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                mainWindowUpdateTitle();
            }
        };
        BiobankPlugin.getDefault().getPreferenceStore()
            .addPropertyChangeListener(propertyListener);

    }
}
