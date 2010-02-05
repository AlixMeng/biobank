package edu.ualberta.med.biobank.forms;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;

import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;

public abstract class AddressViewFormCommon extends BiobankViewForm {

    protected void createAddressSection(ModelWrapper<?> wrapperObject) {
        Composite client = createSectionWithClient("Address");
        Section section = (Section) client.getParent();
        section.setExpanded(false);
        createAddressArea(client, wrapperObject);
    }

    protected void createAddressArea(Composite parent,
        ModelWrapper<?> wrapperObject) {
        FieldInfo field;
        for (String widgetName : AddressEntryFormCommon.ADDRESS_FIELDS.keySet()) {
            field = AddressEntryFormCommon.ADDRESS_FIELDS.get(widgetName);
            addWidget(widgetName, createReadOnlyField(parent, SWT.NONE,
                field.label));
        }
        setAdressValues(wrapperObject);
    }

    protected void setAdressValues(ModelWrapper<?> wrapperObject) {
        setWidgetValues(AddressEntryFormCommon.ADDRESS_FIELDS, wrapperObject);
    }

}
