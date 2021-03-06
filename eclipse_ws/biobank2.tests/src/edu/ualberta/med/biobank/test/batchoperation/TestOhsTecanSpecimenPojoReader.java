package edu.ualberta.med.biobank.test.batchoperation;

import java.io.FileReader;

import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import edu.ualberta.med.biobank.common.action.batchoperation.specimen.SpecimenBatchOpInputPojo;
import edu.ualberta.med.biobank.common.action.exception.BatchOpErrorsException;
import edu.ualberta.med.biobank.common.batchoperation.IBatchOpPojoReader;
import edu.ualberta.med.biobank.common.batchoperation.specimen.OhsTecanSpecimenPojoReader;
import edu.ualberta.med.biobank.common.batchoperation.specimen.SpecimenPojoReaderFactory;
import edu.ualberta.med.biobank.model.Specimen;
import edu.ualberta.med.biobank.model.SpecimenType;
import edu.ualberta.med.biobank.test.action.TestAction;
import edu.ualberta.med.biobank.test.action.batchoperation.CsvUtil;
import edu.ualberta.med.biobank.test.action.batchoperation.specimen.TestSpecimenBatchOp;

public class TestOhsTecanSpecimenPojoReader extends TestAction {

    private static Logger log = LoggerFactory
        .getLogger(TestSpecimenBatchOp.class.getName());

    private Transaction tx;

    @Test
    public void processTecanFile() throws Exception {
        createDbConfiguration();

        final String CSV_NAME = "tecan/ohs_tecan.csv";
        ICsvBeanReader reader =
            new CsvBeanReader(new FileReader(CSV_NAME),
                CsvPreference.EXCEL_PREFERENCE);

        String[] csvHeaders = reader.getCSVHeader(true);

        Assert.assertTrue(csvHeaders.length > 0);

        IBatchOpPojoReader<SpecimenBatchOpInputPojo> pojoReader =
            SpecimenPojoReaderFactory.createPojoReader(
                factory.getDefaultSite(), CSV_NAME, csvHeaders);

        Assert.assertTrue(pojoReader instanceof OhsTecanSpecimenPojoReader);

        pojoReader.readPojos(reader);

        try {
            exec(pojoReader.getAction());
        } catch (BatchOpErrorsException e) {
            CsvUtil.showErrorsInLog(log, e);
            Assert.fail("errors in CVS data: " + e.getMessage());
        }
    }

    private void createDbConfiguration() {
        final String spcInventoryId = "OHSTO1405EDT1";
        final String spcTypeName = "Buffy";

        tx = session.beginTransaction();

        factory.createSite();
        factory.createClinic();
        factory.createStudy();

        // create specimen type Buffy if it does not already exist
        SpecimenType stype =
            (SpecimenType) session.createCriteria(SpecimenType.class)
                .add(Restrictions.eq("name", spcTypeName)).uniqueResult();

        if (stype == null) {
            stype = new SpecimenType();
            stype.setName(spcTypeName);
            stype.setNameShort(spcTypeName);
            session.save(stype);
        }

        factory.createPatient();

        Specimen parentSpecimen =
            (Specimen) session.createCriteria(Specimen.class)
                .add(Restrictions.eq("inventoryId", spcInventoryId))
                .uniqueResult();
        if (parentSpecimen != null) {
            CsvUtil.deleteSpecimen(session, parentSpecimen);
        }

        parentSpecimen = factory.createParentSpecimen();
        parentSpecimen.setInventoryId(spcInventoryId);
        session.save(parentSpecimen);

        tx.commit();
    }
}
