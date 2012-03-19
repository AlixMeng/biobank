package edu.ualberta.med.biobank.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.action.info.OriginInfoSaveInfo;
import edu.ualberta.med.biobank.common.action.info.ShipmentInfoSaveInfo;
import edu.ualberta.med.biobank.common.action.info.ShipmentReadInfo;
import edu.ualberta.med.biobank.common.action.originInfo.OriginInfoSaveAction;
import edu.ualberta.med.biobank.common.action.shipment.ShipmentGetInfoAction;
import edu.ualberta.med.biobank.common.action.specimen.SpecimenInfo;
import edu.ualberta.med.biobank.common.peer.ShipmentInfoPeer;
import edu.ualberta.med.biobank.common.wrappers.CenterWrapper;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.CommentWrapper;
import edu.ualberta.med.biobank.common.wrappers.OriginInfoWrapper;
import edu.ualberta.med.biobank.common.wrappers.ShipmentInfoWrapper;
import edu.ualberta.med.biobank.common.wrappers.ShippingMethodWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenWrapper;
import edu.ualberta.med.biobank.common.wrappers.helpers.SiteQuery;
import edu.ualberta.med.biobank.dialogs.SpecimenOriginSelectDialog;
import edu.ualberta.med.biobank.gui.common.validators.NonEmptyStringValidator;
import edu.ualberta.med.biobank.gui.common.widgets.BgcBaseText;
import edu.ualberta.med.biobank.gui.common.widgets.BgcEntryFormWidgetListener;
import edu.ualberta.med.biobank.gui.common.widgets.DateTimeWidget;
import edu.ualberta.med.biobank.gui.common.widgets.MultiSelectEvent;
import edu.ualberta.med.biobank.gui.common.widgets.utils.ComboSelectionUpdate;
import edu.ualberta.med.biobank.model.OriginInfo;
import edu.ualberta.med.biobank.model.ShipmentInfo;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.treeview.shipment.ShipmentAdapter;
import edu.ualberta.med.biobank.validators.NotNullValidator;
import edu.ualberta.med.biobank.views.SpecimenTransitView;
import edu.ualberta.med.biobank.widgets.SpecimenEntryWidget;
import edu.ualberta.med.biobank.widgets.SpecimenEntryWidget.ItemAction;
import edu.ualberta.med.biobank.widgets.infotables.CommentsInfoTable;
import edu.ualberta.med.biobank.widgets.listeners.VetoListenerSupport.Event;
import edu.ualberta.med.biobank.widgets.listeners.VetoListenerSupport.VetoException;
import edu.ualberta.med.biobank.widgets.listeners.VetoListenerSupport.VetoListener;
import edu.ualberta.med.biobank.widgets.utils.GuiUtil;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class ShipmentEntryForm extends BiobankEntryForm {

    public static final String ID =
        "edu.ualberta.med.biobank.forms.ShipmentEntryForm"; //$NON-NLS-1$

    public static final String MSG_NEW_SHIPMENT_OK =
        Messages.ShipmentEntryForm_new_ship_ok_msg;

    public static final String MSG_SHIPMENT_OK =
        Messages.ShipmentEntryForm_edit_ship_ok_msg;

    private ComboViewer senderComboViewer;

    private ComboViewer receiverComboViewer;

    private ComboViewer shippingMethodComboViewer;

    private SpecimenEntryWidget specimenEntryWidget;

    private Label waybillLabel;

    private NonEmptyStringValidator waybillValidator;

    private static final String WAYBILL_BINDING = "shipment-waybill-binding"; //$NON-NLS-1$

    private static final String DATE_SHIPPED_BINDING =
        "shipment-date-shipped-binding"; //$NON-NLS-1$

    private static final String BOX_NUMBER_BINDING = "box-number-binding"; //$NON-NLS-1$

    private DateTimeWidget dateSentWidget;

    private Label departedLabel;

    private NotNullValidator departedValidator;

    private BgcBaseText waybillWidget;

    private Set<SpecimenWrapper> removedSpecimensToPersist =
        new HashSet<SpecimenWrapper>();

    private BgcBaseText boxNumberWidget;

    private Label boxLabel;

    @SuppressWarnings("unused")
    private NonEmptyStringValidator boxValidator;

    protected boolean tryAgain;

    private CommentsInfoTable commentEntryTable;

    private BgcEntryFormWidgetListener listener =
        new BgcEntryFormWidgetListener() {
            @Override
            public void selectionChanged(MultiSelectEvent event) {
                setDirty(true);
            }
        };

    private ShipmentReadInfo oiInfo;

    private OriginInfoWrapper originInfo = new OriginInfoWrapper(
        SessionManager.getAppService());
    private ShipmentInfoWrapper shipmentInfo = new ShipmentInfoWrapper(
        SessionManager.getAppService());

    private CommentWrapper comment = new CommentWrapper(
        SessionManager.getAppService());

    private List<SpecimenInfo> specimens;

    @Override
    protected void init() throws Exception {
        Assert.isTrue(adapter instanceof ShipmentAdapter,
            "Invalid editor input: object of type " //$NON-NLS-1$
                + adapter.getClass().getName());

        setOiInfo(adapter.getId());

        setDefaultValues();

        String tabName;
        if (oiInfo == null) {
            tabName = Messages.ShipmentEntryForm_title_new;
        } else {
            tabName =
                NLS.bind(Messages.ShipmentEntryForm_title_edit, originInfo
                    .getShipmentInfo().getFormattedDateReceived());
        }
        setPartName(tabName);
    }

    private void setOiInfo(Integer id) throws ApplicationException {
        if (id == null) {
            OriginInfo oi = new OriginInfo();
            oi.setShipmentInfo(new ShipmentInfo());
            originInfo.setWrappedObject(oi);
            shipmentInfo.setWrappedObject(oi.getShipmentInfo());
            specimens = new ArrayList<SpecimenInfo>();
        } else {
            ShipmentReadInfo read =
                SessionManager.getAppService().doAction(
                    new ShipmentGetInfoAction(id));
            originInfo.setWrappedObject(read.originInfo);
            shipmentInfo.setWrappedObject(read.originInfo.getShipmentInfo());
            specimens = read.specimens;
            SessionManager.logLookup(read.originInfo);
        }

    }

    @Override
    protected void createFormContent() throws Exception {
        form.setText(Messages.ShipmentEntryForm_form_title);
        form.setMessage(getOkMessage(), IMessageProvider.NONE);
        page.setLayout(new GridLayout(1, false));
        createMainSection();
        createSpecimensSection();
    }

    private void createMainSection() throws Exception, ApplicationException {
        Composite client = toolkit.createComposite(page);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        senderComboViewer =
            createComboViewer(client, Messages.ShipmentEntryForm_sender_label,
                ClinicWrapper.getAllClinics(SessionManager.getAppService()),
                (ClinicWrapper) originInfo.getCenter(),
                Messages.ShipmentEntryForm_sender_validation_msg,
                new ComboSelectionUpdate() {
                    @Override
                    public void doSelection(Object selectedObject) {
                        originInfo.setCenter((CenterWrapper<?>) selectedObject);
                        activateWidgets(((ClinicWrapper) selectedObject)
                            .getSendsShipments());
                    }
                });
        setFirstControl(senderComboViewer.getControl());

        receiverComboViewer =
            createComboViewer(client,
                Messages.ShipmentEntryForm_receiver_label,
                SiteQuery.getSites(SessionManager.getAppService()),
                originInfo.getReceiverSite(),
                Messages.ShipmentEntryForm_receiver_validation_msg,
                new ComboSelectionUpdate() {
                    @Override
                    public void doSelection(Object selectedObject) {
                        originInfo
                            .setReceiverSite((SiteWrapper) selectedObject);
                    }
                });

        waybillLabel =
            widgetCreator.createLabel(client,
                Messages.ShipmentEntryForm_waybill_label);
        waybillLabel.setLayoutData(new GridData(
            GridData.VERTICAL_ALIGN_BEGINNING));
        waybillValidator =
            new NonEmptyStringValidator(
                Messages.ShipmentEntryForm_waybill_validation_msg);
        waybillWidget =
            (BgcBaseText) createBoundWidget(client, BgcBaseText.class,
                SWT.NONE, waybillLabel, new String[0],
                shipmentInfo,
                ShipmentInfoPeer.WAYBILL.getName(), waybillValidator,
                WAYBILL_BINDING);

        shippingMethodComboViewer =
            createComboViewer(client,
                Messages.ShipmentEntryForm_shipMethod_label,
                ShippingMethodWrapper.getShippingMethods(SessionManager
                    .getAppService()), shipmentInfo
                    .getShippingMethod(), null,
                new ComboSelectionUpdate() {
                    @Override
                    public void doSelection(Object selectedObject) {
                        ShippingMethodWrapper method =
                            (ShippingMethodWrapper) selectedObject;
                        shipmentInfo.setShippingMethod(method);
                        if (dateSentWidget != null && method != null) {
                            activateDepartedWidget(method.needDate());
                        }
                    }
                });

        departedLabel =
            widgetCreator.createLabel(client,
                Messages.ShipmentEntryForm_packed_label);
        departedLabel.setLayoutData(new GridData(
            GridData.VERTICAL_ALIGN_BEGINNING));
        departedValidator =
            new NotNullValidator(
                Messages.ShipmentEntryForm_packed_validation_msg);

        dateSentWidget =
            createDateTimeWidget(client, departedLabel,
                shipmentInfo.getPackedAt(),
                shipmentInfo,
                ShipmentInfoPeer.PACKED_AT.getName(), departedValidator,
                SWT.DATE | SWT.TIME, DATE_SHIPPED_BINDING);
        activateDepartedWidget(shipmentInfo.getShippingMethod() != null
            && shipmentInfo.getShippingMethod().needDate());

        boxLabel =
            widgetCreator.createLabel(client,
                Messages.ShipmentEntryForm_boxNber_label);
        boxLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        boxNumberWidget =
            (BgcBaseText) createBoundWidget(client, BgcBaseText.class,
                SWT.NONE, waybillLabel, new String[0],
                shipmentInfo,
                ShipmentInfoPeer.BOX_NUMBER.getName(), null, BOX_NUMBER_BINDING);

        ClinicWrapper clinic = (ClinicWrapper) originInfo.getCenter();
        if (clinic != null) {
            activateWidgets(clinic.getSendsShipments());
        }

        createDateTimeWidget(client, Messages.ShipmentEntryForm_received_label,
            shipmentInfo.getReceivedAt(),
            shipmentInfo,
            ShipmentInfoPeer.RECEIVED_AT.getName(), new NotNullValidator(
                Messages.ShipmentEntryForm_received_validation_msg));

        createCommentSection();

    }

    protected void activateWidgets(boolean sendsShipments) {
        if (waybillLabel != null && !waybillLabel.isDisposed()) {
            waybillLabel.setVisible(sendsShipments);
            ((GridData) waybillLabel.getLayoutData()).exclude = !sendsShipments;
        }
        if (waybillWidget != null && !waybillWidget.isDisposed()) {
            waybillWidget.setVisible(sendsShipments);
            ((GridData) waybillWidget.getLayoutData()).exclude =
                !sendsShipments;

            if (sendsShipments) {
                widgetCreator.addBinding(WAYBILL_BINDING);
            } else {
                widgetCreator.removeBinding(WAYBILL_BINDING);
                waybillWidget.setText(""); //$NON-NLS-1$
            }
        }

        boxNumberWidget.setVisible(sendsShipments);
        ((GridData) boxNumberWidget.getLayoutData()).exclude = !sendsShipments;
        boxLabel.setVisible(sendsShipments);
        ((GridData) boxLabel.getLayoutData()).exclude = !sendsShipments;
        if (sendsShipments) {
            widgetCreator.addBinding(BOX_NUMBER_BINDING);
        } else {
            widgetCreator.removeBinding(BOX_NUMBER_BINDING);
            boxNumberWidget.setText(""); //$NON-NLS-1$
        }
        form.layout(true, true);
    }

    protected void activateDepartedWidget(boolean departedNeeded) {
        dateSentWidget.setVisible(departedNeeded);
        ((GridData) dateSentWidget.getLayoutData()).exclude = !departedNeeded;
        departedLabel.setVisible(departedNeeded);
        ((GridData) departedLabel.getLayoutData()).exclude = !departedNeeded;
        if (departedNeeded) {
            widgetCreator.addBinding(DATE_SHIPPED_BINDING);
        } else {
            widgetCreator.removeBinding(DATE_SHIPPED_BINDING);
            shipmentInfo.setPackedAt(null);
        }
        form.layout(true, true);
    }

    private void createSpecimensSection() {
        Composite client =
            createSectionWithClient(Messages.ShipmentEntryForm_specimens_title);
        GridLayout layout = new GridLayout(1, false);
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL, GridData.FILL));
        toolkit.paintBordersFor(client);

        specimenEntryWidget =
            new SpecimenEntryWidget(client, SWT.NONE, toolkit,
                SessionManager.getAppService(), true);
        specimenEntryWidget
            .addSelectionChangedListener(new BgcEntryFormWidgetListener() {
                @Override
                public void selectionChanged(MultiSelectEvent event) {
                    setDirty(true);
                }
            });
        specimenEntryWidget
            .addDoubleClickListener(collectionDoubleClickListener);
        specimenEntryWidget.addBinding(widgetCreator,
            Messages.ShipmentEntryForm_specimens_validation_msg);

        VetoListener<ItemAction, SpecimenWrapper> vetoListener =
            new VetoListener<ItemAction, SpecimenWrapper>() {
                @Override
                public void handleEvent(Event<ItemAction, SpecimenWrapper> event)
                    throws VetoException {
                    SpecimenWrapper specimen = event.getObject();
                    switch (event.getType()) {
                    case PRE_ADD:
                        if (specimen == null)
                            throw new VetoException(
                                Messages.ShipmentEntryForm_notfound_error_msg);
                        if (!SessionManager.getUser().getCurrentWorkingCenter()
                            .equals(specimen.getCurrentCenter()))
                            throw new VetoException(
                                NLS.bind(
                                    Messages.ShipmentEntryForm_other_center_error_msg,
                                    specimen.getInventoryId(), specimen
                                        .getCurrentCenter().getNameShort()));
                        if (specimen.isUsedInDispatch())
                            throw new VetoException(
                                Messages.ShipmentEntryForm_dispatched_specimen_error_msg);
                        if (specimen.getParentContainer() != null)
                            throw new VetoException(
                                Messages.ShipmentEntryForm_stored_error_msg);
                        if (specimen.getOriginInfo() != null
                            && specimen.getOriginInfo().getShipmentInfo() != null
                            && !specimen.getOriginInfo().getShipmentInfo()
                                .equals(shipmentInfo))
                            throw new VetoException(
                                NLS.bind(
                                    Messages.ShipmentEntryForm_inAnother_ship_error_msg,
                                    specimen.getOriginInfo().getShipmentInfo()));
                        break;
                    case POST_ADD:
                        // action performs this now
                        break;
                    case PRE_DELETE:
                        if (!originInfo.isNew()) {
                            try {
                                List<CenterWrapper<?>> centers =
                                    CenterWrapper.getCenters(specimen
                                        .getAppService());
                                SpecimenOriginSelectDialog dlg =
                                    new SpecimenOriginSelectDialog(
                                        form.getShell(), specimen, centers);

                                if (dlg.open() == Window.OK) {
                                    removedSpecimensToPersist.add(specimen);
                                } else {
                                    throw new VetoException(
                                        Messages.ShipmentEntryForm_center_select_msg);
                                }
                            } catch (ApplicationException e) {
                                throw new VetoException(e.getMessage());
                            }

                        }
                        break;
                    case POST_DELETE:
                        originInfo.removeFromSpecimenCollection(Arrays
                            .asList(specimen));
                        break;
                    }
                }
            };

        specimenEntryWidget.addVetoListener(ItemAction.PRE_ADD, vetoListener);
        specimenEntryWidget.addVetoListener(ItemAction.POST_ADD, vetoListener);
        specimenEntryWidget
            .addVetoListener(ItemAction.PRE_DELETE, vetoListener);
        specimenEntryWidget.addVetoListener(ItemAction.POST_DELETE,
            vetoListener);

        specimenEntryWidget.setSpecimens(specimens);
    }

    private void createCommentSection() {
        Composite client = createSectionWithClient(Messages.Comments_title);
        GridLayout gl = new GridLayout(2, false);

        client.setLayout(gl);
        commentEntryTable =
            new CommentsInfoTable(client,
                originInfo.getCommentCollection(false));
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        commentEntryTable.setLayoutData(gd);
        createBoundWidgetWithLabel(client, BgcBaseText.class,
            SWT.MULTI, Messages.Comments_add, null, comment, "message", null);

    }

    @Override
    public String getNextOpenedFormId() {
        return ShipmentViewForm.ID;
    }

    @Override
    protected String getOkMessage() {
        return (originInfo.isNew()) ? MSG_NEW_SHIPMENT_OK : MSG_SHIPMENT_OK;
    }

    @Override
    protected void saveForm() throws Exception {

        Set<Integer> addedSpecimenIds = new HashSet<Integer>();
        for (SpecimenInfo info : specimenEntryWidget.getAddedSpecimens()) {
            addedSpecimenIds.add(info.specimen.getId());
        }
        Set<Integer> removedSpecimenIds = new HashSet<Integer>();
        for (SpecimenInfo info : specimenEntryWidget
            .getRemovedSpecimens()) {
            removedSpecimenIds.add(info.specimen.getId());
        }

        OriginInfoSaveInfo oiInfo =
            new OriginInfoSaveInfo(originInfo.getId(), originInfo
                .getReceiverSite().getId(), originInfo.getCenter().getId(),
                comment.getMessage() == null ? ""
                    : comment.getMessage(), addedSpecimenIds,
                removedSpecimenIds);
        ShipmentInfoSaveInfo siInfo =
            new ShipmentInfoSaveInfo(shipmentInfo.getId(),
                shipmentInfo.getBoxNumber(), originInfo
                    .getShipmentInfo()
                    .getPackedAt(),
                shipmentInfo.getReceivedAt(), originInfo
                    .getShipmentInfo().getWaybill(),
                shipmentInfo.getShippingMethod().getId());
        OriginInfoSaveAction save =
            new OriginInfoSaveAction(oiInfo, siInfo);
        originInfo.setId(SessionManager.getAppService().doAction(save).getId());
        ((AdapterBase) adapter).setModelObject(originInfo);
    }

    @Override
    protected void doAfterSave() throws Exception {
        if (tryAgain) {
            tryAgain = false;
            confirm();
        } else {
            SpecimenTransitView.reloadCurrent();
            if (!shipmentInfo.isReceivedToday())
                SpecimenTransitView.showShipment(originInfo);
        }
    }

    @Override
    public void setValues() throws Exception {
        // do not change origin if form reset
        removedSpecimensToPersist.clear();

        originInfo.setShipmentInfo(shipmentInfo);

        specimenEntryWidget.setSpecimens(specimens);

        setDefaultValues();
        GuiUtil.reset(senderComboViewer, originInfo.getCenter());
        GuiUtil.reset(receiverComboViewer, originInfo.getReceiverSite());
        GuiUtil.reset(shippingMethodComboViewer,
            shipmentInfo.getShippingMethod());
    }

    private void setDefaultValues() {
        if (originInfo.isNew()) {
            CenterWrapper<?> userCenter =
                SessionManager.getUser().getCurrentWorkingCenter();
            if (userCenter instanceof SiteWrapper) {
                originInfo.setReceiverSite((SiteWrapper) userCenter);
            }
            Date receivedAt = Calendar.getInstance().getTime();
            shipmentInfo.setReceivedAt(receivedAt);
        }
    }
}
