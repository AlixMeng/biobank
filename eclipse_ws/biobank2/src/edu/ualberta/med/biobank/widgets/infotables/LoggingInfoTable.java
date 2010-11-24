package edu.ualberta.med.biobank.widgets.infotables;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.common.wrappers.LogWrapper;
import edu.ualberta.med.biobank.widgets.BiobankLabelProvider;

public class LoggingInfoTable extends InfoTableWidget<LogWrapper> {

    private static final String[] HEADINGS = new String[] { "Site", "User",
        "Date", "Action", "Type", "Patient #", "Inventory ID", "Location",
        "Details" };

    private static final int PAGE_SIZE_ROWS = 20;

    public LoggingInfoTable(Composite parent, List<LogWrapper> collection) {
        super(parent, collection, HEADINGS, PAGE_SIZE_ROWS);
    }

    class TableRowData {
        String site;
        String user;
        String date;
        String action;
        String type;
        String patientNumber;
        String inventoryId;
        String positionLabel;
        String details;

        @Override
        public String toString() {
            return StringUtils.join(new String[] { site, user, date, action,
                type, patientNumber, inventoryId, positionLabel, details },
                "\t");
        }
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
                    return item.site;
                case 1:
                    return item.user;
                case 2:
                    return item.date;
                case 3:
                    return item.action;
                case 4:
                    return item.type;
                case 5:
                    return item.patientNumber;
                case 6:
                    return item.inventoryId;
                case 7:
                    return item.positionLabel;
                case 8:
                    return item.details;
                default:
                    return "";
                }
            }
        };
    }

    @Override
    public Object getCollectionModelObject(LogWrapper logQuery)
        throws Exception {
        TableRowData info = new TableRowData();
        info.site = logQuery.getSite();
        info.user = logQuery.getUsername();
        info.action = logQuery.getAction();
        info.type = logQuery.getType();
        info.positionLabel = logQuery.getLocationLabel();
        info.patientNumber = logQuery.getPatientNumber();
        info.inventoryId = logQuery.getInventoryId();
        info.details = logQuery.getDetails();

        Date logQueryDate = logQuery.getCreatedAt();
        if (logQueryDate != null) {
            SimpleDateFormat dateTimeSecond = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
            info.date = dateTimeSecond.format(logQueryDate);
        } else {
            info.date = null;
        }

        return info;
    }

    @Override
    protected String getCollectionModelObjectToString(Object o) {
        if (o == null)
            return null;
        return ((TableRowData) o).toString();
    }

    @Override
    public List<LogWrapper> getCollection() {
        return null;
    }

    @Override
    public LogWrapper getSelection() {
        return null;
    }

    @Override
    protected BiobankTableSorter getComparator() {
        return null;
    }
}