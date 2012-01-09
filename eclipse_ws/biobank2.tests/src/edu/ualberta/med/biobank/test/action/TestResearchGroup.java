package edu.ualberta.med.biobank.test.action;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import edu.ualberta.med.biobank.common.action.activityStatus.ActivityStatusEnum;
import edu.ualberta.med.biobank.common.action.collectionEvent.CollectionEventGetInfoAction;
import edu.ualberta.med.biobank.common.action.collectionEvent.CollectionEventGetInfoAction.CEventInfo;
import edu.ualberta.med.biobank.common.action.info.RequestFormReadInfo;
import edu.ualberta.med.biobank.common.action.info.ResearchGroupFormReadInfo;
import edu.ualberta.med.biobank.common.action.request.RequestGetInfoAction;
import edu.ualberta.med.biobank.common.action.researchGroup.ResearchGroupGetInfoAction;
import edu.ualberta.med.biobank.common.action.researchGroup.SubmitRequestAction;
import edu.ualberta.med.biobank.common.action.specimen.SpecimenInfo;
import edu.ualberta.med.biobank.model.RequestSpecimen;
import edu.ualberta.med.biobank.test.action.helper.CollectionEventHelper;
import edu.ualberta.med.biobank.test.action.helper.PatientHelper;
import edu.ualberta.med.biobank.test.action.helper.ResearchGroupHelper;
import edu.ualberta.med.biobank.test.action.helper.StudyHelper;

public class TestResearchGroup extends TestAction {

    @Rule
    public TestName testname = new TestName();

    private String name;
    private Integer studyId;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        name = testname.getMethodName() + r.nextInt();
        studyId =
            StudyHelper
                .createStudy(appService, name, ActivityStatusEnum.ACTIVE);
    }

    @Test
    public void saveResearchGroup() throws Exception {

        Integer rgId =
            ResearchGroupHelper.createResearchGroup(appService, name,
                name,
                studyId);
        ResearchGroupGetInfoAction reader =
            new ResearchGroupGetInfoAction(rgId);
        ResearchGroupFormReadInfo rg = appService.doAction(reader);

        Assert.assertTrue(rg.rg.name.equals(name + "rg"));
        Assert.assertTrue(rg.rg.nameShort.equals(name + "rg"));
        Assert.assertTrue(rg.rg.getStudy().getId().equals(studyId));
        Assert.assertTrue(rg.rg.getActivityStatus().id
            .equals(ActivityStatusEnum.ACTIVE.getId()));

    }

    @Test
    public void testUpload() throws Exception {
        Integer rgId =
            ResearchGroupHelper.createResearchGroup(appService, name + "rg",
                name + "rg",
                studyId);
        ResearchGroupGetInfoAction reader =
            new ResearchGroupGetInfoAction(rgId);
        ResearchGroupFormReadInfo rg = appService.doAction(reader);

        // create specs
        Integer p =
            PatientHelper.createPatient(appService, name + "_patient",
                rg.rg.getStudy().getId());
        Integer ceId =
            CollectionEventHelper.createCEventWithSourceSpecimens(appService,
                p, rgId);

        CollectionEventGetInfoAction ceReader =
            new CollectionEventGetInfoAction(ceId);
        CEventInfo ceInfo = appService.doAction(ceReader);
        List<String> specs = new ArrayList<String>();
        for (SpecimenInfo specInfo : ceInfo.sourceSpecimenInfos)
            specs.add(specInfo.specimen.getInventoryId());

        Assert.assertTrue(ceInfo.sourceSpecimenInfos.size() >= 2);
        specs.remove(Math.abs(r.nextInt()) % specs.size());
        specs.remove(Math.abs(r.nextInt()) % specs.size());

        // request specs
        SubmitRequestAction action =
            new SubmitRequestAction(rgId, specs);
        Integer rId = appService.doAction(action).getId();

        // make sure you got what was requested
        RequestGetInfoAction requestGetInfoAction =
            new RequestGetInfoAction(rId);
        RequestFormReadInfo rInfo = appService.doAction(requestGetInfoAction);

        for (RequestSpecimen spec : rInfo.specimens) {
            Assert.assertTrue(specs.contains(spec.getSpecimen()
                .getInventoryId()));
        }
    }
}
