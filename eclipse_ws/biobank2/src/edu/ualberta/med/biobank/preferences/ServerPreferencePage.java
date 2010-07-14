package edu.ualberta.med.biobank.preferences;

import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.dialogs.NewServerDialog;

public class ServerPreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage {

    private class BiobankListEditor extends ListEditor {

        /*
         * This class uses code from
         * http://sandipchitale.blogspot.com/2008/09/enhanced
         * -listeditor-implementation.html
         */

        private Button editButton;
        private List commandListControl;

        public BiobankListEditor(String serverList, String title,
            Composite parent) {
            super(serverList, title, parent);
        }

        @Override
        protected String createList(String[] items) {
            String appendedList = "";
            for (int i = 0; i < items.length; i++) {
                appendedList += items[i];
                if (i < items.length - 1)
                    appendedList += "\n";
            }
            return appendedList;
        }

        /**
         * Helper method to create a push button.
         * 
         * @param parent the parent control
         * @param key the resource name used to supply the button's label text
         * @return Button
         */
        private Button createPushButton(Composite parent, String key) {
            Button button = new Button(parent, SWT.PUSH);
            button.setText(key);
            button.setFont(parent.getFont());
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            int widthHint = convertHorizontalDLUsToPixels(button,
                IDialogConstants.BUTTON_WIDTH);
            data.widthHint = Math.max(widthHint,
                button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
            button.setLayoutData(data);
            return button;
        }

        @Override
        public List getListControl(Composite parent) {
            List listControl = super.getListControl(parent);
            if (commandListControl == null) {
                commandListControl = listControl;
                commandListControl.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        editButton.setEnabled(commandListControl
                            .getSelectionCount() == 1);
                    }
                });
            }
            return listControl;
        }

        @Override
        public Composite getButtonBoxControl(Composite parent) {
            Composite buttonBoxControl = super.getButtonBoxControl(parent);
            if (editButton == null) {
                editButton = createPushButton(buttonBoxControl, "Edit"); // TODO
                // I18N
                editButton.setEnabled(false);
                editButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (commandListControl.getSelectionCount() == 1) {
                            String modified = getModifiedEntry(commandListControl
                                .getSelection()[0]);
                            if (modified != null) {
                                int selectedIndex = commandListControl
                                    .getSelectionIndex();
                                commandListControl.remove(selectedIndex);
                                commandListControl.add(modified, selectedIndex);
                            }
                        }
                    }
                });
                buttonBoxControl.addDisposeListener(new DisposeListener() {
                    @Override
                    public void widgetDisposed(DisposeEvent event) {
                        editButton = null;
                    }
                });
            }
            return buttonBoxControl;
        }

        protected String getModifiedEntry(String original) {
            InputDialog entryDialog = new InputDialog(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(), "Edit", "Edit Server:",
                original, null);
            if (entryDialog.open() == InputDialog.OK) {
                return entryDialog.getValue();
            }
            return null;
        }

        @Override
        protected String getNewInputObject() {
            NewServerDialog dlg = new NewServerDialog(getFieldEditorParent()
                .getShell());
            if (dlg.open() == Dialog.OK) {
                return dlg.getText();
            } else
                return null;
        }

        @Override
        protected String[] parseString(String stringList) {
            StringTokenizer st = new StringTokenizer(stringList, "\n");
            String[] items = new String[st.countTokens()];
            int i = 0;
            while (st.hasMoreTokens()) {
                items[i++] = st.nextToken();
            }
            return items;
        }
    }

    public ServerPreferencePage() {
        super(GRID);
        setPreferenceStore(BioBankPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        addField(new BiobankListEditor(PreferenceConstants.SERVER_LIST,
            "Servers:", getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {

    }

}
