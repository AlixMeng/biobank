package edu.ualberta.med.biobank.test.internal;

import java.util.ArrayList;
import java.util.List;

import edu.ualberta.med.biobank.common.wrappers.SourceSpecimenWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.test.Utils;

public class SourceSpecimenHelper extends DbHelper {

    public static SourceSpecimenWrapper newSourceSpecimen(StudyWrapper study,
        SpecimenTypeWrapper spcType, boolean needTimeDrawn,
        boolean needOriginalVolume) {
        SourceSpecimenWrapper ssv = new SourceSpecimenWrapper(appService);
        ssv.setStudy(study);
        ssv.setSpecimenType(spcType);
        ssv.setNeedTimeDrawn(needTimeDrawn);
        ssv.setNeedOriginalVolume(needOriginalVolume);
        return ssv;
    }

    public static SourceSpecimenWrapper addSourceSpecimen(StudyWrapper study,
        SpecimenTypeWrapper svType, boolean needTimeDrawn,
        boolean needOriginalVolume) throws Exception {
        SourceSpecimenWrapper ssv = newSourceSpecimen(study, svType,
            needTimeDrawn, needOriginalVolume);
        ssv.persist();
        return ssv;
    }

    public static SourceSpecimenWrapper addSourceSpecimen(StudyWrapper study,
        String spcTypeName, boolean needTimeDrawn, boolean needOriginalVolume)
        throws Exception {
        SpecimenTypeWrapper svType = SpecimenTypeHelper.addSpecimenType("newST"
            + Utils.getRandomString(11));
        SourceSpecimenWrapper ssv = newSourceSpecimen(study, svType,
            needTimeDrawn, needOriginalVolume);
        ssv.persist();
        return ssv;
    }

    public static int addSourceSpecimens(StudyWrapper study, String name,
        boolean needTimeDrawn, boolean needOriginalVolume) throws Exception {
        int nber = r.nextInt(15) + 1;
        List<SourceSpecimenWrapper> ssvs = new ArrayList<SourceSpecimenWrapper>();
        for (int i = 0; i < nber; i++) {
            SpecimenTypeWrapper svType = SpecimenTypeHelper
                .addSpecimenType("newST" + Utils.getRandomString(11));
            ssvs.add(addSourceSpecimen(study, svType, needTimeDrawn,
                needOriginalVolume));
        }
        study.addToSourceSpecimenCollection(ssvs);
        study.persist();
        return nber;
    }
}
