package edu.ualberta.med.biobank.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

import edu.ualberta.med.biobank.widgets.listeners.BiobankEntryFormWidgetListener;
import edu.ualberta.med.biobank.widgets.listeners.MultiSelectEvent;

public class BiobankWidget extends Composite {

    List<BiobankEntryFormWidgetListener> listeners;

    public static ControlDecoration createDecorator(Control control,
        String message) {
        ControlDecoration controlDecoration = new ControlDecoration(control,
            SWT.RIGHT | SWT.TOP);
        controlDecoration.setDescriptionText(message);
        FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
            .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
        controlDecoration.setImage(fieldDecoration.getImage());
        return controlDecoration;
    }

    public BiobankWidget(Composite parent, int style) {
        super(parent, style); // | SWT.H_SCROLL | SWT.V_SCROLL);
        listeners = new ArrayList<BiobankEntryFormWidgetListener>();
    }

    public void adaptToToolkit(FormToolkit toolkit, boolean paintBorder) {
        toolkit.adapt(this, true, true);
        adaptAllChildren(this, toolkit);
        if (paintBorder) {
            toolkit.paintBordersFor(this);
        }
    }

    private void adaptAllChildren(Composite container, FormToolkit toolkit) {
        Control[] children = container.getChildren();
        for (Control aChild : children) {
            toolkit.adapt(aChild, true, true);
            if (aChild instanceof Composite) {
                adaptAllChildren((Composite) aChild, toolkit);
            }
        }
    }

    public void addSelectionChangedListener(
        BiobankEntryFormWidgetListener listener) {
        listeners.add(listener);
    }

    public void removeSelectionChangedListener(
        BiobankEntryFormWidgetListener listener) {
        listeners.remove(listener);
    }

    public void notifyListeners(MultiSelectEvent event) {
        for (BiobankEntryFormWidgetListener listener : listeners) {
            listener.selectionChanged(event);
        }
    }

    public void notifyListeners() {
        notifyListeners(new MultiSelectEvent(this));
    }
}
