package edu.ualberta.med.biobank.widgets.infotables;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.widgets.BiobankLabelProvider;

public class ClinicInfoTable extends InfoTableWidget<ClinicWrapper> {

    class TableRowData {
        public ClinicWrapper clinic;
        public String clinicName;
        public String clinicNameShort;
        public Integer studyCount;
        public String status;
        public Long patientCount;
        public Long visitCount;

        @Override
        public String toString() {
            return StringUtils.join(new String[] { clinicName, clinicNameShort,
                studyCount.toString(), (status != null) ? status : "",
                (patientCount != null) ? patientCount.toString() : "",
                (visitCount != null) ? visitCount.toString() : "" }, "\t");
        }
    }

    private static final String[] HEADINGS = new String[] { "Name",
        "Short name", "Study Count", "Status", "Patients", "Patient Visits" };

    public ClinicInfoTable(Composite parent, List<ClinicWrapper> collection) {
        super(parent, collection, HEADINGS, 10);
    }

    @Override
    protected BiobankLabelProvider getLabelProvider() {
        return new BiobankLabelProvider() {
            @Override
            public String getColumnText(Object element, int columnIndex) {
                TableRowData item = (TableRowData) ((BiobankCollectionModel) element).o;
                if (item == null) {
                    if (columnIndex == 0) {
                        return "loading...";
                    }
                    return "";
                }
                switch (columnIndex) {
                case 0:
                    return item.clinicName;
                case 1:
                    return item.clinicNameShort;
                case 2:
                    return item.studyCount.toString();
                case 3:
                    return item.status;
                case 4:
                    return item.patientCount.toString();
                case 5:
                    return item.visitCount.toString();
                default:
                    return "";
                }
            }
        };
    }

    @Override
    public Object getCollectionModelObject(ClinicWrapper clinic)
        throws Exception {
        TableRowData info = new TableRowData();
        info.clinic = clinic;
        info.clinicName = clinic.getName();
        info.clinicNameShort = clinic.getNameShort();
        List<StudyWrapper> studies = clinic.getStudyCollection();
        if (studies == null) {
            info.studyCount = 0;
        } else {
            info.studyCount = studies.size();
        }
        info.status = clinic.getActivityStatus().getName();
        info.patientCount = clinic.getPatientCount(true);
        info.visitCount = clinic.getProcessingEventCount();
        return info;
    }

    @Override
    protected String getCollectionModelObjectToString(Object o) {
        if (o == null)
            return null;
        return ((TableRowData) o).toString();
    }

    @Override
    public ClinicWrapper getSelection() {
        BiobankCollectionModel item = getSelectionInternal();
        if (item == null)
            return null;
        TableRowData row = (TableRowData) item.o;
        Assert.isNotNull(row);
        return row.clinic;
    }

    @Override
    protected BiobankTableSorter getComparator() {
        return null;
    }

    /*
     * @Override public void setSelection(ClinicWrapper item) {
     * BiobankCollectionModel modelItem = null; for (BiobankCollectionModel m :
     * model) { if (item.equals(m.o)) { modelItem = m; break; } } if (modelItem
     * == null) return;
     * 
     * tableViewer.setSelection(new StructuredSelection(modelItem)); }
     */
}
