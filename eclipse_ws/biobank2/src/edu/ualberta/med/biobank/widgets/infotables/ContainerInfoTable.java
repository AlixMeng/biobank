package edu.ualberta.med.biobank.widgets.infotables;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MenuItem;

import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.treeview.admin.SiteAdapter;
import edu.ualberta.med.biobank.treeview.util.AdapterFactory;
import edu.ualberta.med.biobank.widgets.BiobankLabelProvider;

public class ContainerInfoTable extends InfoTableWidget<ContainerWrapper> {

    class TableRowData {
        ContainerWrapper container;
        String label;
        String typeNameShort;
        String status;
        String barcode;
        Double temperature;

        @Override
        public String toString() {
            return StringUtils.join(new String[] { label, typeNameShort,
                status, barcode,
                (temperature != null) ? temperature.toString() : "" }, "\t");
        }
    }

    private static final String[] HEADINGS = new String[] { "Name",
        "Container Type", "Status", "Product Barcode", "Temperature" };

    private SiteAdapter siteAdapter;

    public ContainerInfoTable(Composite parent, SiteAdapter site)
        throws Exception {
        super(parent, site.getWrapper().getTopContainerCollection(), HEADINGS,
            10);
        siteAdapter = site;
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
                    return item.label;
                case 1:
                    return item.typeNameShort;
                case 2:
                    return item.status;
                case 3:
                    return item.barcode;
                case 4:
                    if (item.temperature == null) {
                        return "";
                    }
                    return item.temperature.toString();
                default:
                    return "";
                }
            }
        };
    }

    @Override
    public Object getCollectionModelObject(ContainerWrapper container)
        throws Exception {
        TableRowData info = new TableRowData();

        info.container = container;
        info.label = container.getLabel();
        ContainerTypeWrapper type = container.getContainerType();
        if (type != null) {
            info.typeNameShort = type.getNameShort();
        }
        info.status = container.getActivityStatus().getName();
        info.barcode = container.getProductBarcode();
        info.temperature = container.getTemperature();
        return info;
    }

    @Override
    protected String getCollectionModelObjectToString(Object o) {
        if (o == null)
            return null;
        return ((TableRowData) o).toString();
    }

    @Override
    public ContainerWrapper getSelection() {
        BiobankCollectionModel item = getSelectionInternal();
        if (item == null)
            return null;
        TableRowData row = (TableRowData) item.o;
        Assert.isNotNull(row);
        return row.container;
    }

    @Override
    protected BiobankTableSorter getComparator() {
        return null;
    }

    @Override
    public void addClickListener(IDoubleClickListener listener) {
        doubleClickListeners.add(listener);
        MenuItem mi = new MenuItem(getMenu(), SWT.PUSH);
        mi.setText("Edit");
        mi.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ModelWrapper<?> selection = ContainerInfoTable.this
                    .getSelection();
                if (selection != null) {
                    AdapterBase adapter = AdapterFactory.getAdapter(selection);
                    adapter.setParent(siteAdapter.getContainersGroupNode());
                    adapter.openEntryForm();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }
        });
    }

}
