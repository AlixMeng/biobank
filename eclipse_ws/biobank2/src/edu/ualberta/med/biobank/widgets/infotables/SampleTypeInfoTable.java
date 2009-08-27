package edu.ualberta.med.biobank.widgets.infotables;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.model.SampleType;

public class SampleTypeInfoTable extends InfoTableWidget<SampleType> {

    private Collection<SampleType> selectedSampleType;

    private static final String[] headings = new String[] { "Sample Type",
        "Short Name" };

    private static final int[] bounds = new int[] { 300, 130, -1, -1, -1, -1,
        -1 };

    public SampleTypeInfoTable(Composite parent,
        Collection<SampleType> sampleTypeCollection) {
        super(parent, sampleTypeCollection, headings, bounds);
        if (sampleTypeCollection == null) {
            selectedSampleType = new HashSet<SampleType>();
        } else {
            selectedSampleType = sampleTypeCollection;
        }
    }
}
