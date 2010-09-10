package edu.ualberta.med.biobank.test.reports;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.util.Predicate;
import edu.ualberta.med.biobank.common.util.PredicateUtil;
import edu.ualberta.med.biobank.common.util.RowColPos;
import edu.ualberta.med.biobank.common.wrappers.AliquotWrapper;
import edu.ualberta.med.biobank.common.wrappers.ClinicShipmentWrapper;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContactWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientVisitWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import edu.ualberta.med.biobank.common.wrappers.SampleStorageWrapper;
import edu.ualberta.med.biobank.common.wrappers.SampleTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ShippingMethodWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.server.reports.AbstractReport;
import edu.ualberta.med.biobank.test.AllTests;
import edu.ualberta.med.biobank.test.internal.AliquotHelper;
import edu.ualberta.med.biobank.test.internal.ClinicHelper;
import edu.ualberta.med.biobank.test.internal.ClinicShipmentHelper;
import edu.ualberta.med.biobank.test.internal.ContactHelper;
import edu.ualberta.med.biobank.test.internal.ContainerHelper;
import edu.ualberta.med.biobank.test.internal.ContainerTypeHelper;
import edu.ualberta.med.biobank.test.internal.PatientHelper;
import edu.ualberta.med.biobank.test.internal.PatientVisitHelper;
import edu.ualberta.med.biobank.test.internal.SampleStorageHelper;
import edu.ualberta.med.biobank.test.internal.SampleTypeHelper;
import edu.ualberta.med.biobank.test.internal.ShippingMethodHelper;
import edu.ualberta.med.biobank.test.internal.SiteHelper;
import edu.ualberta.med.biobank.test.internal.SourceVesselHelper;
import edu.ualberta.med.biobank.test.internal.StudyHelper;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AliquotCountTest.class, AliquotInvoiceByClinicTest.class,
    AliquotInvoiceByPatientTest.class, AliquotRequestTest.class,
    AliquotsByPalletTest.class, AliquotSCountTest.class, CAliquotsTest.class,
    ContainerCapacityTest.class, ContainerEmptyLocationsTest.class,
    DAliquotsTest.class, FTAReportTest.class, FvLPatientVisitsTest.class,
    InvoicingReportTest.class, NewPsByStudyClinicTest.class,
    NewPVsByStudyClinicTest.class, PatientVisitSummaryTest.class,
    PatientWBCTest.class, PsByStudyTest.class, PVsByStudyTest.class,
    QAAliquotsTest.class, SAliquotsTest.class, SampleTypePvCountTest.class,
    SampleTypeSUsageTest.class })
