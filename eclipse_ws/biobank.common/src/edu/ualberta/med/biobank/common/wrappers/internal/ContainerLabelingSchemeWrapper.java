package edu.ualberta.med.biobank.common.wrappers.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.model.ContainerLabelingScheme;
import edu.ualberta.med.biobank.model.ContainerType;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class ContainerLabelingSchemeWrapper extends
    ModelWrapper<ContainerLabelingScheme> {
    private static List<ContainerLabelingScheme> allSchemes;

    public ContainerLabelingSchemeWrapper(
        WritableApplicationService appService,
        ContainerLabelingScheme wrappedObject) {
        super(appService, wrappedObject);
    }

    public ContainerLabelingSchemeWrapper(WritableApplicationService appService) {
        super(appService);
    }

    @Override
    public Class<ContainerLabelingScheme> getWrappedClass() {
        return ContainerLabelingScheme.class;
    }

    @Override
    protected String[] getPropertyChangeNames() {
        return new String[] { "name", "maxRows", "maxCols", "maxCapacity" };
    }

    public void setName(String name) {
        String oldName = wrappedObject.getName();
        wrappedObject.setName(name);
        propertyChangeSupport.firePropertyChange("name", oldName, name);
    }

    public String getName() {
        return wrappedObject.getName();
    }

    public void setMaxRows(Integer maxRows) {
        Integer oldMaxRows = wrappedObject.getMaxRows();
        wrappedObject.setMaxRows(maxRows);
        propertyChangeSupport.firePropertyChange("name", oldMaxRows, maxRows);
    }

    public Integer getMaxRows() {
        return wrappedObject.getMaxRows();
    }

    public void setMaxCols(Integer maxCols) {
        Integer oldMaxCols = wrappedObject.getMaxCols();
        wrappedObject.setMaxCols(maxCols);
        propertyChangeSupport.firePropertyChange("name", oldMaxCols, maxCols);
    }

    public Integer getMaxCols() {
        return wrappedObject.getMaxCols();
    }

    public void setMaxCapacity(Integer maxCapacity) {
        Integer oldMaxCapacity = wrappedObject.getMaxCapacity();
        wrappedObject.setMaxCapacity(maxCapacity);
        propertyChangeSupport.firePropertyChange("name", oldMaxCapacity,
            maxCapacity);
    }

    public Integer getMaxCapacity() {
        return wrappedObject.getMaxCapacity();
    }

    @Override
    protected void deleteChecks() throws BiobankCheckException,
        ApplicationException {
        if (hasContainerTypes()) {
            throw new BiobankCheckException(
                "Can't delete this ContainerLabelingScheme: container types are using it.");
        }
    }

    @Override
    protected void persistChecks() throws BiobankCheckException,
        ApplicationException {
    }

    private boolean hasContainerTypes() throws ApplicationException {
        HQLCriteria criteria = new HQLCriteria("from "
            + ContainerType.class.getName() + " where childLabelingScheme=?",
            Arrays.asList(new Object[] { wrappedObject }));
        List<ContainerType> types = appService.query(criteria);
        return types.size() > 0;
    }

    public static List<ContainerLabelingSchemeWrapper> getAllLabelingSchemes(
        WritableApplicationService appService) throws ApplicationException {
        if (allSchemes == null) {
            allSchemes = appService.search(ContainerLabelingScheme.class,
                new ContainerLabelingScheme());
        }
        return transformToWrapperList(appService, allSchemes);
    }

    private static List<ContainerLabelingSchemeWrapper> transformToWrapperList(
        WritableApplicationService appService,
        List<ContainerLabelingScheme> schemes) {
        List<ContainerLabelingSchemeWrapper> list = new ArrayList<ContainerLabelingSchemeWrapper>();
        for (ContainerLabelingScheme scheme : schemes) {
            list.add(new ContainerLabelingSchemeWrapper(appService, scheme));
        }
        return list;
    }

    public static Map<Integer, ContainerLabelingSchemeWrapper> getAllLabelingSchemesMap(
        WritableApplicationService appService) throws ApplicationException {
        List<ContainerLabelingSchemeWrapper> allLabelingSchemes = getAllLabelingSchemes(appService);
        Map<Integer, ContainerLabelingSchemeWrapper> allLabelingSchemesMap = new HashMap<Integer, ContainerLabelingSchemeWrapper>();

        for (ContainerLabelingSchemeWrapper scheme : allLabelingSchemes) {
            allLabelingSchemesMap.put(scheme.getId(), scheme);
        }

        return allLabelingSchemesMap;
    }

    @Override
    public int compareTo(ModelWrapper<ContainerLabelingScheme> o) {
        return 0;
    }

    @Override
    public void persist() throws Exception {
        super.persist();
        allSchemes = null;
    }

    @Override
    public void delete() throws Exception {
        super.delete();
        allSchemes = null;
    }
}
