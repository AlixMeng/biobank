package edu.ualberta.med.biobank.expressions;

import org.eclipse.core.expressions.PropertyTester;

import edu.ualberta.med.biobank.treeview.AdapterBase;

public class AdapterBasePropertyTester extends PropertyTester {

    public AdapterBasePropertyTester() {
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args,
        Object expectedValue) {
        if (receiver instanceof AdapterBase) {
            AdapterBase adapter = (AdapterBase) receiver;
            if (property.equals("canDelete")) {
                return adapter.isDeletable();
            }
        }
        return false;
    }
}
