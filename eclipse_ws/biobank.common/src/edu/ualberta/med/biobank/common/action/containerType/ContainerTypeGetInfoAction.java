package edu.ualberta.med.biobank.common.action.containerType;

import org.hibernate.Query;

import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.action.ActionResult;
import edu.ualberta.med.biobank.common.action.containerType.ContainerTypeGetInfoAction.ContainerTypeInfo;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.permission.containerType.ContainerTypeReadPermission;
import edu.ualberta.med.biobank.model.ContainerType;

public class ContainerTypeGetInfoAction implements Action<ContainerTypeInfo> {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("nls")
    private static final String CTYPE_INFO_HQL =
        "SELECT DISTINCT ctype"
            + " FROM " + ContainerType.class.getName() + " ctype"
            + " INNER JOIN FETCH ctype.capacity"
            + " INNER JOIN FETCH ctype.childLabelingScheme"
            + " LEFT JOIN FETCH ctype.childContainerTypeCollection"
            + " LEFT JOIN FETCH ctype.specimenTypeCollection"
            + " LEFT JOIN FETCH ctype.commentCollection comments"
            + " LEFT JOIN FETCH comments.user"
            + " WHERE ctype.id = ?";

    public static class ContainerTypeInfo implements ActionResult {
        private static final long serialVersionUID = 1L;
        private ContainerType containerType;

        public ContainerTypeInfo(ContainerType containerType) {
            this.containerType = containerType;
        }

        public ContainerType getContainerType() {
            return containerType;
        }
    }

    private final Integer ctypeId;

    public ContainerTypeGetInfoAction(Integer ctypeId) {
        this.ctypeId = ctypeId;
    }

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        return new ContainerTypeReadPermission(ctypeId).isAllowed(context);
    }

    @Override
    public ContainerTypeInfo run(ActionContext context)
        throws ActionException {
        Query query = context.getSession().createQuery(CTYPE_INFO_HQL);
        query.setParameter(0, ctypeId);
        ContainerTypeInfo containerTypeInfo =
            new ContainerTypeInfo((ContainerType) query.uniqueResult());
        return containerTypeInfo;
    }
}
