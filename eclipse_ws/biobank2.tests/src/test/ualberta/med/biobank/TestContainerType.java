package test.ualberta.med.biobank;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import test.ualberta.med.biobank.internal.ContainerTypeHelper;
import test.ualberta.med.biobank.internal.SiteHelper;
import edu.ualberta.med.biobank.common.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.model.ContainerType;

public class TestContainerType extends TestDatabase {
    private static final int CONTAINER_TOP_ROWS = 5;

    private static final int CONTAINER_TOP_COLS = 9;

    private static final int CONTAINER_CHILD_L3_ROWS = 8;

    private static final int CONTAINER_CHILD_L3_COLS = 12;

    private Map<String, ContainerTypeWrapper> containerTypeMap;

    private SiteWrapper site;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        containerTypeMap = new HashMap<String, ContainerTypeWrapper>();
        site = SiteHelper.addSite("Site - Container Test"
            + Utils.getRandomString(10));
        addTopContainerType(site);
    }

    private void addTopContainerType(SiteWrapper site) throws Exception {
        ContainerTypeWrapper topType;
        topType = ContainerTypeHelper.addContainerType(site,
            "Top Container Type", "TCT", 2, CONTAINER_TOP_ROWS,
            CONTAINER_TOP_COLS, true);
        containerTypeMap.put("TopCT", topType);
    }

    private ContainerTypeWrapper addContainerTypeHierarchy(
        ContainerTypeWrapper topType) throws BiobankCheckException, Exception {
        ContainerTypeWrapper childType;

        childType = ContainerTypeHelper.addContainerType(site,
            "Child L3 Container Type", "CCTL3", 4, CONTAINER_CHILD_L3_ROWS,
            CONTAINER_CHILD_L3_COLS, false);
        containerTypeMap.put("ChildCtL3", childType);

        childType = ContainerTypeHelper.newContainerType(site,
            "Child L2 Container Type", "CCTL2", 1, 1, 10, false);
        childType.setChildContainerTypeCollection(Arrays
            .asList(containerTypeMap.get("ChildCtL3")));
        childType.persist();
        containerTypeMap.put("ChildCtL2", childType);

        childType = ContainerTypeHelper.newContainerType(site,
            "Child L1 Container Type", "CCTL1", 3, 1, 10, false);
        childType.setChildContainerTypeCollection(Arrays
            .asList(containerTypeMap.get("ChildCtL2")));
        childType.persist();
        containerTypeMap.put("ChildCtL1", childType);

        topType.setChildContainerTypeCollection(Arrays.asList(containerTypeMap
            .get("ChildCtL1")));
        topType.persist();
        topType.reload();
        return topType;
    }

    @Test
    public void testGettersAndSetters() throws Exception {
        testGettersAndSetters(containerTypeMap.get("TopCT"));
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
    public void testNameUnique() throws Exception {
        ContainerTypeWrapper topType2;

        // use same name as containerTypeMap.get("TopCT")
        topType2 = ContainerTypeHelper.addContainerType(site,
            "Top Container Type", "TCT", 3, CONTAINER_TOP_ROWS + 1,
            CONTAINER_TOP_COLS - 1, true);

        try {
            topType2.persist();
            Assert
                .fail("should not be allowed to add container type because of duplicate name");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

    }

    @Test
    public void testGetAllChildren() {
        fail("Not yet implemented");
    }

    @Test
    public void testIsUsedByContainers() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetParentContainers() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetSampleTypeCollection() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetSampleTypeCollectionRecursive() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetChildContainerTypeCollection() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetSite() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetCapacity() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetChildLabelingSchemeName() {
        fail("Not yet implemented");
    }

    @Test
    public void testCanRemoveChildrenContainer() {
        fail("Not yet implemented");
    }

    @Test
    public void testCheckNewCapacity() {
        fail("Not yet implemented");
    }

    @Test
    public void testCheckTopLevel() {
        fail("Not yet implemented");
    }

    @Test
    public void testCheckLabelingScheme() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetTopContainerTypesInSite() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetContainerTypesInSite() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetAllLabelingSchemes() {
        fail("Not yet implemented");
    }

}
