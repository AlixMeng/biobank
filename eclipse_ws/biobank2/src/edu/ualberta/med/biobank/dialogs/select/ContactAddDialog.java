package edu.ualberta.med.biobank.dialogs.select;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ContactWrapper;
import edu.ualberta.med.biobank.dialogs.BiobankDialog;
import edu.ualberta.med.biobank.widgets.BiobankText;

public class ContactAddDialog extends BiobankDialog {

    private static final String TITLE = "Contact Information";

    private ContactWrapper contactWrapper;

    public ContactAddDialog(Shell parent) {
        this(parent, new ContactWrapper(SessionManager.getAppService()));
    }

    public ContactAddDialog(Shell parent, ContactWrapper contactWrapper) {
        super(parent);
        Assert.isNotNull(contactWrapper);
        this.contactWrapper = contactWrapper;
    }

    @Override
    protected String getDialogShellTitle() {
        String title = new String();
        if (contactWrapper.getName() == null) {
            title = "Add";
        } else {
            title = "Edit ";
        }
        title += TITLE;
        return title;
    }

    @Override
    protected String getTitleAreaMessage() {
        if (contactWrapper.getName() == null) {
            return "Add a contact person to this clinic";
        }
        return "Edit contact person for this clinic";
    }

    @Override
    protected String getTitleAreaTitle() {
        if (contactWrapper.getName() == null) {
            return "Add Contact";
        }
        return "Edit Contact";
    }

    @Override
    protected void createDialogAreaInternal(Composite parent) {
        Composite contents = new Composite(parent, SWT.NONE);
        contents.setLayout(new GridLayout(2, false));
        contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Control c =
            createBoundWidgetWithLabel(contents, BiobankText.class, SWT.BORDER,
                "Name", new String[0],
                BeansObservables.observeValue(contactWrapper, "name"), null);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 250;
        c.setLayoutData(gd);

        createBoundWidgetWithLabel(contents, BiobankText.class, SWT.BORDER,
            "Title", new String[0],
            BeansObservables.observeValue(contactWrapper, "title"), null);

        createBoundWidgetWithLabel(contents, BiobankText.class, SWT.BORDER,
            "email", new String[0],
            BeansObservables.observeValue(contactWrapper, "emailAddress"), null);

        createBoundWidgetWithLabel(contents, BiobankText.class, SWT.BORDER,
            "Mobile #", new String[0],
            BeansObservables.observeValue(contactWrapper, "mobileNumber"), null);

        createBoundWidgetWithLabel(contents, BiobankText.class, SWT.BORDER,
            "Pager #", new String[0],
            BeansObservables.observeValue(contactWrapper, "pagerNumber"), null);

        createBoundWidgetWithLabel(contents, BiobankText.class, SWT.BORDER,
            "Office #", new String[0],
            BeansObservables.observeValue(contactWrapper, "officeNumber"), null);

        createBoundWidgetWithLabel(contents, BiobankText.class, SWT.BORDER,
            "Fax #", new String[0],
            BeansObservables.observeValue(contactWrapper, "faxNumber"), null);
    }

    public ContactWrapper getContactWrapper() {
        return contactWrapper;
    }

}