public final class TestReportsProcedurallyGeneratedData implements
    ReportDataSource {
    private static TestReportsProcedurallyGeneratedData instance = null;

    private static final int NUM_SITES = 2;
    private static final int NUM_STUDIES_PER_SITE = 11;
    private static final int NUM_SAMPLE_TYPES = 2; // 5
    private static final int NUM_CLINICS = 7;
    private static final int NUM_CONTAINER_ROWS = 3; // 5
    private static final int NUM_CONTAINER_COLS = 3; // 5
    // the maximum ratio of "filled positions" / "total positions" for a
    // container; should be at least (int) 1 / (NUM_CONTAINER_ROWS *
    // NUM_CONTAINER_COLS)
    private static final double MAX_CONTAINER_CAPACITY = 0.2d; // 0.1d
    private static final int CONTAINER_DEPTH = 3;
    private static final int[] NUM_CONTACTS_PER_CLINIC = { 1, 2, 3 };
    private static final int[] NUM_CONTACTS_PER_STUDY = { 2, 2, 3 };
    private static final int PATIENTS_PER_STUDY = 11; // 17
    private static final int SHIPMENTS_PER_SITE = 23; // 43
    private static final int[] NUM_STUDIES_PER_SHIPMENT = { 2, 3, 4 };
    private static final int[] NUM_PATIENTS_PER_SHIPMENT = { 1, 2, 3 };
    private static final int NUM_SHIPMENTS_WITHOUT_PVS = 3;
    // leave every xth Container position empty
    private static final int SKIP_ALIQUOT = 2; // 10

    private final WritableApplicationService appService;
    private final Random random = new Random(1); // consistent randomness (;

    private final List<SiteWrapper> sites = new ArrayList<SiteWrapper>();
    private final List<SampleTypeWrapper> sampleTypes = new ArrayList<SampleTypeWrapper>();
    private final List<SampleStorageWrapper> sampleStorages = new ArrayList<SampleStorageWrapper>();
    private final List<AliquotWrapper> aliquots = new ArrayList<AliquotWrapper>();
    private final List<ContainerWrapper> containers = new ArrayList<ContainerWrapper>();
    private final List<StudyWrapper> studies = new ArrayList<StudyWrapper>();
    private final List<PatientVisitWrapper> patientVisits = new ArrayList<PatientVisitWrapper>();
    private final List<PatientWrapper> patients = new ArrayList<PatientWrapper>();

    private TestReportsProcedurallyGeneratedData() {
        try {
            AllTests.setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        appService = AllTests.appService;
        Assert.assertNotNull("setUp: appService is null", appService);
    }

    public static TestReportsProcedurallyGeneratedData getInstance() {
        if (instance == null) {
            synchronized (TestReportsProcedurallyGeneratedData.class) {
                // this is needed if two threads are waiting at the monitor at
                // the time when singleton was getting instantiated
                if (instance == null) {
                    instance = new TestReportsProcedurallyGeneratedData();
                }
            }
        }

        return instance;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    private static List<SiteWrapper> generateSites(final int numSites)
        throws Exception {
        List<SiteWrapper> sites = new ArrayList<SiteWrapper>();
        for (int i = 0; i < numSites; i++) {
            sites.add(SiteHelper.addSite(getInstance().getRandString()));
        }
        return sites;
    }

    private static List<StudyWrapper> generateStudies(final int numStudies)
        throws Exception {
        List<StudyWrapper> studies = new ArrayList<StudyWrapper>();

        for (int studyIndex = 0; studyIndex < numStudies; studyIndex++) {
            StudyWrapper study = StudyHelper.addStudy(getInstance()
                .getRandString());

            study.persist();
            study.reload();

            studies.add(study);
        }

        return studies;
    }

    private static List<SampleTypeWrapper> generateSampleTypes(
        final int numSampleTypes) throws Exception {
        List<SampleTypeWrapper> sampleTypes = new ArrayList<SampleTypeWrapper>();

        for (int i = 0; i < numSampleTypes; i++) {
            sampleTypes.add(SampleTypeHelper.addSampleType(getInstance()
                .getRandString()));
        }

        try {
            // add a sample type with the name
            // "AbstractReport.FTA_CARD_SAMPLE_TYPE_NAME" as some reports query
            // for this specific sample type; however, this sample type may
            // already exist
            SampleTypeHelper
                .addSampleType(AbstractReport.FTA_CARD_SAMPLE_TYPE_NAME);
        } catch (Exception e) {
        }

        for (SampleTypeWrapper sampleType : SampleTypeWrapper
            .getAllSampleTypes(getInstance().getAppService(), true)) {
            if (sampleType.getName().equals(
                AbstractReport.FTA_CARD_SAMPLE_TYPE_NAME)) {
                sampleTypes.add(sampleType);
            }
        }

        // ensure there is one sample type named
        // "AbstractReport.FTA_CARD_SAMPLE_TYPE_NAME"
        Assert.assertTrue(PredicateUtil.filter(sampleTypes,
            new Predicate<SampleTypeWrapper>() {
                public boolean evaluate(SampleTypeWrapper sampleType) {
                    return sampleType.getName().equals(
                        AbstractReport.FTA_CARD_SAMPLE_TYPE_NAME);
                }

            }).size() == 1);

        return sampleTypes;
    }

    private static List<ClinicWrapper> generateClinics(final int numClinics)
        throws Exception {
        List<ClinicWrapper> clinics = new ArrayList<ClinicWrapper>();
        for (int i = 0; i < numClinics; i++) {
            clinics.add(ClinicHelper.addClinic(getInstance().getRandString()));
        }
        return clinics;
    }

    private static List<ContactWrapper> generateContacts(
        List<ClinicWrapper> clinics, List<StudyWrapper> studies)
        throws Exception {
        List<ContactWrapper> contacts = new ArrayList<ContactWrapper>();

        int clinicIndex = 0;
        int numContactsPerClinic;
        for (ClinicWrapper clinic : clinics) {
            // cycle through different numbers of Contact-s per Clinic
            numContactsPerClinic = NUM_CONTACTS_PER_CLINIC[clinicIndex
                % NUM_CONTACTS_PER_CLINIC.length];

            for (int contactIndex = 0; contactIndex < numContactsPerClinic; contactIndex++) {
                contacts.add(ContactHelper.addContact(clinic, getInstance()
                    .getRandString()));
            }

            // need to reload clinic-contact relationship
            clinic.reload();
            clinicIndex++;
        }

        int contactsAdded = 0;
        int numContactsPerStudy;

        for (int studyIndex = 0, numStudies = studies.size(); studyIndex < numStudies; studyIndex++) {
            StudyWrapper study = studies.get(studyIndex);
            // cycle through different numbers of contacts to add per study.
            numContactsPerStudy = NUM_CONTACTS_PER_STUDY[studyIndex
                % NUM_CONTACTS_PER_STUDY.length];

            // don't try to add more contacts than we have
            numContactsPerStudy = Math
                .min(numContactsPerStudy, contacts.size());

            for (int j = 0; j < numContactsPerStudy; j++) {
                // cycle through contacts
                ContactWrapper contact = contacts.get(contactsAdded
                    % contacts.size());

                study.addContacts(Arrays.asList(contact));

                contactsAdded++;
            }

            study.persist();
        }

        for (ContactWrapper contact : contacts) {
            // need to reload contact-study relationship
            contact.reload();
        }

        return contacts;
    }

    /**
     * Generate various possibilities of good ContainerType-s, since the
     * generation of Container-s can then depend solely on these
     * ContainerType-s.
     * 
     * @param site which Site to add the ContainerType-s to
     * @param parentContainerType which ContainerType to add children
     *            ContainerType-s to
     * @param sampleTypes valid SampleType-s for the lowest/ bottom level
     * @param height total height of the ContainerType tree
     * @return all children, sub-children, sub-sub-children, etc. generated
     * @throws BiobankCheckException
     * @throws Exception
     */
    private static List<ContainerTypeWrapper> generateChildContainerTypes(
        SiteWrapper site, ContainerTypeWrapper parentContainerType,
        List<SampleTypeWrapper> sampleTypes, final int height)
        throws BiobankCheckException, Exception {

        if (height == 0) {
            return Arrays.asList();
        }

        List<ContainerTypeWrapper> childContainerTypes = new ArrayList<ContainerTypeWrapper>();

        final boolean isTopLevel = parentContainerType == null;

        if (height == 1) {
            // bottom level
            ContainerTypeWrapper containerType;
            // generate a ContainerType that can hold one SampleType, for each
            // possible SampleType
            for (int i = 0, numSampleTypes = sampleTypes.size(); i < numSampleTypes; i++) {
                containerType = addContainerType(site, getInstance()
                    .getRandString(), isTopLevel);

                containerType.addSampleTypes(sampleTypes.subList(i, i + 1));

                childContainerTypes.add(containerType);
            }

            // also generate a ContainerType that can hold every SampleType
            containerType = addContainerType(site, getInstance()
                .getRandString(), isTopLevel);
            containerType.addSampleTypes(sampleTypes);
            childContainerTypes.add(containerType);
        } else {
            // middle- or top-level

            // add a ContainerType for each parent
            childContainerTypes.add(addContainerType(site, getInstance()
                .getRandString(), isTopLevel));

            // add a top-level ContainerType with "Cabinet" in it since some
            // reports look for this
            if (isTopLevel) {
                childContainerTypes.add(addContainerType(site, "Cabinet"
                    + getInstance().getRandString(), isTopLevel));
            }
        }

        if (parentContainerType != null) {
            parentContainerType.addChildContainerTypes(childContainerTypes);
        }

        List<ContainerTypeWrapper> descendantContainerTypes = new ArrayList<ContainerTypeWrapper>();

        for (ContainerTypeWrapper containerType : childContainerTypes) {
            descendantContainerTypes.addAll(generateChildContainerTypes(site,
                containerType, sampleTypes, height - 1));

            // persist and reload after all its children have been generated
            containerType.persist();
            containerType.reload();
        }

        childContainerTypes.addAll(descendantContainerTypes);

        return childContainerTypes;
    }

    private static ContainerTypeWrapper addContainerType(SiteWrapper site,
        String nameShort, boolean isTopLevel) throws BiobankCheckException,
        Exception {
        // TODO: vary the ContainerLabelingScheme at all?
        return ContainerTypeHelper.addContainerType(site, nameShort + "LONG",
            nameShort, 1, NUM_CONTAINER_ROWS, NUM_CONTAINER_COLS, isTopLevel);
    }

    /**
     * 
     * @param site which Site to add the ContainerType-s to
     * @param parentContainer the parent Container. If null, will create "some"
     *            of each top-level ContainerType
     * @param allContainerTypes every ContainerType to consider (especially
     *            imporant to include top-level ContainerType-s)
     * @return ALL Container-s generated (including sub-containers of the
     *         sub-containers of the ... etc.)
     * @throws Exception
     */
    private static List<ContainerWrapper> generateContainers(SiteWrapper site,
        ContainerWrapper parentContainer,
        List<ContainerTypeWrapper> allContainerTypes) throws Exception {
        List<ContainerWrapper> containers = new ArrayList<ContainerWrapper>();

        if (parentContainer == null) {
            // top-level Container
            // add a new Container for each top ContainerType and
            // possible label prefix

            // start some Container-s' label with "SS" (sent samples) as
            // these Container-s are to be ignored by some reports/ queries.
            // note that the top level Container label will determine the
            // childrens' label
            String[] labelPrefixes = { "", "SS" };

            for (String labelPrefix : labelPrefixes) {
                for (ContainerTypeWrapper containerType : allContainerTypes) {
                    if (containerType.getTopLevel()) {
                        containers.add(ContainerHelper.addContainer(labelPrefix
                            + getInstance().getRandString(), null,
                            parentContainer, site, containerType));
                    }
                }
            }
        } else {
            // non top-level Container
            ContainerTypeWrapper parentContainerType = parentContainer
                .getContainerType();

            ContainerTypeWrapper childContainerType;
            List<ContainerTypeWrapper> childContainerTypes = parentContainerType
                .getChildContainerTypeCollection();

            if (childContainerTypes.size() > 0) {
                // middle-level (not a top- or bottom-level Container): can add
                // child Container-s until limit breached
                int containersAdded = 0;
                int numRows = parentContainerType.getRowCapacity();
                int numCols = parentContainerType.getColCapacity();
                int totalCapacity = numRows * numCols;

                for (int row = 0; row < numRows; row++) {
                    for (int col = 0; col < numCols; col++) {
                        // don't breach some limit, otherwise we will generate
                        // WAY TOO MANY Container-s
                        if (totalCapacity * MAX_CONTAINER_CAPACITY <= containersAdded) {
                            break;
                        }

                        // cycle through child ContainerType-s
                        childContainerType = childContainerTypes
                            .get(containersAdded % childContainerTypes.size());

                        containers.add(ContainerHelper
                            .addContainer(null, null, parentContainer, site,
                                childContainerType, row, col));

                        containersAdded++;
                    }
                }
            }
        }

        List<ContainerWrapper> descendantContainers = new ArrayList<ContainerWrapper>();

        // add children Container-s for each container we just added
        for (ContainerWrapper container : containers) {
            container.reload();
            descendantContainers.addAll(generateContainers(site, container,
                allContainerTypes));

            container.persist();
            container.reload();
        }

        containers.addAll(descendantContainers);

        return containers;
    }

    private static List<SampleStorageWrapper> generateSampleStorages(
        List<StudyWrapper> studies, List<SampleTypeWrapper> sampleTypes)
        throws Exception {
        List<SampleStorageWrapper> sampleStorages = new ArrayList<SampleStorageWrapper>();

        for (StudyWrapper study : studies) {
            // leave the first SampleType unassociated with any Study via
            // SampleStorage since at least one report checks for these
            for (int sampleTypeIndex = 1, numSampleTypes = sampleTypes.size(); sampleTypeIndex < numSampleTypes; sampleTypeIndex++) {
                SampleTypeWrapper type = sampleTypes.get(sampleTypeIndex);
                SampleStorageWrapper sampleStorage = SampleStorageHelper
                    .addSampleStorage(study, type);
                sampleStorages.add(sampleStorage);
            }
        }
        return sampleStorages;
    }

    private static List<PatientWrapper> generatePatients(
        List<StudyWrapper> studies, final int numPatients) throws Exception {
        List<PatientWrapper> patients = new ArrayList<PatientWrapper>();
        for (int i = 0; i < numPatients; i++) {
            // leave one Study without any Patient-s
            for (int studyIndex = 1, numStudies = studies.size(); studyIndex < numStudies; studyIndex++) {
                StudyWrapper study = studies.get(studyIndex);
                // add 1 patient to each study, then another to each study,
                // etc. so that the list of patients is not grouped by study
                PatientWrapper patient = PatientHelper.addPatient(getInstance()
                    .getRandString(), study);
                patients.add(patient);
            }
        }
        return patients;
    }

    private static List<ClinicShipmentWrapper> generateShipments(
        SiteWrapper site, final int shipmentLimit, List<ClinicWrapper> clinics)
        throws ApplicationException, Exception {
        List<ClinicShipmentWrapper> shipments = new ArrayList<ClinicShipmentWrapper>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(0));

        // keep track of the next study and patient index as we travel through
        // the same collections multiple times (e.g. we don't want to add the
        // same first 2 of 5 patients in a study every time, and never the last
        // 2)
        Map<Integer, Integer> nextStudyIndex = new HashMap<Integer, Integer>();
        Map<List<Integer>, Integer> nextPatientIndex = new HashMap<List<Integer>, Integer>();

        int clinicIndex = 0;
        int shipmentsAdded = 0;
        int studiesPerShipmentCounter = 0;
        int patientsPerShipmentCounter = 0;

        while (shipmentsAdded < shipmentLimit) {
            ClinicWrapper clinic = clinics.get(clinicIndex);
            List<StudyWrapper> studies = clinic.getStudyCollection();

            Integer tmp = nextStudyIndex.get(clinicIndex);
            int studyIndex = tmp != null ? tmp : 0;

            // cycle through number of studies per shipment
            int studiesAdded = 0;
            int studyLimit = NUM_STUDIES_PER_SHIPMENT[studiesPerShipmentCounter++
                % NUM_STUDIES_PER_SHIPMENT.length];
            studyLimit = Math.min(studyLimit, studies.size());

            while ((shipmentsAdded < shipmentLimit)
                && (studiesAdded < studyLimit)) {
                StudyWrapper study = studies.get(studyIndex);
                List<PatientWrapper> patients = study.getPatientCollection();

                tmp = nextPatientIndex.get(Arrays.asList(clinicIndex,
                    studyIndex));
                int patientIndex = tmp != null ? tmp : 0;

                // cycle through number of patients per shipment
                int patientsAdded = 0;
                int patientLimit = NUM_PATIENTS_PER_SHIPMENT[patientsPerShipmentCounter++
                    % NUM_PATIENTS_PER_SHIPMENT.length];
                patientLimit = Math.min(patientLimit, patients.size());

                if (patientLimit > 0) {
                    ClinicShipmentWrapper shipment = ClinicShipmentHelper
                        .addShipment(site, clinic, ShippingMethodWrapper
                            .getShippingMethods(getInstance().getAppService())
                            .get(0), patients.get(patientIndex));

                    // TODO: more appropriate Date-s?
                    shipment.setDateShipped(calendar.getTime());
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    shipment.setDateReceived(calendar.getTime());

                    patientsAdded++;
                    patientIndex = (patientIndex + 1) % patients.size();

                    while (patientsAdded++ < patientLimit) {
                        PatientWrapper patient = patients.get(patientIndex);
                        shipment.addPatients(Arrays.asList(patient));

                        // advance to the next legal Patient index
                        patientIndex = (patientIndex + 1) % patients.size();
                    }

                    nextPatientIndex.put(
                        Arrays.asList(clinicIndex, studyIndex), patientIndex);

                    shipmentsAdded++;
                    studiesAdded++;

                    shipments.add(shipment);

                    shipment.persist();
                    shipment.reload();
                }

                // advance to the next legal Study index
                studyIndex = (studyIndex + 1) % studies.size();
            }

            nextStudyIndex.put(clinicIndex, studyIndex);

            // advance to the next legal Clinic index
            clinicIndex = (clinicIndex + 1) % clinics.size();
        }

        return shipments;
    }

    private static List<PatientVisitWrapper> generatePatientVisits(
        List<ClinicShipmentWrapper> shipments, List<PatientWrapper> allPatients)
        throws Exception {
        List<PatientVisitWrapper> patientVisits = new ArrayList<PatientVisitWrapper>();

        Calendar calendar = Calendar.getInstance();

        for (int shipmentIndex = 0, numShipments = shipments.size()
            - NUM_SHIPMENTS_WITHOUT_PVS; shipmentIndex < numShipments; shipmentIndex++) {
            ClinicShipmentWrapper shipment = shipments.get(shipmentIndex);
            List<PatientWrapper> patients = shipment.getPatientCollection();

            for (PatientWrapper patient : patients) {
                calendar.setTime(shipment.getDateShipped());
                calendar.add(Calendar.DAY_OF_YEAR, -1);

                // TODO: more appropriate Date-s?
                Date drawn = calendar.getTime();
                calendar.add(Calendar.DAY_OF_YEAR, 2);
                Date processed = calendar.getTime();

                PatientVisitWrapper patientVisit = PatientVisitHelper
                    .addPatientVisit(patient, shipment, drawn, processed);

                patientVisits.add(patientVisit);
            }
        }

        // reload all patients since Shipment-s have been added involving them
        // as have PatientVisit-s
        for (PatientWrapper patient : allPatients) {
            patient.reload();
        }

        return patientVisits;
    }

    private static List<AliquotWrapper> generateAliquots(
        List<ContainerWrapper> containers,
        List<PatientVisitWrapper> patientVisits,
        List<SampleTypeWrapper> allSampleTypes) throws Exception {
        List<AliquotWrapper> aliquots = new ArrayList<AliquotWrapper>();

        // ignore the last PatientVisit
        Assert.assertTrue(patientVisits.size() > 1);
        patientVisits = patientVisits.subList(0, patientVisits.size() - 1);

        Calendar calendar = Calendar.getInstance();

        int aliquotsAdded = 0;
        for (ContainerWrapper container : containers) {
            List<SampleTypeWrapper> sampleTypes = container.getContainerType()
                .getSampleTypeCollection();
            if ((sampleTypes != null) && (sampleTypes.size() > 0)) {
                for (int row = 0, numRows = container.getRowCapacity(); row < numRows; row++) {
                    for (int col = 0, numCols = container.getColCapacity(); col < numCols; col++) {
                        // cycle through sample types
                        SampleTypeWrapper sampleType = sampleTypes
                            .get(aliquotsAdded % sampleTypes.size());

                        // cycle through patient visits
                        PatientVisitWrapper patientVisit = patientVisits
                            .get(aliquotsAdded % patientVisits.size());

                        AliquotWrapper aliquot = AliquotHelper
                            .newAliquot(sampleType);

                        // leave some positions without an Aliquot (but still
                        // add the Aliquot since having some Aliquot-s without
                        // parent containers is also useful)
                        if (aliquotsAdded % SKIP_ALIQUOT != 0) {
                            aliquot.setParent(container);
                            aliquot.setPosition(new RowColPos(row, col));
                        }

                        aliquot.setPatientVisit(patientVisit);
                        aliquot.setInventoryId(getInstance().getRandString());

                        // base the link date on the date the patient visit
                        // is processed
                        calendar.setTime(patientVisit.getDateProcessed());
                        calendar.add(Calendar.MINUTE, 10);

                        aliquot.setLinkDate(calendar.getTime());
                        aliquot.persist();
                        aliquot.reload();

                        aliquots.add(aliquot);

                        aliquotsAdded++;
                    }
                }
            }

            container.reload();
        }

        // add an Aliquot of each SampleType that is not in a Container
        for (SampleTypeWrapper sampleType : allSampleTypes) {
            // cycle through patient visits
            PatientVisitWrapper patientVisit = patientVisits.get(aliquotsAdded
                % patientVisits.size());

            AliquotWrapper aliquot = AliquotHelper.newAliquot(sampleType);

            aliquot.setPatientVisit(patientVisit);
            aliquot.setInventoryId(getInstance().getRandString());

            // base the link date on the date the patient visit
            // is processed
            calendar.setTime(patientVisit.getDateProcessed());
            calendar.add(Calendar.MINUTE, 10);

            aliquot.setLinkDate(calendar.getTime());
            aliquot.persist();
            aliquot.reload();

            aliquots.add(aliquot);

            aliquotsAdded++;
        }

        return aliquots;
    }

    @BeforeClass
    public static void setUp() throws Exception {
        ReportDataSource dataSource;

        // if the database is blank before we run any tests, we could use
        // ourself as the ReportDataSource; however, if SampleType-s and other
        // data exist, we should query the database to get EVERYTHING.
        // Otherwise, our expected results will come from only a subset of the
        // true data
        //
        // dataSource = getInstance(); // <-- BAD IF DATABASE NOT EMPTY TO START
        dataSource = new CachedReportDataSource(getInstance().getAppService());

        AbstractReportTest.setReportDataSource(dataSource);

        // TODO: sublist a lot of what we pass in here so that some clinics,
        // contacts, studies, etc. are left unpopulated with other data?

        List<SiteWrapper> sites = generateSites(NUM_SITES);
        List<StudyWrapper> studies = generateStudies(NUM_STUDIES_PER_SITE);
        List<SampleTypeWrapper> sampleTypes = generateSampleTypes(NUM_SAMPLE_TYPES);
        List<ClinicWrapper> clinics = generateClinics(NUM_CLINICS);

        for (SiteWrapper site : sites) {

            generateContacts(clinics, studies);

            List<ContainerTypeWrapper> containerTypes = generateChildContainerTypes(
                site, null, sampleTypes, CONTAINER_DEPTH);

            List<ContainerWrapper> containers = generateContainers(site, null,
                containerTypes);
            List<SampleStorageWrapper> sampleStorages = generateSampleStorages(
                studies, sampleTypes);
            List<PatientWrapper> patients = generatePatients(studies,
                PATIENTS_PER_STUDY);

            List<ClinicShipmentWrapper> shipments = generateShipments(site,
                SHIPMENTS_PER_SITE, clinics);

            List<PatientVisitWrapper> patientVisits = generatePatientVisits(
                shipments, patients);

            List<AliquotWrapper> aliquots = generateAliquots(containers,
                patientVisits, sampleTypes);

            getInstance().sampleStorages.addAll(sampleStorages);
            getInstance().aliquots.addAll(aliquots);
            getInstance().containers.addAll(containers);
            getInstance().patientVisits.addAll(patientVisits);
            getInstance().patients.addAll(patients);
        }

        getInstance().sites.addAll(sites);
        getInstance().sampleTypes.addAll(sampleTypes);
        getInstance().studies.addAll(studies);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            StudyHelper.deleteCreatedStudies();
            SiteHelper.deleteCreatedSites();
            ClinicHelper.deleteCreatedClinics();
            SampleTypeHelper.deleteCreatedSampleTypes();
            SourceVesselHelper.deleteCreatedSourceVessels();
            ShippingMethodHelper.deleteCreateShippingMethods();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            Assert.fail();
        }
    }

    public WritableApplicationService getAppService() {
        return appService;
    }

    public final String getRandString() {
        return new BigInteger(130, random).toString(32);
    }

    public final Random getRandom() {
        return random;
    }

    public List<SiteWrapper> getSites() {
        return sites;
    }

    public List<SampleTypeWrapper> getSampleTypes() {
        return sampleTypes;
    }

    public List<AliquotWrapper> getAliquots() {
        return aliquots;
    }

    public List<ContainerWrapper> getContainers() {
        return containers;
    }

    public List<StudyWrapper> getStudies() {
        return studies;
    }

    public List<PatientVisitWrapper> getPatientVisits() {
        return patientVisits;
    }

    public List<PatientWrapper> getPatients() {
        return patients;
    }

    public List<SampleStorageWrapper> getSampleStorages() {
        return sampleStorages;
    }
}
