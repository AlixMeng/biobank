package edu.ualberta.med.biobank.test.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.exception.BiobankRuntimeException;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.CollectionEventWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerLabelingSchemeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import edu.ualberta.med.biobank.common.wrappers.ProcessingEventWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.model.ActivityStatus;
import edu.ualberta.med.biobank.model.Container;
import edu.ualberta.med.biobank.model.ContainerPosition;
import edu.ualberta.med.biobank.model.util.RowColPos;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.BiobankSessionException;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.DuplicatePropertySetException;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.NullPropertyException;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.ValueNotSetException;
import edu.ualberta.med.biobank.test.TestDatabase;
import edu.ualberta.med.biobank.test.Utils;
import edu.ualberta.med.biobank.test.internal.ClinicHelper;
import edu.ualberta.med.biobank.test.internal.CollectionEventHelper;
import edu.ualberta.med.biobank.test.internal.ContactHelper;
import edu.ualberta.med.biobank.test.internal.ContainerHelper;
import edu.ualberta.med.biobank.test.internal.ContainerTypeHelper;
import edu.ualberta.med.biobank.test.internal.PatientHelper;
import edu.ualberta.med.biobank.test.internal.ProcessingEventHelper;
import edu.ualberta.med.biobank.test.internal.SiteHelper;
import edu.ualberta.med.biobank.test.internal.SpecimenHelper;
import edu.ualberta.med.biobank.test.internal.StudyHelper;
import gov.nih.nci.system.applicationservice.ApplicationException;

@SuppressWarnings({ "unused", "deprecation" })
@Deprecated
public class TestContainer extends TestDatabase {

    // the methods to skip in the getters and setters test
    private static final List<String> GETTER_SKIP_METHODS = Arrays
        .asList("getPath");

    private final String CBSR_ALPHA = "ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static final int CONTAINER_TOP_ROWS = 5;

    private static final int CONTAINER_TOP_COLS = 9;

    private static final int CONTAINER_CHILD_L3_ROWS = 8;

    private static final int CONTAINER_CHILD_L3_COLS = 12;

    private Map<String, ContainerWrapper> containerMap;

    private SiteWrapper site;

