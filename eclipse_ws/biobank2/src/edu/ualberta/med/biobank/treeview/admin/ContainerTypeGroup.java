package edu.ualberta.med.biobank.treeview.admin;

import java.util.ArrayList;
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

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.treeview.listeners.AdapterChangedEvent;

public class ContainerTypeGroup extends AdapterBase {

    public ContainerTypeGroup(SiteAdapter parent, int id) {
        super(parent, id, "Container Types", true, true);
    }

    @Override
    protected String getLabelInternal() {
        return null;
    }

    @Override
    public void executeDoubleClick() {
        performExpand();
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        if (SessionManager.canCreate(ContainerTypeWrapper.class,
            getParentFromClass(SiteAdapter.class).getWrapper())) {
            MenuItem mi = new MenuItem(menu, SWT.PUSH);
            mi.setText("Add Container Type");
            mi.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    addContainerType(ContainerTypeGroup.this
                        .getParentFromClass(SiteAdapter.class), false);
                }
            });
        }
    }

    @Override
    public String getTooltipText() {
        return null;
    }

    @Override
    public List<AdapterBase> search(Object searchedObject) {
        return findChildFromClass(searchedObject, ContainerTypeWrapper.class);
    }

    @Override
    protected AdapterBase createChildNode() {
        return new ContainerTypeAdapter(this, null);
    }

    @Override
    protected AdapterBase createChildNode(ModelWrapper<?> child) {
        Assert.isTrue(child instanceof ContainerTypeWrapper);
        return new ContainerTypeAdapter(this, (ContainerTypeWrapper) child);
    }

    @Override
    protected Collection<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        SiteWrapper currentSite = ((SiteAdapter) getParent()).getWrapper();
        Assert.isNotNull(currentSite, "null site");
        currentSite.reload();
        return new ArrayList<ContainerTypeWrapper>(
            currentSite.getContainerTypeCollection());
    }

    @Override
    protected int getWrapperChildCount() throws Exception {
        return getWrapperChildren().size();
    }

    @Override
    public void notifyListeners(AdapterChangedEvent event) {
        getParent().notifyListeners(event);
    }

    public void addContainerType(SiteAdapter siteAdapter,
        boolean hasPreviousForm) {
        ContainerTypeWrapper ct = new ContainerTypeWrapper(
            siteAdapter.getAppService());
        ct.setSite(siteAdapter.getWrapper());
        ContainerTypeAdapter adapter = new ContainerTypeAdapter(
            siteAdapter.getContainerTypesGroupNode(), ct);
        adapter.openEntryForm(hasPreviousForm);
    }

    @Override
    public String getEntryFormId() {
        return null;
    }

    @Override
    public String getViewFormId() {
        return null;
    }

}
