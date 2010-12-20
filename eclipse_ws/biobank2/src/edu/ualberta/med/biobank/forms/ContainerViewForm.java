package edu.ualberta.med.biobank.forms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Section;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.util.RowColPos;
import edu.ualberta.med.biobank.common.wrappers.AliquotWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.logs.BiobankLogger;
import edu.ualberta.med.biobank.model.Cell;
import edu.ualberta.med.biobank.model.CellStatus;
import edu.ualberta.med.biobank.model.ContainerCell;
import edu.ualberta.med.biobank.treeview.admin.ContainerAdapter;
import edu.ualberta.med.biobank.treeview.admin.SiteAdapter;
import edu.ualberta.med.biobank.widgets.BiobankText;
import edu.ualberta.med.biobank.widgets.grids.ContainerDisplayWidget;
import edu.ualberta.med.biobank.widgets.grids.selection.MultiSelectionEvent;
import edu.ualberta.med.biobank.widgets.grids.selection.MultiSelectionListener;
import edu.ualberta.med.biobank.widgets.grids.selection.MultiSelectionSpecificBehaviour;
import edu.ualberta.med.biobank.widgets.infotables.AliquotListInfoTable;

public class ContainerViewForm extends BiobankViewForm {

    public static final String ID = "edu.ualberta.med.biobank.forms.ContainerViewForm";

    private static BiobankLogger logger = BiobankLogger
        .getLogger(ContainerViewForm.class.getName());

    private ContainerAdapter containerAdapter;

    private ContainerWrapper container;

    private AliquotListInfoTable aliquotsWidget;

    private BiobankText siteLabel;

    private BiobankText containerLabelLabel;

    private BiobankText productBarcodeLabel;

    private BiobankText activityStatusLabel;

    private BiobankText commentsLabel;

    private BiobankText containerTypeLabel;

    private BiobankText temperatureLabel;

    private BiobankText rowLabel = null;

    private BiobankText colLabel;

    private ContainerDisplayWidget containerWidget;

    private Map<RowColPos, ContainerCell> cells;

    private boolean childrenOk = true;

    private Composite childrenActionSection;

    private boolean canCreate;

    private boolean canDelete;

    private ComboViewer initSelectionCv;

    private ComboViewer deleteCv;

    @Override
    public void init() throws Exception {
        Assert.isTrue(adapter instanceof ContainerAdapter,
            "Invalid editor input: object of type "
                + adapter.getClass().getName());

        containerAdapter = (ContainerAdapter) adapter;
        container = containerAdapter.getContainer();
        container.reload();
        setPartName(container.getLabel() + " ("
            + container.getContainerType().getNameShort() + ")");
        initCells();
        canCreate = SessionManager.canCreate(ContainerWrapper.class,
            container.getSite());
        canDelete = SessionManager.canDelete(ContainerWrapper.class,
            container.getSite());
    }

    @Override
    protected void createFormContent() throws Exception {
        form.setText("Container " + container.getLabel() + " ("
            + container.getContainerType().getNameShort() + ")");
        page.setLayout(new GridLayout(1, false));

        createContainerSection();

        if (container.getContainerType().getSampleTypeCollection().size() > 0) {
            // only show aliquots section this if this container type does not
            // have child containers
            createAliquotsSection();
        }
    }

    private void createContainerSection() {
        Composite client = toolkit.createComposite(page);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        client.setLayoutData(gridData);
        toolkit.paintBordersFor(client);

        siteLabel = createReadOnlyLabelledField(client, SWT.NONE,
            "Repository Site");
        containerLabelLabel = createReadOnlyLabelledField(client, SWT.NONE,
            "Label");
        productBarcodeLabel = createReadOnlyLabelledField(client, SWT.NONE,
            "Product Bar Code");
        activityStatusLabel = createReadOnlyLabelledField(client, SWT.NONE,
            "Activity Status");
        commentsLabel = createReadOnlyLabelledField(client, SWT.MULTI,
            "Comments");
        containerTypeLabel = createReadOnlyLabelledField(client, SWT.NONE,
            "Container Type");
        temperatureLabel = createReadOnlyLabelledField(client, SWT.NONE,
            "Temperature");

        setContainerValues();

        if (container.getContainerType().getChildContainerTypeCollection()
            .size() > 0) {
            createVisualizeContainer();
        }
    }

