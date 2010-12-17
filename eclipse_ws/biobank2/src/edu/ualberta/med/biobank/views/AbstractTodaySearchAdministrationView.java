package edu.ualberta.med.biobank.views;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.treeview.AbstractSearchedNode;
import edu.ualberta.med.biobank.treeview.AbstractTodayNode;
import edu.ualberta.med.biobank.treeview.AdapterBase;

public abstract class AbstractTodaySearchAdministrationView extends
    AbstractAdministrationView {

    protected AbstractTodayNode todayNode;

    protected AbstractSearchedNode searchedNode;

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        todayNode = getTodayNode();
        todayNode.setParent(rootNode);
        rootNode.addChild(todayNode);

        searchedNode = getSearchedNode();
        searchedNode.setParent(rootNode);
        rootNode.addChild(searchedNode);
    }

    protected abstract AbstractTodayNode getTodayNode();

    protected abstract AbstractSearchedNode getSearchedNode();

    public abstract AdapterBase addToNode(AdapterBase parentNode,
        ModelWrapper<?> wrapper);

    @Override
    protected void internalSearch() {
        String text = treeText.getText();
        try {
            List<? extends ModelWrapper<?>> searchedObject = search(text);
            if (searchedObject == null || searchedObject.size() == 0) {
                notFound(text);
            } else {
                showSearchedObjectsInTree(searchedObject, true);
                getTreeViewer().expandToLevel(searchedNode, 3);
            }
        } catch (Exception e) {
            BioBankPlugin.openAsyncError("Search error", e);
        }
    }

    protected abstract List<? extends ModelWrapper<?>> search(String text)
        throws Exception;

    protected abstract void notFound(String text);

    protected void showSearchedObjectsInTree(
        List<? extends ModelWrapper<?>> searchedObjects, boolean doubleClick) {
        for (ModelWrapper<?> searchedObject : searchedObjects) {
            List<AdapterBase> nodeRes = todayNode.search(searchedObject);
            if (nodeRes.size() == 0) {
                nodeRes = searchedNode.search(searchedObject);
                if (nodeRes.size() == 0) {
                    searchedNode.addSearchObject(searchedObject);
                    searchedNode.performExpand();
                    nodeRes = searchedNode.search(searchedObject);
                }
            }
            if (nodeRes.size() > 0) {
                setSelectedNode(nodeRes.get(0));
                if (doubleClick) {
                    nodeRes.get(0).performDoubleClick();
                }
            }
        }
    }

    @Override
    public void reload() {
        todayNode.performExpand();
        searchedNode.performExpand();
        super.reload();
    }

}
