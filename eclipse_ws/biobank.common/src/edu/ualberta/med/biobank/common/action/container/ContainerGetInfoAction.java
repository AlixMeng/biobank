package edu.ualberta.med.biobank.common.action.container;

import org.hibernate.Query;

import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.action.ActionResult;
import edu.ualberta.med.biobank.common.action.container.ContainerGetInfoAction.ContainerInfo;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.permission.container.ContainerReadPermission;
import edu.ualberta.med.biobank.model.Container;

public class ContainerGetInfoAction implements Action<ContainerInfo> {
    private static final long serialVersionUID = 1L;

    // this query is ridiculous!
    @SuppressWarnings("nls")
    private static final String CONTAINER_INFO_HQL =
        "SELECT DISTINCT container"
            + " FROM " + Container.class.getName() + " container"
            + " INNER JOIN FETCH container.containerType ctype"
            + " INNER JOIN FETCH ctype.capacity"
            + " LEFT JOIN FETCH ctype.childContainerTypeCollection"
            + " LEFT JOIN FETCH ctype.specimenTypeCollection"
            + " INNER JOIN FETCH ctype.childLabelingScheme"
            + " LEFT JOIN FETCH container.position"
            + " INNER JOIN FETCH container.topContainer topContainer"
            + " INNER JOIN FETCH topContainer.containerType topContainerType"
            + " INNER JOIN FETCH topContainerType.childLabelingScheme"
            + " INNER JOIN FETCH container.site"
            + " LEFT JOIN FETCH container.childPositionCollection childPos"
            + " LEFT JOIN FETCH childPos.container"
            + " LEFT JOIN FETCH container.specimenPositionCollection spcPos"
            + " LEFT JOIN FETCH spcPos.specimen specimen"
            + " LEFT JOIN FETCH specimen.parentSpecimen parentSpecimen"
            + " LEFT JOIN FETCH specimen.collectionEvent cevent"
            + " LEFT JOIN FETCH cevent.patient patient"
            + " LEFT JOIN FETCH patient.study"
            + " LEFT JOIN FETCH parentSpecimen.processingEvent"
            + " LEFT JOIN FETCH container.commentCollection containerComments"
            + " LEFT JOIN FETCH containerComments.user"
            + " LEFT JOIN FETCH specimen.commentCollection"
            + " LEFT JOIN FETCH specimen.originInfo spcOriginInfo"
            + " LEFT JOIN FETCH spcOriginInfo.center"
            + " LEFT JOIN FETCH container.position position"
            + " LEFT JOIN FETCH position.parentContainer parentContainer"
            + " LEFT JOIN FETCH parentContainer.containerType parentCtype"
            + " LEFT JOIN FETCH parentCtype.capacity"
            + " LEFT JOIN FETCH parentCtype.childLabelingScheme"
            + " LEFT JOIN FETCH parentCtype.childContainerTypeCollection"
            + " WHERE container.id = ?";

    public static class ContainerInfo implements ActionResult {
        private static final long serialVersionUID = 1L;
        public Container container;
    }

    private final Integer containerId;

    public ContainerGetInfoAction(Integer containerId) {
        this.containerId = containerId;
    }

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        return new ContainerReadPermission(containerId).isAllowed(context);
    }

    @Override
    public ContainerInfo run(ActionContext context) throws ActionException {
        ContainerInfo containerInfo = new ContainerInfo();
        Query query = context.getSession().createQuery(CONTAINER_INFO_HQL);
        query.setParameter(0, containerId);

        containerInfo.container = (Container) query.uniqueResult();
        return containerInfo;
    }

}
