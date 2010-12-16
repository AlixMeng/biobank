package edu.ualberta.med.biobank.treeview.dispatch;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

import edu.ualberta.med.biobank.common.wrappers.DispatchWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.treeview.listeners.AdapterChangedEvent;

public abstract class AbstractDispatchGroup extends AdapterBase {

    SiteWrapper site;

    public AbstractDispatchGroup(AdapterBase parent, int id, String name,
        SiteWrapper site) {
        super(parent, id, name, true, true);
        this.site = site;
    }

    @Override
    public void openViewForm() {
        Assert.isTrue(false, "should not be called");
    }

    @Override
    public void executeDoubleClick() {
        performExpand();
    }

    @Override
    protected String getLabelInternal() {
        return null;
    }

    @Override
    public String getTooltipText() {
        return null;
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {

    }

    @Override
    protected int getWrapperChildCount() throws Exception {
        return getWrapperChildren() == null ? 0 : getWrapperChildren().size();
    }

    @Override
    public void notifyListeners(AdapterChangedEvent event) {
        getParent().notifyListeners(event);
    }

    @Override
    public String getViewFormId() {
        return null;
    }

    @Override
    public String getEntryFormId() {
        return null;
    }

    @Override
    public List<AdapterBase> search(Object searchedObject) {
        return searchChildren(searchedObject);
    }

    @Override
    protected AdapterBase createChildNode() {
        return new DispatchAdapter(this, null);
    }

    @Override
    protected AdapterBase createChildNode(ModelWrapper<?> child) {
        Assert.isTrue(child instanceof DispatchWrapper);
        return new DispatchAdapter(this, (DispatchWrapper) child);
    }

}
