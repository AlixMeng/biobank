package edu.ualberta.med.biobank.widgets;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import edu.ualberta.med.biobank.common.formatters.DateFormatter;
import edu.ualberta.med.biobank.widgets.nebula.CDT;
import edu.ualberta.med.biobank.widgets.nebula.CDateTime;

public class DateTimeWidget extends BiobankWidget {

    private CDateTime dateEntry;

    private List<ModifyListener> modifyListeners = new ArrayList<ModifyListener>();

    private Listener dataEntryModifyListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            if (event.type == SWT.Modify || event.type == SWT.Selection) {
                fireModifyListeners();
            }
        }
    };

    public DateTimeWidget(Composite parent, int style, Date date) {
        super(parent, style);

        GridLayout layout = new GridLayout(1, false);
        layout.horizontalSpacing = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        setLayout(layout);

        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        setLayoutData(gd);

        if ((style & SWT.DATE) != 0) {
            style |= CDT.DROP_DOWN | CDT.DATE_SHORT;
        }

        dateEntry = new CDateTime(this, CDT.BORDER | CDT.COMPACT
            | CDT.TIME_SHORT | CDT.CLOCK_24_HOUR | CDT.BORDER | style);
        if ((style & SWT.TIME) != 0 && (style & SWT.DATE) != 0)
            dateEntry.setPattern(DateFormatter.DATE_TIME_FORMAT);
        else if ((style & SWT.TIME) != 0)
            dateEntry.setPattern(DateFormatter.TIME_FORMAT);
        else
            dateEntry.setPattern(DateFormatter.DATE_FORMAT);

        dateEntry.addListener(SWT.Modify, dataEntryModifyListener);
        dateEntry.addListener(SWT.Selection, dataEntryModifyListener);

        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.widthHint = SWT.DEFAULT;
        gd.heightHint = SWT.DEFAULT;
        dateEntry.setLayoutData(gd);

        if (date != null) {
            setDate(date);
        }
    }

    public String getText() {
        return getDate().toString();
    }

    public Date getDate() {
        Calendar cal = new GregorianCalendar();
        if (dateEntry.getSelection() != null) {
            cal.setTime(dateEntry.getSelection());
            return cal.getTime();
        }
        return null;
    }

    public void setDate(Date date) {
        dateEntry.setSelection(date);
        fireModifyListeners();
    }

    public void addModifyListener(ModifyListener modifyListener) {
        modifyListeners.add(modifyListener);
    }

    protected void fireModifyListeners() {
        Event event = new Event();
        event.type = SWT.Modify;
        event.widget = this;
        ModifyEvent modifyEvent = new ModifyEvent(event);
        for (ModifyListener listener : modifyListeners) {
            listener.modifyText(modifyEvent);
        }
    }

    public void removeModifyListener(ModifyListener listener) {
        modifyListeners.remove(listener);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (dateEntry != null) {
            dateEntry.setEnabled(enabled);
        }
    }

    public void addSelectionListener(SelectionListener listener) {
        dateEntry.addSelectionListener(listener);
    }
}
