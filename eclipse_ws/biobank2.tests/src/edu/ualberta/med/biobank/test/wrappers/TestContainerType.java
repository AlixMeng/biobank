package edu.ualberta.med.biobank.test.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.ActivityStatusWrapper;
import edu.ualberta.med.biobank.common.wrappers.AliquotWrapper;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContactWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientVisitWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import edu.ualberta.med.biobank.common.wrappers.SampleTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ShipmentWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.model.ContainerType;
import edu.ualberta.med.biobank.test.TestDatabase;
import edu.ualberta.med.biobank.test.Utils;
import edu.ualberta.med.biobank.test.internal.AliquotHelper;
import edu.ualberta.med.biobank.test.internal.ClinicHelper;
import edu.ualberta.med.biobank.test.internal.ContactHelper;
import edu.ualberta.med.biobank.test.internal.ContainerHelper;
import edu.ualberta.med.biobank.test.internal.ContainerTypeHelper;
import edu.ualberta.med.biobank.test.internal.PatientHelper;
import edu.ualberta.med.biobank.test.internal.PatientVisitHelper;
import edu.ualberta.med.biobank.test.internal.ShipmentHelper;
import edu.ualberta.med.biobank.test.internal.SiteHelper;
import edu.ualberta.med.biobank.test.internal.StudyHelper;

public class TestContainerType extends TestDatabase {
    private static final int CONTAINER_TOP_ROWS = 5;

    private static final int CONTAINER_TOP_COLS = 9;

    private static final int CONTAINER_CHILD_L3_ROWS = 8;

    private static final int CONTAINER_CHILD_L3_COLS = 12;

    private Map<String, ContainerTypeWrapper> containerTypeMap;

    private SiteWrapper site;