    private Map<String, ContainerTypeWrapper> containerTypeMap;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        containerMap = new HashMap<String, ContainerWrapper>();
        containerTypeMap = new HashMap<String, ContainerTypeWrapper>();
        site = SiteHelper.addSite("Site - Container Test"
            + Utils.getRandomString(5, 10));
        addContainerTypes(site);
        addContainers();
    }

    private void addContainerTypes(SiteWrapper site)
        throws BiobankCheckException, Exception {
        ContainerTypeWrapper topType, childType;

        childType = ContainerTypeHelper.addContainerType(site,
            "Child L4 Container Type", "CCTL4", 3, 4, 9, false);
        containerTypeMap.put("ChildCtL4", childType);

        childType = ContainerTypeHelper.addContainerType(site,
            "Child L3 Container Type", "CCTL3", 1, CONTAINER_CHILD_L3_ROWS,
            CONTAINER_CHILD_L3_COLS, false);
        childType.addToChildContainerTypeCollection(Arrays
            .asList(containerTypeMap.get("ChildCtL4")));
        childType.persist();
        containerTypeMap.put("ChildCtL3", childType);

        childType = ContainerTypeHelper.newContainerType(site,
            "Child L2 Container Type", "CCTL2", 1, 3, 12, false);
        childType.addToChildContainerTypeCollection(Arrays
            .asList(containerTypeMap.get("ChildCtL3")));
        childType.persist();
        containerTypeMap.put("ChildCtL2", childType);

        childType = ContainerTypeHelper.newContainerType(site,
            "Child L1 Container Type", "CCTL1", 3, 4, 5, false);
        childType.addToChildContainerTypeCollection(Arrays
            .asList(containerTypeMap.get("ChildCtL2")));
        childType.persist();
        containerTypeMap.put("ChildCtL1", childType);

        topType = ContainerTypeHelper.newContainerType(site,
            "Top Container Type", "TCT", 2, CONTAINER_TOP_ROWS,
            CONTAINER_TOP_COLS, true);
        topType.addToChildContainerTypeCollection(Arrays
            .asList(containerTypeMap.get("ChildCtL1")));
        topType.persist();
        containerTypeMap.put("TopCT", topType);
    }

    private void addContainers() throws BiobankCheckException, Exception {
        ContainerWrapper top = ContainerHelper.addContainer("01",
            TestCommon.getNewBarcode(r), site, containerTypeMap.get("TopCT"));
        containerMap.put("Top", top);
    }

    private ContainerWrapper addContainerHierarchy(ContainerWrapper parent,
        String mapPrefix, int level) throws Exception {
        ContainerWrapper childL1, childL2, childL3, childL4;
        Collection<ContainerWrapper> children;

        if (level >= 1) {
            childL1 = ContainerHelper.newContainer(null,
                TestCommon.getNewBarcode(r), parent, site,
                containerTypeMap.get("ChildCtL1"), 0, 0);
            parent.persist();
            parent.reload();
            childL1.reload();
            children = parent.getChildren().values();
            Assert.assertTrue(children.size() == 1);
            Assert.assertEquals(childL1, parent.getChild(0, 0));

            if (level >= 2) {
                childL2 = ContainerHelper.newContainer(null,
                    TestCommon.getNewBarcode(r), childL1, site,
                    containerTypeMap.get("ChildCtL2"), 0, 0);
                childL1.persist();
                childL1.reload();
                childL2.reload();
                children = childL1.getChildren().values();
                Assert.assertTrue(children.size() == 1);
                Assert.assertEquals(childL2, childL1.getChild(0, 0));

                if (level >= 3) {
                    childL3 = ContainerHelper.newContainer(null,
                        TestCommon.getNewBarcode(r), childL2, site,
                        containerTypeMap.get("ChildCtL3"), 0, 0);
                    childL2.persist();
                    childL2.reload();
                    childL3.reload();
                    children = childL2.getChildren().values();
                    Assert.assertTrue(children.size() == 1);
                    Assert.assertEquals(childL3, childL2.getChild(0, 0));

                    if (level >= 4) {
                        childL4 = ContainerHelper.newContainer(null,
                            TestCommon.getNewBarcode(r), childL3, site,
                            containerTypeMap.get("ChildCtL4"), 0, 0);
                        childL3.persist();
                        childL3.reload();
                        childL4.reload();
                        children = childL3.getChildren().values();
                        Assert.assertTrue(children.size() == 1);
                        Assert.assertEquals(childL4, childL3.getChild(0, 0));

                        containerMap.put(mapPrefix + "ChildL4", childL4);
                    }
                    childL3.reload();
                    containerMap.put(mapPrefix + "ChildL3", childL3);
                }
                childL2.reload();
                containerMap.put(mapPrefix + "ChildL2", childL2);
            }
            childL1.reload();
            containerMap.put(mapPrefix + "ChildL1", childL1);
            parent.reload();
            containerMap.put(mapPrefix + "Top", parent);
        }

        return parent;
    }

    private ContainerWrapper addContainerHierarchy(ContainerWrapper parent)
        throws Exception {
        return addContainerHierarchy(parent, "", 4);
    }

    public void addDupLabelledHierarchy() throws Exception {
        ContainerTypeWrapper freezerType, hotelType, cabinetType, drawerType;

        freezerType = ContainerTypeHelper.addContainerType(site, "freezer3x10",
            "F3x10", 2, 3, 10, true);
        containerTypeMap.put("frezer3x10", freezerType);
        hotelType = ContainerTypeHelper.addContainerType(site, "Hotel13",
            "H-13", 2, 1, 13, false);
        List<ContainerTypeWrapper> childContainerTypes =
            new ArrayList<ContainerTypeWrapper>();
        childContainerTypes.add(hotelType);
        freezerType.addToChildContainerTypeCollection(childContainerTypes);
        freezerType.persist();
        containerTypeMap.put("hotel13", hotelType);

        cabinetType = ContainerTypeHelper.addContainerType(site, "Cabinet",
            "C", 2, 1, 4, true);
        containerTypeMap.put("cabinet", cabinetType);
        drawerType = ContainerTypeHelper.addContainerType(site, "Drawer36",
            "D36", 2, 1, 36, false);
        childContainerTypes = new ArrayList<ContainerTypeWrapper>();
        childContainerTypes.add(drawerType);
        cabinetType.addToChildContainerTypeCollection(childContainerTypes);
        cabinetType.persist();
        containerTypeMap.put("drawer36", drawerType);

        ContainerWrapper freezer, cabinet, hotel, drawer;

        freezer = ContainerHelper.addContainer("02",
            TestCommon.getNewBarcode(r), site, freezerType);
        hotel = ContainerHelper.addContainer(null, TestCommon.getNewBarcode(r),
            freezer, site, hotelType, 0, 0);
        freezer.reload();
        containerMap.put("freezer02", freezer);
        containerMap.put("H02AA", hotel);

        cabinet = ContainerHelper.addContainer("02",
            TestCommon.getNewBarcode(r), site, cabinetType);
        drawer = ContainerHelper.addContainer(null,
            TestCommon.getNewBarcode(r), cabinet, site, drawerType, 0, 0);
        cabinet.reload();
        containerMap.put("cabinet", cabinet);
        containerMap.put("D02AA", drawer);
    }

    @Test
    public void testGettersAndSetters() throws BiobankCheckException, Exception {
        ContainerWrapper container = ContainerHelper.addContainer(
            String.valueOf(r.nextInt()), null, site,
            containerTypeMap.get("TopCT"));
        testGettersAndSetters(container, GETTER_SKIP_METHODS);
    }

    @Test
    public void testGetWrappedClass() throws Exception {
        ContainerWrapper container = ContainerHelper.addContainer(
            String.valueOf(r.nextInt()), null, site,
            containerTypeMap.get("TopCT"));
        Assert.assertEquals(Container.class, container.getWrappedClass());
    }

    @Test
    public void createValidContainer() throws Exception {
        ContainerWrapper container = ContainerHelper.addContainer("05", null,
            site, containerTypeMap.get("TopCT"));

        Integer id = container.getId();
        Assert.assertNotNull(id);
        Container containerInDB = ModelUtils.getObjectWithId(appService,
            Container.class, id);
        Assert.assertNotNull(containerInDB);
    }

    @Test
    public void testCreateNoSite() throws Exception {
        try {
            ContainerHelper.addContainer("05", null, null,
                containerTypeMap.get("TopCT"));
            Assert.fail("should not be allowed to add container with no site");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        ContainerWrapper container = ContainerHelper.newContainer("05", null,
            null, containerTypeMap.get("TopCT"));
        Assert.assertEquals(null, container.getSite());
    }

    @Test
    public void testCreateNoContainerType() throws Exception {
        try {
            ContainerHelper.addContainer("05", null, site, null);
            Assert
                .fail("should not be allowed to add container with no container type");
        } catch (NullPropertyException e) {
            Assert.assertTrue(true);
        }

        ContainerWrapper container = ContainerHelper.newContainer("05", null,
            site, null);
        Assert.assertEquals(null, container.getContainerType());
        Assert.assertEquals(null, container.getRowCapacity());
        Assert.assertEquals(null, container.getColCapacity());
    }

    @Test(expected = BiobankSessionException.class)
    public void createTopLevelNoParent() throws Exception {
        ContainerWrapper top = containerMap.get("Top");
        ContainerHelper.addContainer(TestCommon.getNewBarcode(r),
            TestCommon.getNewBarcode(r), top, site,
            containerTypeMap.get("TopCT"), 0, 0);
    }

    @Test
    public void testLabel() throws Exception {
        // make sure label is unique
        ContainerWrapper container2;
        ContainerHelper.addContainer("05", null, site,
            containerTypeMap.get("TopCT"));
        container2 = ContainerHelper.newContainer("05", null, site,
            containerTypeMap.get("TopCT"));

        try {
            container2.persist();
            Assert
                .fail("should not be allowed to add container because of duplicate label");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        // test getFullInfoLabel()
        Assert.assertEquals("05 ("
            + container2.getContainerType().getNameShort() + ")",
            container2.getFullInfoLabel());
        Assert.fail("test ctype null or nameshort null");

        // test getFullInfoLabel(): short name is null
        ContainerTypeWrapper topType2 = ContainerTypeHelper.addContainerType(
            site, "Top Container Type 2", "TCT2", 2, 3, 10, true);

        ContainerWrapper top2 = ContainerHelper.addContainer("02",
            TestCommon.getNewBarcode(r), site, topType2);
        Assert.assertEquals("02 (TCT2)", top2.getFullInfoLabel());
    }

    @Test
    public void testLabelNonTopLevel() throws Exception {
        String label = "ABCDEF";
        ContainerWrapper top, child;

        try {
            top = containerMap.get("Top");
            child = ContainerHelper.addContainer(label, "uvwxyz", top, site,
                containerTypeMap.get("ChildCtL1"), 0, 0);
            child.reload();
            // label should be assigned correct value by wrapper
            Assert.assertTrue(child.getLabel().equals(
                top.getLabel() + child.getPositionString()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testProductBarcodeUnique() throws Exception {
        ContainerWrapper container2;

        String barcode = TestCommon.getNewBarcode(r);

        ContainerHelper.addContainer("05", barcode, site,
            containerTypeMap.get("TopCT"));
        container2 = ContainerHelper.newContainer("06", barcode, site,
            containerTypeMap.get("TopCT"));

        try {
            container2.persist();
            Assert
                .fail("should not be allowed to add container because of duplicate product barcode");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testCheckParentAcceptContainerType() throws Exception {
        ContainerTypeWrapper type = containerTypeMap.get("ChildCtL4");

        ContainerWrapper container = ContainerHelper.newContainer("02",
            TestCommon.getNewBarcode(r), site, type);

        // should have a parent
        try {
            container.persist();
            Assert
                .fail("this container type is not top level. A parent is needed");
        } catch (BiobankSessionException bce) {
            Assert.assertTrue(true);
        }

        ContainerWrapper parent = containerMap.get("Top");
        container.setParent(parent, new RowColPos(0, 0));
        try {
            container.persist();
            Assert.fail("Parent does not accept this container type");
        } catch (BiobankSessionException e) {
            Assert.assertTrue(true);
        }

        container.setContainerType(parent.getContainerType()
            .getChildContainerTypeCollection().get(0));
        container.persist();
    }

    @Test
    public void testCheckPositionOk() throws Exception {
        ContainerWrapper parent = containerMap.get("Top");
        ContainerTypeWrapper type = parent.getContainerType()
            .getChildContainerTypeCollection().get(0);

        ContainerWrapper container = ContainerHelper.newContainer("02",
            TestCommon.getNewBarcode(r), site, type);
        try {
            container.setParent(parent, new RowColPos(10, 10));
            Assert.fail("position not ok in parent container");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        }

        container.setParent(parent, new RowColPos(0, 0));
        container.persist();

        ContainerWrapper container2 = ContainerHelper.newContainer(null,
            TestCommon.getNewBarcode(r), site, type);
        container2.setContainerType(type);
        try {
            container2.setParent(parent, new RowColPos(0, 0));
            container2.persist();
            Assert.fail("position not available");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        } catch (BiobankSessionException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGetPath() throws Exception {
        ContainerWrapper top, child;

        top = containerMap.get("Top");
        child = ContainerHelper.newContainer(TestCommon.getNewBarcode(r),
            "uvwxyz", top, site, containerTypeMap.get("ChildCtL1"), 0, 0);

        try {
            child.getPath();
            Assert
                .fail("cannot call get path on container net yet in database");
        } catch (BiobankRuntimeException e) {
            Assert.assertTrue(true);
        }

        try {
            child.setPath("");
            Assert.fail("cannot call setPath() on container");
        } catch (BiobankRuntimeException e) {
            Assert.assertTrue(true);
        }

        child.persist();
        child.reload();
        String expectedPath = top.getId() + "/" + child.getId();
        Assert.assertEquals(expectedPath, child.getPath());
    }

    @Test
    public void testActivityStatus() throws Exception {
        ContainerWrapper container = ContainerHelper.addContainer("05",
            TestCommon.getNewBarcode(r), site, containerTypeMap.get("TopCT"));
        container.setActivityStatus(null);

        try {
            container.persist();
            Assert.fail("Should not be allowed: no activity status");
        } catch (ValueNotSetException vnse) {
            Assert.assertTrue(true);
        }

        container.setActivityStatus(ActivityStatus.ACTIVE);
        container.persist();
    }

    @Test
    public void testReset() throws Exception {
        ContainerWrapper container = ContainerHelper.addContainer("05",
            TestCommon.getNewBarcode(r), site, containerTypeMap.get("TopCT"));
        container.reset();
    }

    @Test
    public void testReload() throws Exception {
        ContainerWrapper container = ContainerHelper.newContainer("05",
            "uvwxyz", site, containerTypeMap.get("TopCT"));
        container.reload();
    }

    @Test
    public void testSetPositionOnChild() throws Exception {
        ContainerWrapper top, child;

        top = containerMap.get("Top");

        child = ContainerHelper.addContainer(TestCommon.getNewBarcode(r),
            "uvwxyz", containerMap.get("Top"), site,
            containerTypeMap.get("ChildCtL1"), 0, 0);

        // set position to null
        child.setParent(null, null);
        try {
            child.persist();
            Assert.fail("should not be allowed to set an null position");
        } catch (BiobankSessionException e) {
            Assert.assertTrue(true);
        }

        // create new child
        try {
            child = ContainerHelper.newContainer(null, "uvwxyzabcdef", top,
                site, containerTypeMap.get("ChildCtL1"), top.getRowCapacity(),
                top.getColCapacity());
            child.persist();
            Assert.fail("should not be allowed to set an invalid position");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        } catch (BiobankSessionException e) {
            Assert.assertTrue(true);
        }

        try {
            child.setParent(top,
                new RowColPos(top.getRowCapacity() + 1,
                    top.getColCapacity() + 1));
            child.persist();
            Assert.fail("should not be allowed to set an invalid position");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        } catch (BiobankSessionException e) {
            Assert.assertTrue(true);
        }

        child.setParent(top, new RowColPos(-1, -1));
        try {
            child.persist();
            Assert.fail("should not be allowed to set an invalid position");
        }
        // JMF: I removed this exception because of an upgrade to a new
        // hibernate-validation jar
        // catch (ValidationException e) {
        // Assert.assertTrue(true);
        // }
        catch (ApplicationException e) {
            // TODO: figure out why this isn't being wrapped and thrown as
            // ValidationException
        }
    }

    @Test
    public void testUniquePosition() throws Exception {
        ContainerWrapper top;

        top = containerMap.get("Top");
        ContainerHelper.addContainer(TestCommon.getNewBarcode(r), "uvwxyz",
            top, site, containerTypeMap.get("ChildCtL1"), 0, 0);

        try {
            ContainerHelper.addContainer(null, "uvwxyz", top, site,
                containerTypeMap.get("ChildCtL1"), 0, 0);
            Assert
                .fail("should not be allowed to add container because of duplicate product barcode");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGetContainersInSite() throws Exception {
        ContainerWrapper freezer, hotel, cabinet, drawer;

        addDupLabelledHierarchy();

        freezer = containerMap.get("freezer02");
        hotel = containerMap.get("H02AA");
        cabinet = containerMap.get("cabinet");
        drawer = containerMap.get("D02AA");

        List<ContainerWrapper> list = ContainerWrapper.getContainersInSite(
            appService, site, "02AA");
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains(hotel));
        Assert.assertTrue(list.contains(drawer));

        list = ContainerWrapper.getContainersInSite(appService, site, "02");
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains(freezer));
        Assert.assertTrue(list.contains(cabinet));
    }

    @Test
    public void testGetPossibleParents() throws Exception {
        ContainerWrapper top1, top2, childL1, childL2;

        ContainerTypeWrapper topType2 = ContainerTypeHelper.addContainerType(
            site, "Top Container Type 2", "TCT2", 2, 3, 10, true);

        top2 = ContainerHelper.addContainer("02", TestCommon.getNewBarcode(r),
            site, topType2);

        top1 = ContainerHelper.addContainer("02", TestCommon.getNewBarcode(r),
            site, containerTypeMap.get("TopCT"));
        childL1 = ContainerHelper.addContainer("02AA", "0001", top1, site,
            containerTypeMap.get("ChildCtL1"), 0, 0);
        top1.reload();

        List<ContainerWrapper> parents = childL1.getPossibleParents("02AA");
        Assert.assertTrue(parents.contains(top1));
        Assert.assertFalse(parents.contains(top2));

        addContainerHierarchy(containerMap.get("Top"));
        childL1 = containerMap.get("ChildL1");
        childL2 = containerMap.get("ChildL2");
        parents = childL2.getPossibleParents("01AA01");
        Assert.assertTrue((parents.size() == 1) && parents.contains(childL1));
        parents = childL2.getPossibleParents("01AA");
        Assert.assertEquals(0, parents.size());

        parents = top1.getPossibleParents("");
        Assert.assertEquals(0, parents.size());
    }

    private String getLabel(Integer labelingScheme, int maxRows, int maxCol,
        int row, int col) {
        String label = null;
        int len = ContainerLabelingSchemeWrapper.CBSR_2_CHAR_LABELLING_PATTERN
            .length();

        switch (labelingScheme) {
        case 1: {
            label = String.format("%c%d",
                ContainerLabelingSchemeWrapper.SBS_ROW_LABELLING_PATTERN
                    .charAt(row), col + 1);
            break;
        }
        case 2: {
            int index = (maxRows * col) + row;
            label = String.format("%c%c",
                ContainerLabelingSchemeWrapper.CBSR_2_CHAR_LABELLING_PATTERN
                    .charAt(index / len),
                ContainerLabelingSchemeWrapper.CBSR_2_CHAR_LABELLING_PATTERN
                    .charAt(index % len));
            break;
        }
        case 3: {
            int index = (maxRows * col) + row + 1;
            label = String.format("%02d", index);
            break;
        }
        default:
            Assert.fail("labeling scheme not used");
        }
        Assert.assertNotNull(label);
        return label;
    }

    private void testGetPositionFromLabelingScheme(ContainerWrapper container)
        throws Exception {
        ContainerTypeWrapper type = container.getContainerType();
        int labelingScheme = type.getChildLabelingSchemeId();
        int maxRows = type.getRowCapacity();
        int maxCols = type.getColCapacity();

        for (int row = 0; row < maxRows; ++row) {
            for (int col = 0; col < maxCols; ++col) {
                RowColPos result = container
                    .getPositionFromLabelingScheme(getLabel(labelingScheme,
                        maxRows, maxCols, row, col));

                // System.out.println("type/" + type + " scheme/"
                // + type.getChildLabelingScheme() + " label/" + label
                // + " row/" + result.row + " col/" + result.col);

                Assert.assertNotNull(result);
                Assert.assertEquals(row, result.getRow().intValue());
                Assert.assertEquals(col, result.getCol().intValue());
            }
        }

        try {
            container.getPositionFromLabelingScheme(getLabel(labelingScheme,
                maxRows + 1, maxCols + 1, maxRows, maxCols));
            Assert.fail("invalid position requested");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        Assert.fail("check with negative values");
    }

    @Test
    public void testGetPositionFromLabelingScheme() throws Exception {
        ContainerWrapper top = containerMap.get("Top");
        addContainerHierarchy(top);

        for (ContainerWrapper container : containerMap.values()) {
            testGetPositionFromLabelingScheme(container);
        }
    }

    private void testAddChildrenByLabel(ContainerWrapper parent,
        ContainerTypeWrapper childType) throws Exception {
        int labelingScheme = parent.getContainerType()
            .getChildLabelingSchemeId();
        int maxRows = parent.getRowCapacity();
        int maxCols = parent.getColCapacity();
        String label;

        for (int row = 0; row < maxRows; ++row) {
            for (int col = 0; col < maxCols; ++col) {
                label = getLabel(labelingScheme, maxRows, maxCols, row, col);

                // System.out.println("type/" + childType + " scheme/"
                // + childType.getChildLabelingScheme() + " label/" + label);
                ContainerWrapper child = ContainerHelper.newContainer(null,
                    TestCommon.getNewBarcode(r), site, childType);
                parent.addChild(label, child);
            }
        }
        parent.persist();
        parent.reload();

        // now add one more outside bounds
        label = getLabel(labelingScheme, maxRows + 1, maxCols + 1, maxRows,
            maxCols);

        try {
            ContainerWrapper child = ContainerHelper.newContainer(null,
                TestCommon.getNewBarcode(r), site, childType);
            parent.addChild(label, child);
            Assert.fail("should not be allowed to add children beyond limit");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAddChildrenByLabel() throws Exception {
        ContainerWrapper top = containerMap.get("Top");

        // before adding children make sure there are none
        Assert.assertFalse(top.hasChildren());
        Assert.assertEquals(null, top.getChild(0, 0));

        testAddChildrenByLabel(top, containerTypeMap.get("ChildCtL1"));

        int maxRows = top.getRowCapacity();
        int maxCols = top.getColCapacity();

        testAddChildrenByLabel(top.getChild(0, 0),
            containerTypeMap.get("ChildCtL2"));
        testAddChildrenByLabel(top.getChild(maxRows - 1, maxCols - 1),
            containerTypeMap.get("ChildCtL2"));

        maxRows = top.getChild(0, 0).getRowCapacity();
        maxCols = top.getChild(0, 0).getColCapacity();

        testAddChildrenByLabel(top.getChild(0, 0).getChild(0, 0),
            containerTypeMap.get("ChildCtL3"));
        testAddChildrenByLabel(
            top.getChild(0, 0).getChild(maxRows - 1, maxCols - 1),
            containerTypeMap.get("ChildCtL3"));

        maxRows = top.getChild(0, 0).getChild(0, 0).getRowCapacity();
        maxCols = top.getChild(0, 0).getChild(0, 0).getColCapacity();

        testAddChildrenByLabel(
            top.getChild(0, 0).getChild(0, 0).getChild(0, 0),
            containerTypeMap.get("ChildCtL4"));
        testAddChildrenByLabel(
            top.getChild(0, 0).getChild(0, 0)
                .getChild(maxRows - 1, maxCols - 1),
            containerTypeMap.get("ChildCtL4"));
    }

    @Test
    public void testGetCapacity() throws Exception {
        ContainerWrapper top = containerMap.get("Top");
        Assert.assertEquals(new Integer(CONTAINER_TOP_ROWS),
            top.getRowCapacity());
        Assert.assertEquals(new Integer(CONTAINER_TOP_COLS),
            top.getColCapacity());

    }

    @Test
    public void testgetParentContainer() throws Exception {
        ContainerWrapper top = containerMap.get("Top");
        addContainerHierarchy(top);

        Assert.assertEquals(null, containerMap.get("Top").getParentContainer());
        Assert.assertEquals(containerMap.get("Top"), containerMap
            .get("ChildL1").getParentContainer());
        Assert.assertEquals(containerMap.get("ChildL1"),
            containerMap.get("ChildL2").getParentContainer());
        Assert.assertEquals(containerMap.get("ChildL2"),
            containerMap.get("ChildL3").getParentContainer());
    }

    @Test
    public void testHasParent() throws Exception {
        ContainerWrapper top = containerMap.get("Top");
        addContainerHierarchy(top);
        Assert.assertFalse(containerMap.get("Top").hasParentContainer());
        Assert.assertTrue(containerMap.get("ChildL1").hasParentContainer());
        Assert.assertTrue(containerMap.get("ChildL2").hasParentContainer());
        Assert.assertTrue(containerMap.get("ChildL3").hasParentContainer());
    }

    @Test
    public void testCanHoldSample() throws Exception {
        String name = "testCanHoldSample" + r.nextInt();
        List<SpecimenTypeWrapper> allSampleTypes = SpecimenTypeWrapper
            .getAllSpecimenTypes(appService, true);
        List<SpecimenTypeWrapper> selectedSampleTypes = TestCommon
            .getRandomSampleTypeList(r, allSampleTypes);

        ContainerTypeWrapper childTypeL3 = TestCommon.addSampleTypes(
            containerTypeMap.get("ChildCtL3"), selectedSampleTypes);
        containerTypeMap.put("ChildCtL3", childTypeL3);

        addContainerHierarchy(containerMap.get("Top"));
        ContainerWrapper childL3 = containerMap.get("ChildL3");

        // reload because we changed container type
        childL3.reload();
        SpecimenWrapper specimen = null;

        ClinicWrapper clinic = ClinicHelper
            .addClinic("Clinic - Processing Event Test "
                + Utils.getRandomString(10));
        CollectionEventWrapper ce = CollectionEventHelper
            .addCollectionEventWithRandomPatient(clinic, name, 1);

        for (SpecimenTypeWrapper st : allSampleTypes) {
            specimen = SpecimenHelper.newSpecimen(st);
            ce.addToAllSpecimenCollection(Arrays.asList(specimen));
            if (selectedSampleTypes.contains(st)) {
                Assert.assertTrue(childL3.canHoldSpecimenType(specimen));
            } else {
                Assert.assertTrue(!childL3.canHoldSpecimenType(specimen));
            }
        }

        specimen = SpecimenHelper.newSpecimen((SpecimenTypeWrapper) null);
        try {
            childL3.canHoldSpecimenType(specimen);
            Assert
                .fail("should not be allowed to add aliquot with null sample type");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGetSpecimens() throws Exception {
        List<SpecimenTypeWrapper> allSampleTypes = SpecimenTypeWrapper
            .getAllSpecimenTypes(appService, true);
        List<SpecimenTypeWrapper> selectedSampleTypes = TestCommon
            .getRandomSampleTypeList(r, allSampleTypes);
        List<SpecimenTypeWrapper> unselectedSampleTypes =
            new ArrayList<SpecimenTypeWrapper>();

        for (SpecimenTypeWrapper sampleType : allSampleTypes) {
            if (!selectedSampleTypes.contains(sampleType)) {
                unselectedSampleTypes.add(sampleType);
            }
        }

        ContainerTypeWrapper childTypeL3 = TestCommon.addSampleTypes(
            containerTypeMap.get("ChildCtL3"), selectedSampleTypes);
        containerTypeMap.put("ChildCtL3", childTypeL3);

        StudyWrapper study = StudyHelper.addStudy("Study1");
        ContactHelper.addContactsToStudy(study, "contactsStudy1");
        PatientWrapper patient = PatientHelper.addPatient("1000", study);

        ContainerWrapper top = containerMap.get("Top");
        addContainerHierarchy(top);

        Map<RowColPos, SpecimenTypeWrapper> samplesTypesMap =
            new TreeMap<RowColPos, SpecimenTypeWrapper>();
        SpecimenTypeWrapper sampleType;

        CollectionEventWrapper ce = CollectionEventHelper.addCollectionEvent(
            site, patient, 1);
        ContainerWrapper childL3 = containerMap.get("ChildL3");
        for (int row = 0, maxRow = childL3.getRowCapacity(), n =
            selectedSampleTypes
                .size(); row < maxRow; ++row) {
            for (int col = 0, maxCol = childL3.getColCapacity(); col < maxCol; ++col) {
                if ((row == 1) && (col == 1)) {
                    // attempt to add invalid sample type
                    sampleType = unselectedSampleTypes.get(r
                        .nextInt(unselectedSampleTypes.size()));
                    Assert.assertNull(childL3.getSpecimen(row, col));
                    try {
                        childL3.addSpecimen(row, col,
                            SpecimenHelper.newSpecimen(sampleType));
                        Assert
                            .fail("should not be allowed to add invalid sample type");
                    } catch (Exception e) {
                        Assert.assertTrue(true);
                    }
                }

                sampleType = selectedSampleTypes.get(r.nextInt(n));
                samplesTypesMap.put(new RowColPos(row, col), sampleType);
                childL3.addSpecimen(row, col, SpecimenHelper.addSpecimen(
                    sampleType, ActivityStatus.ACTIVE,
                    Utils.getRandomDate(), ce, site));
                SpecimenWrapper spc = childL3.getSpecimen(row, col);
                spc.persist();
            }
        }
        childL3.persist();
        childL3.reload();

        // attempt to add aliquot where there already is one
        sampleType = selectedSampleTypes.get(r.nextInt(selectedSampleTypes
            .size()));
        try {
            childL3.addSpecimen(0, 0, SpecimenHelper.newSpecimen(sampleType));
            Assert
                .fail("should not be allowed to add second sample type in same position");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        // force samples to be loaded from DB
        childL3 = containerMap.get("ChildL2").getChild(0, 0);
        Map<RowColPos, SpecimenWrapper> spcs = childL3.getSpecimens();
        Assert.assertEquals(samplesTypesMap.size(), spcs.size());
        for (RowColPos pos : spcs.keySet()) {
            SpecimenWrapper spc = spcs.get(pos);
            Assert.assertTrue((pos.getRow() >= 0)
                && (pos.getRow() < CONTAINER_CHILD_L3_ROWS));
            Assert.assertTrue((pos.getCol() >= 0)
                && (pos.getCol() < CONTAINER_CHILD_L3_COLS));
            Assert
                .assertEquals(samplesTypesMap.get(pos), spc.getSpecimenType());
        }

        for (int row = 0, maxRow = childL3.getRowCapacity(); row < maxRow; ++row) {
            for (int col = 0, maxCol = childL3.getColCapacity(); col < maxCol; ++col) {
                SpecimenWrapper spc = childL3.getSpecimen(row, col);
                Assert.assertEquals(
                    samplesTypesMap.get(new RowColPos(row, col)),
                    spc.getSpecimenType());
                spc.delete();
                childL3.reload();
                Assert.assertNull(childL3.getSpecimen(row, col));
            }
        }

        try {
            childL3.getSpecimen(CONTAINER_CHILD_L3_ROWS + 1,
                CONTAINER_CHILD_L3_COLS);
            Assert.fail("should not be allowed to get children beyond limit");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        try {
            childL3.getSpecimen(CONTAINER_CHILD_L3_ROWS,
                CONTAINER_CHILD_L3_COLS + 1);
            Assert.fail("should not be allowed to get children beyond limit");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        Assert.fail("check also with fast = true");
    }

    @Test
    public void testGetChildren() throws Exception {
        ContainerWrapper top, childL1, childL2, childL3, childL4, childL3_2;

        top = addContainerHierarchy(containerMap.get("Top"));
        childL1 = containerMap.get("ChildL1");
        childL2 = containerMap.get("ChildL2");
        childL3 = containerMap.get("ChildL3");
        childL4 = containerMap.get("ChildL4");
        childL4.delete();
        childL3.reload();

        Map<RowColPos, ContainerWrapper> childrenMap = childL2.getChildren();
        Assert.assertTrue(childrenMap.size() == 1);
        Assert.assertEquals(childrenMap.get(new RowColPos(0, 0)), childL3);
        Assert.assertEquals(childL2.getChild(0, 0), childL3);

        childrenMap = childL1.getChildren();
        Assert.assertTrue(childrenMap.size() == 1);
        Assert.assertEquals(childrenMap.get(new RowColPos(0, 0)), childL2);
        Assert.assertEquals(childL1.getChild(0, 0), childL2);

        childrenMap = top.getChildren();
        Assert.assertTrue(childrenMap.size() == 1);
        Assert.assertEquals(childrenMap.get(new RowColPos(0, 0)), childL1);
        Assert.assertEquals(top.getChild(0, 0), childL1);

        // remove childL3 from childL2
        childL3.delete();
        childL2.reload();
        Assert.assertTrue(childL2.getChildren().size() == 0);

        // add again
        childL3 = ContainerHelper.addContainer(null, "0003", childL2, site,
            containerTypeMap.get("ChildCtL3"), 0, 0);
        childL2.reload();
        childrenMap = childL2.getChildren();
        Assert.assertTrue(childrenMap.size() == 1);
        Assert.assertEquals(childrenMap.get(new RowColPos(0, 0)), childL3);
        Assert.assertEquals(childL2.getChild(0, 0), childL3);

        childL3_2 = ContainerHelper.addContainer(null, "0004", childL2, site,
            containerTypeMap.get("ChildCtL3"), 0, 1);
        childL2.reload();
        childrenMap = childL2.getChildren();
        Assert.assertTrue(childrenMap.size() == 2);
        Assert.assertEquals(childrenMap.get(new RowColPos(0, 0)), childL3);
        Assert.assertEquals(childrenMap.get(new RowColPos(0, 1)), childL3_2);
        Assert.assertEquals(childL2.getChild(new RowColPos(0, 0)), childL3);
        Assert.assertEquals(childL2.getChild(new RowColPos(0, 1)), childL3_2);

        // remove first child
        childL3.delete();
        childL2.reload();
        childrenMap = childL2.getChildren();
        Assert.assertTrue(childrenMap.size() == 1);
        Assert.assertEquals(childrenMap.get(new RowColPos(0, 1)), childL3_2);
        Assert.assertEquals(childL2.getChild(0, 1), childL3_2);
    }

    private void testGetChilddByLabel(ContainerWrapper container)
        throws Exception {
        ContainerWrapper child;

        String label;
        int labelingScheme = container.getContainerType()
            .getChildLabelingSchemeId();

        for (int row = 0, maxRow = container.getRowCapacity(); row < maxRow; ++row) {
            for (int col = 0, maxCol = container.getColCapacity(); col < maxCol; ++col) {
                // label does not contain parent's label
                switch (labelingScheme) {
                case 1:
                    label = String.format("%c%d", 'A' + row, col + 1);
                    break;
                case 2:
                    int sum = row + (col * maxRow);
                    label = "" + CBSR_ALPHA.charAt(sum / CBSR_ALPHA.length())
                        + CBSR_ALPHA.charAt(sum % CBSR_ALPHA.length());
                    break;
                case 3:
                default:
                    label = String.format("%02d", row + (col * maxRow) + 1);
                }
                child = container.getChild(row, col);
                Assert.assertEquals(child, container.getChildByLabel(label));

                // add parent's label
                label = container.getLabel() + label;
                Assert.assertEquals(child, container.getChildByLabel(label));
            }
        }

    }

    @Test
    public void testGetChilddByLabel() throws Exception {
        ContainerWrapper top = containerMap.get("Top");

        // test getChildByLabel()
        top.initChildrenWithType(containerTypeMap.get("ChildCtL1"), null);
        top.getChild(0, 0).initChildrenWithType(
            containerTypeMap.get("ChildCtL2"), null);
        top.getChild(0, 0).getChild(0, 0)
            .initChildrenWithType(containerTypeMap.get("ChildCtL3"), null);

        testGetChilddByLabel(top);
        testGetChilddByLabel(top.getChild(0, 0));
        testGetChilddByLabel(top.getChild(0, 0).getChild(0, 0));
    }

    @Test
    public void testGetContainersByLabel() {
        try {
            ContainerHelper.addContainer("01AA", "asdf", site,
                ContainerTypeHelper.addContainerTypeRandom(site, "ct", true));
        } catch (Exception e) {
            Assert.fail();
        }
        List<ContainerWrapper> containers = null;
        try {
            containers = ContainerWrapper
                .getContainersByLabel(appService, "01");
        } catch (ApplicationException e) {
            Assert.fail();
        }
        if (containers.size() == 1)
            Assert.assertTrue(true);
    }

    @Test
    public void testGetContainerWithProductBarcodeInSite() throws Exception {
        ContainerWrapper top;

        top = containerMap.get("Top");
        addContainerHierarchy(top);

        String barcode = TestCommon.getNewBarcode(r);

        // ensure barcode not in DB
        Assert.assertEquals(null, ContainerWrapper
            .getContainerWithProductBarcodeInSite(appService, site, barcode));

        // now add to DB

        ContainerWrapper child = ContainerHelper.newContainer(null, barcode,
            site, containerTypeMap.get("ChildCtL1"));

        top.addChild(1, 0, child);
        top.persist();
        top.reload();

        Assert.assertEquals(top.getChild(1, 0), ContainerWrapper
            .getContainerWithProductBarcodeInSite(appService, site, barcode));
    }

    @Test
    public void testInitChildrenWithType() throws Exception {
        ContainerWrapper top, child;

        top = containerMap.get("Top");
        addContainerHierarchy(top, null, 1);

        // create a new child type to go under top level container
        ContainerTypeWrapper childType1_2 = ContainerTypeHelper
            .addContainerType(site, "Child L1 Container Type - 2", "CCTL1-2",
                3, 1, 15, false);

        ContainerTypeWrapper topType = containerTypeMap.get("TopCT");

        topType.addToChildContainerTypeCollection(Arrays.asList(
            containerTypeMap.get("ChildCtL1"), childType1_2));
        topType.persist();
        topType.reload();

        Assert.assertTrue(top.getChildren().size() == 1);
        top.initChildrenWithType(childType1_2, null);
        top.reload();

        Collection<ContainerWrapper> children = top.getChildren().values();
        Assert
            .assertTrue(children.size() == (CONTAINER_TOP_ROWS * CONTAINER_TOP_COLS));
        for (ContainerWrapper container : children) {
            if (container.getPositionAsRowCol().equals(0, 0)) {
                Assert.assertTrue(container.getContainerType().equals(
                    containerTypeMap.get("ChildCtL1")));
            } else {
                Assert.assertTrue(container.getContainerType().equals(
                    childType1_2));
            }
            container.delete();
        }

        top.reload();
        Set<RowColPos> positions = new HashSet<RowColPos>();
        positions.add(new RowColPos(0, 0));
        positions.add(new RowColPos(0, 1));
        positions.add(new RowColPos(1, 0));
        positions.add(new RowColPos(1, 1));
        top.initChildrenWithType(childType1_2, positions);

        for (RowColPos pos : positions) {
            child = top.getChild(pos);
            Assert.assertNotNull(child);
            Assert.assertEquals(childType1_2, child.getContainerType());
        }
    }

    public void testInitChildrenWithTypeWithPositionList() throws Exception {
        Assert.fail("not yet implemented");
    }

    @Test
    public void testDeleteChildrenWithType() throws Exception {
        ContainerWrapper top, childL2, childL3;

        top = containerMap.get("Top");
        addContainerHierarchy(top);
        childL2 = containerMap.get("ChildL2");
        childL3 = containerMap.get("ChildL3");

        ContainerHelper.addContainer(null, "NewChildL3", childL3,
            childL3.getSite(), containerTypeMap.get("ChildCtL4"), 0, 1);
        childL3.reload();
        Assert.assertEquals(2, childL3.getChildren().size());
        Assert.assertTrue(childL3.deleteChildrenWithType(
            containerTypeMap.get("ChildCtL4"), null));
        // no position list: all children of this type are deleted.
        Assert.assertEquals(0, childL3.getChildren().size());

        ContainerHelper.addContainer(null, "NewChildL2", childL2,
            childL2.getSite(), containerTypeMap.get("ChildCtL3"), 0, 1);
        childL2.reload();
        Assert.assertEquals(2, childL2.getChildren().size());
        Assert.assertTrue(childL2.deleteChildrenWithType(
            containerTypeMap.get("ChildCtL3"),
            new TreeSet<RowColPos>(Arrays.asList(new RowColPos(0, 0)))));
        // one position: only this one is deleted.
        Assert.assertEquals(1, childL2.getChildren().size());
        Assert.assertNotNull(childL2.getChild(0, 1));
        Assert.assertNull(childL2.getChild(0, 0));
    }

    @Test
    public void testCompareTo() throws Exception {
        ContainerWrapper top, childL1, childL2, childL3;

        top = addContainerHierarchy(containerMap.get("Top"));
        childL1 = containerMap.get("ChildL1");
        childL2 = containerMap.get("ChildL2");
        childL3 = containerMap.get("ChildL3");

        Assert.assertTrue(top.compareTo(childL1) < 0);
        Assert.assertTrue(childL1.compareTo(childL2) < 0);
        Assert.assertTrue(childL2.compareTo(childL3) < 0);
        Assert.assertEquals(0, top.compareTo(top));
    }

    @Test(expected = DuplicatePropertySetException.class)
    public void testContainerTypeSameSite() throws Exception {
        SiteWrapper altSite = SiteHelper.addSite("Site2 - Container Test"
            + Utils.getRandomString(10));

        ContainerTypeWrapper altTopType = ContainerTypeHelper.newContainerType(
            altSite, "Alt Top Container Type", "ATCT", 2, CONTAINER_TOP_ROWS,
            CONTAINER_TOP_COLS, true);
        altTopType.persist();

        ContainerHelper.addContainer("01", TestCommon.getNewBarcode(r), site,
            containerTypeMap.get("TopCT"));
    }

    @Test
    public void testParentSameSite() throws Exception {
        // create an alternate site and create a container type for the
        // alternate site
        SiteWrapper altSite = SiteHelper.addSite("Site2 - Container Test"
            + Utils.getRandomString(10));

        ContainerTypeWrapper childType = ContainerTypeHelper.addContainerType(
            altSite, "Alt Child L1 Container Type", "ACCTL1", 3, 1, 10, false);

        ContainerTypeWrapper altTopType = ContainerTypeHelper.newContainerType(
            altSite, "Alt Top Container Type", "ATCT", 2, CONTAINER_TOP_ROWS,
            CONTAINER_TOP_COLS, true);
        altTopType.addToChildContainerTypeCollection(Arrays.asList(childType));
        altTopType.persist();
        childType.reload();

        ContainerWrapper altTop = ContainerHelper.addContainer("01",
            TestCommon.getNewBarcode(r), altSite, altTopType);

        // now a container of type container type for alternate site to the main
        // site
        try {
            ContainerHelper.addContainer(null, TestCommon.getNewBarcode(r),
                altTop, site, childType, 0, 0);
            Assert.fail("Parent should be in the same site");
        } catch (BiobankSessionException e) {
            Assert.assertTrue(true);
        }

        try {
            ContainerHelper.addContainer(null, TestCommon.getNewBarcode(r),
                containerMap.get("Top"), site, childType, 0, 0);
            Assert.fail("type should be in the same site");
        } catch (BiobankSessionException bce) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAddChildrenTooMany() throws Exception {
        ContainerWrapper top;

        top = containerMap.get("Top");
        for (int row = 0; row < CONTAINER_TOP_ROWS; ++row) {
            for (int col = 0; col < CONTAINER_TOP_COLS; ++col) {
                ContainerWrapper child = ContainerHelper.newContainer(null,
                    TestCommon.getNewBarcode(r), site,
                    containerTypeMap.get("ChildCtL1"));
                top.addChild(row, col, child);
            }
        }
        top.persist();
        top.reload();

        // now add one more
        try {
            ContainerWrapper child = ContainerHelper.newContainer(null,
                TestCommon.getNewBarcode(r), site,
                containerTypeMap.get("ChildCtL1"));
            top.addChild(0, 0, child);
            Assert.fail("position already occupied");
        } catch (BiobankCheckException bce) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testDelete() throws Exception {
        String name = "testDelete" + r.nextInt();
        addContainerHierarchy(containerMap.get("Top"));

        // add a aliquot to childL4
        List<SpecimenTypeWrapper> allSampleTypes = SpecimenTypeWrapper
            .getAllSpecimenTypes(appService, true);
        ContainerWrapper childL4 = containerMap.get("ChildL4");
        SpecimenTypeWrapper spcType = allSampleTypes.get(0);
        childL4.getContainerType().addToSpecimenTypeCollection(
            Arrays.asList(spcType));
        childL4.getContainerType().persist();
        CollectionEventWrapper ce = CollectionEventHelper.addCollectionEvent(
            site,
            PatientHelper.addPatient(Utils.getRandomString(5),
                StudyHelper.addStudy("tests")), 1);

        SpecimenWrapper parentSpc = SpecimenHelper.addSpecimen(spcType,
            ActivityStatus.ACTIVE, Utils.getRandomDate(),
            ce, site);

        PatientHelper.addPatient(
            Utils.getRandomString(5), StudyHelper.addStudy(name));

        ProcessingEventWrapper pe = ProcessingEventHelper.addProcessingEvent(
            site, Utils.getRandomDate());

        SpecimenWrapper spc = SpecimenHelper.addSpecimen(parentSpc, spcType,
            pe, childL4, 3, 3);

        // attempt to delete the containers - should fail
        String[] cnames = new String[] { "ChildL4", "ChildL3", "ChildL2",
            "ChildL1", "Top" };
        for (String cname : cnames) {
            ContainerWrapper container = containerMap.get(cname);
            container.reload();

            try {
                container.delete();
                Assert.fail("should not be allowed to delete container "
                    + "because it has child containers or samples: "
                    + container);
            } catch (Exception e) {
                Assert.assertTrue(true);
            }
        }

        // now delete again - should work this time
        spc.delete();
        for (String cname : cnames) {
            ContainerWrapper container = containerMap.get(cname);
            container.reload();
            container.delete();
        }
    }

    @Test
    public void testCheckParentFromSameSite() throws BiobankCheckException,
        Exception {
        String name = "testCheckParentFromSameSite" + r.nextInt();
        ContainerWrapper top = addContainerHierarchy(containerMap.get("Top"));

        SiteWrapper newSite = SiteHelper.addSite(name);
        ContainerTypeWrapper type = ContainerTypeHelper.addContainerType(
            newSite, name, "N", 1, 3, 5, false);
        ContainerWrapper newContainer = ContainerHelper.newContainer(null,
            name, top, newSite, type, 1, 1);
        try {
            newContainer.persist();
            Assert.fail("container not from same site that parent");
        } catch (BiobankSessionException e) {
            Assert.assertTrue(true);
        }
    }

    /**
     * Check the sub children are renamed too
     * 
     * @throws Exception
     */
    @Test
    public void testMoveRenamingSubChildren() throws Exception {
        ContainerWrapper top = containerMap.get("Top");

        ContainerWrapper child = ContainerHelper.newContainer(null,
            TestCommon.getNewBarcode(r), top, site,
            containerTypeMap.get("ChildCtL1"), 0, 0);
        top.persist();
        top.reload();

        ContainerWrapper child2 = ContainerHelper.newContainer(null,
            TestCommon.getNewBarcode(r), child, site,
            containerTypeMap.get("ChildCtL2"), 0, 0);
        child.persist();
        child.reload();

        String child2Label = child2.getLabel();
        String childLabel = child.getLabel();
        Assert.assertTrue(child2Label.startsWith(childLabel));
        String endChild2Label = child2Label.substring(childLabel.length());

        Assert.assertEquals(1, top.getChildren().size());

        top.addChild(0, 2, child);
        top.persist();
        top.reload();
        child.reload();
        child2.reload();
        Assert.assertEquals(1, top.getChildren().size());
        Assert.assertFalse(childLabel.equals(child.getLabel()));
        Assert.assertEquals(child.getLabel() + endChild2Label,
            child2.getLabel());
    }

    @Test
    public void testRenameParent() throws Exception {
        ContainerWrapper top = containerMap.get("Top");

        ContainerWrapper child = ContainerHelper.newContainer(null,
            TestCommon.getNewBarcode(r), top, site,
            containerTypeMap.get("ChildCtL1"), 0, 0);
        top.persist();
        top.reload();
        String childLabel = child.getLabel();
        Assert.assertTrue(childLabel.startsWith(top.getLabel()));
        String endChildLabel = childLabel.substring(top.getLabel().length());

        ContainerWrapper child2 = ContainerHelper.newContainer(null,
            TestCommon.getNewBarcode(r), child, site,
            containerTypeMap.get("ChildCtL2"), 0, 0);
        child.persist();
        child.reload();
        String child2Label = child2.getLabel();
        Assert.assertTrue(child2Label.startsWith(childLabel));
        String endChild2Label = child2Label.substring(childLabel.length());

        Assert.assertEquals(1, top.getChildren().size());

        top.setLabel("15");
        top.persist();
        top.reload();
        child.reload();
        child2.reload();
        Assert.assertFalse(childLabel.equals(child.getLabel()));
        Assert.assertEquals(top.getLabel() + endChildLabel, child.getLabel());
        Assert.assertFalse(child2Label.equals(child2.getLabel()));
        Assert.assertEquals(child.getLabel() + endChild2Label,
            child2.getLabel());
    }

    @Test
    public void testInitObjectWith() throws Exception {
        ContainerWrapper container = new ContainerWrapper(appService);
        try {
            container.initObjectWith(null);
            Assert
                .fail("should not be allowed to add initialize container with null wrapper");
        } catch (BiobankCheckException e) {
            Assert.assertTrue(true);
        }

        container.initObjectWith(containerMap.get("Top"));
    }

    @Test
    public void testGetChildCount() throws Exception {
        ContainerWrapper top = containerMap.get("Top");

        ContainerWrapper child = ContainerHelper.newContainer(null,
            TestCommon.getNewBarcode(r), site,
            containerTypeMap.get("ChildCtL1"));
        top.addChild(0, 0, child);
        ContainerWrapper child2 = ContainerHelper.newContainer(null,
            TestCommon.getNewBarcode(r), site,
            containerTypeMap.get("ChildCtL1"));
        top.addChild(0, 1, child2);
        top.persist();
        top.reload();
        top.getChildren(); // need to load into property map
        Assert.assertEquals(2, top.getChildCount(false));
        Assert.assertEquals(2, top.getChildCount(true));

        Assert.fail("test if not cached ?");
    }

    @Test
    public void testMoveSpecimens() throws Exception {
        ContainerWrapper top = containerMap.get("Top");

        ContainerTypeWrapper childType = ContainerTypeHelper.addContainerType(
            site, "Aliquot Container Type", "ACT", 1, 4, 9, false);
        childType.addToSpecimenTypeCollection(SpecimenTypeWrapper
            .getAllSpecimenTypes(appService, false));
        childType.persist();
        top.getContainerType().addToChildContainerTypeCollection(
            Arrays.asList(childType));
        top.getContainerType().persist();

        ContainerWrapper child = ContainerHelper.newContainer(null,
            TestCommon.getNewBarcode(r), site, childType);
        top.addChild(0, 0, child);
        ContainerWrapper child2 = ContainerHelper.newContainer(null,
            TestCommon.getNewBarcode(r), site, childType);
        top.addChild(0, 1, child2);
        top.persist();

        Assert.assertEquals(0, child.getSpecimens() == null ? 0 : child
            .getSpecimens().size());
        Assert.assertEquals(0, child2.getSpecimens() == null ? 0 : child2
            .getSpecimens().size());

        StudyWrapper study = StudyHelper.addStudy("Study1");
        ContactHelper.addContactsToStudy(study, "contactsStudy1");
        for (int i = 0; i < 3; i++) {
            SpecimenWrapper spc = SpecimenHelper.addParentSpecimen();
            childType.addToSpecimenTypeCollection(Arrays.asList(spc
                .getSpecimenType()));
            child.setContainerType(childType);
            child2.setContainerType(childType);
            child.addSpecimen(0, i, spc);
        }
        childType.persist();
        child.persist();
        child2.persist();

        child.reload();
        child2.reload();

        Assert.assertEquals(3, child.getSpecimens() == null ? 0 : child
            .getSpecimens().size());
        Assert.assertEquals(0, child2.getSpecimens() == null ? 0 : child2
            .getSpecimens().size());

        child.moveSpecimens(child2);
        child.reload();
        child2.reload();

        Assert.assertEquals(0, child.getSpecimens() == null ? 0 : child
            .getSpecimens().size());
        Assert.assertEquals(3, child2.getSpecimens() == null ? 0 : child2
            .getSpecimens().size());
    }

    @Test
    public void testGetEmptyContainersHoldingSampleType() throws Exception {
        ContainerWrapper top = containerMap.get("Top");

        List<SpecimenTypeWrapper> allSampleTypes = SpecimenTypeWrapper
            .getAllSpecimenTypes(appService, false);

        ContainerTypeWrapper childType = ContainerTypeHelper.addContainerType(
            site, "Aliquot Container Type", "ACT", 1, 4, 9, false);
        childType.addToSpecimenTypeCollection(Arrays.asList(allSampleTypes
            .get(0)));
        childType.persist();
        ContainerTypeWrapper childType2 = ContainerTypeHelper.addContainerType(
            site, "Aliquot Container Type2", "ACT2", 1, 4, 9, false);
        childType2.addToSpecimenTypeCollection(Arrays.asList(allSampleTypes
            .get(1)));
        childType2.persist();
        top.getContainerType().addToChildContainerTypeCollection(
            Arrays.asList(childType, childType2));
        top.getContainerType().persist();

        ContainerWrapper child = ContainerHelper.newContainer(null,
            TestCommon.getNewBarcode(r), site, childType);
        top.addChild(0, 0, child);
        ContainerWrapper child2 = ContainerHelper.newContainer(null,
            TestCommon.getNewBarcode(r), site, childType);
        top.addChild(0, 1, child2);
        ContainerWrapper child3 = ContainerHelper.newContainer(null,
            TestCommon.getNewBarcode(r), site, childType2);
        top.addChild(0, 2, child3);
        top.persist();

        List<ContainerWrapper> emptyContainers = ContainerWrapper
            .getEmptyContainersHoldingSpecimenType(appService, site,
                Arrays.asList(allSampleTypes.get(0)), 2, 2);
        Assert.assertEquals(2, emptyContainers.size());
        Assert.assertTrue(emptyContainers.contains(child));
        Assert.assertTrue(emptyContainers.contains(child2));

        emptyContainers = ContainerWrapper
            .getEmptyContainersHoldingSpecimenType(appService, site,
                Arrays.asList(allSampleTypes.get(1)), 2, 2);
        Assert.assertEquals(1, emptyContainers.size());
        Assert.assertTrue(emptyContainers.contains(child3));

        emptyContainers = ContainerWrapper
            .getEmptyContainersHoldingSpecimenType(appService, site,
                Arrays.asList(allSampleTypes.get(0)), 5, 2);
        Assert.assertEquals(0, emptyContainers.size());

        emptyContainers = ContainerWrapper
            .getEmptyContainersHoldingSpecimenType(appService, site,
                Arrays.asList(allSampleTypes.get(1)), 2, 10);
        Assert.assertEquals(0, emptyContainers.size());
    }

    @Test
    public void testIsContainerFull() throws Exception {
        ContainerWrapper top = containerMap.get("Top");

        Assert.assertFalse(top.isContainerFull());

        for (int row = 0; row < CONTAINER_TOP_ROWS; row++) {
            for (int col = 0; col < CONTAINER_TOP_COLS; col++) {
                ContainerWrapper child = ContainerHelper.newContainer(null,
                    TestCommon.getNewBarcode(r), site,
                    containerTypeMap.get("ChildCtL1"));
                top.addChild(row, col, child);
            }
        }
        top.persist();
        top.reload();

        Assert.assertTrue(top.isContainerFull());
    }

    /**
     * see ContainerAdapter.setNewPositionFromLabel
     * 
     * @throws Exception
     */
    @Test
    public void testMoveSameContainer() throws Exception {
        ContainerWrapper top = addContainerHierarchy(containerMap.get("Top"));
        ContainerWrapper child = containerMap.get("ChildL1"); // 01AA

        ContainerHelper.addContainer("02", TestCommon.getNewBarcode(r), site,
            containerTypeMap.get("TopCT"));

        String newLabel = "01AF";
        List<ContainerWrapper> newParentContainers = child
            .getPossibleParents(newLabel);
        Assert.assertEquals(1, newParentContainers.size());
        ContainerWrapper newParent = newParentContainers.get(0);
        Assert.assertEquals(top, newParent);

        newParent.addChild(newLabel.substring(newLabel.length() - 2), child);
        child.persist();
        child.reload();
        Assert.assertEquals(top, child.getParentContainer());
        Assert.assertEquals(newLabel, child.getLabel());
    }

    @Test
    public void testMoveSameContainer2() throws Exception {
        ContainerWrapper top = addContainerHierarchy(containerMap.get("Top"));
        ContainerWrapper child = containerMap.get("ChildL1"); // 01AA

        ContainerHelper.addContainer("02", TestCommon.getNewBarcode(r), site,
            containerTypeMap.get("TopCT"));

        top.addChild(2, 3, child);
        child.persist();

        // Use model object to be sure the DB is up-to-date
        Container container = new Container();
        container.setId(child.getId());
        List<Container> containersDB = appService.search(Container.class,
            container);
        Assert.assertEquals(1, containersDB.size());
        container = containersDB.get(0);
        ContainerPosition pos = container.getPosition();
        Assert.assertEquals(top.getId(), pos.getParentContainer().getId());
        Assert.assertEquals(2, pos.getRow().intValue());
        Assert.assertEquals(3, pos.getCol().intValue());
    }

    /**
     * see ContainerAdapter.setNewPositionFromLabel
     * 
     * @throws Exception
     */
    @Test
    public void testMoveOtherContainer() throws Exception {
        addContainerHierarchy(containerMap.get("Top"));
        ContainerWrapper child = containerMap.get("ChildL1"); // 01AA01

        ContainerWrapper top2 = ContainerHelper.addContainer("02",
            TestCommon.getNewBarcode(r), site, containerTypeMap.get("TopCT"));

        String newLabel = "02AF";
        List<ContainerWrapper> altParentContainers = child
            .getPossibleParents(newLabel);
        Assert.assertEquals(1, altParentContainers.size());
        ContainerWrapper altParent = altParentContainers.get(0);
        Assert.assertEquals(top2, altParent);

        top2.addChild(newLabel.substring(newLabel.length() - 2), child);
        top2.persist();
        top2.reload();
        Assert.assertEquals(top2, child.getParentContainer());
        Assert.assertEquals(newLabel, child.getLabel());
    }

    @Test
    public void testGetSpecificPositionWrapper() {
        Assert.fail("to be implemented");
        // internal but see why this is never called
    }

    @Test
    public void testCheckHasPosition() {
        Assert.fail("to be implemented");
        // see persist checks
    }

    @Test
    public void testCheckContainerTypeSameSite() {
        Assert.fail("to be implemented");
        // see persist checks
    }

    @Test
    public void testGetPositionString() {
        Assert.fail("to be implemented");
    }

    @Test
    public void testSetTopContainer() {
        Assert.fail("to be implemented");
        // 2 methods
    }

    @Test
    public void testMisc() {
        Assert
            .fail("see tests made after getSpecimens to check if it is null in addSpecimen and getSpecimen");
    }
}
