package edu.ualberta.med.biobank.action.site;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;

import edu.ualberta.med.biobank.action.Action;
import edu.ualberta.med.biobank.action.ActionContext;
import edu.ualberta.med.biobank.action.ListResult;
import edu.ualberta.med.biobank.action.exception.ActionException;
import edu.ualberta.med.biobank.action.info.StudyCountInfo;
import edu.ualberta.med.biobank.model.Site;
import edu.ualberta.med.biobank.model.Study;

public class SiteGetStudyInfoAction implements
    Action<ListResult<StudyCountInfo>> {
    private static final long serialVersionUID = 1L;
    // @formatter:off
    @SuppressWarnings("nls")
    private static final String STUDY_INFO_HQL = "SELECT studies, COUNT(DISTINCT patients), COUNT(DISTINCT collectionEvents)"
        + " FROM " + Site.class.getName() + " site"
        + " INNER JOIN site.studies AS studies"
        + " LEFT JOIN studies.patients AS patients"
        + " LEFT JOIN patients.collectionEvents AS collectionEvents"
        + " WHERE site.id = ?"
        + " GROUP BY studies"
        + " ORDER BY studies.nameShort";
    // @formatter:on

    private final Integer siteId;

    public SiteGetStudyInfoAction(Integer siteId) {
        this.siteId = siteId;
    }

    public SiteGetStudyInfoAction(Site site) {
        this(site.getId());
    }

    @Override
    public boolean isAllowed(ActionContext context) {
        return true;
    }

    @Override
    public ListResult<StudyCountInfo> run(ActionContext context)
        throws ActionException {
        ArrayList<StudyCountInfo> studies = new ArrayList<StudyCountInfo>();

        Query query = context.getSession().createQuery(STUDY_INFO_HQL);
        query.setParameter(0, siteId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.list();
        for (Object[] row : results) {
            StudyCountInfo studyInfo =
                new StudyCountInfo((Study) row[0], (Long) row[1],
                    (Long) row[2]);

            studies.add(studyInfo);
        }

        return new ListResult<StudyCountInfo>(studies);
    }
}