package edu.ualberta.med.biobank.widgets.infotables.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.dialogs.SelectStudyDialog;
import edu.ualberta.med.biobank.widgets.infotables.IInfoTableAddItemListener;
import edu.ualberta.med.biobank.widgets.infotables.IInfoTableDeleteItemListener;
import edu.ualberta.med.biobank.widgets.infotables.InfoTableEvent;
import edu.ualberta.med.biobank.widgets.infotables.StudyInfoTable;

/**
 * Allows the user to select a clinic and a contact from a clinic. Note that
 * some clinics may have more than one contact.
 */
public class StudyAddInfoTable extends StudyInfoTable {

    private SiteWrapper site;

    public StudyAddInfoTable(Composite parent, SiteWrapper site) {
        super(parent, site.getStudyCollection(true));
        this.site = site;
        addDeleteSupport();
    }

    @Override
    protected boolean isEditMode() {
        return true;
    }

    public void createStudyDlg() {
        SelectStudyDialog dlg;
        try {
            List<StudyWrapper> availableStudies = StudyWrapper
                .getAllStudies(SessionManager.getAppService());
            availableStudies.removeAll(site.getStudyCollection(true));
            dlg = new SelectStudyDialog(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(), availableStudies);
            if (dlg.open() == Dialog.OK) {
                notifyListeners();
                StudyWrapper study = dlg.getSelection();
                if (study != null) {
                    List<StudyWrapper> dummyList = new ArrayList<StudyWrapper>();
                    dummyList.add(study);
                    site.addStudies(dummyList);
                    setCollection(site.getStudyCollection(true));
                }
            }
        } catch (Exception e) {
            BioBankPlugin.openAsyncError(
                "Unable to retrieve available contacts", e);
        }
    }

    private void addDeleteSupport() {
        addAddItemListener(new IInfoTableAddItemListener() {
            @Override
            public void addItem(InfoTableEvent event) {
                createStudyDlg();
            }
        });

        addDeleteItemListener(new IInfoTableDeleteItemListener() {
            @Override
            public void deleteItem(InfoTableEvent event) {
                StudyWrapper study = getSelection();
                if (study == null)
                    return;

                if (!BioBankPlugin.openConfirm(
                    "Remove Study",
                    "Are you sure you want to remove study \""
                        + study.getName() + "\"")) {
                    return;
                }

                try {
                    site.removeStudies(Arrays.asList(study));
                    setCollection(site.getStudyCollection(true));
                    notifyListeners();
                } catch (BiobankCheckException e) {
                    BioBankPlugin.openAsyncError("Delete failed", e);
                }
            }
        });
    }

    public void setStudies(List<StudyWrapper> studies) {
        setCollection(studies);
    }

    public void reload() {
        setCollection(site.getStudyCollection(true));
    }

}
