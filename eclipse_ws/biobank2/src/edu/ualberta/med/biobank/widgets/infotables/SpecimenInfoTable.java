package edu.ualberta.med.biobank.widgets.infotables;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.common.wrappers.ActivityStatusWrapper;
import edu.ualberta.med.biobank.common.wrappers.CollectionEventWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenWrapper;
import edu.ualberta.med.biobank.widgets.BiobankLabelProvider;

public class SpecimenInfoTable extends InfoTableWidget<SpecimenWrapper> {

    public static enum ColumnsShown {
        ALL(new String[] { "Inventory ID", "Type", "Patient", "Visit#",
            "Position", "Creation Date", "Quantity (ml)", "Activity Status",
            "Comment" }) {
            @Override
            public String getColumnValue(TableRowData row, int columnIndex) {
                switch (columnIndex) {
                case 0:
                    return row.inventoryId;
                case 1:
                    return row.type;
                case 2:
                    return row.patient;
                case 3:
                    return row.pvNumber.toString();
                case 4:
                    return row.position;
                case 5:
                    return row.createdAt;
                case 6:
                    return row.quantity;
                case 7:
                    return row.activityStatus;
                case 8:
                    return row.comment;
                default:
                    return "";
                }
            }
        },
        EVENT_FORM(new String[] { "Inventory ID", "Type", "Creation Date",
            "Quantity (ml)", "Activity Status" }) {
            @Override
            public String getColumnValue(TableRowData row, int columnIndex) {
                switch (columnIndex) {
                case 0:
                    return row.inventoryId;
                case 1:
                    return row.type;
                case 2:
                    return row.createdAt;
                case 3:
                    return row.quantity;
                case 4:
                    return row.activityStatus;
                default:
                    return "";
                }
            }
        };

        private String[] headings;

        private ColumnsShown(String[] headings) {
            this.headings = headings;
        }

        public String[] getheadings() {
            return headings;
        }

        public abstract String getColumnValue(TableRowData row, int columnIndex);
    }

    protected class TableRowData {
        public SpecimenWrapper specimen;
        public String inventoryId;
        public String type;
        public String patient;
        public String pvNumber;
        public String createdAt;
        public String quantity;
        public String position;
        public String activityStatus;
        public String comment;

        @Override
        public String toString() {
            return StringUtils.join(new String[] { inventoryId, type, patient,
                pvNumber, createdAt, quantity, position, activityStatus,
                comment }, "\t");
        }
    }

    private ColumnsShown currentColumnsShowns;

    public SpecimenInfoTable(Composite parent,
        List<SpecimenWrapper> specimenCollection, ColumnsShown columnsShown,
        int rowsPerPage) {
        super(parent, specimenCollection, columnsShown.getheadings(),
            rowsPerPage);
        this.currentColumnsShowns = columnsShown;
    }

    @Override
    protected BiobankLabelProvider getLabelProvider() {
        return new BiobankLabelProvider() {
            @Override
            public String getColumnText(Object element, int columnIndex) {
                TableRowData info = (TableRowData) ((BiobankCollectionModel) element).o;
                if (info == null) {
                    if (columnIndex == 0) {
                        return "loading...";
                    }
                    return "";
                }
                return currentColumnsShowns.getColumnValue(info, columnIndex);
            }
        };
    }

    @Override
    public TableRowData getCollectionModelObject(SpecimenWrapper specimen)
        throws Exception {
        TableRowData info = new TableRowData();
        info.specimen = specimen;
        info.inventoryId = specimen.getInventoryId();
        SpecimenTypeWrapper type = specimen.getSpecimenType();
        Assert.isNotNull(type, "aliquot with null for sample type");
        info.type = type.getName();
        CollectionEventWrapper cEvent = specimen.getCollectionEvent();
        info.patient = cEvent == null ? "" : cEvent.getPatient().getPnumber();
        Integer visitNumber = cEvent == null ? null : cEvent.getVisitNumber();
        info.pvNumber = (visitNumber == null) ? "" : visitNumber.toString();
        info.createdAt = specimen.getFormattedCreatedAt();
        Double quantity = specimen.getQuantity();
        info.quantity = (quantity == null) ? "" : quantity.toString();
        info.position = specimen.getPositionString();
        ActivityStatusWrapper status = specimen.getActivityStatus();
        info.activityStatus = (status == null) ? "" : status.getName();
        info.comment = specimen.getComment();

        return info;
    }

    @Override
    protected String getCollectionModelObjectToString(Object o) {
        if (o == null)
            return null;
        TableRowData r = (TableRowData) o;
        return r.toString();
    }

    public void setSelection(SpecimenWrapper selectedSample) {
        if (selectedSample == null)
            return;
        for (BiobankCollectionModel item : model) {
            TableRowData info = (TableRowData) item.o;
            if (info.specimen == selectedSample) {
                getTableViewer().setSelection(new StructuredSelection(item),
                    true);
            }
        }
    }

    @Override
    public SpecimenWrapper getSelection() {
        BiobankCollectionModel item = getSelectionInternal();
        if (item == null)
            return null;
        TableRowData row = (TableRowData) item.o;
        Assert.isNotNull(row);
        return row.specimen;
    }

    @Override
    protected BiobankTableSorter getComparator() {
        return null;
    }
}
