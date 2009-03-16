package edu.ualberta.med.biobank.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ListAddDialog extends Dialog {
    
    String title;
    String prompt;
    String helpText;
    Text items;
    String [] result;

    public ListAddDialog(Shell parentShell, String title, String prompt, String helpText) {
        super(parentShell);
        this.title = title;
        this.prompt = prompt;
        this.helpText = helpText;
    }
    
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
    }
    
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        return contents;
    }
    
    protected Control createDialogArea(Composite parent) {      
        Composite parentComposite = (Composite) super.createDialogArea(parent);  
        Composite contents = new Composite(parentComposite, SWT.NONE);
        contents.setLayout(new GridLayout(1, false));
        contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Label label = new Label(contents, SWT.NONE);
        label.setText(prompt);
        items = new Text(contents, SWT.BORDER);
        items.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label = new Label(contents, SWT.NONE);
        label.setText(helpText);
        return contents;
    }
    
    protected void okPressed() {
        result = items.getText().split(";");        
        super.okPressed();  
    }
    
    public String[] getResult() {
        return result;
    }
}
