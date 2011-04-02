package edu.ualberta.med.biobank.server.reports;

import java.util.ArrayList;
import java.util.List;

import edu.ualberta.med.biobank.common.formatters.DateFormatter;
import edu.ualberta.med.biobank.common.reports.BiobankReport;
import edu.ualberta.med.biobank.model.ContainerPath;
import edu.ualberta.med.biobank.model.ProcessingEvent;
import edu.ualberta.med.biobank.model.Specimen;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

public class QAAliquotsImpl extends AbstractReport {

    private static final String QUERY = ("FROM " + Specimen.class.getName() + " as s")
        + ("    inner join fetch s.collectionEvent ce")
        + ("    inner join fetch s.specimenType st")
        + ("    inner join fetch ce.patient p")
        + ("    inner join fetch s.specimenPosition sp")
        + ("    left join fetch s.parentSpecimen ps")
        + ("    left join fetch ps.processingEvent pe")
        + " WHERE s.createdAt between ? and ?"
        + "     and s.specimenType.nameShort = ?"
        + "     and s.specimenPosition.container.id in (SELECT path1.container.id"
        + ("        FROM " + ContainerPath.class.getName() + " as path1 ")
        + ("            ," + ContainerPath.class.getName() + " as path2 ")
        + "         WHERE path1.path like path2.path || '/%' "
        + ("             and path2.container.id in (" + CONTAINER_LIST + "))")
        + " ORDER BY RAND()";

    private int numResults;

    public QAAliquotsImpl(BiobankReport report) {
        super(QUERY, report);
        numResults = (Integer) report.getParams().remove(
            report.getParams().size() - 1);
    }

    @Override
    public List<Object> postProcess(WritableApplicationService appService,
        List<Object> results) {
        int lastIndex;
        if (results.size() != -1)
            lastIndex = Math.min(numResults, results.size());
        else
            lastIndex = numResults;
        if (lastIndex > 0) {
            results = results.subList(0, lastIndex);
        }
        List<Object> modifiedResults = new ArrayList<Object>();
        // get the info
        for (Object result : results) {
            Specimen specimen = (Specimen) result;
            String pnumber = specimen.getCollectionEvent().getPatient()
                .getPnumber();
            String inventoryId = specimen.getInventoryId();
            String specimenType = specimen.getSpecimenType().getNameShort();

            String dateProcessed = "No Date Processed";
            Specimen parentSpecimen = specimen.getParentSpecimen();
            if (parentSpecimen != null) {
                ProcessingEvent pe = parentSpecimen.getProcessingEvent();
                if (pe != null) {
                    dateProcessed = DateFormatter.formatAsDate(pe
                        .getCreatedAt());
                }
            }

            String positionString = specimen.getSpecimenPosition()
                .getPositionString();
            modifiedResults.add(new Object[] { positionString, inventoryId,
                pnumber, dateProcessed, specimenType });
        }
        return modifiedResults;
    }
}