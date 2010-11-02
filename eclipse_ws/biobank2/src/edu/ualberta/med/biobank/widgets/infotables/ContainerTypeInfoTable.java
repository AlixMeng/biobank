package edu.ualberta.med.biobank.widgets.infotables;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.widgets.BiobankLabelProvider;

public class ContainerTypeInfoTable extends
    InfoTableWidget<ContainerTypeWrapper> {

    class TableRowData {
        ContainerTypeWrapper containerType;
        String name;
        String nameShort;
        Integer capacity;
        String status;
        Long inUseCount;
        Double temperature;

        @Override
        public String toString() {
            return StringUtils.join(new String[] { name, nameShort,
                (capacity != null) ? capacity.toString() : "", status,
                (inUseCount != null) ? inUseCount.toString() : "",
                (temperature != null) ? temperature.toString() : "" }, "\t");
        }
    }

    private static final String[] HEADINGS = new String[] { "Name",
        "Short Name", "Capacity", "Status", "In Use", "Temperature" };

    public ContainerTypeInfoTable(Composite parent,
        List<ContainerTypeWrapper> collection) {
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
                    return item.name;
                case 1:
                    return item.nameShort;
                case 2:
                    return (item.capacity != null) ? item.capacity.toString()
                        : null;
                case 3:
                    return item.status;
                case 4:
                    return (item.inUseCount != null) ? item.inUseCount
                        .toString() : null;
                case 5:
                    return (item.temperature != null) ? item.temperature
                        .toString() : null;
                default:
                    return "";
                }
            }
        };
    }

    @Override
    public Object getCollectionModelObject(ContainerTypeWrapper type)
        throws Exception {
        TableRowData info = new TableRowData();
        Integer rowCapacity = type.getRowCapacity();
        Integer colCapacity = type.getColCapacity();

        info.containerType = type;
        info.name = type.getName();
        info.nameShort = type.getNameShort();
        info.status = type.getActivityStatus().getName();
        if ((rowCapacity != null) && (colCapacity != null)) {
            info.capacity = rowCapacity * colCapacity;
        }
        info.inUseCount = type.getContainersCount();
        info.temperature = type.getDefaultTemperature();
        return info;
    }

    @Override
    protected String getCollectionModelObjectToString(Object o) {
        if (o == null)
            return null;
        return ((TableRowData) o).toString();
    }

    @Override
    public ContainerTypeWrapper getSelection() {
        BiobankCollectionModel item = getSelectionInternal();
        if (item == null)
            return null;
        TableRowData row = (TableRowData) item.o;
        Assert.isNotNull(row);
        return row.containerType;
    }

    @Override
    protected BiobankTableSorter getComparator() {
        return null;
    }
}
