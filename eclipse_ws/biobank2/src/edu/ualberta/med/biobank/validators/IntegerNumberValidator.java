package edu.ualberta.med.biobank.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class IntegerNumberValidator extends AbstractValidator {

    private static final Pattern pattern = Pattern.compile("^[0-9\\+-]*$");

    private boolean allowEmpty = true;

    public IntegerNumberValidator(String message) {
        super(message);
    }

    public IntegerNumberValidator(String message, boolean allowEmpty) {
        this(message);
        this.allowEmpty = allowEmpty;
    }

    @Override
    public IStatus validate(Object value) {
        if ((value == null) || (value instanceof Integer)) {
            hideDecoration();
            return Status.OK_STATUS;
        }

        if (((String) value).length() == 0) {
            if (allowEmpty) {
                hideDecoration();
                return Status.OK_STATUS;
            } else {
                showDecoration();
                return ValidationStatus.error(errorMessage);
            }
        }

        Matcher m = pattern.matcher((String) value);
        if (m.matches()) {
            hideDecoration();
            return Status.OK_STATUS;
        }

        showDecoration();
        return ValidationStatus.error(errorMessage);
    }

}
