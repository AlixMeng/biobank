package edu.ualberta.med.biobank.forms.input;

import edu.ualberta.med.biobank.common.reports.AbstractReportTreeNode;

public class ReportInput extends FormInput {

    public ReportInput(AbstractReportTreeNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return "";
    }

    public Object getNode() {
        return obj;
    }
}
