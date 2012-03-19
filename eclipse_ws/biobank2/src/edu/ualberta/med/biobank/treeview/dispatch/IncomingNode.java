package edu.ualberta.med.biobank.treeview.dispatch;

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

import edu.ualberta.med.biobank.common.wrappers.CenterWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.treeview.AbstractAdapterBase;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.treeview.request.ReceivingRequestGroup;

public class IncomingNode extends AdapterBase {

    private ReceivingInTransitDispatchGroup receivedTransitNode;
    private ReceivingNoErrorsDispatchGroup receivingNode;
    private ReceivingWithErrorsDispatchGroup receivingWithErrorsNode;
    private ReceivingRequestGroup requestNode;

    public IncomingNode(AdapterBase parent, int id, CenterWrapper<?> center) {
        super(parent, id, Messages.IncomingNode_incoming_node_label, true);
        receivedTransitNode = new ReceivingInTransitDispatchGroup(this, 0);
        receivedTransitNode.setParent(this);
        addChild(receivedTransitNode);

        receivingNode = new ReceivingNoErrorsDispatchGroup(this, 1);
        receivingNode.setParent(this);
        addChild(receivingNode);

        receivingWithErrorsNode = new ReceivingWithErrorsDispatchGroup(this, 2);
        receivingWithErrorsNode.setParent(this);
        addChild(receivingWithErrorsNode);

        requestNode = new ReceivingRequestGroup(this, 3);
        requestNode.setParent(this);
        addChild(requestNode);

    }

    @Override
    protected String getLabelInternal() {
        return null;
    }

    @Override
    public String getTooltipTextInternal() {
        return null;
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        //
    }

    @Override
    protected AdapterBase createChildNode() {
        return null;
    }

    @Override
    protected AdapterBase createChildNode(Object child) {
        return null;
    }

    @Override
    protected List<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        return null;
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
    public void rebuild() {
        for (AbstractAdapterBase adaper : getChildren()) {
            adaper.rebuild();
        }
    }

    @Override
    public List<AbstractAdapterBase> search(Class<?> searchedClass,
        Integer objectId) {
        return searchChildren(searchedClass, objectId);
    }

    @Override
    public int compareTo(AbstractAdapterBase o) {
        return 0;
    }

}
