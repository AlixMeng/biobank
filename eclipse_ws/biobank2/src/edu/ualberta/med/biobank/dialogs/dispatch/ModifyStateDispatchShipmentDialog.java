package edu.ualberta.med.biobank.dialogs.dispatch;

import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import edu.ualberta.med.biobank.common.util.DispatchAliquotState;
import edu.ualberta.med.biobank.dialogs.BiobankDialog;
import edu.ualberta.med.biobank.validators.NonEmptyStringValidator;
import edu.ualberta.med.biobank.widgets.BiobankText;

public class ModifyStateDispatchShipmentDialog extends BiobankDialog {

    private static final String TITLE_STATE = "Setting {0} state to aliquots in current dispatch";
    private static final String TITLE_COMMENT_ONLY = "Modifying comment of aliquots in current dispatch";
    private String currentTitle;
    private String message;

    private class CommentValue {
        private String value;

        @SuppressWarnings("unused")
        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private CommentValue commentValue = new CommentValue();

    public ModifyStateDispatchShipmentDialog(Shell parentShell,
        DispatchAliquotState newState) {
        super(parentShell);
        if (newState == null) {
            currentTitle = TITLE_COMMENT_ONLY;
            message = "Set a comment";

        } else {
            currentTitle = MessageFormat.format(TITLE_STATE,
                newState.getLabel());
            message = "Set a comment to explain the state modification";
        }
    }

    @Override
    protected String getTitleAreaMessage() {
        return message;
    }

    @Override
    protected String getTitleAreaTitle() {
        return currentTitle;
    }

    @Override
    protected String getDialogShellTitle() {
        return currentTitle;
    }

    @Override
    protected void createDialogAreaInternal(Composite parent) throws Exception {
        Composite contents = new Composite(parent, SWT.NONE);
        contents.setLayout(new GridLayout(2, false));
        contents.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createBoundWidgetWithLabel(contents, BiobankText.class, SWT.MULTI,
            "Comment", null, commentValue, "value",
            new NonEmptyStringValidator("Comment should not be empty"));
    }

    public String getComment() {
        return commentValue.getValue();
    }

}
