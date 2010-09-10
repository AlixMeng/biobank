package edu.ualberta.med.biobank.treeview.listeners;

import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.widgets.TreeItem;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.treeview.ContainerAdapter;

public class AdapterTreeDragDropListener implements DropTargetListener,
    DragSourceListener {

    private TreeViewer treeViewer;

    private ContainerAdapter srcContainerAdapter;
    private ContainerWrapper srcContainer;
    private boolean dstLocationSelected;

    public AdapterTreeDragDropListener(TreeViewer treeViewerI) {
        this.treeViewer = treeViewerI;
        this.srcContainer = null;
        this.dstLocationSelected = false;
    }

    private ContainerWrapper getSelectedContainer() {
        TreeSelection ts = (TreeSelection) treeViewer.getSelection();
        if (ts == null || ts.isEmpty())
            return null;

        if (ts.getFirstElement() instanceof ContainerAdapter) {
            if (ts.size() != 1)
                BioBankPlugin
                    .openError("Cannot move multiple container",
                        "You cannot move multiple containers, please drag them one at a time.");

            srcContainerAdapter = (ContainerAdapter) ts.getFirstElement();
            if (srcContainerAdapter != null)
                return srcContainerAdapter.getContainer();

        }
        return null;
    }

    @Override
    public void dragOver(DropTargetEvent event) {
        event.feedback = DND.FEEDBACK_NONE;
        if (event.item != null && srcContainer != null) {

            TreeItem item = (TreeItem) event.item;

            ModelWrapper<?> wrapper = ((AdapterBase) (item.getData()))
                .getModelObject();

            if ((wrapper instanceof ContainerWrapper)) {
                try {
                    ContainerWrapper container = (ContainerWrapper) wrapper;
                    if (container.getContainerType()
                        .getChildContainerTypeCollection().size() != 0) {
                        if (container.getContainerType()
                            .getChildContainerTypeCollection()
                            .contains(srcContainer.getContainerType())) {
                            if (!container.isContainerFull()) {
                                event.feedback |= DND.FEEDBACK_SELECT;
                                event.feedback |= DND.FEEDBACK_EXPAND;
                            }
                        } else {
                            /*
                             * TODO expand only when an ancestor can hold the
                             * srcContainerType. It does not make sense to
                             * expand a branch when none of the children of that
                             * branch can hold the src container.
                             */
                            event.feedback |= DND.FEEDBACK_EXPAND;
                        }
                    }
                } catch (Exception ex) {
                    BioBankPlugin.openAsyncError("Error in drag", ex);
                }
            }
        }
        dstLocationSelected = ((event.feedback & DND.FEEDBACK_SELECT) != 0);

    }

    @Override
    public void dragEnter(DropTargetEvent event) {
    }

    @Override
    public void dragLeave(DropTargetEvent event) {
    }

    @Override
    public void dragOperationChanged(DropTargetEvent event) {
    }

    @Override
    public void drop(DropTargetEvent event) {
        if (!dstLocationSelected || event.item == null) {
            event.detail = DND.DROP_NONE;
            return;
        }
        TreeItem item = (TreeItem) event.item;

        ModelWrapper<?> wrapper = ((AdapterBase) (item.getData()))
            .getModelObject();

        if (wrapper != null && (wrapper instanceof ContainerWrapper)) {
            ContainerWrapper dstContainer = (ContainerWrapper) wrapper;
            if (dstContainer != null) {
                try {
                    /* sanity checks */
                    if (dstContainer.getContainerType()
                        .getChildContainerTypeCollection()
                        .contains(srcContainer.getContainerType())
                        && !dstContainer.isContainerFull()) {

                        // TODO implement the moving of containers here.
                        System.out.println("Valid Drag Detected:");
                        System.out.println(srcContainer + " --> "
                            + dstContainer);
                        srcContainerAdapter.moveAction(dstContainer);
                        return;
                    } else {
                        BioBankPlugin
                            .openError(
                                "Invalid state",
                                "ERROR: an unexpected state occured in TreeDragDropListener. Please report this.");

                    }
                } catch (Exception ex) {
                    BioBankPlugin.openAsyncError("Drop error", ex);
                }
            }
        }
        event.detail = DND.DROP_NONE;
    }

    @Override
    public void dropAccept(DropTargetEvent event) {
    }

    @Override
    public void dragStart(DragSourceEvent event) {

        ContainerWrapper container = getSelectedContainer();
        if (container != null && container.hasParent()) {
            event.doit = true;
            srcContainer = container;
        } else {
            event.doit = false;
            srcContainer = null;
        }
        dstLocationSelected = false;
    }

    @Override
    public void dragSetData(DragSourceEvent event) {
        event.data = getSelectedContainer();
        event.doit = true;
    }

    @Override
    public void dragFinished(DragSourceEvent event) {
        srcContainer = null;
        dstLocationSelected = false;
    }

    /* TODO move containers in tree view */
    @SuppressWarnings("unused")
    private void moveContainerTo(ContainerAdapter containerAdapter) {
        // containerAdapter.moveAliquots();
    }

}
