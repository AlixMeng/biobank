package edu.ualberta.med.biobank.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenTypeWrapper;
import edu.ualberta.med.biobank.validators.NonEmptyStringValidator;
import edu.ualberta.med.biobank.widgets.BasicSiteCombo;
import edu.ualberta.med.biobank.widgets.BiobankLabelProvider;
import edu.ualberta.med.biobank.widgets.BiobankText;
import edu.ualberta.med.biobank.widgets.BiobankWidget;
import edu.ualberta.med.biobank.widgets.utils.ComboSelectionUpdate;
import gov.nih.nci.system.applicationservice.ApplicationException;

/**
 * Allows the user to choose a container to which aliquots will be moved
 */
public class MoveAliquotsToDialog extends BiobankDialog {

    private ContainerWrapper oldContainer;

    private HashMap<String, ContainerWrapper> map = new HashMap<String, ContainerWrapper>();

    private BasicSiteCombo siteCombo;

    private ListViewer lv;

    private BiobankText newLabelText;

    private ISWTObservableValue listObserveSelection;

    private WritableValue selectedValue;

    public MoveAliquotsToDialog(Shell parent, ContainerWrapper oldContainer) {
        super(parent);
        Assert.isNotNull(oldContainer);
        this.oldContainer = oldContainer;
    }

    @Override
    protected String getDialogShellTitle() {
        return "Move aliquots from one container to another";
    }

    @Override
    protected String getTitleAreaMessage() {
        return "Select the new container that can hold the aliquots.\n"
            + "It should be initialized, empty, as big as the previous one,"
            + " and should accept these aliquots.";
    }

    @Override
    protected String getTitleAreaTitle() {
        return "Move aliquots from container " + oldContainer.getLabel()
            + " to another";
    }

    @Override
    protected void createDialogAreaInternal(Composite parent) throws Exception {
        Composite contents = new Composite(parent, SWT.NONE);
        contents.setLayout(new GridLayout(2, false));
        contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label siteLabel = widgetCreator
            .createLabel(contents, "Repository Site");
        siteLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
        siteCombo = new BasicSiteCombo(contents, widgetCreator,
            SessionManager.getAppService(), siteLabel, true,
            new ComboSelectionUpdate() {
                @Override
                public void doSelection(Object selectedObject) {
                    buildContainersMap();
                }
            });
        siteCombo.setSelectedSite(oldContainer.getSite(), false);
        if (!SessionManager.getUser().isWebsiteAdministrator()) {
            siteCombo.setEnabled(false);
            siteLabel
                .setToolTipText("Only Website administrator can move aliquot to another site");
        }

        newLabelText = (BiobankText) createBoundWidgetWithLabel(contents,
            BiobankText.class, SWT.FILL, "New Container Label", null, null,
            null, null);
        newLabelText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                lv.getList().deselectAll();
                lv.refresh();
                if (lv.getList().getItemCount() == 1) {
                    lv.getList().setSelection(0);
                }
                IStructuredSelection sel = (IStructuredSelection) lv
                    .getSelection();
                String currentSelection = null;
                if (sel.size() == 1)
                    currentSelection = (String) sel.getFirstElement();
                // to trigger the binding when we modify the selection by
                // code:
                listObserveSelection.setValue(currentSelection);
            }
        });

        Label listLabel = widgetCreator.createLabel(contents,
            "Available containers");
        lv = new ListViewer(contents);
        lv.setContentProvider(new ArrayContentProvider());
        lv.setLabelProvider(new BiobankLabelProvider());
        lv.setInput(map.keySet());
        lv.setComparator(new ViewerComparator());
        lv.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement,
                Object element) {
                return ((String) element).startsWith(newLabelText.getText());
            }
        });
        GridData gd = new GridData();
        gd.heightHint = 150;
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessHorizontalSpace = true;
        lv.getControl().setLayoutData(gd);

        // "Destination container should accept these aliquots, "
        // + "must be initialized but empty, "
        // + " and as big as the previous one.") {

        String errorMessage = "A label should be selected";
        NonEmptyStringValidator validator = new NonEmptyStringValidator(
            errorMessage);
        validator.setControlDecoration(BiobankWidget.createDecorator(listLabel,
            errorMessage));
        UpdateValueStrategy uvs = new UpdateValueStrategy();
        uvs.setAfterGetValidator(validator);
        selectedValue = new WritableValue("", String.class);
        listObserveSelection = SWTObservables.observeSelection(lv.getList());
        widgetCreator.bindValue(listObserveSelection, selectedValue, uvs, uvs);
    }

    protected void buildContainersMap() {
        map.clear();
        List<SpecimenTypeWrapper> typesFromOlContainer = oldContainer
            .getContainerType().getSpecimenTypeCollection();
        List<ContainerWrapper> conts = new ArrayList<ContainerWrapper>();
        if (siteCombo.getSelectedSite() != null)
            try {
                conts = ContainerWrapper.getEmptyContainersHoldingSampleType(
                    SessionManager.getAppService(),
                    siteCombo.getSelectedSite(), typesFromOlContainer,
                    oldContainer.getRowCapacity(),
                    oldContainer.getColCapacity());
            } catch (ApplicationException ae) {
                BioBankPlugin.openAsyncError("Error retrieving containers", ae);
            }
        for (ContainerWrapper cont : conts) {
            map.put(cont.getLabel(), cont);
        }
        if (lv != null) {
            lv.setInput(map.keySet());
        }
    }

    public ContainerWrapper getNewContainer() {
        return map.get(selectedValue.getValue());
    }

    @Override
    public void okPressed() {
        boolean sure = BioBankPlugin.openConfirm("Other site",
            "You are about to move these aliquots into a container that belongs "
                + "to another site. Are you sure ?");
        if (sure)
            super.okPressed();
    }

}
