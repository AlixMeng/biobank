package edu.ualberta.med.biobank.server.reports;

import java.util.ArrayList;
import java.util.List;

import edu.ualberta.med.biobank.common.formatters.DateFormatter;
import edu.ualberta.med.biobank.common.reports.BiobankReport;
import edu.ualberta.med.biobank.model.Specimen;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

public class FTAReportImpl extends AbstractReport {
    private static final String QUERY = "SELECT s2"
        + (" FROM " + Specimen.class.getName() + " s2")
        + ("    inner join fetch s2.collectionEvent ce")
        + ("    inner join fetch ce.patient p")
        + ("    inner join fetch s2.topSpecimen ts")
        + ("    inner join fetch s2.specimenType st")
        + ("    inner join fetch s2.currentCenter c")
        + ("    inner join fetch s2.specimenPosition pos")
        + " WHERE s2.id = (SELECT min(s.id) "
        + ("        FROM " + Specimen.class.getName() + " s")
        + "         WHERE s.collectionEvent.visitNumber = 1"
        + "             and s.collectionEvent = s2.collectionEvent"
        + "             and s.collectionEvent.patient.study.nameShort = ?"
        + ("            and s.specimenType.nameShort = '"
            + FTA_CARD_SAMPLE_TYPE_NAME + "'")
        + "             and s.topSpecimen.createdAt > ?"
        + ("            and s.specimenPosition.container.label not like '"
            + SENT_SAMPLES_FREEZER_NAME + "'") + ")"
        + " ORDER BY s2.collectionEvent.patient.pnumber";

    public FTAReportImpl(BiobankReport report) {
        super(QUERY, report);
    }

    @Override
    public List<Object> postProcess(WritableApplicationService appService,
        List<Object> results) {
        ArrayList<Object> modifiedResults = new ArrayList<Object>();

        for (Object result : results) {
            Specimen specimen = (Specimen) result;

            String pnumber = specimen.getCollectionEvent().getPatient()
                .getPnumber();
            String inventoryId = specimen.getInventoryId();
            String dateDrawn = DateFormatter.formatAsDate(specimen
                .getTopSpecimen().getCreatedAt());
            String specimenType = specimen.getSpecimenType().getNameShort();
            String currentCenter = specimen.getCurrentCenter().getNameShort();
            String positionString = specimen.getSpecimenPosition()
                .getPositionString();

            modifiedResults.add(new Object[] { pnumber, dateDrawn, inventoryId,
                specimenType, currentCenter, positionString });
        }
        return modifiedResults;
    }
}