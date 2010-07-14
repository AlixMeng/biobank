package edu.ualberta.med.biobank.server.reports;

import java.text.MessageFormat;
import java.util.List;

import edu.ualberta.med.biobank.common.util.AbstractRowPostProcess;
import edu.ualberta.med.biobank.common.util.DateRangeRowPostProcess;
import edu.ualberta.med.biobank.common.util.ReportOption;
import edu.ualberta.med.biobank.model.PatientVisit;

public class NewPVsByStudyClinicImpl extends AbstractReport {

    private static final String QUERY = "Select Alias.patient.study.nameShort, "
        + "Alias.shipment.clinic.name, Year(Alias.dateProcessed), "
        + "{0}(Alias.dateProcessed), count(*) from "
        + PatientVisit.class.getName()
        + " as Alias where Alias.patient.study.site "
        + SITE_OPERATOR
        + SITE_ID
        + " GROUP BY Alias.patient.study.nameShort, Alias.shipment.clinic.name, "
        + "Year(Alias.dateProcessed), {0}(Alias.dateProcessed)";

    private DateRangeRowPostProcess dateRangePostProcess;

    public NewPVsByStudyClinicImpl(List<Object> parameters,
        List<ReportOption> options) {
        super(QUERY, parameters, options);
        for (int i = 0; i < options.size(); i++) {
            ReportOption option = options.get(i);
            if (parameters.get(i) == null)
                parameters.set(i, option.getDefaultValue());
            if (option.getType().equals(String.class))
                parameters.set(i, "%" + parameters.get(i) + "%");
        }
        String groupBy = (String) parameters.remove(0);
        queryString = MessageFormat.format(queryString, groupBy);
        dateRangePostProcess = new DateRangeRowPostProcess(
            groupBy.equals("Year"), 2);
    }

    @Override
    protected AbstractRowPostProcess getRowPostProcess() {
        return dateRangePostProcess;
    }

}