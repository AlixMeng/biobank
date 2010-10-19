package edu.ualberta.med.biobank.server.reports;

import edu.ualberta.med.biobank.common.reports.BiobankReport;
import edu.ualberta.med.biobank.model.Aliquot;
import edu.ualberta.med.biobank.model.AliquotPosition;

public class AliquotSCountImpl extends AbstractReport {

    private static final String QUERY =
        "Select Alias.patientVisit.shipmentPatient.patient.study.nameShort,"
            + " Alias.sampleType.name, count(*) from "
            + Aliquot.class.getName()
            + " as Alias left join Alias.aliquotPosition p where (p is null or p not in (from "
            + AliquotPosition.class.getName()
            + " a where a.container.label like '"
            + SENT_SAMPLES_FREEZER_NAME
            + "')) and Alias.linkDate between ? and ? and Alias.patientVisit.shipmentPatient.shipment.site "
            + SITE_OPERATOR
            + SITE_ID
            + " GROUP BY Alias.patientVisit.shipmentPatient.patient.study.nameShort, Alias.sampleType.name";

    public AliquotSCountImpl(BiobankReport report) {
        super(QUERY, report);
    }

}
