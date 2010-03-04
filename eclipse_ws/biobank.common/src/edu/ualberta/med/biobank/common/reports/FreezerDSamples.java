package edu.ualberta.med.biobank.common.reports;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import edu.ualberta.med.biobank.model.ContainerPath;
import edu.ualberta.med.biobank.model.Sample;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class FreezerDSamples extends QueryObject {

    protected static final String NAME = "Freezer Samples per Study per Clinic by Date";

    protected static final String query = "select sample.patientVisit.patient.study.nameShort, sample.patientVisit.shipment.clinic.name , year(sample.linkDate), {2}(sample.linkDate), count(sample.linkDate) from "
        + Sample.class.getName()
        + " as sample where sample.samplePosition.container.id in (select path1.container.id from "
        + ContainerPath.class.getName()
        + " as path1, "
        + ContainerPath.class.getName()
        + " as path2 where locate(path2.path, path1.path) > 0 and path2.container.containerType.name like ?) and sample.patientVisit.patient.study.site {0} {1}"
        + " group by sample.patientVisit.patient.study.nameShort, sample.patientVisit.shipment.clinic.name,  year(sample.linkDate), {2}(sample.linkDate)";

    public FreezerDSamples(String op, Integer siteId) {
        super(
            "Displays the total number of freezer samples per study per clinic by date range.",
            MessageFormat.format(query, op, siteId, "{0}"), new String[] {
                "Study", "Clinic", "", "Total" });
        addOption("Date Range", DateRange.class, DateRange.Week);
    }

    @Override
    public List<Object> executeQuery(WritableApplicationService appService,
        List<Object> params) throws ApplicationException {
        for (int i = 0; i < queryOptions.size(); i++) {
            Option option = queryOptions.get(i);
            if (params.get(i) == null)
                params.set(i, option.getDefaultValue());
            if (option.type.equals(String.class))
                params.set(i, "%" + params.get(i) + "%");
        }
        columnNames[2] = (String) params.get(0);
        queryString = MessageFormat.format(queryString, columnNames[2]);
        params.set(0, "%Freezer%");
        HQLCriteria c = new HQLCriteria(queryString);
        c.setParameters(params);
        List<Object> results = appService.query(c);
        return postProcess(results);
    }

    @Override
    public List<Object> postProcess(List<Object> results) {
        List<Object> compressedDates = new ArrayList<Object>();
        if (columnNames[2].compareTo("Year") == 0) {
            for (Object ob : results) {
                Object[] castOb = (Object[]) ob;
                compressedDates.add(new Object[] { castOb[0], castOb[1],
                    castOb[3], castOb[4] });
            }
        } else {
            for (Object ob : results) {
                Object[] castOb = (Object[]) ob;
                compressedDates.add(new Object[] { castOb[0], castOb[1],
                    castOb[3] + "(" + castOb[2] + ")", castOb[4] });
            }
        }
        return compressedDates;
    }

    @Override
    public String getName() {
        return NAME;
    }
}