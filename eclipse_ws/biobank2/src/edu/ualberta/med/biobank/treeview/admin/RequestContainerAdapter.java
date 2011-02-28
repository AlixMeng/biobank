package edu.ualberta.med.biobank.treeview.admin;

import java.util.ArrayList;
import java.util.List;

import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.treeview.Node;

public class RequestContainerAdapter implements Node {

    public Object parent;
    public ContainerWrapper container;
    List<Object> children;

    public RequestContainerAdapter(Object parent, ContainerWrapper container) {
        this.parent = parent;
        this.container = container;
        this.children = new ArrayList<Object>();
    }

    @Override
    public Object getParent() {
        return parent;
    }

    public boolean hasChildren() {
        return getChildren().size() != 0;
    }

    @Override
    public List<Object> getChildren() {
        return children;
    }

    public String getLabelInternal() {
        return container.getLabel() + " ("
            + container.getContainerType().getNameShort() + ")" + " ("
            + getSpecimenCount() + ")";
    }

    private Integer getSpecimenCount() {
        Integer aliquots = 0;
        for (Object child : getChildren()) {
            if (child instanceof RequestContainerAdapter)
                aliquots += ((RequestContainerAdapter) child).getSpecimenCount();
            else
                aliquots++;
        }
        return aliquots;
    }

    public void addChild(Object c) {
        children.add(c);
    }
}