    private void initCells() {
        try {
            Integer rowCap = container.getRowCapacity();
            Integer colCap = container.getColCapacity();
            Assert.isNotNull(rowCap, "row capacity is null");
            Assert.isNotNull(colCap, "column capacity is null");
            if (rowCap == 0)
                rowCap = 1;
            if (colCap == 0)
                colCap = 1;

            cells = new TreeMap<RowColPos, ContainerCell>();
            Map<RowColPos, ContainerWrapper> childrenMap = container
                .getChildren();
            for (int i = 0; i < rowCap; i++) {
                for (int j = 0; j < colCap; j++) {
                    ContainerCell cell = new ContainerCell(i, j);
                    cells.put(new RowColPos(i, j), cell);
                    ContainerWrapper container = childrenMap.get(new RowColPos(
                        i, j));
                    if (container == null) {
                        cell.setStatus(CellStatus.NOT_INITIALIZED);
                    } else {
                        cell.setContainer(container);
                        cell.setStatus(CellStatus.INITIALIZED);
                    }
                }
            }
        } catch (Exception ex) {
            BioBankPlugin.openAsyncError("Positions errors",
                "Some child container has wrong position number");
            childrenOk = false;
        }
    }

    private void refreshVis() {
        initCells();
        if (containerWidget == null) {
            createVisualizeContainer();
            form.layout(true, true);
        }
        containerWidget.setCells(cells);
    }

