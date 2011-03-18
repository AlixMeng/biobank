package edu.ualberta.med.biobank.treeview.dispatch;

import java.util.List;

import org.eclipse.core.runtime.Assert;

import edu.ualberta.med.biobank.common.wrappers.DispatchWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.OriginInfoWrapper;
import edu.ualberta.med.biobank.treeview.AbstractSearchedNode;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.treeview.shipment.ShipmentAdapter;
import edu.ualberta.med.biobank.views.SpecimenTransitView;

public class OriginInfoSearchedNode extends AbstractSearchedNode {

    public OriginInfoSearchedNode(AdapterBase parent, int id) {
        super(parent, id, true);
    }

    @Override
    protected AdapterBase createChildNode(ModelWrapper<?> child) {
        Assert.isTrue(child instanceof DispatchWrapper
            || child instanceof OriginInfoWrapper);
        if (child instanceof OriginInfoWrapper)
            return new ShipmentAdapter(this, (OriginInfoWrapper) child);
        else
            return new DispatchAdapter(this, (DispatchWrapper) child);
    }

    @Override
    protected AdapterBase createChildNode() {
        return null;
    }

    @Override
    protected boolean isParentTo(ModelWrapper<?> parent, ModelWrapper<?> child) {
        if (child instanceof DispatchWrapper) {
            return parent.equals(((DispatchWrapper) child).getSenderCenter());
        }
        return false;
    }

    @Override
    public List<AdapterBase> search(Object searchedObject) {
        return searchChildren(searchedObject);
    }

    @Override
    protected void addNode(ModelWrapper<?> wrapper) {
        SpecimenTransitView.addToNode(this, wrapper);
    }

    @Override
    public void rebuild() {
        performExpand();
    }

}