    // the methods to skip in the getters and setters test
    private static final List<String> GETTER_SKIP_METHODS = Arrays.asList(
        "getChildLabelingScheme", "getChildLabelingSchemeName");

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        containerTypeMap = new HashMap<String, ContainerTypeWrapper>();
        site = SiteHelper.addSite("Site - Container Test"
            + Utils.getRandomString(10));
        addTopContainerType(site);
    }

    private ContainerTypeWrapper addTopContainerType(SiteWrapper site)
        throws Exception {
        ContainerTypeWrapper topType = ContainerTypeHelper.addContainerType(
            site, "Top Container Type", "TCT", 2, CONTAINER_TOP_ROWS,
            CONTAINER_TOP_COLS, true);
        containerTypeMap.put("TopCT", topType);
        return topType;
    }

    private ContainerTypeWrapper addContainerTypeHierarchy(
        ContainerTypeWrapper topType, int level) throws Exception {
        ContainerTypeWrapper childType;

        if (level >= 3) {
            childType = ContainerTypeHelper.addContainerType(site,
                "Child L3 Container Type", "CCTL3", 1, CONTAINER_CHILD_L3_ROWS,
                CONTAINER_CHILD_L3_COLS, false);
            containerTypeMap.put("ChildCtL3", childType);
        }

        if (level >= 2) {
            childType = ContainerTypeHelper.newContainerType(site,
                "Child L2 Container Type", "CCTL2", 3, 1, 10, false);
            if (containerTypeMap.get("ChildCtL3") != null) {
                childType.addChildContainerTypes(Arrays.asList(containerTypeMap
                    .get("ChildCtL3")));
            }
            childType.persist();
            containerTypeMap.put("ChildCtL2", childType);
        }

        if (level >= 1) {
            childType = ContainerTypeHelper.newContainerType(site,
                "Child L1 Container Type", "CCTL1", 3, 1, 10, false);
            if (containerTypeMap.get("ChildCtL2") != null) {
                childType.addChildContainerTypes(Arrays.asList(containerTypeMap
                    .get("ChildCtL2")));
            }
            childType.persist();
            containerTypeMap.put("ChildCtL1", childType);

            if (containerTypeMap.get("ChildCtL1") != null) {
                topType.addChildContainerTypes(Arrays.asList(containerTypeMap
                    .get("ChildCtL1")));
            }
            topType.persist();
            topType.reload();
        }
        return topType;
    }

    private ContainerTypeWrapper addContainerTypeHierarchy(
        ContainerTypeWrapper topType) throws Exception {
        return addContainerTypeHierarchy(topType, 3);
    }

    @Test
    public void testGettersAndSetters() throws BiobankCheckException, Exception {
        ContainerTypeWrapper topType = containerTypeMap.get("TopCT");
        testGettersAndSetters(topType, GETTER_SKIP_METHODS);
    }

    @Test
    public void testCompareTo() throws Exception {
        ContainerTypeWrapper topType, childTypeL1, childTypeL2, childTypeL3;

        topType = addContainerTypeHierarchy(containerTypeMap.get("TopCT"));
        childTypeL1 = containerTypeMap.get("ChildCtL1");
        childTypeL2 = containerTypeMap.get("ChildCtL2");
        childTypeL3 = containerTypeMap.get("ChildCtL3");

        Assert.assertEquals(1, topType.compareTo(childTypeL1));
        Assert.assertEquals(-1, childTypeL1.compareTo(childTypeL2));
        Assert.assertEquals(-1, childTypeL2.compareTo(childTypeL3));
        Assert.assertEquals(0, topType.compareTo(topType));
    }

    @Test
    public void testReset() throws Exception {
        ContainerTypeWrapper topType = containerTypeMap.get("TopCT");
        topType.reset();
    }

    @Test
    public void testReload() throws Exception {
        ContainerTypeWrapper topType = containerTypeMap.get("TopCT");
        topType.reload();
    }

    @Test
    public void testGetWrappedClass() {
        ContainerTypeWrapper topType = containerTypeMap.get("TopCT");
        Assert.assertEquals(ContainerType.class, topType.getWrappedClass());
    }

    @Test
    public void testSite() throws Exception {
        ContainerTypeWrapper topType2;

        // use same name as containerTypeMap.get("TopCT")
        topType2 = ContainerTypeHelper.newContainerType(null,
            "Top Container Type 2", "TCT 2", 3, CONTAINER_TOP_ROWS + 1,
            CONTAINER_TOP_COLS - 1, true);

        try {
            topType2.persist();
            Assert
                .fail("should not be allowed to add container type because of duplicate name");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNameUnique() throws Exception {
        ContainerTypeWrapper topType2;

        // use same name as containerTypeMap.get("TopCT")
        topType2 = ContainerTypeHelper.newContainerType(site,
            "Top Container Type", "TCT", 3, CONTAINER_TOP_ROWS + 1,
            CONTAINER_TOP_COLS - 1, true);

        try {
            topType2.persist();
            Assert
                .fail("should not be allowed to add container type because of duplicate name");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCapacity() throws Exception {
        ContainerTypeWrapper topType2;

        topType2 = ContainerTypeHelper.newContainerType(site,
            "Top Container Type 2", "TCT2", 3, null, 1, true);

        try {
            topType2.persist();
            Assert
                .fail("should not be allowed to add container with null rows");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        }

        topType2 = ContainerTypeHelper.newContainerType(site,
            "Top Container Type 2", "TCT2", 3, 1, null, true);

        try {
            topType2.persist();
            Assert
                .fail("should not be allowed to add container with null columns");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        }

        topType2 = ContainerTypeHelper.newContainerType(site,
            "Top Container Type 2", "TCT2", 3, null, null, true);

        try {
            topType2.persist();
            Assert
                .fail("should not be allowed to add container with null capacity");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testChangeLabelingScheme() throws Exception {
        ContainerTypeWrapper topType, topType2;

        // test null labeling scheme
        topType2 = ContainerTypeHelper.newContainerType(site,
            "Top Container Type 2", "TCT2", null, null, null, true);

        try {
            topType2.persist();
            Assert
                .fail("should not be allowed to add container with null capacity");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        }

        // test changing labeling scheme
        topType = addContainerTypeHierarchy(containerTypeMap.get("TopCT"));
        ContainerHelper.addContainer(null, TestCommon.getNewBarcode(r), null,
            site, topType);
        topType.setChildLabelingScheme(3);

        try {
            topType.persist();
            Assert.fail("should not be allowed to change labeling scheme");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testChangeTopLevel() throws Exception {
        ContainerTypeWrapper topType;

        topType = addContainerTypeHierarchy(containerTypeMap.get("TopCT"));
        ContainerHelper.addContainer(null, TestCommon.getNewBarcode(r), null,
            site, topType);
        topType.setTopLevel(false);

        try {
            topType.persist();
            Assert.fail("should not be allowed to change top level setting");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testChangeCapacity() throws Exception {
        ContainerTypeWrapper topType;

        topType = addContainerTypeHierarchy(containerTypeMap.get("TopCT"));
        ContainerHelper.addContainer(null, TestCommon.getNewBarcode(r), null,
            site, topType);
        topType.setRowCapacity(1);

        try {
            topType.persist();
            Assert.fail("should not be allowed to change capacity");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        }

        topType.setColCapacity(1);

        try {
            topType.persist();
            Assert.fail("should not be allowed to change capacity");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testDelete() throws Exception {
        ContainerTypeWrapper topType = addContainerTypeHierarchy(containerTypeMap
            .get("TopCT"));
        ContainerHelper.addContainer(null, TestCommon.getNewBarcode(r), null,
            site, topType);
        try {
            topType.delete();
            Assert.fail("cannot delete, one container is using this type");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGetAllChildren() throws Exception {
        ContainerTypeWrapper topType, childTypeL1, childTypeL2, childTypeL3;

        topType = addContainerTypeHierarchy(containerTypeMap.get("TopCT"));
        childTypeL1 = containerTypeMap.get("ChildCtL1");
        childTypeL2 = containerTypeMap.get("ChildCtL2");
        childTypeL3 = containerTypeMap.get("ChildCtL3");

        Collection<ContainerTypeWrapper> children = topType
            .getChildrenRecursively();
        Assert.assertEquals(3, children.size());
        Assert.assertTrue(children.contains(childTypeL1));
        Assert.assertTrue(children.contains(childTypeL2));
        Assert.assertTrue(children.contains(childTypeL3));
        Assert.assertFalse(children.contains(topType));

        children = childTypeL1.getChildrenRecursively();
        Assert.assertEquals(2, children.size());
        Assert.assertTrue(children.contains(childTypeL2));
        Assert.assertTrue(children.contains(childTypeL3));
        Assert.assertFalse(children.contains(topType));
        Assert.assertFalse(children.contains(childTypeL1));

        children = childTypeL2.getChildrenRecursively();
        Assert.assertEquals(1, children.size());
        Assert.assertTrue(children.contains(childTypeL3));
        Assert.assertFalse(children.contains(topType));
        Assert.assertFalse(children.contains(childTypeL1));
        Assert.assertFalse(children.contains(childTypeL2));
    }

    @Test
    public void testIsUsedByContainers() throws Exception {
        addContainerTypeHierarchy(containerTypeMap.get("TopCT"));

        String[] keys = new String[] { "TopCT", "ChildCtL1", "ChildCtL2",
            "ChildCtL3" };

        List<ContainerWrapper> containers = new ArrayList<ContainerWrapper>();
        for (String key : keys) {
            ContainerTypeWrapper ct = containerTypeMap.get(key);
            Assert.assertFalse(ct.isUsedByContainers());

            if (key.equals("TopCT")) {
                ContainerWrapper top = ContainerHelper.addContainer("01",
                    TestCommon.getNewBarcode(r), null, site, ct);
                containers.add(top);
            } else {
                containers.add(ContainerHelper.addContainer(null, TestCommon
                    .getNewBarcode(r), containers.get(containers.size() - 1),
                    site, ct, 0, 0));
            }

            ct.reload();
            Assert.assertTrue(ct.isUsedByContainers());

        }

        // now delete all containers
        containers.get(3).delete();
        containers.get(2).delete();
        containers.get(1).delete();
        containers.get(0).delete();
        containers.clear();

        for (String key : keys) {
            ContainerTypeWrapper ct = containerTypeMap.get(key);
            ct.reload();
            Assert.assertFalse(ct.isUsedByContainers());
        }
    }

    @Test
    public void testGetParentContainerTypes() throws Exception {
        ContainerTypeWrapper topType, childTypeL1, childTypeL2, childTypeL3, childTypeL2_2, childTypeL2_3;

        topType = addContainerTypeHierarchy(containerTypeMap.get("TopCT"));
        childTypeL1 = containerTypeMap.get("ChildCtL1");
        childTypeL2 = containerTypeMap.get("ChildCtL2");
        childTypeL3 = containerTypeMap.get("ChildCtL3");

        // each childTypeL1, childTypeL2, and childTypeL3 should have single
        // parent
        List<ContainerTypeWrapper> list = childTypeL1.getParentContainerTypes();
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains(topType));

        list = childTypeL2.getParentContainerTypes();
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains(childTypeL1));

        list = childTypeL3.getParentContainerTypes();
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains(childTypeL2));

        // add a second parent to childTypeL3
        childTypeL2_2 = ContainerTypeHelper.newContainerType(site,
            "Child L2 Container Type 2", "CCTL2_2", 1, 4, 4, false);
        childTypeL2_2.addChildContainerTypes(Arrays.asList(childTypeL3));
        childTypeL2_2.persist();

        list = childTypeL3.getParentContainerTypes();
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains(childTypeL2));
        Assert.assertTrue(list.contains(childTypeL2_2));

        // add a third parent to childTypeL3
        childTypeL2_3 = ContainerTypeHelper.newContainerType(site,
            "Child L2 Container Type 3", "CCTL2_3", 1, 5, 7, false);
        childTypeL2_3.addChildContainerTypes(Arrays.asList(childTypeL3));
        childTypeL2_3.persist();

        list = childTypeL3.getParentContainerTypes();
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains(childTypeL2));
        Assert.assertTrue(list.contains(childTypeL2_2));
        Assert.assertTrue(list.contains(childTypeL2_3));

        // now delete childTypeL2_2
        childTypeL2_2.delete();

        // test childTypeL3's parents again
        list = childTypeL3.getParentContainerTypes();
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains(childTypeL2));
        Assert.assertTrue(list.contains(childTypeL2_3));

        // now delete childTypeL2
        childTypeL2.delete();

        // test childTypeL3's parents again
        list = childTypeL3.getParentContainerTypes();
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains(childTypeL2_3));

        // now delete childTypeL2_3
        childTypeL2_3.delete();
        list = childTypeL3.getParentContainerTypes();
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void testGetSampleTypeCollection() throws Exception {
        addContainerTypeHierarchy(containerTypeMap.get("TopCT"));
        ContainerTypeWrapper childTypeL3 = containerTypeMap.get("ChildCtL3");
        Collection<SampleTypeWrapper> childTypeL3SampleTypes = childTypeL3
            .getSampleTypeCollection();
        Assert.assertTrue((childTypeL3SampleTypes == null)
            || (childTypeL3SampleTypes.size() == 0));

        List<SampleTypeWrapper> allSampleTypes = SampleTypeWrapper
            .getGlobalSampleTypes(appService, true);
        List<SampleTypeWrapper> selectedSampleTypes = TestCommon
            .getRandomSampleTypeList(r, allSampleTypes);
        // get list of unselected sample types
        List<SampleTypeWrapper> unselectedSampleTypes = new ArrayList<SampleTypeWrapper>();
        for (SampleTypeWrapper sampleType : allSampleTypes) {
            if (!selectedSampleTypes.contains(sampleType)) {
                unselectedSampleTypes.add(sampleType);
            }
        }
        childTypeL3.addSampleTypes(selectedSampleTypes);
        childTypeL3.persist();
        childTypeL3.reload();
        childTypeL3SampleTypes = childTypeL3.getSampleTypeCollection();
        Assert.assertEquals(selectedSampleTypes.size(), childTypeL3SampleTypes
            .size());
        for (SampleTypeWrapper type : selectedSampleTypes) {
            Assert.assertTrue(childTypeL3SampleTypes.contains(type));
        }

        childTypeL3.removeSampleTypes(childTypeL3.getSampleTypeCollection());
        childTypeL3SampleTypes = childTypeL3.getSampleTypeCollection();
        Assert.assertTrue((childTypeL3SampleTypes == null)
            || (childTypeL3SampleTypes.size() == 0));
    }

    @Test
    public void testAddSampleTypes() throws Exception {
        addContainerTypeHierarchy(containerTypeMap.get("TopCT"));
        ContainerTypeWrapper childTypeL3 = containerTypeMap.get("ChildCtL3");

        List<SampleTypeWrapper> allSampleTypes = SampleTypeWrapper
            .getGlobalSampleTypes(appService, true);
        List<SampleTypeWrapper> selectedSampleTypes = TestCommon
            .getRandomSampleTypeList(r, allSampleTypes);

        childTypeL3.addSampleTypes(selectedSampleTypes);
        childTypeL3.persist();
        childTypeL3.reload();
        List<SampleTypeWrapper> childTypeL3SampleTypes = childTypeL3
            .getSampleTypeCollection();
        Assert.assertEquals(selectedSampleTypes.size(), childTypeL3SampleTypes
            .size());
        for (SampleTypeWrapper type : selectedSampleTypes) {
            Assert.assertTrue(childTypeL3SampleTypes.contains(type));
        }

        childTypeL3.removeSampleTypes(childTypeL3.getSampleTypeCollection());
        childTypeL3SampleTypes = childTypeL3.getSampleTypeCollection();
        Assert.assertTrue((childTypeL3SampleTypes == null)
            || (childTypeL3SampleTypes.size() == 0));
    }

    @Test
    public void testRemoveSampleTypes() throws Exception {
        addContainerTypeHierarchy(containerTypeMap.get("TopCT"));
        ContainerTypeWrapper childTypeL3 = containerTypeMap.get("ChildCtL3");

        List<SampleTypeWrapper> allSampleTypes = SampleTypeWrapper
            .getGlobalSampleTypes(appService, true);
        List<SampleTypeWrapper> selectedSampleTypes = TestCommon
            .getRandomSampleTypeList(r, allSampleTypes);

        childTypeL3.addSampleTypes(selectedSampleTypes);
        childTypeL3.persist();
        childTypeL3.reload();

        // add containers
        ContainerWrapper top = ContainerHelper.addContainer("01", TestCommon
            .getNewBarcode(r), null, site, containerTypeMap.get("TopCT"));
        ContainerWrapper cont1 = ContainerHelper.addContainer(null, TestCommon
            .getNewBarcode(r), top, site, containerTypeMap.get("ChildCtL1"), 0,
            0);
        ContainerWrapper cont2 = ContainerHelper.addContainer(null, TestCommon
            .getNewBarcode(r), cont1, site, containerTypeMap.get("ChildCtL2"),
            0, 0);
        ContainerWrapper cont3 = ContainerHelper.addContainer(null, TestCommon
            .getNewBarcode(r), cont2, site, containerTypeMap.get("ChildCtL3"),
            0, 0);

        StudyWrapper study = StudyHelper.addStudy(site, "studyname"
            + r.nextInt());
        PatientWrapper patient = PatientHelper.addPatient("5684", study);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, "clinicname");
        ContactWrapper contact = ContactHelper.addContact(clinic,
            "ContactClinic");
        study.addContacts(Arrays.asList(contact));
        study.persist();
        ShipmentWrapper shipment = ShipmentHelper.addShipment(clinic, patient);
        PatientVisitWrapper pv = PatientVisitHelper.addPatientVisit(patient,
            shipment, null, Utils.getRandomDate());
        AliquotHelper.addAliquot(selectedSampleTypes.get(0), cont3, pv, 0, 0);
        AliquotWrapper aliquot = AliquotHelper.addAliquot(selectedSampleTypes
            .get(1), cont3, pv, 0, 1);

        childTypeL3
            .removeSampleTypes(Arrays.asList(selectedSampleTypes.get(1)));
        try {
            childTypeL3.persist();
            Assert
                .fail("Cannot remove a sample type if one container of this type contains this sample type");
        } catch (BiobankCheckException bce) {
            Assert.assertTrue(true);
        }

        childTypeL3
            .removeSampleTypes(Arrays.asList(selectedSampleTypes.get(1)));

        aliquot.delete();
        childTypeL3.persist();
    }

    @Test
    public void testGetSampleTypesRecursively() throws Exception {
        ContainerTypeWrapper topType, childTypeL3;

        topType = addContainerTypeHierarchy(containerTypeMap.get("TopCT"));
        childTypeL3 = containerTypeMap.get("ChildCtL3");
        Collection<SampleTypeWrapper> collection = topType
            .getSampleTypesRecursively();
        Assert.assertEquals(0, collection.size());

        List<SampleTypeWrapper> allSampleTypes = SampleTypeWrapper
            .getGlobalSampleTypes(appService, true);
        List<SampleTypeWrapper> selectedSampleTypes = TestCommon
            .getRandomSampleTypeList(r, allSampleTypes);

        childTypeL3 = TestCommon.addSampleTypes(childTypeL3,
            selectedSampleTypes);
        childTypeL3.addSampleTypes(selectedSampleTypes);
        childTypeL3.persist();
        topType.reload();
        collection = topType.getSampleTypesRecursively();
        Assert.assertEquals(selectedSampleTypes.size(), collection.size());
        for (SampleTypeWrapper type : selectedSampleTypes) {
            Assert.assertTrue(collection.contains(type));
        }

        childTypeL3.removeSampleTypes(childTypeL3.getSampleTypeCollection());
        childTypeL3.persist();
        topType.reload();
        collection = topType.getSampleTypesRecursively();
        Assert.assertTrue((collection == null) || (collection.size() == 0));
    }

    @Test
    public void testAddRemoveChildContainerTypes() throws Exception {
        ContainerTypeWrapper topType, childType1, childType2, childType3, childType3_2;

        topType = containerTypeMap.get("TopCT");

        childType3 = ContainerTypeHelper.addContainerType(site,
            "Child L3 Container Type", "CCTL3", 1, CONTAINER_CHILD_L3_ROWS,
            CONTAINER_CHILD_L3_COLS, false);

        // add childType3
        childType2 = ContainerTypeHelper.newContainerType(site,
            "Child L2 Container Type", "CCTL2", 3, 1, 10, false);
        childType2.addChildContainerTypes(Arrays.asList(childType3));
        childType2.persist();
        childType2.reload();
        Assert.assertEquals(1, childType2.getChildContainerTypeCollection()
            .size());

        // now add childType3_2
        childType3_2 = ContainerTypeHelper.addContainerType(site,
            "Child L3_2 Container Type", "CCTL3_2", 1,
            CONTAINER_CHILD_L3_ROWS - 1, CONTAINER_CHILD_L3_COLS - 1, false);
        childType2.addChildContainerTypes(Arrays.asList(childType3_2));
        childType2.persist();
        childType2.reload();
        Assert.assertEquals(2, childType2.getChildContainerTypeCollection()
            .size());

        // now remove childType3_2
        childType2.removeChildContainers(Arrays.asList(childType3_2));
        childType2.persist();
        childType2.reload();
        Assert.assertEquals(1, childType2.getChildContainerTypeCollection()
            .size());

        childType1 = ContainerTypeHelper.newContainerType(site,
            "Child L1 Container Type", "CCTL1", 3, 1, 10, false);
        childType1.addChildContainerTypes(Arrays.asList(childType2));
        childType1.persist();
        childType1.reload();
        Assert.assertEquals(1, childType1.getChildContainerTypeCollection()
            .size());

        topType.addChildContainerTypes(Arrays.asList(childType1));
        topType.persist();
        topType.reload();
        Assert
            .assertEquals(1, topType.getChildContainerTypeCollection().size());

        // now add childType1_2 and childType1_3 to topType and add a containers
        ContainerTypeWrapper childType1_2, childType1_3;
        childType1_2 = ContainerTypeHelper.addContainerType(site,
            "Child L1_2 Container Type", "CCTL1_2", 1, 3, 10, false);
        childType1_3 = ContainerTypeHelper.addContainerType(site,
            "Child L1_3 Container Type", "CCTL1_3", 1, 2, 18, false);
        topType.addChildContainerTypes(Arrays
            .asList(childType1_2, childType1_3));
        topType.persist();
        topType.reload();
        Assert
            .assertEquals(3, topType.getChildContainerTypeCollection().size());

        ContainerWrapper top = ContainerHelper.addContainer("01", TestCommon
            .getNewBarcode(r), null, site, containerTypeMap.get("TopCT"));
        top.addChild(0, 0, ContainerHelper.newContainer(null, TestCommon
            .getNewBarcode(r), top, site, childType1));
        top.addChild(0, 1, ContainerHelper.newContainer(null, TestCommon
            .getNewBarcode(r), top, site, childType1_2));
        top.addChild(0, 2, ContainerHelper.newContainer(null, TestCommon
            .getNewBarcode(r), top, site, childType1_3));
        top.persist();
        top.reload();

        // now attempt to remove childType1_2 and childType1_3
        topType
            .removeChildContainers(Arrays.asList(childType1_2, childType1_3));
        try {
            topType.persist();
            Assert.fail("cannot remove used child container types");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        }

        // remove the container child and then remove types
        top.deleteChildrenWithType(null, null);
        // type has not been reseted, so the removed types are still there.
        topType.persist();
        Assert
            .assertEquals(1, topType.getChildContainerTypeCollection().size());
    }

    @Test
    public void testGetChildContainerTypeCollection() throws Exception {
        ContainerTypeWrapper topType, childTypeL1, childTypeL2, childTypeL3, childTypeL2_2, childTypeL2_3;

        topType = addContainerTypeHierarchy(containerTypeMap.get("TopCT"));
        childTypeL1 = containerTypeMap.get("ChildCtL1");
        childTypeL2 = containerTypeMap.get("ChildCtL2");
        childTypeL3 = containerTypeMap.get("ChildCtL3");

        // each childTypeL1, childTypeL2, and childTypeL3 should have single
        // child
        List<ContainerTypeWrapper> list = topType
            .getChildContainerTypeCollection();
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains(childTypeL1));

        list = childTypeL1.getChildContainerTypeCollection();
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains(childTypeL2));

        list = childTypeL2.getChildContainerTypeCollection();
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains(childTypeL3));

        // add a second child to childTypeL1
        childTypeL2_2 = ContainerTypeHelper.addContainerType(site,
            "Child L2 Container Type 2", "CCTL2_2", 1, 4, 4, false);
        childTypeL1.addChildContainerTypes(Arrays.asList(childTypeL2,
            childTypeL2_2));
        childTypeL1.persist();

        list = childTypeL1.getChildContainerTypeCollection();
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains(childTypeL2));
        Assert.assertTrue(list.contains(childTypeL2_2));

        // add a third child to childTypeL1
        childTypeL2_3 = ContainerTypeHelper.addContainerType(site,
            "Child L2 Container Type 3", "CCTL2_3", 1, 5, 7, false);
        childTypeL1.addChildContainerTypes(Arrays.asList(childTypeL2,
            childTypeL2_2, childTypeL2_3));
        childTypeL1.persist();

        list = childTypeL1.getChildContainerTypeCollection();
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains(childTypeL2));
        Assert.assertTrue(list.contains(childTypeL2_2));
        Assert.assertTrue(list.contains(childTypeL2_3));

        // now delete childTypeL2_2
        childTypeL2_2.delete();

        // test childTypeL1's children again
        childTypeL1.reload();
        list = childTypeL1.getChildContainerTypeCollection();
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains(childTypeL2));
        Assert.assertTrue(list.contains(childTypeL2_3));

        // now delete childTypeL2
        childTypeL2.delete();

        // test childTypeL3's parents again
        childTypeL1.reload();
        list = childTypeL1.getChildContainerTypeCollection();
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains(childTypeL2_3));

        // now delete childTypeL2_3
        childTypeL2_3.delete();
        childTypeL1.reload();
        list = childTypeL1.getChildContainerTypeCollection();
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void testGetSite() throws Exception {
        ContainerTypeWrapper topType, childTypeL1, childTypeL2, childTypeL3;

        topType = addContainerTypeHierarchy(containerTypeMap.get("TopCT"));
        childTypeL1 = containerTypeMap.get("ChildCtL1");
        childTypeL2 = containerTypeMap.get("ChildCtL2");
        childTypeL3 = containerTypeMap.get("ChildCtL3");

        Assert.assertEquals(site, topType.getSite());
        Assert.assertEquals(site, childTypeL1.getSite());
        Assert.assertEquals(site, childTypeL2.getSite());
        Assert.assertEquals(site, childTypeL3.getSite());
    }

    @Test
    public void testGetCapacity() throws Exception {
        ContainerTypeWrapper topType, childTypeL1, childTypeL2, childTypeL3;

        topType = addContainerTypeHierarchy(containerTypeMap.get("TopCT"));
        childTypeL1 = containerTypeMap.get("ChildCtL1");
        childTypeL2 = containerTypeMap.get("ChildCtL2");
        childTypeL3 = containerTypeMap.get("ChildCtL3");

        Assert.assertEquals(CONTAINER_TOP_ROWS, topType.getRowCapacity()
            .intValue());
        Assert.assertEquals(CONTAINER_TOP_COLS, topType.getColCapacity()
            .intValue());

        Assert.assertEquals(1, childTypeL1.getRowCapacity().intValue());
        Assert.assertEquals(10, childTypeL1.getColCapacity().intValue());

        Assert.assertEquals(1, childTypeL2.getRowCapacity().intValue());
        Assert.assertEquals(10, childTypeL2.getColCapacity().intValue());

        Assert.assertEquals(CONTAINER_CHILD_L3_ROWS, childTypeL3
            .getRowCapacity().intValue());
        Assert.assertEquals(CONTAINER_CHILD_L3_COLS, childTypeL3
            .getColCapacity().intValue());

        childTypeL3.setRowCapacity(CONTAINER_CHILD_L3_ROWS - 1);
        childTypeL3.setColCapacity(CONTAINER_CHILD_L3_COLS - 1);

        Assert.assertEquals(CONTAINER_CHILD_L3_ROWS - 1, childTypeL3
            .getRowCapacity().intValue());
        Assert.assertEquals(CONTAINER_CHILD_L3_COLS - 1, childTypeL3
            .getColCapacity().intValue());
    }

    @Test
    public void testGetChildLabelingSchemeName() throws Exception {
        ContainerTypeWrapper topType, topType2, childTypeL1, childTypeL2, childTypeL3;

        // its important that topType2 is not saved to the database
        topType2 = ContainerTypeHelper.newContainerType(site,
            "Top Container Type 2", "TCT2", null, CONTAINER_TOP_ROWS,
            CONTAINER_TOP_COLS, true);
        Assert.assertEquals(null, topType2.getChildLabelingScheme());
        Assert.assertEquals(null, topType2.getChildLabelingSchemeName());

        topType = addContainerTypeHierarchy(containerTypeMap.get("TopCT"));
        childTypeL1 = containerTypeMap.get("ChildCtL1");
        childTypeL2 = containerTypeMap.get("ChildCtL2");
        childTypeL3 = containerTypeMap.get("ChildCtL3");

        Assert.assertEquals(2, topType.getChildLabelingScheme().intValue());
        Assert.assertTrue(topType.getChildLabelingSchemeName().equals(
            "CBSR 2 char alphabetic"));

        Assert.assertEquals(3, childTypeL1.getChildLabelingScheme().intValue());
        Assert.assertTrue(childTypeL1.getChildLabelingSchemeName().equals(
            "2 char numeric"));

        Assert.assertEquals(3, childTypeL2.getChildLabelingScheme().intValue());
        Assert.assertTrue(childTypeL2.getChildLabelingSchemeName().equals(
            "2 char numeric"));

        Assert.assertEquals(1, childTypeL3.getChildLabelingScheme().intValue());
        Assert.assertTrue(childTypeL3.getChildLabelingSchemeName().equals(
            "SBS Standard"));
    }

    @Test
    public void testActivityStatus() throws Exception {
        ContainerTypeWrapper topType;

        // its important that topType2 is not saved to the database
        topType = ContainerTypeHelper.newContainerType(site,
            "Top Container Type 2", "TCT2", 2, CONTAINER_TOP_ROWS,
            CONTAINER_TOP_COLS, true);
        topType.setActivityStatus(null);

        try {
            topType.persist();
            Assert.fail("Should not be allowed: no activity status");
        } catch (BiobankCheckException bce) {
            Assert.assertTrue(true);
        }

        topType.setActivityStatus(ActivityStatusWrapper.getActivityStatus(
            appService, "Active"));
        topType.persist();
    }

    @Test
    public void testGetTopContainerTypesInSite() throws Exception {
        ContainerTypeWrapper topType, topType2, childType;

        topType = containerTypeMap.get("TopCT");

        topType2 = ContainerTypeHelper.addContainerType(site,
            "Top Container Type 2", "TCT 2", 2, CONTAINER_TOP_ROWS - 1,
            CONTAINER_TOP_COLS + 1, true);

        childType = ContainerTypeHelper.addContainerType(site,
            "Child L1 Container Type", "CCTL1", 3, 1, 10, false);

        topType.addChildContainerTypes(Arrays.asList(childType));
        topType.persist();
        topType.reload();

        List<ContainerTypeWrapper> list = ContainerTypeWrapper
            .getTopContainerTypesInSite(appService, site);
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains(topType));
        Assert.assertTrue(list.contains(topType2));
        Assert.assertFalse(list.contains(childType));
    }

    @Test
    public void testGetContainerTypesInSite() throws Exception {
        ContainerTypeWrapper topType, childTypeL1, childTypeL2, childTypeL3, childTypeL2_2;

        topType = addContainerTypeHierarchy(containerTypeMap.get("TopCT"));
        childTypeL1 = containerTypeMap.get("ChildCtL1");
        childTypeL2 = containerTypeMap.get("ChildCtL2");
        childTypeL3 = containerTypeMap.get("ChildCtL3");

        // add a second child to childTypeL1
        childTypeL2_2 = ContainerTypeHelper.addContainerType(site,
            "Child L2 Container Type 2", "CCTL2_2", 1, 4, 4, false);
        childTypeL1.addChildContainerTypes(Arrays.asList(childTypeL2,
            childTypeL2_2));
        childTypeL1.persist();

        List<ContainerTypeWrapper> list = ContainerTypeWrapper
            .getContainerTypesInSite(appService, site, "Container", false);
        Assert.assertEquals(5, list.size());
        Assert.assertTrue(list.contains(topType));
        Assert.assertTrue(list.contains(childTypeL1));
        Assert.assertTrue(list.contains(childTypeL2));
        Assert.assertTrue(list.contains(childTypeL2_2));
        Assert.assertTrue(list.contains(childTypeL3));

        list = ContainerTypeWrapper.getContainerTypesInSite(appService, site,
            "Top Container Type", true);
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains(topType));

        childTypeL3.delete();

        list = ContainerTypeWrapper.getContainerTypesInSite(appService, site,
            "Container", false);
        Assert.assertEquals(4, list.size());
        Assert.assertTrue(list.contains(topType));
        Assert.assertTrue(list.contains(childTypeL1));
        Assert.assertTrue(list.contains(childTypeL2));
        Assert.assertTrue(list.contains(childTypeL2_2));
    }

    @Test
    public void testGetAllLabelingSchemes() {
        Map<Integer, String> map = ContainerTypeWrapper
            .getAllLabelingSchemes(appService);

        // currently only 4 labeling schemes
        Assert.assertEquals(4, map.size());
    }

    @Test
    public void testGetContainersCount() throws Exception {
        ContainerTypeWrapper topType = addContainerTypeHierarchy(containerTypeMap
            .get("TopCT"));
        ContainerTypeWrapper childTypeL1 = containerTypeMap.get("ChildCtL1");

        ContainerWrapper top = ContainerHelper.addContainer("01", "01", null,
            site, topType);
        ContainerHelper.addContainer(null, "1stChild", top, site, childTypeL1,
            0, 0);
        ContainerHelper.addContainer(null, "2ndChild", top, site, childTypeL1,
            0, 1);
        ContainerHelper.addContainer(null, "3rdChild", top, site, childTypeL1,
            0, 2);

        topType.reload();
        childTypeL1.reload();
        Assert.assertEquals(1, topType.getContainersCount());
        Assert.assertEquals(3, childTypeL1.getContainersCount());
    }
}
