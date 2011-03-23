package edu.ualberta.med.biobank.widgets;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

import edu.ualberta.med.biobank.Messages;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenWrapper;
import edu.ualberta.med.biobank.widgets.utils.WidgetCreator;

/**
 * Create widgets to show types selection for specimens on a pallet: one label,
 * one combo with different source types, one combo with different result types
 * and one text showing total number of samples found
 */
public class AliquotedSpecimenSelectionWidget {
    private ComboViewer cvSource;
    private ComboViewer cvResult;
    private ControlDecoration rowControlDecoration;
    private ControlDecoration sourceControlDecoration;
    private ControlDecoration resultControlDecoration;
    private Label textNumber;
    private Integer number;

    private IObservableValue bothSelected = new WritableValue(Boolean.FALSE,
        Boolean.class);

    private IObservableValue sourceSelected = new WritableValue(Boolean.FALSE,
        Boolean.class);

    private IObservableValue resultSelected = new WritableValue(Boolean.FALSE,
        Boolean.class);

    private Binding oneRowBinding;
    private Binding sourceBinding;
    private Binding resultBinding;

    private Object nextWidget;
    private WidgetCreator widgetCreator;
    private boolean oneRow;
    private Label sourceLabel;
    private Label resultLabel;

    public AliquotedSpecimenSelectionWidget(Composite parent, Character letter,
        WidgetCreator widgetCreator, boolean oneRow) {
        this.widgetCreator = widgetCreator;
        this.oneRow = oneRow;
        if (letter != null) {
            widgetCreator.getToolkit().createLabel(parent, letter.toString(),
                SWT.LEFT);
        }
        if (!oneRow) {
            sourceLabel = widgetCreator.createLabel(parent, "Source specimen");
            sourceControlDecoration = BiobankWidget
                .createDecorator(
                    sourceLabel,
                    Messages
                        .getString("AliquotedSpecimenSelectionWidget.selections.validation.msg"));
        }
        cvSource = new ComboViewer(parent, SWT.DROP_DOWN | SWT.READ_ONLY
            | SWT.BORDER);
        setComboProperties(cvSource, widgetCreator.getToolkit(), 0);
        cvSource.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                SpecimenWrapper spc = (SpecimenWrapper) element;
                return spc.getSpecimenType().getNameShort() + "("
                    + spc.getInventoryId() + ")";
            }
        });
        if (oneRow) {
            GridData gd = new GridData();
            gd.widthHint = 250;
            cvSource.getControl().setLayoutData(gd);
        }

        if (!oneRow) {
            resultLabel = widgetCreator.createLabel(parent,
                "Aliquoted specimen type");
            resultControlDecoration = BiobankWidget
                .createDecorator(
                    resultLabel,
                    Messages
                        .getString("AliquotedSpecimenSelectionWidget.selections.validation.msg"));
        }
        cvResult = new ComboViewer(parent, SWT.DROP_DOWN | SWT.READ_ONLY
            | SWT.BORDER);
        setComboProperties(cvResult, widgetCreator.getToolkit(), 1);
        cvResult.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((SpecimenTypeWrapper) element).getName();
            }
        });

        if (oneRow) {
            textNumber = widgetCreator.getToolkit().createLabel(parent, "",
                SWT.BORDER);
            GridData data = new GridData();
            data.widthHint = 20;
            data.horizontalAlignment = SWT.LEFT;
            textNumber.setLayoutData(data);

            setNumber(null);

            rowControlDecoration = BiobankWidget
                .createDecorator(
                    textNumber,
                    Messages
                        .getString("AliquotedSpecimenSelectionWidget.selections.validation.msg"));
        }
    }

    private void setComboProperties(ComboViewer cv, FormToolkit toolkit,
        final int selectionPosition) {
        cv.getControl().setLayoutData(
            new GridData(SWT.FILL, SWT.TOP, true, false));
        toolkit.adapt(cv.getControl(), true, true);
        cv.setContentProvider(new ArrayContentProvider());
        cv.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                boolean res = true;
                if (event.getSelection() == null
                    || ((IStructuredSelection) event.getSelection()).size() == 0) {
                    res = false;
                }
                if (selectionPosition == 0)
                    sourceSelected.setValue(res);
                else
                    resultSelected.setValue(res);
                updateBothSelectedField();
            }
        });
        cv.setComparator(new ViewerComparator());
        if (selectionPosition != 0)
            cv.getControl().addTraverseListener(new TraverseListener() {
                @Override
                public void keyTraversed(TraverseEvent e) {
                    if (e.detail == SWT.TRAVERSE_TAB_NEXT
                        || e.detail == SWT.TRAVERSE_RETURN) {
                        e.doit = setNextFocus();
                    }
                }
            });
    }

    private boolean setNextFocus() {
        if (nextWidget != null) {
            if (nextWidget instanceof Control) {
                ((Control) nextWidget).setFocus();
                return false; // cancel doit
            } else if (nextWidget instanceof AliquotedSpecimenSelectionWidget) {
                ((AliquotedSpecimenSelectionWidget) nextWidget).cvSource
                    .getControl().setFocus();
            }
        }
        return true;
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        cvSource.addSelectionChangedListener(listener);
        cvResult.addSelectionChangedListener(listener);
    }

    public void setNumber(Integer number) {
        if (textNumber != null) {
            this.number = number;
            String text = "";
            if (number != null) {
                text = number.toString();
            }
            if (number == null || number == 0) {
                cvSource.getControl().setEnabled(false);
                sourceSelected.setValue(true);
                cvResult.getControl().setEnabled(false);
                resultSelected.setValue(true);
                bothSelected.setValue(true);
            } else {
                cvSource.getControl().setEnabled(true);
                sourceSelected.setValue(getSourceSelection() != null);
                cvResult.getControl().setEnabled(true);
                resultSelected.setValue(getResultTypeSelection() != null);
                updateBothSelectedField();
            }
            textNumber.setText(text);
        }
    }

    public void increaseNumber() {
        if (number == null)
            number = 0;
        number++;
        setNumber(number);
        sourceSelected.setValue(getSourceSelection() != null);
        resultSelected.setValue(getResultTypeSelection() != null);
        updateBothSelectedField();
    }

    private void updateBothSelectedField() {
        bothSelected.setValue((Boolean) sourceSelected.getValue()
            && (Boolean) resultSelected.getValue());
    }

    public boolean canFocus() {
        return cvSource.getControl().isEnabled();
    }

    /**
     * return true if this selection need to be save, i.e. number > 0
     */
    public boolean needToSave() {
        if (number == null) {
            return false;
        } else {
            return number > 0;
        }
    }

    private SpecimenTypeWrapper getResultTypeSelection() {
        return (SpecimenTypeWrapper) ((StructuredSelection) cvResult
            .getSelection()).getFirstElement();
    }

    private SpecimenWrapper getSourceSelection() {
        return (SpecimenWrapper) ((StructuredSelection) cvSource.getSelection())
            .getFirstElement();
    }

    public void addBindings() {
        if (oneRow) {
            if (oneRowBinding == null) {
                UpdateValueStrategy rowUpdateValue = createOneRowUpdateValueStrategy(rowControlDecoration);
                oneRowBinding = widgetCreator.bindValue(new WritableValue(
                    Boolean.FALSE, Boolean.class), bothSelected,
                    rowUpdateValue, rowUpdateValue);
            } else {
                widgetCreator.addBinding(oneRowBinding);
            }
        } else {
            if (sourceBinding == null || resultBinding == null) {
                UpdateValueStrategy sourceUpdateValue = createOneRowUpdateValueStrategy(sourceControlDecoration);
                sourceBinding = widgetCreator.bindValue(new WritableValue(
                    Boolean.FALSE, Boolean.class), sourceSelected,
                    sourceUpdateValue, sourceUpdateValue);
                UpdateValueStrategy resultUpdateValue = createOneRowUpdateValueStrategy(resultControlDecoration);
                resultBinding = widgetCreator.bindValue(new WritableValue(
                    Boolean.FALSE, Boolean.class), resultSelected,
                    resultUpdateValue, resultUpdateValue);
            } else {
                widgetCreator.addBinding(sourceBinding);
                widgetCreator.addBinding(resultBinding);
            }
        }
    }

    private UpdateValueStrategy createOneRowUpdateValueStrategy(
        final ControlDecoration decoration) {
        UpdateValueStrategy uvs = new UpdateValueStrategy();
        uvs.setAfterGetValidator(new IValidator() {
            @Override
            public IStatus validate(Object value) {
                if (value instanceof Boolean && !(Boolean) value) {
                    decoration.show();
                    return ValidationStatus.error(Messages
                        .getString("AliquotedSpecimenSelectionWidget.selections.status.msg"));
                } else {
                    decoration.hide();
                    return Status.OK_STATUS;
                }
            }
        });
        return uvs;
    }

    public void removeBindings() {
        if (sourceBinding != null)
            widgetCreator.removeBinding(sourceBinding);
        if (resultBinding != null)
            widgetCreator.removeBinding(resultBinding);
    }

    public void resetValues(boolean resetSelection, boolean resetNumber) {
        if (resetSelection) {
            cvSource.setSelection(null);
            cvResult.setSelection(null);
        }
        if (resetNumber)
            setNumber(null);
        else
            setNumber(number); // to re-do the validation tests
    }

    public void resetValues(final boolean resetSelection,
        final boolean resetNumber, boolean async) {
        if (async) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    resetValues(resetSelection, resetNumber);
                }
            });
        } else {
            resetValues(resetSelection, resetNumber);
        }
    }

    public void setNextWidget(Object nextWidget) {
        this.nextWidget = nextWidget;
    }

    public void setResultTypes(List<SpecimenTypeWrapper> types) {
        cvResult.setInput(types);
    }

    public void setSourceSpecimens(List<SpecimenWrapper> sourceSpecimens) {
        cvSource.setInput(sourceSpecimens);
    }

    public void setFocus() {
        cvSource.getControl().setFocus();
    }

    /**
     * @return an array of [SpecimenLink (source), SpecimenType (result)]
     */
    public ModelWrapper<?>[] getSelection() {
        if (getSourceSelection() != null && getResultTypeSelection() != null)
            return new ModelWrapper<?>[] { getSourceSelection(),
                getResultTypeSelection() };
        return null;
    }

    public void setEnabled(boolean enabled) {
        cvSource.getControl().setEnabled(enabled);
        cvResult.getControl().setEnabled(enabled);
    }

    public void deselectAll() {
        cvSource.getCombo().deselectAll();
        cvResult.getCombo().deselectAll();
    }

    public void showWidget(boolean enabled) {
        if (sourceLabel != null)
            widgetCreator.showWidget(sourceLabel, enabled);
        widgetCreator.showWidget(cvSource.getControl(), enabled);
        if (resultLabel != null)
            widgetCreator.showWidget(resultLabel, enabled);
        widgetCreator.showWidget(cvResult.getControl(), enabled);
    }

    public void setReadOnlySelections(SpecimenWrapper sourceSpecimen,
        SpecimenTypeWrapper resultType) {
        cvSource.setInput(Arrays.asList(sourceSpecimen));
        cvSource.setSelection(new StructuredSelection(sourceSpecimen));
        cvResult.setInput(Arrays.asList(resultType));
        cvResult.setSelection(new StructuredSelection(resultType));
    }
}
