package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.model.Entity;
import edu.ualberta.med.biobank.model.EntityColumn;
import edu.ualberta.med.biobank.model.EntityFilter;
import edu.ualberta.med.biobank.model.EntityProperty;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class EntityWrapper extends ModelWrapper<Entity> {
    public static final Comparator<Entity> ORDER_BY_NAME = new Comparator<Entity>() {
        @Override
        public int compare(Entity lhs, Entity rhs) {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    };

    private Collection<EntityProperty> properties;
    private Collection<EntityFilter> filters;
    private Collection<EntityColumn> columns;

    public EntityWrapper(WritableApplicationService appService, Entity entity) {
        super(appService, entity);
    }

    public Collection<EntityProperty> getEntityPropertyCollection() {
        if (properties == null) {
            Collection<EntityProperty> properties = new ArrayList<EntityProperty>();
            Collection<EntityProperty> epc = wrappedObject
                .getEntityPropertyCollection();

            if (epc != null) {
                properties.addAll(epc);
            }

            this.properties = properties;
        }
        return properties;
    }

    public Collection<EntityFilter> getEntityFilterCollection() {
        if (filters == null) {
            List<EntityFilter> filters = new ArrayList<EntityFilter>();

            for (EntityProperty entityProperty : getEntityPropertyCollection()) {
                filters.addAll(getEntityFilterCollection(entityProperty));
            }

            this.filters = filters;
        }
        return filters;
    }

    public Collection<EntityColumn> getEntityColumnCollection() {
        if (columns == null) {
            List<EntityColumn> columns = new ArrayList<EntityColumn>();

            for (EntityProperty entityProperty : getEntityPropertyCollection()) {
                columns.addAll(getEntityColumnCollection(entityProperty));
            }

            this.columns = columns;
        }
        return columns;
    }

    public String getName() {
        return wrappedObject.getName();
    }

    private static Collection<EntityColumn> getEntityColumnCollection(
        EntityProperty entityProperty) {
        Collection<EntityColumn> columns = new ArrayList<EntityColumn>();
        Collection<EntityColumn> ecc = entityProperty
            .getEntityColumnCollection();

        if (ecc != null) {
            columns.addAll(ecc);
        }

        return columns;
    }

    private static Collection<EntityFilter> getEntityFilterCollection(
        EntityProperty entityProperty) {
        Collection<EntityFilter> filters = new ArrayList<EntityFilter>();
        Collection<EntityFilter> efc = entityProperty
            .getEntityFilterCollection();

        if (efc != null) {
            filters.addAll(efc);
        }

        return filters;
    }

    public static Collection<Entity> getEntities(
        WritableApplicationService appService, Comparator<Entity> comparator) {
        List<Entity> entities = new ArrayList<Entity>();
        HQLCriteria criteria = new HQLCriteria("from " + Entity.class.getName());

        try {
            List<Entity> results = appService.query(criteria);
            entities.addAll(results);

            if (comparator != null) {
                Collections.sort(entities, comparator);
            }
        } catch (ApplicationException e) {
            e.printStackTrace();
        }

        return entities;
    }

    @Override
    protected String[] getPropertyChangeNames() {
        return new String[] {};
    }

    @Override
    public Class<Entity> getWrappedClass() {
        return Entity.class;
    }

    @Override
    protected void persistChecks() throws BiobankCheckException,
        ApplicationException, WrapperException {
    }

    @Override
    protected void deleteChecks() throws Exception {
    }
}
