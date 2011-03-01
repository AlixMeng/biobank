package edu.ualberta.med.biobank.test.internal;

import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.CollectionEventWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.DispatchWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.Assert;

public class DbHelper {

    protected static WritableApplicationService appService;

    protected static Random r = new Random();

    public static void setAppService(WritableApplicationService appService) {
        Assert.assertNotNull("appService is null", appService);
        DbHelper.appService = appService;
    }

    public static <T> T chooseRandomlyInList(List<T> list) {
        if (list.size() == 1) {
            return list.get(0);
        }
        if (list.size() > 1) {
            int pos = r.nextInt(list.size());
            return list.get(pos);
        }
        return null;
    }

    public static void deleteContainers(Collection<ContainerWrapper> containers)
        throws Exception {
        Assert.assertNotNull("appService is null", appService);
        if ((containers == null) || (containers.size() == 0))
            return;

        for (ContainerWrapper container : containers) {
            container.reload();
            if (container.hasChildren()) {
                deleteContainers(container.getChildren().values());
            }
            if (container.hasSpecimens()) {
                deleteFromList(container.getSpecimens().values());
            }
            container.reload();
            container.delete();
        }
    }

    public static void deleteDispatchs(Collection<DispatchWrapper> shipments)
        throws Exception {
        Assert.assertNotNull("appService is null", appService);
        if ((shipments == null) || (shipments.size() == 0))
            return;

        for (DispatchWrapper shipment : shipments) {
            shipment.delete();
        }
    }

    public static void deleteCollectionEvents(
        Collection<CollectionEventWrapper> cevents) throws Exception {
        Assert.assertNotNull("appService is null", appService);
        if ((cevents == null) || (cevents.size() == 0))
            return;

        for (CollectionEventWrapper cevent : cevents) {
            if (!cevent.isNew()) {
                cevent.delete();
            }
        }
    }

    public static void deletePatients(List<PatientWrapper> patients)
        throws Exception {
        Assert.assertNotNull("appService is null", appService);
        if (patients == null)
            return;

        // visites liees au ship avec patient de la visit non lie au shipment
        for (PatientWrapper patient : patients) {
            patient.reload();
            deleteCollectionEvents(patient.getCollectionEventCollection(false));
            patient.reload();
            patient.delete();
        }
    }

    public static void deleteCollectionEvents(
        List<CollectionEventWrapper> cevents) throws Exception {

        for (CollectionEventWrapper ce : cevents) {
            deleteFromList(ce.getSpecimenCollection(false));
            ce.reload();
            ce.delete();
        }
    }

    public static void deleteClinics(List<ClinicWrapper> clinics)
        throws Exception {
        Assert.assertNotNull("appService is null", appService);
        for (ClinicWrapper clinic : clinics) {
            clinic.reload();
            deleteFromList(clinic.getOriginInfoCollection(false));
            clinic.delete();
        }
    }

    public static void deleteFromList(Collection<? extends ModelWrapper<?>> list)
        throws Exception {
        if (list == null)
            return;

        for (ModelWrapper<?> object : list) {
            object.reload();
            object.delete();
        }
    }

}
