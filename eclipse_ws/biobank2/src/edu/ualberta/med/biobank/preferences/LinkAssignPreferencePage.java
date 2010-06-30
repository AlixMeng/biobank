package edu.ualberta.med.biobank.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.ualberta.med.biobank.BioBankPlugin;

public class LinkAssignPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

    public LinkAssignPreferencePage() {
        super(GRID);
        setPreferenceStore(BioBankPlugin.getDefault().getPreferenceStore());
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    @Override
    public void createFieldEditors() {
        addField(new StringFieldEditor(PreferenceConstants.GENERAL_CONFIRM,
            "Confirm barcode:", getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.GENERAL_CANCEL,
            "Cancel barcode:", getFieldEditorParent()));
        addField(new BooleanFieldEditor(
            PreferenceConstants.LINK_ASSIGN_ACTIVITY_LOG_INTO_FILE,
            "Save activity logs into a file", getFieldEditorParent()));
        addField(new DirectoryFieldEditor(
            PreferenceConstants.LINK_ASSIGN_ACTIVITY_LOG_PATH,
            "Path for activity logs files", getFieldEditorParent()));
        addField(new BooleanFieldEditor(
            PreferenceConstants.LINK_ASSIGN_ACTIVITY_LOG_ASK_PRINT,
            "Ask to print activity log", getFieldEditorParent()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
    }
}