    protected void createVisualizeContainer() {
        Section s = createSection("Container Visual");
        s.setLayout(new GridLayout(1, false));
        Composite containerSection = new Composite(s, SWT.NONE);
        containerSection.setLayout(new FillLayout(SWT.VERTICAL));
        ScrolledComposite sc = new ScrolledComposite(containerSection,
            SWT.H_SCROLL);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        Composite client = new Composite(sc, SWT.NONE);
        client.setLayout(new GridLayout(1, false));
        toolkit.adapt(containerSection);
        toolkit.adapt(sc);
        toolkit.adapt(client);
        sc.setContent(client);
        s.setClient(containerSection);
        if (!childrenOk) {
            Label label = toolkit
                .createLabel(client,
                    "Error in container children : can't display those initialized");
            label.setForeground(Display.getCurrent().getSystemColor(
                SWT.COLOR_RED));
        }
        containerWidget = new ContainerDisplayWidget(client,
            CellStatus.DEFAULT_CONTAINER_STATUS_LIST);
        containerWidget.setContainer(container);
        containerWidget.setCells(cells);
        toolkit.adapt(containerWidget);

        // Set the minimum size

        sc.setMinSize(containerWidget.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        containerWidget.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                Cell cell = ((ContainerDisplayWidget) e.widget)
                    .getObjectAtCoordinates(e.x, e.y);
                if (cell != null)
                    openFormFor((ContainerCell) cell);
            }
        });
        containerWidget.getMultiSelectionManager().enableMultiSelection(
            new MultiSelectionSpecificBehaviour() {
                @Override
                public void removeSelection(Cell cell) {
                }

                @Override
                public boolean isSelectable(Cell cell) {
                    return true;
                }
            });
        containerWidget.getMultiSelectionManager().addMultiSelectionListener(
            new MultiSelectionListener() {
                @Override
                public void selectionChanged(MultiSelectionEvent mse) {
                    setChildrenActionSectionEnabled(mse.selections > 0);
                }
            });
        containerWidget.displayFullInfoString(true);

        createChildrenActionsSection(containerSection);
    }

    private void createChildrenActionsSection(Composite client) {
        childrenActionSection = toolkit.createComposite(client);
        childrenActionSection.setLayout(new GridLayout(3, false));

        if (canCreate || canDelete) {
            List<ContainerTypeWrapper> containerTypes = container
                .getContainerType().getChildContainerTypeCollection();

            if (canCreate) {
                // Initialisation action for selection
                initSelectionCv = createComboViewer(childrenActionSection,
                    "Initialize selection to", containerTypes,
                    containerTypes.get(0));
                initSelectionCv.getCombo()
                    .setLayoutData(new GridData(SWT.LEFT));
                Button initializeSelectionButton = toolkit.createButton(
                    childrenActionSection, "Initialize", SWT.PUSH);
                initializeSelectionButton
                    .addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            ContainerTypeWrapper type = (ContainerTypeWrapper) ((IStructuredSelection) initSelectionCv
                                .getSelection()).getFirstElement();
                            initSelection(type);
                        }
                    });
                initializeSelectionButton.setLayoutData(new GridData(SWT.LEFT));
            }

            if (canDelete) {
                // Delete action for selection
                List<Object> deleteComboList = new ArrayList<Object>();
                deleteComboList.add("All");
                deleteComboList.addAll(containerTypes);
                deleteCv = createComboViewer(childrenActionSection,
                    "Delete selected containers of type", deleteComboList,
                    "All");
                deleteCv.getCombo().setLayoutData(new GridData(SWT.LEFT));
                Button deleteButton = toolkit.createButton(
                    childrenActionSection, "Delete", SWT.PUSH);
                deleteButton.setLayoutData(new GridData(SWT.LEFT));
                deleteButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Boolean confirm = MessageDialog
                            .openConfirm(PlatformUI.getWorkbench()
                                .getActiveWorkbenchWindow().getShell(),
                                "Confirm Delete",
                                "Are you sure you want to delete these containers?");
                        if (confirm) {
                            Object selection = ((IStructuredSelection) deleteCv
                                .getSelection()).getFirstElement();
                            if (selection instanceof ContainerTypeWrapper) {
                                deleteSelection((ContainerTypeWrapper) selection);
                            } else {
                                deleteSelection(null);
                            }
                        }
                    }
                });
            }
        }
        setChildrenActionSectionEnabled(false);
    }

    private void setChildrenActionSectionEnabled(boolean enable) {
        // don't use the method seEnabled on the composite because the children
        // are not greyed out in this case
        for (Control c : childrenActionSection.getChildren()) {
            c.setEnabled(enable);
        }
    }

    private void initSelection(final ContainerTypeWrapper type) {
        IRunnableContext context = new ProgressMonitorDialog(Display
            .getDefault().getActiveShell());
        try {
            context.run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(final IProgressMonitor monitor) {
                    monitor.beginTask("Initializing...",
                        IProgressMonitor.UNKNOWN);
                    boolean initDone = true;
                    try {
                        final Set<RowColPos> positions = containerWidget
                            .getMultiSelectionManager().getSelectedPositions();
                        container.initChildrenWithType(type, positions);
                    } catch (Exception e) {
                        initDone = false;
                        BioBankPlugin.openAsyncError(
                            "Error while creating children", e);
                    }
                    refresh(initDone, false);
                    monitor.done();
                    Display.getDefault().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            containerWidget.getMultiSelectionManager()
                                .clearMultiSelection();
                        }
                    });
                }
            });

        } catch (Exception e) {
            BioBankPlugin.openAsyncError("Error while creating children", e);
            refresh(false, false);
        }
    }

    private void deleteSelection(final ContainerTypeWrapper type) {
        IRunnableContext context = new ProgressMonitorDialog(Display
            .getDefault().getActiveShell());
        try {
            context.run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(final IProgressMonitor monitor) {
                    monitor.beginTask("Deleting...", IProgressMonitor.UNKNOWN);
                    boolean deleteDones = false;
                    try {

                        Set<RowColPos> positions = containerWidget
                            .getMultiSelectionManager().getSelectedPositions();
                        deleteDones = container.deleteChildrenWithType(type,
                            positions);
                    } catch (Exception ex) {
                        BioBankPlugin.openAsyncError("Can't Delete Containers",
                            ex);
                    }
                    refresh(deleteDones, true);
                    monitor.done();
                    Display.getDefault().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            containerWidget.getMultiSelectionManager()
                                .clearMultiSelection();
                        }
                    });
                }
            });
        } catch (Exception e) {
            BioBankPlugin.openAsyncError("Can't Delete Containers", e);
            refresh(false, false);
        }
    }

    private void refresh(boolean initDone, final boolean rebuild) {
        if (initDone) {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        reload();
                    } catch (Exception e) {
                        logger.error("Error loading", e);
                    }
                    if (rebuild) {
                        containerAdapter.rebuild();
                    }
                    containerAdapter.performExpand();
                }
            });
        }
    }

    private void openFormFor(ContainerCell cell) {
        ContainerAdapter newAdapter = null;
        if (cell.getStatus() == CellStatus.NOT_INITIALIZED) {
            if (canCreate) {
                ContainerWrapper containerToOpen = cell.getContainer();
                if (containerToOpen == null) {
                    containerToOpen = new ContainerWrapper(appService);
                }
                containerToOpen.setSite(containerAdapter.getParentFromClass(
                    SiteAdapter.class).getWrapper());
                containerToOpen.setParent(container);
                containerToOpen.setPosition(new RowColPos(cell.getRow(), cell
                    .getCol()));
                newAdapter = new ContainerAdapter(containerAdapter,
                    containerToOpen);
                containerToOpen.setSite(containerAdapter.getParentFromClass(
                    SiteAdapter.class).getWrapper());
                containerToOpen.setParent(container);
                containerToOpen.setPosition(new RowColPos(cell.getRow(), cell
                    .getCol()));
                newAdapter = new ContainerAdapter(containerAdapter,
                    containerToOpen);
                newAdapter.openEntryForm(true);
            }
        } else {
            ContainerWrapper child = cell.getContainer();
            Assert.isNotNull(child);
            SessionManager.openViewForm(child);
        }
        containerAdapter.performExpand();
    }

    private void setContainerValues() {
        setTextValue(siteLabel, container.getSite().getName());
        setTextValue(containerLabelLabel, container.getLabel());
        setTextValue(productBarcodeLabel, container.getProductBarcode());
        setTextValue(activityStatusLabel, container.getActivityStatus());
        setTextValue(commentsLabel, container.getComment());
        setTextValue(containerTypeLabel, container.getContainerType().getName());
        setTextValue(temperatureLabel, container.getTemperature());
        if (container.hasParent()) {
            if (rowLabel != null) {
                setTextValue(rowLabel, container.getPosition().row);
            }

            if (colLabel != null) {
                setTextValue(colLabel, container.getPosition().col);
            }
        }
    }

    private void createAliquotsSection() {
        Composite parent = createSectionWithClient("Aliquots");
        List<AliquotWrapper> aliquots = new ArrayList<AliquotWrapper>(container
            .getAliquots().values());
        aliquotsWidget = new AliquotListInfoTable(parent, aliquots);
        aliquotsWidget.adaptToToolkit(toolkit, true);
        aliquotsWidget.addClickListener(collectionDoubleClickListener);
    }

    @Override
    public void reload() throws Exception {
        if (!form.isDisposed()) {
            container.reload();
            form.setText("Container " + container.getLabel() + " ("
                + container.getContainerType().getNameShort() + ")");
            if (container.getContainerType().getChildContainerTypeCollection()
                .size() > 0)
                refreshVis();
            setContainerValues();
            List<ContainerTypeWrapper> containerTypes = container
                .getContainerType().getChildContainerTypeCollection();
            List<Object> deleteComboList = new ArrayList<Object>();
            deleteComboList.add("All");
            deleteComboList.addAll(containerTypes);

            if (initSelectionCv != null) {
                initSelectionCv.setInput(containerTypes);
                initSelectionCv.getCombo().select(0);
            }
            if (deleteCv != null) {
                deleteCv.setInput(deleteComboList);
                deleteCv.getCombo().select(0);
            }

            if (aliquotsWidget != null) {
                aliquotsWidget.reloadCollection(new ArrayList<AliquotWrapper>(
                    container.getAliquots().values()));
            }
        }
    }

}
