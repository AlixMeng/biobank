package edu.ualberta.med.biobank.widgets;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import edu.ualberta.med.biobank.BiobankPlugin;
import edu.ualberta.med.biobank.common.formatters.DateFormatter;
import edu.ualberta.med.biobank.common.wrappers.ActivityStatusWrapper;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.CollectionEventWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContactWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.DispatchSpecimenWrapper;
import edu.ualberta.med.biobank.common.wrappers.RequestSpecimenWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.SourceSpecimenWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.model.StudyContactInfo;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.widgets.infotables.BiobankCollectionModel;

/**
 * This code must not run in the UI thread.
 * 
 */
public class BiobankLabelProvider extends LabelProvider implements
    ITableLabelProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof SiteWrapper) {
            final SiteWrapper site = (SiteWrapper) element;
            switch (columnIndex) {
            case 0:
                return site.getName();
            }
        } else if (element instanceof ClinicWrapper) {
            final ClinicWrapper clinic = (ClinicWrapper) element;
            switch (columnIndex) {
            case 0:
                return clinic.getName();
            }
        } else if (element instanceof StudyWrapper) {
            final StudyWrapper study = (StudyWrapper) element;
            switch (columnIndex) {
            case 0:
                return study.getName();
            case 1:
                return study.getNameShort();
            case 2:
                try {
                    return String.valueOf(study.getPatientCount(true));
                } catch (Exception e) {
                    BiobankPlugin.openAsyncError("Error in count", e);
                }
            }
        } else if (element instanceof SpecimenWrapper) {
            final SpecimenWrapper aliquot = (SpecimenWrapper) element;
            switch (columnIndex) {
            case 0:
                return aliquot.getInventoryId();
            case 1:
                return aliquot.getSpecimenType() == null ? "" : aliquot
                    .getSpecimenType().getName();
            case 2:
                String position = aliquot.getPositionString();
                return (position != null) ? position : "none";
            case 3:
                return aliquot.getCreatedAt() == null ? "" : DateFormatter
                    .formatAsDateTime(aliquot.getCreatedAt());
            case 4:
                return aliquot.getQuantity() == null ? "" : aliquot
                    .getQuantity().toString();
            case 6:
                return aliquot.getComment() == null ? "" : aliquot.getComment();
            }
        } else if (element instanceof SpecimenTypeWrapper) {
            final SpecimenTypeWrapper st = (SpecimenTypeWrapper) element;
            switch (columnIndex) {
            case 0:
                return st.getName();
            case 1:
                return st.getNameShort();
            case 2:
                return String.valueOf(st.getId());
            }
        } else if (element instanceof BiobankCollectionModel) {
            BiobankCollectionModel m = (BiobankCollectionModel) element;
            if (m.o != null) {
                return getColumnText(m.o, columnIndex);
            } else if (columnIndex == 0) {
                return "loading ...";
            }
        } else if (element instanceof StudyContactInfo) {
            StudyContactInfo info = (StudyContactInfo) element;
            if (columnIndex == 0) {
                if (info.contact != null)
                    return info.contact.getClinic().getName();
                return "";
            }
            return getContactWrapperColumnIndex(info.contact, columnIndex);
        } else if (element instanceof DispatchSpecimenWrapper) {
            DispatchSpecimenWrapper dsa = (DispatchSpecimenWrapper) element;
            if (columnIndex == 0)
                return dsa.getSpecimen().getInventoryId();
            if (columnIndex == 1)
                return dsa.getSpecimen().getSpecimenType().getNameShort();
            if (columnIndex == 2)
                return dsa.getSpecimen().getCollectionEvent().getPatient()
                    .getPnumber();
            if (columnIndex == 3)
                return dsa.getSpecimen().getActivityStatus().toString();
            if (columnIndex == 4)
                return dsa.getComment();
        } else if (element instanceof RequestSpecimenWrapper) {
            RequestSpecimenWrapper dsa = (RequestSpecimenWrapper) element;
            if (columnIndex == 0)
                return dsa.getSpecimen().getInventoryId();
            if (columnIndex == 1)
                return dsa.getSpecimen().getSpecimenType().getNameShort();
            if (columnIndex == 2)
                return dsa.getSpecimen().getPositionString(true, true);
            if (columnIndex == 3)
                return dsa.getClaimedBy();
        } else if (element instanceof AdapterBase)
            return ((AdapterBase) element).getLabel();
        else {
            Assert.isTrue(false, "invalid object type: " + element.getClass());
        }
        return "";
    }

    @Override
    public String getText(Object element) {
        if (element instanceof ContainerTypeWrapper) {
            return ((ContainerTypeWrapper) element).getName();
        } else if (element instanceof StudyWrapper) {
            StudyWrapper study = (StudyWrapper) element;
            return study.getNameShort() + " - " + study.getName();
        } else if (element instanceof ClinicWrapper) {
            return ((ClinicWrapper) element).getName();
        } else if (element instanceof SiteWrapper) {
            return ((SiteWrapper) element).getNameShort();
        } else if (element instanceof CollectionEventWrapper) {
            return ((CollectionEventWrapper) element).getVisitNumber()
                .toString();
        } else if (element instanceof SpecimenTypeWrapper) {
            return ((SpecimenTypeWrapper) element).getNameShort();
        } else if (element instanceof SourceSpecimenWrapper) {
            return ((SourceSpecimenWrapper) element).getSpecimenType()
                .getNameShort();
        } else if (element instanceof SiteWrapper) {
            return ((SiteWrapper) element).getName();
        } else if (element instanceof ActivityStatusWrapper) {
            return ((ActivityStatusWrapper) element).getName();
        } else if (element instanceof AdapterBase) {
            return ((AdapterBase) element).getLabel();
        }
        return element.toString();
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    private String getContactWrapperColumnIndex(ContactWrapper contact,
        int columnIndex) {
        switch (columnIndex) {
        case 1:
            if ((contact != null) && (contact.getName() != null))
                return contact.getName();
            break;
        case 2:
            if ((contact != null) && (contact.getTitle() != null))
                return contact.getTitle();
            break;
        case 3:
            if ((contact != null) && (contact.getEmailAddress() != null))
                return contact.getEmailAddress();
            break;
        case 4:
            if ((contact != null) && (contact.getMobileNumber() != null))
                return contact.getMobileNumber();
            break;
        case 5:
            if ((contact != null) && (contact.getPagerNumber() != null))
                return contact.getPagerNumber();
            break;
        case 6:
            if ((contact != null) && (contact.getOfficeNumber() != null))
                return contact.getOfficeNumber();
            break;
        case 7:
            if ((contact != null) && (contact.getFaxNumber() != null))
                return contact.getFaxNumber();
            break;
        }
        return "";
    }

}
