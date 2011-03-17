package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ualberta.med.biobank.common.wrappers.base.ReportBaseWrapper;
import edu.ualberta.med.biobank.model.EntityColumn;
import edu.ualberta.med.biobank.model.EntityFilter;
import edu.ualberta.med.biobank.model.Report;
import edu.ualberta.med.biobank.model.ReportColumn;
import edu.ualberta.med.biobank.model.ReportFilter;
import edu.ualberta.med.biobank.model.ReportFilterValue;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

public class ReportWrapper extends ReportBaseWrapper {
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_IS_COUNT = "isCount";
    public static final String PROPERTY_IS_PUBLIC = "isPublic";
    public static final String PROPERTY_USER_ID = "userId";
    public static final String PROPERTY_REPORT_COLUMN_COLLECTION = "reportColumnCollection";
    public static final String PROPERTY_REPORT_FILTER_COLLECTION = "reportFilterCollection";

    public ReportWrapper(WritableApplicationService appService, Report report) {
        super(appService, report);
    }

    public ReportWrapper(WritableApplicationService appService) {
        super(appService);
    }

    private static <E> Collection<E> notNull(Collection<E> collection) {
        if (collection == null) {
            return new ArrayList<E>();
        }
        return collection;
    }

    public ReportWrapper(ReportWrapper report) {
        super(report.getAppService());

        setName(report.wrappedObject.getName());
        setDescription(report.wrappedObject.getDescription());
        setIsCount(report.wrappedObject.getIsCount());
        setIsPublic(report.wrappedObject.getIsPublic());

        wrappedObject.setId(null);
        wrappedObject.setEntity(report.wrappedObject.getEntity());
        wrappedObject.setUserId(report.wrappedObject.getUserId());

        Collection<ReportColumn> reportColumns = new ArrayList<ReportColumn>();
        for (ReportColumn column : notNull(report.wrappedObject
            .getReportColumnCollection())) {
            ReportColumn columnCopy = new ReportColumn();
            columnCopy.setEntityColumn(column.getEntityColumn());
            columnCopy.setPosition(column.getPosition());
            columnCopy.setPropertyModifier(column.getPropertyModifier());

            reportColumns.add(columnCopy);
        }
        setReportColumnCollection(reportColumns);

        Collection<ReportFilter> reportFilters = new ArrayList<ReportFilter>();
        for (ReportFilter filter : notNull(report.wrappedObject
            .getReportFilterCollection())) {
            ReportFilter filterCopy = new ReportFilter();
            filterCopy.setEntityFilter(filter.getEntityFilter());
            filterCopy.setOperator(filter.getOperator());
            filterCopy.setPosition(filter.getPosition());

            Collection<ReportFilterValue> values = new ArrayList<ReportFilterValue>();
            for (ReportFilterValue value : notNull(filter
                .getReportFilterValueCollection())) {
                ReportFilterValue valueCopy = new ReportFilterValue();
                valueCopy.setPosition(value.getPosition());
                valueCopy.setValue(value.getValue());
                valueCopy.setSecondValue(value.getSecondValue());
            }
            filterCopy.setReportFilterValueCollection(values);

            reportFilters.add(filterCopy);
        }
        setReportFilterCollection(reportFilters);
    }

    public List<ReportColumn> getReportColumnCollection() {
        @SuppressWarnings("unchecked")
        List<ReportColumn> columns = (List<ReportColumn>) propertiesMap
            .get(PROPERTY_REPORT_COLUMN_COLLECTION);

        if (columns == null) {
            columns = new ArrayList<ReportColumn>();

            Collection<ReportColumn> rcc = wrappedObject
                .getReportColumnCollection();
            if (rcc != null) {
                columns.addAll(rcc);
            }

            Collections.sort(columns, new Comparator<ReportColumn>() {
                @Override
                public int compare(ReportColumn lhs, ReportColumn rhs) {
                    return lhs.getPosition() - rhs.getPosition();
                }
            });

            propertiesMap.put(PROPERTY_REPORT_COLUMN_COLLECTION, columns);
        }

        return columns;
    }

    public void setReportColumnCollection(Collection<ReportColumn> reportColumns) {
        Collection<ReportColumn> oldReportColumns = wrappedObject
            .getReportColumnCollection();

        Set<ReportColumn> newReportColumns = new HashSet<ReportColumn>();
        newReportColumns.addAll(reportColumns);

        wrappedObject.setReportColumnCollection(newReportColumns);
        propertiesMap.remove(PROPERTY_REPORT_COLUMN_COLLECTION);
        propertyChangeSupport.firePropertyChange(
            PROPERTY_REPORT_COLUMN_COLLECTION, oldReportColumns, reportColumns);
    }

    public List<ReportFilter> getReportFilterCollection() {
        @SuppressWarnings("unchecked")
        List<ReportFilter> filters = (List<ReportFilter>) propertiesMap
            .get(PROPERTY_REPORT_FILTER_COLLECTION);

        if (filters == null) {
            filters = new ArrayList<ReportFilter>();

            Collection<ReportFilter> rfc = wrappedObject
                .getReportFilterCollection();
            if (rfc != null) {
                filters.addAll(rfc);
            }

            Collections.sort(filters, new Comparator<ReportFilter>() {
                @Override
                public int compare(ReportFilter lhs, ReportFilter rhs) {
                    return lhs.getPosition() - rhs.getPosition();
                }
            });

            propertiesMap.put(PROPERTY_REPORT_FILTER_COLLECTION, filters);
        }

        return filters;
    }

    public void setReportFilterCollection(Collection<ReportFilter> reportFilters) {
        Collection<ReportFilter> oldReportFilters = wrappedObject
            .getReportFilterCollection();

        Set<ReportFilter> newReportFilters = new HashSet<ReportFilter>();
        newReportFilters.addAll(reportFilters);

        wrappedObject.setReportFilterCollection(newReportFilters);
        propertiesMap.remove(PROPERTY_REPORT_FILTER_COLLECTION);
        propertyChangeSupport.firePropertyChange(
            PROPERTY_REPORT_FILTER_COLLECTION, oldReportFilters, reportFilters);
    }

    public static Collection<String> getFilterValueStrings(
        ReportFilter reportFilter) {
        Collection<String> strings = new ArrayList<String>();
        Collection<ReportFilterValue> rfvCollection = reportFilter
            .getReportFilterValueCollection();

        if (rfvCollection != null) {
            for (ReportFilterValue rfv : rfvCollection) {
                strings.add(rfv.getValue());
            }
        }

        return strings;
    }

    public Collection<EntityColumn> getEntityColumnCollection() {
        return getEntity().getEntityColumnCollection();
    }

    public Collection<EntityFilter> getEntityFilterCollection() {
        return getEntity().getEntityFilterCollection();
    }

    @Override
    public Boolean getIsCount() {
        Boolean isCount = super.getIsCount();
        return isCount == null || !isCount ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public int compareTo(ModelWrapper<Report> o) {
        if (o instanceof ReportWrapper) {
            return getName().compareTo(((ReportWrapper) o).getName());
        }
        return 0;
    }
}
