package edu.ualberta.med.biobank.common.action.csvimport;

import edu.ualberta.med.biobank.common.action.specimen.SpecimenActionHelper;
import edu.ualberta.med.biobank.model.ActivityStatus;
import edu.ualberta.med.biobank.model.Center;
import edu.ualberta.med.biobank.model.CollectionEvent;
import edu.ualberta.med.biobank.model.Container;
import edu.ualberta.med.biobank.model.OriginInfo;
import edu.ualberta.med.biobank.model.Patient;
import edu.ualberta.med.biobank.model.Specimen;
import edu.ualberta.med.biobank.model.SpecimenType;
import edu.ualberta.med.biobank.model.util.RowColPos;

public class SpecimenImportInfo {
    private SpecimenCsvInfo csvInfo;
    private SpecimenImportInfo parentInfo;
    private Patient patient;
    private CollectionEvent cevent;
    private Specimen parentSpecimen;
    private Center originCenter;
    private Center currentCenter;
    private SpecimenType specimenType;
    private Container container;
    private RowColPos specimenPos;

    SpecimenImportInfo(SpecimenCsvInfo csvInfo) {
        this.setCsvInfo(csvInfo);
    }

    public int getLineNumber() {
        return csvInfo.getLineNumber();
    }

    public SpecimenCsvInfo getCsvInfo() {
        return csvInfo;
    }

    public SpecimenImportInfo getParentInfo() {
        return parentInfo;
    }

    public void setParentInfo(SpecimenImportInfo parentInfo) {
        this.parentInfo = parentInfo;
    }

    public void setCsvInfo(SpecimenCsvInfo csvInfo) {
        this.csvInfo = csvInfo;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public CollectionEvent getCevent() {
        return cevent;
    }

    public void setCevent(CollectionEvent cevent) {
        this.cevent = cevent;
    }

    public Specimen getParentSpecimen() {
        return parentSpecimen;
    }

    public void setParentSpecimen(Specimen parentSpecimen) {
        this.parentSpecimen = parentSpecimen;
    }

    public String getParentInventoryID() {
        return csvInfo.getParentInventoryID();
    }

    public Center getOriginCenter() {
        return originCenter;
    }

    public void setOriginCenter(Center originCenter) {
        this.originCenter = originCenter;
    }

    public Center getCurrentCenter() {
        return currentCenter;
    }

    public void setCurrentCenter(Center currentCenter) {
        this.currentCenter = currentCenter;
    }

    public SpecimenType getSpecimenType() {
        return specimenType;
    }

    public void setSpecimenType(SpecimenType specimenType) {
        this.specimenType = specimenType;
    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public RowColPos getSpecimenPos() {
        return specimenPos;
    }

    public void setSpecimenPos(RowColPos specimenPos) {
        this.specimenPos = specimenPos;
    }

    public boolean isAliquotedSpecimen() {
        return (csvInfo.getParentInventoryID() != null)
            && !csvInfo.getParentInventoryID().isEmpty();
    }

    public CollectionEvent createCollectionEvent() {
        cevent = new CollectionEvent();
        cevent.setPatient(patient);
        cevent.setVisitNumber(csvInfo.getVisitNumber());
        cevent.setActivityStatus(ActivityStatus.ACTIVE);
        patient.getCollectionEvents().add(cevent);
        return cevent;
    }

    @SuppressWarnings("nls")
    public Specimen getSpecimen() {
        // add the specimen to the collection event
        OriginInfo oi = new OriginInfo();
        oi.setCenter(originCenter);

        Specimen spc = new Specimen();
        spc.setOriginInfo(oi);
        spc.setCurrentCenter(currentCenter);
        spc.setActivityStatus(ActivityStatus.ACTIVE);
        spc.setCollectionEvent(cevent);
        spc.setOriginalCollectionEvent(cevent);
        spc.setCreatedAt(csvInfo.getCreatedAt());
        spc.setInventoryId(csvInfo.getInventoryId());
        spc.setSpecimenType(specimenType);

        if (parentSpecimen == null) {
            throw new IllegalStateException(
                "parent specimen has not be created yet");
        }

        SpecimenActionHelper.setParent(spc, parentSpecimen);
        SpecimenActionHelper.setQuantityFromType(spc);

        if (container != null) {
            SpecimenActionHelper.createOrChangePosition(spc, container,
                specimenPos);
        }

        if (!isAliquotedSpecimen()) {
            cevent.getOriginalSpecimens().add(spc);
        }
        cevent.getAllSpecimens().add(spc);
        return spc;
    }
}
