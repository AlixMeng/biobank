package edu.ualberta.med.biobank.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.ualberta.med.biobank.forms.AbstractAliquotAdminForm;

public class PrintHandler extends AbstractHandler implements IHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        AbstractAliquotAdminForm form = (AbstractAliquotAdminForm) HandlerUtil
            .getActiveEditor(event);
        boolean doPrint = MessageDialog.openQuestion(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell(), "Print",
            "Do you want to print information ?");
        if (doPrint) {
            if (form.print())
                form.setPrinted(true);
        }

        return null;
    }
}
