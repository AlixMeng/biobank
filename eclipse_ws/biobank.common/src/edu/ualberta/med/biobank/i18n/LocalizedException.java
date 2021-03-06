package edu.ualberta.med.biobank.i18n;

import edu.ualberta.med.biobank.common.action.exception.ActionException;

/**
 * 
 * @author Jonathan Ferland
 */
public class LocalizedException extends ActionException
    implements HasLocalizedString {
    private static final long serialVersionUID = 1L;

    private final LString localizedString;

    public LocalizedException(LString localizedString) {
        this(localizedString, null);
    }

    public LocalizedException(LString localizedString, Throwable cause) {
        super(qualifiedMessage(localizedString), cause);

        this.localizedString = localizedString;
    }

    @Override
    public LString getLocalizedString() {
        return localizedString;
    }

    @Override
    public String getLocalizedMessage() {
        return localizedString.toString();
    }

    @SuppressWarnings("nls")
    private static String qualifiedMessage(LString localizedString) {
        StringBuilder message = new StringBuilder();

        message.append(localizedString.getTemplate());
        message.append(" => ");
        message.append(localizedString);

        return message.toString();
    }
}
