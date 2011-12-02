package edu.ualberta.med.biobank.common.permission.clinic;

import org.hibernate.Session;

import edu.ualberta.med.biobank.common.action.ActionUtil;
import edu.ualberta.med.biobank.common.permission.Permission;
import edu.ualberta.med.biobank.common.permission.PermissionEnum;
import edu.ualberta.med.biobank.model.Clinic;
import edu.ualberta.med.biobank.model.User;

public class ClinicReadPermission implements Permission {
    private static final long serialVersionUID = 1L;

    private final Integer clinicId;

    public ClinicReadPermission(Integer clinicId) {
        this.clinicId = clinicId;
    }

    public ClinicReadPermission(Clinic clinic) {
        this(clinic.getId());
    }

    @Override
    public boolean isAllowed(User user, Session session) {
        Clinic clinic = ActionUtil.sessionGet(session, Clinic.class, clinicId);
        return PermissionEnum.CLINIC_READ.isAllowed(user, clinic);
    }
}