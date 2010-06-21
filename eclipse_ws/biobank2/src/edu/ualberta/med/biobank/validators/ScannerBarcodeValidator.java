package edu.ualberta.med.biobank.validators;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import edu.ualberta.med.biobank.BioBankPlugin;

public class ScannerBarcodeValidator extends AbstractValidator {

    public ScannerBarcodeValidator(String message) {
        super(message);
    }

    @Override
    public IStatus validate(Object value) {
        if (!(value instanceof String)) {
            throw new RuntimeException(
                "Not supposed to be called for non-strings.");
        }

        String barcode = (String) value;
        if (barcode.length() != 0
            && BioBankPlugin.getDefault().isValidPlateBarcode(barcode)) {
            hideDecoration();
            return Status.OK_STATUS;
        }

        showDecoration();
        return ValidationStatus.error(errorMessage);
    }

}
