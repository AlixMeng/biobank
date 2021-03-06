package edu.ualberta.med.biobank.common.action.reports;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Session;

import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.action.IdResult;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.action.reports.ReportInput.ReportColumnInput;
import edu.ualberta.med.biobank.common.action.reports.ReportInput.ReportFilterInput;
import edu.ualberta.med.biobank.common.action.reports.ReportInput.ReportFilterValueInput;
import edu.ualberta.med.biobank.common.permission.reports.ReportsPermission;
import edu.ualberta.med.biobank.model.Entity;
import edu.ualberta.med.biobank.model.EntityColumn;
import edu.ualberta.med.biobank.model.EntityFilter;
import edu.ualberta.med.biobank.model.PropertyModifier;
import edu.ualberta.med.biobank.model.Report;
import edu.ualberta.med.biobank.model.ReportColumn;
import edu.ualberta.med.biobank.model.ReportFilter;
import edu.ualberta.med.biobank.model.ReportFilterValue;
import edu.ualberta.med.biobank.model.User;

public class AdvancedReportSaveAction implements Action<IdResult> {

    private static final long serialVersionUID = 1L;

    private final ReportInput info;

    public AdvancedReportSaveAction(ReportInput input) {
        this.info = input;
    }

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        return new ReportsPermission().isAllowed(context);
    }

    @Override
    public IdResult run(ActionContext context) throws ActionException {
        Session session = context.getSession();
        Report report = context.load(Report.class, info.getReportId(), new Report());

        report.setName(info.getName());
        report.setDescription(info.getDescription());
        report.setIsCount(info.isCount());
        report.setIsPublic(info.isPublic());

        Entity entity = context.load(Entity.class, info.getEntityId());
        report.setEntity(entity);

        User user = context.load(User.class, info.getUserId());
        report.setUser(user);
        session.saveOrUpdate(report);

        // delete old report columns
        for (ReportColumn column : report.getReportColumns()) {
            session.delete(column);
        }
        report.getReportColumns().clear();

        Set<ReportColumn> reportColumns = new HashSet<ReportColumn>();
        for (ReportColumnInput columnInput : info.getReportColumnInputs()) {
            ReportColumn column = new ReportColumn();
            column.setPosition(columnInput.getPosition());

            Integer propertyModifierId = columnInput.getPropertyModifierId();

            if (propertyModifierId != null) {
                PropertyModifier modifier = context.load(
                    PropertyModifier.class, columnInput.getPropertyModifierId());

                column.setPropertyModifier(modifier);
            }

            EntityColumn entityColumn = context.load(
                EntityColumn.class, columnInput.getEntityColumnId());

            column.setEntityColumn(entityColumn);
            column.setReport(report);
            session.save(column);
            reportColumns.add(column);
        }

        report.getReportColumns().addAll(reportColumns);

        // delete old report filters
        for (ReportFilter filter : report.getReportFilters()) {
            for (ReportFilterValue filterValue : filter.getReportFilterValues()) {
                session.delete(filterValue);
            }
            session.delete(filter);
        }
        report.getReportFilters().clear();

        Set<ReportFilter> reportFilters = new HashSet<ReportFilter>();
        for (ReportFilterInput filterInput : info.getReportFilterInputs()) {
            ReportFilter filter = new ReportFilter();
            filter.setPosition(filterInput.getPosition());
            filter.setOperator(filterInput.getOperator());
            filter.setReport(report);

            EntityFilter entityFilter = context.load(
                EntityFilter.class, filterInput.getEntityFilterId());
            filter.setEntityFilter(entityFilter);
            session.save(filter);

            Set<ReportFilterValue> filterValues = new HashSet<ReportFilterValue>();

            for (ReportFilterValueInput filterValueInput : filterInput.getFilterValueInputs()) {
                ReportFilterValue filterValue = new ReportFilterValue();
                filterValue.setPosition(filterValueInput.getPosition());
                filterValue.setValue(filterValueInput.getValue());
                filterValue.setSecondValue(filterValueInput.getSecondValue());
                filterValue.setReportFilter(filter);
                session.save(filterValue);
                filterValues.add(filterValue);
            }
            filter.getReportFilterValues().clear();
            filter.getReportFilterValues().addAll(filterValues);
            session.saveOrUpdate(filter);
            reportFilters.add(filter);
        }

        report.getReportFilters().addAll(reportFilters);
        session.saveOrUpdate(report);

        return new IdResult(report.getId());
    }
}
