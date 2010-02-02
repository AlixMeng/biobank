package edu.ualberta.med.biobank.validators;

import java.util.regex.Pattern;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class CabinetInventoryIDValidator extends AbstractValidator {

    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z]{6}$");

    private static final Pattern PATTERN2 = Pattern.compile("^C[a-zA-Z]{4}$");

    private boolean manageOldInventoryIDs = false;

    private static final String ERROR_MESSAGE = "Enter Inventory ID (6 letters for new samples, 4 allowed for old samples)";

    public CabinetInventoryIDValidator() {
        super(ERROR_MESSAGE);
    }

    @Override
    public IStatus validate(Object value) {
        if (!(value instanceof String)) {
            throw new RuntimeException(
                "Not supposed to be called for non-strings.");
        }

        String v = (String) value;
        boolean matches = PATTERN.matcher(v).matches();
        if (manageOldInventoryIDs) {
            matches = matches || PATTERN2.matcher(v).matches();
        }
        if (matches) {
            controlDecoration.hide();
            return Status.OK_STATUS;
        }
        controlDecoration.show();
        return ValidationStatus.error(errorMessage);
    }

    public void setManageOldInventoryIDs(boolean manageOldInventoryIDs) {
        this.manageOldInventoryIDs = manageOldInventoryIDs;
    }
}
