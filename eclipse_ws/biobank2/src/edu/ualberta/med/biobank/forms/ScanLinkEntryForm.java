package edu.ualberta.med.biobank.forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import edu.ualberta.med.biobank.BiobankPlugin;
import edu.ualberta.med.biobank.Messages;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.util.RowColPos;
import edu.ualberta.med.biobank.common.wrappers.ActivityStatusWrapper;
import edu.ualberta.med.biobank.common.wrappers.AliquotedSpecimenWrapper;
import edu.ualberta.med.biobank.common.wrappers.CenterWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerLabelingSchemeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.OriginInfoWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenLinkWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenWrapper;
import edu.ualberta.med.biobank.logs.BiobankLogger;
import edu.ualberta.med.biobank.model.Cell;
import edu.ualberta.med.biobank.model.CellStatus;
import edu.ualberta.med.biobank.model.PalletCell;
import edu.ualberta.med.biobank.preferences.PreferenceConstants;
import edu.ualberta.med.biobank.widgets.AliquotedSpecimenSelectionWidget;
import edu.ualberta.med.biobank.widgets.grids.ScanPalletWidget;
import edu.ualberta.med.biobank.widgets.grids.selection.MultiSelectionEvent;
import edu.ualberta.med.biobank.widgets.grids.selection.MultiSelectionListener;
import edu.ualberta.med.biobank.widgets.grids.selection.MultiSelectionSpecificBehaviour;
import edu.ualberta.med.scannerconfig.dmscanlib.ScanCell;
import gov.nih.nci.system.applicationservice.ApplicationException;

/**
 * Link aliquoted specimens to their source specimens
 */
public class ScanLinkEntryForm extends AbstractPalletSpecimenAdminForm {

    public static final String ID = "edu.ualberta.med.biobank.forms.ScanLinkEntryForm"; //$NON-NLS-1$

    private static BiobankLogger logger = BiobankLogger
        .getLogger(ScanLinkEntryForm.class.getName());

    private LinkFormPatientManagement linkFormPatientManagement;

    private ScanPalletWidget spw;

    // choose selection mode - deactivated by default
    private Composite radioComponents;

    // select per row
    private Composite typesSelectionPerRowComposite;
    private List<AliquotedSpecimenSelectionWidget> specimenTypesWidgets;

    // custom selection with mouse
    private Composite typesSelectionCustomComposite;
    private AliquotedSpecimenSelectionWidget customSelectionWidget;

    // should be set to true when all scanned aliquots have a type set
    private IObservableValue typesFilledValue = new WritableValue(Boolean.TRUE,
        Boolean.class);

    // button to choose a fake scan - debug only
    private Button fakeScanRandom;

    // sampleTypes for containers of type that contains 'palletNameContains'
    private List<SpecimenTypeWrapper> authorizedPalletSpecimenTypes;

    private Composite fieldsComposite;

    private boolean processScanResult;

    private boolean isFakeScanRandom;

    private ScrolledComposite containersScroll;

    private List<ModelWrapper<?>[]> preSelections;

    private CenterWrapper<?> currentSelectedCentre;

    @Override
    protected void init() throws Exception {
        super.init();
        setPartName(Messages.getString("ScanLink.tabTitle")); //$NON-NLS-1$
        linkFormPatientManagement = new LinkFormPatientManagement(
            widgetCreator, this);
        setCanLaunchScan(true);
    }

    @Override
    protected String getOkMessage() {
        return Messages.getString("ScanLink.okMessage"); //$NON-NLS-1$
    }

    @Override
    protected void createFormContent() throws Exception {
        form.setText(Messages.getString("ScanLink.form.title")); //$NON-NLS-1$
        GridLayout layout = new GridLayout(2, false);
        page.setLayout(layout);

        createFieldsComposite();

        createPalletSection();

        createCancelConfirmWidget();

        AliquotedSpecimenSelectionWidget lastWidget = specimenTypesWidgets
            .get(specimenTypesWidgets.size() - 1);
        lastWidget.setNextWidget(cancelConfirmWidget);

        addBooleanBinding(new WritableValue(Boolean.TRUE, Boolean.class),
            typesFilledValue,
            Messages.getString("ScanLink.sampleType.select.validationMsg")); //$NON-NLS-1$
    }

