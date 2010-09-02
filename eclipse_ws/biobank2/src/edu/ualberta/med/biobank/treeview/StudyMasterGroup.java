package edu.ualberta.med.biobank.treeview;

import java.util.Collection;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;

public class StudyMasterGroup extends AbstractStudyGroup {

    public StudyMasterGroup(SessionAdapter parent, int id) {
        super(parent, id, "Studies Master");
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        if (SessionManager.canCreate(StudyWrapper.class)) {
            MenuItem mi = new MenuItem(menu, SWT.PUSH);
            mi.setText("Add Study");
            mi.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    addStudy(SessionManager.getInstance().getSession(), false);
                }
            });
        }
    }

    @Override
    protected Collection<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        return StudyWrapper.getAllStudies(getAppService());
    }

    public static void addStudy(SessionAdapter sessionAdapter,
        boolean hasPreviousForm) {
        StudyWrapper study = new StudyWrapper(sessionAdapter.getAppService());
        StudyAdapter adapter = new StudyAdapter(
            sessionAdapter.getStudiesGroupNode(), study);
        adapter.openEntryForm(hasPreviousForm);
    }

}
