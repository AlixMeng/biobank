package edu.ualberta.med.biobank.test.internal;

import java.util.ArrayList;
import java.util.List;

import edu.ualberta.med.biobank.common.wrappers.ActivityStatusWrapper;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;

public class ClinicHelper extends DbHelper {

    private static List<ClinicWrapper> createdClinics = new ArrayList<ClinicWrapper>();

    public static ClinicWrapper newClinic(String name) throws Exception {
        ClinicWrapper clinic = new ClinicWrapper(appService);
        clinic.setName(name);
        if (name != null) {
            if (name.length() <= 50) {
                clinic.setNameShort(name);
            } else {
                clinic.setNameShort(name.substring(name.length() - 49));
            }
        }
        clinic.setCity("");
        clinic.setSendsShipments(false);
        clinic.setActivityStatus(ActivityStatusWrapper.getActivityStatus(
            appService, ActivityStatusWrapper.ACTIVE_STATUS_STRING));

        return clinic;
    }

    public static ClinicWrapper addClinic(String name, boolean addContacts,
        boolean addToCreatedList) throws Exception {
        ClinicWrapper clinic = newClinic(name);
        clinic.persist();
        if (addContacts) {
            ContactHelper.addContactsToClinic(clinic, name);
        }
        if (addToCreatedList) {
            createdClinics.add(clinic);
        }
        return clinic;
    }

    public static ClinicWrapper addClinic(String name, boolean addContacts)
        throws Exception {
        return addClinic(name, addContacts, true);
    }

    public static ClinicWrapper addClinic(String name) throws Exception {
        return addClinic(name, false);
    }

    public static ClinicWrapper addClinicWithShipments(String name)
        throws Exception {
        ClinicWrapper clinic = addClinic(name, false, true);
        clinic.setSendsShipments(true);
        clinic.persist();
        return clinic;
    }

    public static void addClinics(String name, int count, boolean addContacts)
        throws Exception {
        for (int i = 0; i < count; i++) {
            addClinic(name + i, addContacts);
        }
    }

    public static void addClinics(String name, int count) throws Exception {
        addClinics(name, count, false);
    }

    public static void deleteCreatedClinics() throws Exception {
        deleteClinics(createdClinics);
        createdClinics.clear();
    }

}