    /**
     * Pallet visualisation
     */
    private void createPalletSection() {
        containersScroll = new ScrolledComposite(page, SWT.H_SCROLL);
        containersScroll.setExpandHorizontal(true);
        containersScroll.setExpandVertical(true);
        containersScroll.setLayout(new FillLayout());
        GridData scrollData = new GridData();
        scrollData.horizontalAlignment = SWT.FILL;
        scrollData.grabExcessHorizontalSpace = true;
        containersScroll.setLayoutData(scrollData);
        Composite client = toolkit.createComposite(containersScroll);
        GridLayout layout = new GridLayout(2, false);
        client.setLayout(layout);
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.CENTER;
        gd.grabExcessHorizontalSpace = true;
        client.setLayoutData(gd);
        containersScroll.setContent(client);

        spw = new ScanPalletWidget(client,
            CellStatus.DEFAULT_PALLET_SCAN_LINK_STATUS_LIST);
        spw.setVisible(true);
        toolkit.adapt(spw);
        spw.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));

        spw.getMultiSelectionManager().addMultiSelectionListener(
            new MultiSelectionListener() {
                @Override
                public void selectionChanged(MultiSelectionEvent mse) {
                    customSelectionWidget.setNumber(mse.selections);
                }
            });
        spw.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                scanTubeAlone(e);
            }
        });
        spw.loadProfile(profilesCombo.getCombo().getText());

        createScanTubeAloneButton(client);

        containersScroll.setMinSize(client
            .computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * Specimen types selection.
     */
    private void createTypesSelectionSection(Composite parent) throws Exception {
        // Radio buttons
        radioComponents = toolkit.createComposite(parent);
        RowLayout compLayout = new RowLayout();
        radioComponents.setLayout(compLayout);
        toolkit.paintBordersFor(radioComponents);
        GridData gd = new GridData();
        gd.horizontalSpan = 3;
        radioComponents.setLayoutData(gd);
        radioComponents.setEnabled(false);

        // radio button to choose how the sample types are selected
        final Button radioRowSelection = toolkit.createButton(radioComponents,
            Messages.getString("ScanLink.rowChoice.label"), SWT.RADIO); //$NON-NLS-1$
        final Button radioCustomSelection = toolkit.createButton(
            radioComponents,
            Messages.getString("ScanLink.customChoice.label"), SWT.RADIO); //$NON-NLS-1$
        IPreferenceStore store = BiobankPlugin.getDefault()
            .getPreferenceStore();
        boolean hideRadio = store
            .getBoolean(PreferenceConstants.SCAN_LINK_ROW_SELECT_ONLY);
        radioComponents.setVisible(!hideRadio);

        // stackLayout
        final Composite selectionComp = toolkit.createComposite(parent);
        final StackLayout selectionStackLayout = new StackLayout();
        selectionComp.setLayout(selectionStackLayout);

        initAuthorizedPalletSpecimenTypeList();
        createTypeSelectionPerRowComposite(selectionComp, null,
            authorizedPalletSpecimenTypes);
        createTypeSelectionCustom(selectionComp, null,
            authorizedPalletSpecimenTypes);
        radioRowSelection.setSelection(true);
        selectionStackLayout.topControl = typesSelectionPerRowComposite;

        radioRowSelection.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (radioRowSelection.getSelection()) {
                    selectionStackLayout.topControl = typesSelectionPerRowComposite;
                    selectionComp.layout();
                    for (AliquotedSpecimenSelectionWidget sampleType : specimenTypesWidgets) {
                        sampleType.addBinding(widgetCreator);
                        sampleType.resetValues(true, false);
                    }
                    customSelectionWidget.addBinding(widgetCreator);
                    spw.getMultiSelectionManager().disableMultiSelection();
                    typesFilledValue.setValue(Boolean.TRUE);
                    spw.redraw();
                }
            }
        });
        radioCustomSelection.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (radioCustomSelection.getSelection()) {
                    selectionStackLayout.topControl = typesSelectionCustomComposite;
                    selectionComp.layout();
                    for (AliquotedSpecimenSelectionWidget sampleType : specimenTypesWidgets) {
                        sampleType.removeBinding(widgetCreator);
                    }
                    customSelectionWidget.addBinding(widgetCreator);
                    spw.getMultiSelectionManager().enableMultiSelection(
                        new MultiSelectionSpecificBehaviour() {
                            @Override
                            public void removeSelection(Cell cell) {
                                PalletCell pCell = (PalletCell) cell;
                                if (pCell != null && pCell.getValue() != null) {
                                    pCell.setSpecimenType(null);
                                    pCell.setStatus(CellStatus.NO_TYPE);
                                }
                            }

                            @Override
                            public boolean isSelectable(Cell cell) {
                                return ((PalletCell) cell).getValue() != null;
                            }
                        });
                    typesFilledValue.setValue(spw.isEverythingTyped());
                    spw.redraw();
                }
            }
        });
    }

    private void initAuthorizedPalletSpecimenTypeList()
        throws ApplicationException {
        SiteWrapper currentSite = SessionManager.getUser()
            .getCurrentWorkingSite();
        if (currentSite != null) {
            // FIXME should not need check on container types when is in a
            // clinic
            authorizedPalletSpecimenTypes = SpecimenTypeWrapper
                .getSpecimenTypeForPallet96(appService, currentSite);
            if (authorizedPalletSpecimenTypes.size() == 0) {
                BiobankPlugin.openAsyncError(Messages
                    .getString("ScanLink.dialog.sampleTypesError.title"), //$NON-NLS-1$
                    Messages.getString("ScanLink.dialog.sampleTypesError.msg")); //$NON-NLS-1$
            }
        }
    }

    /**
     * Give a sample type to selected aliquots
     */
    private void createTypeSelectionCustom(Composite parent,
        List<SpecimenLinkWrapper> sourceSpecimenLinks,
        List<SpecimenTypeWrapper> resultSpecimenTypes) {
        typesSelectionCustomComposite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(4, false);
        typesSelectionCustomComposite.setLayout(layout);
        toolkit.paintBordersFor(typesSelectionCustomComposite);

        Label label = toolkit.createLabel(typesSelectionCustomComposite,
            Messages.getString("ScanLink.custom.type.label")); //$NON-NLS-1$
        GridData gd = new GridData();
        gd.horizontalSpan = 3;
        label.setLayoutData(gd);

        customSelectionWidget = new AliquotedSpecimenSelectionWidget(
            typesSelectionCustomComposite, null, sourceSpecimenLinks,
            resultSpecimenTypes, toolkit);
        customSelectionWidget.resetValues(true, true);

        Button applyType = toolkit.createButton(typesSelectionCustomComposite,
            "Apply", SWT.PUSH); //$NON-NLS-1$
        applyType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ModelWrapper<?>[] selection = customSelectionWidget
                    .getSelection();
                if (selection != null) {
                    for (Cell cell : spw.getMultiSelectionManager()
                        .getSelectedCells()) {
                        PalletCell pCell = (PalletCell) cell;
                        setTypeToCell(pCell, selection);
                    }
                    spw.getMultiSelectionManager().clearMultiSelection();
                    customSelectionWidget.resetValues(true, true);
                    typesFilledValue.setValue(spw.isEverythingTyped());
                    spw.redraw();
                }
            }
        });
    }

    /**
     * give sample type row per row (default)
     */
    private void createTypeSelectionPerRowComposite(Composite parent,
        List<SpecimenLinkWrapper> sourceSpecimenLinks,
        List<SpecimenTypeWrapper> resultSpecimenTypes) {
        typesSelectionPerRowComposite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(4, false);
        layout.horizontalSpacing = 10;
        typesSelectionPerRowComposite.setLayout(layout);
        toolkit.paintBordersFor(typesSelectionPerRowComposite);
        GridData gd = new GridData();
        gd.widthHint = 500;
        typesSelectionPerRowComposite.setLayoutData(gd);

        toolkit.createLabel(typesSelectionPerRowComposite, ""); //$NON-NLS-1$
        toolkit.createLabel(typesSelectionPerRowComposite,
            Messages.getString("ScanLink.source.column.title")); //$NON-NLS-1$
        toolkit.createLabel(typesSelectionPerRowComposite,
            Messages.getString("ScanLink.result.column.title")); //$NON-NLS-1$
        toolkit.createLabel(typesSelectionPerRowComposite, ""); //$NON-NLS-1$

        specimenTypesWidgets = new ArrayList<AliquotedSpecimenSelectionWidget>();
        AliquotedSpecimenSelectionWidget precedent = null;
        for (int i = 0; i < ScanCell.ROW_MAX; i++) {
            final AliquotedSpecimenSelectionWidget typeWidget = new AliquotedSpecimenSelectionWidget(
                typesSelectionPerRowComposite,
                ContainerLabelingSchemeWrapper.SBS_ROW_LABELLING_PATTERN
                    .charAt(i), sourceSpecimenLinks, resultSpecimenTypes,
                toolkit);
            final int indexRow = i;
            typeWidget
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    @Override
                    public void selectionChanged(SelectionChangedEvent event) {
                        updateRowType(typeWidget, indexRow);
                        if (spw.isEverythingTyped()) {
                            setDirty(true);
                        }
                    }

                });
            typeWidget.addBinding(widgetCreator);
            specimenTypesWidgets.add(typeWidget);
            if (precedent != null) {
                precedent.setNextWidget(typeWidget);
            }
            precedent = typeWidget;
        }
    }

    private void createFieldsComposite() throws Exception {
        Composite leftSideComposite = toolkit.createComposite(page);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        leftSideComposite.setLayout(layout);
        GridData gd = new GridData();
        gd.verticalAlignment = SWT.TOP;
        leftSideComposite.setLayoutData(gd);
        toolkit.paintBordersFor(leftSideComposite);

        fieldsComposite = toolkit.createComposite(leftSideComposite);
        layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        fieldsComposite.setLayout(layout);
        toolkit.paintBordersFor(fieldsComposite);
        gd = new GridData();
        gd.widthHint = 500;
        gd.verticalAlignment = SWT.TOP;
        fieldsComposite.setLayoutData(gd);

        linkFormPatientManagement.createPatientNumberText(fieldsComposite);
        linkFormPatientManagement.createCollectionEventWidgets(fieldsComposite);

        createProfileComboBox(fieldsComposite);
        // specific for scan link:
        profilesCombo.getCombo().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                spw.loadProfile(profilesCombo.getCombo().getText());
            }
        });
        // FIXME 666 ????? what is this suppose to do ?
        profilesCombo.getCombo().notifyListeners(666, new Event());

        createPlateToScanField(fieldsComposite);

        createScanButton(leftSideComposite);

        createTypesSelectionSection(leftSideComposite);

    }

    @Override
    protected void createFakeOptions(Composite fieldsComposite) {
        GridData gd;
        Composite comp = toolkit.createComposite(fieldsComposite);
        comp.setLayout(new GridLayout());
        gd = new GridData();
        gd.horizontalSpan = 3;
        gd.widthHint = 400;
        comp.setLayoutData(gd);
        fakeScanRandom = toolkit.createButton(comp, "Get random scan values", //$NON-NLS-1$
            SWT.RADIO);
        fakeScanRandom.setSelection(true);
        toolkit.createButton(comp,
            "Get random and already linked aliquots", SWT.RADIO); //$NON-NLS-1$
    }

    @Override
    protected void afterScanAndProcess() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                typesSelectionPerRowComposite.setEnabled(processScanResult);
                for (AliquotedSpecimenSelectionWidget typeWidget : specimenTypesWidgets) {
                    if (typeWidget.canFocus()) {
                        typeWidget.setFocus();
                        break;
                    }
                }
                // Show result in grid
                spw.setCells(getCells());
                setRescanMode();
                // not needed on windows. This was if the textfield number
                // go after 9, needed to resize on linux : need to check that
                // again
                // form.layout(true, true);
            }
        });
        setScanValid(processScanResult);
    }

    @Override
    protected void beforeScanThreadStart() {
        isFakeScanRandom = fakeScanRandom != null
            && fakeScanRandom.getSelection();
        currentSelectedCentre = SessionManager.getUser()
            .getCurrentWorkingCentre();
        preSelections = new ArrayList<ModelWrapper<?>[]>();
        for (AliquotedSpecimenSelectionWidget stw : specimenTypesWidgets) {
            preSelections.add(stw.getSelection());
        }
    }

    @Override
    protected Map<RowColPos, PalletCell> getFakeScanCells() throws Exception {
        if (isFakeScanRandom) {
            return PalletCell.getRandomScanLink();
        }
        try {
            return PalletCell.getRandomScanLinkWithAliquotsAlreadyLinked(
                appService, currentSelectedCentre.getId());
        } catch (Exception ex) {
            BiobankPlugin.openAsyncError("Fake Scan problem", ex); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * go through cells retrieved from scan, set status and update the types
     * combos components
     */
    @Override
    protected void processScanResult(IProgressMonitor monitor) throws Exception {
        processScanResult = false;
        boolean everythingOk = true;
        Map<RowColPos, PalletCell> cells = getCells();
        if (cells != null) {
            final Map<Integer, Integer> typesRows = new HashMap<Integer, Integer>();
            for (RowColPos rcp : cells.keySet()) {
                monitor.subTask(Messages.getString(
                    "ScanLink.scan.monitor.position", //$NON-NLS-1$
                    ContainerLabelingSchemeWrapper.rowColToSbs(rcp)));
                Integer typesRowsCount = typesRows.get(rcp.row);
                if (typesRowsCount == null) {
                    typesRowsCount = 0;
                    specimenTypesWidgets.get(rcp.row).resetValues(
                        !isRescanMode(), true, true);
                }
                PalletCell cell = null;
                cell = cells.get(rcp);
                if (!isRescanMode()
                    || (cell != null && cell.getStatus() != CellStatus.TYPE && cell
                        .getStatus() != CellStatus.NO_TYPE)) {
                    processCellStatus(cell, false);
                }
                everythingOk = cell.getStatus() != CellStatus.ERROR
                    && everythingOk;
                if (PalletCell.hasValue(cell)) {
                    typesRowsCount++;
                    typesRows.put(rcp.row, typesRowsCount);
                }
            }
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    setCombosLists(typesRows);
                }
            });
            processScanResult = everythingOk;
        }
    }

    /**
     * Get types only defined in the patient's study. Then set these types to
     * the types combos
     */
    private void setCombosLists(Map<Integer, Integer> typesRows) {
        List<SpecimenTypeWrapper> studiesAliquotedTypes = null;
        if (isFirstSuccessfulScan()) {
            // done at first successful scan
            studiesAliquotedTypes = new ArrayList<SpecimenTypeWrapper>();
            for (AliquotedSpecimenWrapper ss : linkFormPatientManagement
                .getCurrentPatient().getStudy()
                .getAliquotedSpecimenCollection(true)) {
                if (ss.getActivityStatus().isActive()) {
                    SpecimenTypeWrapper type = ss.getSpecimenType();
                    if (authorizedPalletSpecimenTypes.contains(type)) {
                        studiesAliquotedTypes.add(type);
                    }
                }
            }
            if (studiesAliquotedTypes.size() == 0) {
                BiobankPlugin.openAsyncError(Messages
                    .getString("ScanLink.aliquotedSpecimenTypes.error.title"), //$NON-NLS-1$
                    Messages.getString(
                        "ScanLink.aliquotedSpecimenTypes.error.msg", //$NON-NLS-1$
                        linkFormPatientManagement.getCurrentPatient()
                            .getStudy().getNameShort()));
            }
        }
        List<SpecimenLinkWrapper> availableSourceSpecimenLinks = linkFormPatientManagement
            .getSpecimenLinksInCollectionEvent();
        // set the list of aliquoted types to all widgets, in case the list is
        // activated using the handheld scanner
        for (int row = 0; row < specimenTypesWidgets.size(); row++) {
            AliquotedSpecimenSelectionWidget widget = specimenTypesWidgets
                .get(row);
            Integer number = typesRows.get(row);
            if (number != null)
                widget.setNumber(number);
            if (isFirstSuccessfulScan()) {
                widget.setSourceSpecimenLinks(availableSourceSpecimenLinks);
                widget.setResultTypes(studiesAliquotedTypes);
            }
        }
    }

    /**
     * Process the cell: apply a status and set correct information
     * 
     * @throws BiobankCheckException
     */
    private CellStatus processCellStatus(PalletCell cell,
        boolean independantProcess) throws ApplicationException,
        BiobankCheckException {
        if (cell == null) {
            return CellStatus.EMPTY;
        } else {
            String value = cell.getValue();
            if (value != null) {
                SpecimenWrapper foundAliquot = SpecimenWrapper.getSpecimen(
                    appService, value, SessionManager.getUser());
                if (foundAliquot != null) {
                    cell.setStatus(CellStatus.ERROR);
                    cell.setInformation(Messages
                        .getString("ScanLink.scanStatus.aliquot.alreadyExists")); //$NON-NLS-1$
                    String palletPosition = ContainerLabelingSchemeWrapper
                        .rowColToSbs(new RowColPos(cell.getRow(), cell.getCol()));
                    appendLogNLS("ScanLink.activitylog.aliquot.existsError",
                        palletPosition, value, foundAliquot
                            .getCollectionEvent().getVisitNumber(),
                        foundAliquot.getCollectionEvent().getPatient()
                            .getPnumber(), foundAliquot.getCurrentCenter()
                            .getNameShort());
                } else {
                    cell.setStatus(CellStatus.NO_TYPE);
                    if (independantProcess) {
                        AliquotedSpecimenSelectionWidget widget = specimenTypesWidgets
                            .get(cell.getRow());
                        widget.increaseNumber();
                    }
                    ModelWrapper<?>[] selection = preSelections.get(cell
                        .getRow());
                    if (selection != null)
                        setTypeToCell(cell, selection);
                }
            } else {
                cell.setStatus(CellStatus.EMPTY);
            }
            return cell.getStatus();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void saveForm() throws Exception {
        Map<RowColPos, PalletCell> cells = (Map<RowColPos, PalletCell>) spw
            .getCells();
        StringBuffer sb = new StringBuffer("ALIQUOTED SPECIMENS:\n"); //$NON-NLS-1$
        int nber = 0;
        ActivityStatusWrapper activeStatus = ActivityStatusWrapper
            .getActiveActivityStatus(appService);
        OriginInfoWrapper originInfo = new OriginInfoWrapper(
            SessionManager.getAppService());
        originInfo
            .setCenter(SessionManager.getUser().getCurrentWorkingCentre());
        originInfo.persist();
        Map<Integer, SpecimenLinkWrapper> links = new HashMap<Integer, SpecimenLinkWrapper>();
        Map<Integer, List<SpecimenWrapper>> newSpecimens = new HashMap<Integer, List<SpecimenWrapper>>();
        for (PalletCell cell : cells.values()) {
            if (PalletCell.hasValue(cell)
                && cell.getStatus() == CellStatus.TYPE) {
                SpecimenLinkWrapper sourceSpecimenLink = cell
                    .getSourceSpecimenLink();
                List<SpecimenWrapper> aliquotedSpecimens = newSpecimens
                    .get(sourceSpecimenLink.getId());
                // link not yet retrieved
                if (aliquotedSpecimens == null) {
                    links.put(sourceSpecimenLink.getId(), sourceSpecimenLink);
                    aliquotedSpecimens = new ArrayList<SpecimenWrapper>();
                    newSpecimens.put(sourceSpecimenLink.getId(),
                        aliquotedSpecimens);
                }
                SpecimenWrapper aliquotedSpecimen = cell.getSpecimen();
                aliquotedSpecimen.setInventoryId(cell.getValue());
                aliquotedSpecimen.setCreatedAt(new Date());
                aliquotedSpecimen.setActivityStatus(activeStatus);
                aliquotedSpecimen.setCurrentCenter(currentSelectedCentre);
                aliquotedSpecimen.setOriginInfo(originInfo);
                aliquotedSpecimen.setParentSpecimenLink(sourceSpecimenLink);
                aliquotedSpecimen.setCollectionEvent(sourceSpecimenLink
                    .getParentSpecimen().getCollectionEvent());
                aliquotedSpecimens.add(aliquotedSpecimen);

                // LINKED\: {0} - Type: {1} - Patient\: {2} - Visit\: {3} -
                // Centre: {4} \n
                sb.append(Messages.getString(
                    "ScanLink.activitylog.aliquot.linked", //$NON-NLS-1$
                    cell.getValue(), cell.getType().getName(),
                    sourceSpecimenLink.getParentSpecimen().getCollectionEvent()
                        .getPatient().getPnumber(), sourceSpecimenLink
                        .getParentSpecimen().getCollectionEvent()
                        .getVisitNumber(), currentSelectedCentre.getNameShort()));
                nber++;
            }
        }

        for (SpecimenLinkWrapper link : links.values()) {
            link.addToChildSpecimenCollection(newSpecimens.get(link.getId()));
            link.persist();
        }
        appendLog(sb.toString());

        // SCAN-LINK\: {0} specimens linked to patient {1} on centre {2}
        appendLogNLS("ScanLink.activitylog.save.summary", nber, //$NON-NLS-1$
            linkFormPatientManagement.getCurrentPatient().getPnumber(),
            currentSelectedCentre.getNameShort());
        setFinished(false);
    }

    /**
     * update types of specimens of one given row
     */
    @SuppressWarnings("unchecked")
    private void updateRowType(AliquotedSpecimenSelectionWidget typeWidget,
        int indexRow) {
        if (typeWidget.needToSave()) {
            ModelWrapper<?>[] selection = typeWidget.getSelection();
            if (selection != null) {
                Map<RowColPos, PalletCell> cells = (Map<RowColPos, PalletCell>) spw
                    .getCells();
                if (cells != null) {
                    for (RowColPos rcp : cells.keySet()) {
                        if (rcp.row == indexRow) {
                            PalletCell cell = cells.get(rcp);
                            if (PalletCell.hasValue(cell)) {
                                setTypeToCell(cell, selection);
                            }
                        }
                    }
                    spw.redraw();
                }
            }
        }
    }

    private void setTypeToCell(PalletCell cell, ModelWrapper<?>[] selection) {
        cell.setSourceSpecimenLink((SpecimenLinkWrapper) selection[0]);
        cell.setSpecimenType((SpecimenTypeWrapper) selection[1]);
        cell.setStatus(CellStatus.TYPE);
    }

    @Override
    public void reset() throws Exception {
        super.reset();
        setDirty(false);
        fieldsComposite.setEnabled(true);
        setScanValid(true);
        reset(true);
    }

    public void reset(boolean resetAll) {
        linkFormPatientManagement.reset(resetAll);
        cancelConfirmWidget.reset();
        removeRescanMode();
        setScanHasBeenLauched(false);
        if (resetAll) {
            resetPlateToScan();
            spw.setCells(null);
            for (AliquotedSpecimenSelectionWidget stw : specimenTypesWidgets) {
                stw.resetValues(true, true);
            }
        }
        setFocus();
    }

    @Override
    public String getNextOpenedFormID() {
        return ID;
    }

    @Override
    protected String getActivityTitle() {
        return "Scan link activity"; //$NON-NLS-1$
    }

    @Override
    protected void disableFields() {
        fieldsComposite.setEnabled(false);
    }

    @Override
    public BiobankLogger getErrorLogger() {
        return logger;
    }

    @Override
    protected void postprocessScanTubeAlone(PalletCell cell) throws Exception {
        CellStatus status = processCellStatus(cell, true);
        boolean ok = isScanValid() && (status != CellStatus.ERROR);
        setScanValid(ok);
        typesSelectionPerRowComposite.setEnabled(ok);
        spw.redraw();
        form.layout();
    }

    @Override
    protected boolean fieldsValid() {
        return isPlateValid() && linkFormPatientManagement.fieldsValid();
    }

}
