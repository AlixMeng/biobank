package edu.ualberta.med.biobank.test.internal;

import java.util.ArrayList;
import java.util.List;

import edu.ualberta.med.biobank.common.wrappers.ShippingMethodWrapper;

public class ShippingMethodHelper extends DbHelper {

    public static List<ShippingMethodWrapper> createdCompanies = new ArrayList<ShippingMethodWrapper>();

    public static ShippingMethodWrapper newShippingCompany(String name) {
        ShippingMethodWrapper company = new ShippingMethodWrapper(appService);
        company.setName(name);
        return company;
    }

    public static ShippingMethodWrapper addShippingMethod(String name,
        boolean addToCreatedList) throws Exception {
        ShippingMethodWrapper company = newShippingCompany(name);
        company.persist();
        if (addToCreatedList) {
            createdCompanies.add(company);
        }
        return company;
    }

    public static ShippingMethodWrapper addShippingMethod(String name)
        throws Exception {
        return addShippingMethod(name, true);
    }

    public static void deleteCreateShippingMethods() throws Exception {
        for (ShippingMethodWrapper company : createdCompanies) {
            company.reload();
            company.delete();
        }
        createdCompanies.clear();
    }

}
