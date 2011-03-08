package edu.ualberta.med.biobank.forms;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;

import edu.ualberta.med.biobank.Messages;
import edu.ualberta.med.biobank.common.peer.SitePeer;
import edu.ualberta.med.biobank.common.wrappers.ActivityStatusWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContactWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.logs.BiobankLogger;
import edu.ualberta.med.biobank.treeview.admin.SiteAdapter;
import edu.ualberta.med.biobank.validators.NonEmptyStringValidator;
import edu.ualberta.med.biobank.widgets.BiobankText;
import edu.ualberta.med.biobank.widgets.infotables.entry.StudyAddInfoTable;
import edu.ualberta.med.biobank.widgets.listeners.BiobankEntryFormWidgetListener;
import edu.ualberta.med.biobank.widgets.listeners.MultiSelectEvent;
import edu.ualberta.med.biobank.widgets.utils.ComboSelectionUpdate;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class SiteEntryForm extends AddressEntryFormCommon {

    private static BiobankLogger logger = BiobankLogger
        .getLogger(SiteEntryForm.class.getName());

    public static final String ID = "edu.ualberta.med.biobank.forms.SiteEntryForm";

    private static final String MSG_NEW_SITE_OK = Messages
        .getString("SiteEntryForm.creation.msg");
    private static final String MSG_SITE_OK = Messages
        .getString("SiteEntryForm.edition.msg");

    private SiteAdapter siteAdapter;

    private SiteWrapper site;

    protected Combo session;

    private ComboViewer activityStatusComboViewer;

    private StudyAddInfoTable studiesTable;

    private BiobankEntryFormWidgetListener listener = new BiobankEntryFormWidgetListener() {
        @Override
        public void selectionChanged(MultiSelectEvent event) {
            setDirty(true);
        }
    };

    @Override
    public void init() throws Exception {
        Assert.isTrue((adapter instanceof SiteAdapter),
            "Invalid editor input: object of type "
                + adapter.getClass().getName());

        siteAdapter = (SiteAdapter) adapter;
        site = siteAdapter.getWrapper();
        try {
            site.reload();
        } catch (Exception e) {
            logger.error("Can't reload site", e);
        }

        String tabName;
        if (site.isNew()) {
            tabName = Messages.getString("SiteEntryForm.title.new");
            site.setActivityStatus(ActivityStatusWrapper
                .getActiveActivityStatus(appService));
        } else {
            tabName = Messages.getString("SiteEntryForm.title.edit",
                site.getNameShort());
        }
        setPartName(tabName);
    }

    @Override
    protected void createFormContent() throws ApplicationException {
        form.setText(Messages.getString("SiteEntryForm.main.title"));
        page.setLayout(new GridLayout(1, false));
        createSiteSection();
        createAddressArea(site);
        createStudySection();

        // When adding help uncomment line below
        // PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
        // IJavaHelpContextIds.XXXXX);
    }

    private void createSiteSection() throws ApplicationException {
        toolkit.createLabel(page,
            Messages.getString("SiteEntryForm.main.description"), SWT.LEFT);

        Composite client = toolkit.createComposite(page);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        setFirstControl(createBoundWidgetWithLabel(
            client,
            BiobankText.class,
            SWT.NONE,
            Messages.getString("label.name"),
            null,
            site,
            SitePeer.NAME.getName(),
            new NonEmptyStringValidator(Messages
                .getString("SiteEntryForm.field.name.validation.msg"))));

        createBoundWidgetWithLabel(
            client,
            BiobankText.class,
            SWT.NONE,
            Messages.getString("label.nameShort"),
            null,
            site,
            SitePeer.NAME_SHORT.getName(),
            new NonEmptyStringValidator(Messages
                .getString("SiteEntryForm.field.nameShort.validation.msg")));

        activityStatusComboViewer = createComboViewer(client,
            Messages.getString("label.activity"),
            ActivityStatusWrapper.getAllActivityStatuses(appService),
            site.getActivityStatus(),
            Messages.getString("SiteEntryForm.field.activity.validation.msg"),
            new ComboSelectionUpdate() {
                @Override
                public void doSelection(Object selectedObject) {
                    site.setActivityStatus((ActivityStatusWrapper) selectedObject);
                }
            });

        createBoundWidgetWithLabel(client, BiobankText.class, SWT.MULTI,
            Messages.getString("label.comments"), null, site,
            SitePeer.COMMENT.getName(), null);
    }

    private void createStudySection() {
        Section section = createSection(Messages
            .getString("SiteEntryForm.studies.title"));
        addSectionToolbar(section,
            Messages.getString("SiteEntryForm.studies.add"),
            new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    studiesTable.createStudyDlg();
                }
            }, ContactWrapper.class);
        studiesTable = new StudyAddInfoTable(section, site);
        studiesTable.adaptToToolkit(toolkit, true);
        studiesTable.addClickListener(collectionDoubleClickListener);
        studiesTable.addSelectionChangedListener(listener);
        section.setClient(studiesTable);
    }

    @Override
    protected String getOkMessage() {
        if (site.getId() == null) {
            return MSG_NEW_SITE_OK;
        }
        return MSG_SITE_OK;
    }

    @Override
    protected void saveForm() throws Exception {
        site.persist();
    }

    @Override
    public String getNextOpenedFormID() {
        return SiteViewForm.ID;
    }

    @Override
    public void reset() throws Exception {
        ActivityStatusWrapper currentActivityStatus = site.getActivityStatus();
        if (currentActivityStatus != null) {
            activityStatusComboViewer.setSelection(new StructuredSelection(
                currentActivityStatus));
        } else if (activityStatusComboViewer.getCombo().getItemCount() > 1) {
            activityStatusComboViewer.getCombo().deselectAll();
        }
        studiesTable.reload();
        super.reset();
    }
}
