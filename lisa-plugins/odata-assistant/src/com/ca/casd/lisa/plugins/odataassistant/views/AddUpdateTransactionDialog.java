/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.lisa.plugins.odataassistant.views;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ca.casd.utilities.commonUtils.metadata.Metadata;
import com.ca.casd.lisa.plugins.odataassistant.utils.EnumDefaultTransactions;
import com.ca.dvs.utilities.lisamar.VSITransactionObject;

public class AddUpdateTransactionDialog extends TitleAreaDialog {	

	private static String 	URL_PATH_SEPERATOR = "/";
	private static String 	TOOLTIP_MESSAGE_PATH = "Specify a resource path which is started with '/'.\nExample: /Brewers(<id>)/Beers";
	private static String 	TOOLTIP_MESSAGE_DESCTIPTION = "Specify a description";
	private static String 	TOOLTIP_MESSAGE_RESET_RESPONSE = "Set the response template as default format";
	private static String 	TOOLTIP_MESSAGE_RESPONSE = "Specify the response template in JSON format. Example:\n" ;

	private Combo 	cbMethod;
	private Text 	txtDescription;
	private Text 	txtResponseBody;
	private Table 	transTable;	
	private Combo 	cbResourcePath; 
	private Button 	btnResetResponse;
	
	/*****************/
	private boolean bAdd = true;		//true, create a new transaction, false is for update the existing 
	private boolean bSaved = false;	
	private Shell parentShell;
	private VSITransactionObject object;
	private String odataVersion;
	EnumDefaultTransactions enumTranscation;
	
	public AddUpdateTransactionDialog(Shell parentShell, VSITransactionObject object, Table table, Metadata metadata, int limitPath, String odataVersion, boolean bAdd) {
		  super(parentShell);
		  this.parentShell = parentShell;
		  this.bAdd = bAdd;
		  this.object = object;
		  this.transTable = table;
		  this.odataVersion = odataVersion;		  
		  enumTranscation = new EnumDefaultTransactions(object.getBaseURL(), limitPath, metadata, odataVersion);
		  
		  setHelpAvailable(false);
	}
	
	public void create(String title, String message) {
		  super.create();
		  setTitle(title);
		  setMessage(message);
	}

	  @Override
	  protected Control createDialogArea(Composite parent) {
		  
	    Composite area = (Composite) super.createDialogArea(parent);
	    Composite container = new Composite(area, SWT.NONE);
	    GridLayout layout = new GridLayout(2, false);
	    GridData gdata = new GridData(SWT.FILL, SWT.FILL, true, true);
	    gdata.minimumWidth = 700;
	    gdata.minimumHeight = 520;
	    container.setLayoutData(gdata);
	    container.setLayout(layout);

	    createTransactionMethod(container);	    
	    createResourcePath(container);	    
	    createDescription(container);
	    createResponseBody(container);
	    
	    addControlsListener();
	    
	    initTranscationInformation(object);
	    
	    return area;
	  }

	  private void createTransactionMethod(Composite container) {
		    Composite lblComposite = new Composite(container, SWT.NONE);
		    GridLayout lblLayout = new GridLayout(1, true);
		    lblComposite.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));
		    lblComposite.setLayout(lblLayout);
		    Label lblMethod = new Label(lblComposite, SWT.NONE);
		    lblMethod.setText("Method:");
		    
		    Composite ctrMethod = new Composite(container, SWT.NONE);
		    GridLayout layout = new GridLayout(1, true);
		    ctrMethod.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		    ctrMethod.setLayout(layout);
		    
		    cbMethod = new Combo(ctrMethod, SWT.READ_ONLY);
		    //cbMethod = new Combo(container, SWT.READ_ONLY);
	        for (int i = 0, n = VSITransactionObject.DEFAULT_OPTIONS.length; i < n; i++) {
	        	cbMethod.add(VSITransactionObject.DEFAULT_OPTIONS[i]);
	        }
	        cbMethod.select(cbMethod.indexOf(object.getOperation()));
	  }
	  
	  private void createResourcePath(Composite container) {
		    Composite lblComposite = new Composite(container, SWT.NONE);
		    GridLayout lblLayout = new GridLayout(1, true);
		    lblComposite.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));
		    lblComposite.setLayout(lblLayout);
		    Label lblPath = new Label(lblComposite, SWT.NONE);
		    lblPath.setText("Resource Path:");
		    
		    Composite ctrResourcePath = new Composite(container, SWT.NONE);
		    GridLayout layout = new GridLayout(1, true);
		    ctrResourcePath.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		    ctrResourcePath.setLayout(layout);
		    
		    GridData gdtResourcePath = new GridData();
		    gdtResourcePath.grabExcessHorizontalSpace = true;
		    gdtResourcePath.horizontalAlignment = GridData.FILL;
			cbResourcePath = new Combo(ctrResourcePath, SWT.READ_ONLY);
			cbResourcePath.setLayoutData(gdtResourcePath);
			
			//cbResourcePath = new Combo(container, SWT.READ_ONLY);
			String method = cbMethod.getText();  
		    buildAvailableResourcePath(method);    
		    
	  }
	  
	  private void createDescription(Composite container) {
		    Composite lblComposite = new Composite(container, SWT.NONE);
		    GridLayout lblLayout = new GridLayout(1, true);
		    lblComposite.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));
		    lblComposite.setLayout(lblLayout);
		    Label lblDescription = new Label(lblComposite, SWT.NONE);
		    lblDescription.setText("Description:");
		    
		    Composite ctrDescription = new Composite(container, SWT.NONE);
		    GridLayout layout = new GridLayout(1, true);
		    ctrDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		    ctrDescription.setLayout(layout);
		    
		    GridData gdtDescription = new GridData();
		    gdtDescription.grabExcessHorizontalSpace = true;
		    gdtDescription.horizontalAlignment = GridData.FILL;
		    gdtDescription.heightHint = 40;		    
		    txtDescription = new Text(ctrDescription, SWT.BORDER | SWT.WRAP);
		    txtDescription.setLayoutData(gdtDescription);
		    txtDescription.setToolTipText(TOOLTIP_MESSAGE_DESCTIPTION);	
		    
		    buildDefaultDescription();
		    
	  }
	  
	  
	  private void createResponseBody(Composite container) {

		    Composite lblComposite = new Composite(container, SWT.NONE);
		    GridLayout lblLayout = new GridLayout(1, true);
		    lblComposite.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));
		    lblComposite.setLayout(lblLayout);
		    
		    Label lblResponse = new Label(lblComposite, SWT.NONE);
		    lblResponse.setText("Response Body:");
		    lblResponse.setAlignment(SWT.TOP);
		    
		    Composite ctrResponse = new Composite(container, SWT.NONE);
		    GridLayout layout = new GridLayout(1, true);
		    ctrResponse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		    ctrResponse.setLayout(layout);
		    
		    GridData gdtResponse = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);		    
		    gdtResponse.grabExcessHorizontalSpace = true;
		    gdtResponse.horizontalAlignment = GridData.FILL;
		    gdtResponse.heightHint = 300;		    
		    txtResponseBody = new Text(ctrResponse, SWT.MULTI | SWT.BORDER | SWT.WRAP |SWT.H_SCROLL | SWT.V_SCROLL);
		    txtResponseBody.setLayoutData(gdtResponse);
		    txtResponseBody.setToolTipText(TOOLTIP_MESSAGE_RESPONSE);
		    
		    Label lblResetButton = new Label(lblComposite, SWT.NONE);
		    lblResetButton.setText("");
		    lblResetButton.setAlignment(SWT.TOP);
		    
		    btnResetResponse = new Button(ctrResponse, SWT.PUSH);
		    btnResetResponse.setText("Set as Default");
		    btnResetResponse.setToolTipText(TOOLTIP_MESSAGE_RESET_RESPONSE);
		    btnResetResponse.addSelectionListener(new SelectionAdapter() {
		    	@Override
		    	public void widgetSelected(SelectionEvent e) {	
		    		buildDefaultResponseBody();
		    	}
		    });
		    
	  }
	  	   
	  private void addControlsListener() {
		  
		  cbMethod.addSelectionListener(new SelectionAdapter() {
			   public void widgetSelected(SelectionEvent e) {
			   	  	String selText = cbMethod.getText();
			   	  	if (false == selText.isEmpty()) {
			   	  		buildAvailableResourcePath(selText);				        	
				    }
			   }
		  });
		  		  
		  cbResourcePath.addModifyListener(new ModifyListener() {
			  @Override
			  public void modifyText(ModifyEvent arg0) {
				// TODO Auto-generated method stub
				  if (cbResourcePath.getSelectionIndex() == -1)
					  return;				  
				  buildDefaultResponseBody();
				  buildDefaultDescription();
			  }
		  });
		  
	  }
	  
	  @Override
	  protected boolean isResizable() {
	    return true;
	  }

	  // save content of the Text fields because they get disposed
	  // as soon as the Dialog closes
	  private boolean saveInputs() {
		  
		  String 	strMethod = cbMethod.getItem(cbMethod.getSelectionIndex());
		  String 	strPath = cbResourcePath.getText().trim(); 
		  String 	strDescription = txtDescription.getText().trim();
		  String 	strResponseBody = txtResponseBody.getText().trim();
		  
		  Control curControl = null;
		  
		  String  errMessage = "";
		  if (strMethod.isEmpty()) {
			  errMessage = "Please specify a transaction method!";	
			  curControl = cbMethod;
		  }
		  else if (strPath.isEmpty() || strPath.length()<=1) {
			  errMessage = TOOLTIP_MESSAGE_PATH; //"Please specify the resource path!";			    
			  curControl = cbResourcePath;
		  }
		  else if (false == strPath.startsWith(URL_PATH_SEPERATOR)) {
			  errMessage = TOOLTIP_MESSAGE_PATH; //"A transaction path has to be started with '/'";	
			  curControl = cbResourcePath;

		  }
		  
		  if (errMessage.isEmpty()){
			  object.setOperation(strMethod);
			  object.setPath(strPath);
			  object.setDescription(strDescription);
			  object.setResponseBody(strResponseBody);
			  object.setEnableXML(false);        			
			  
			  if (strResponseBody.isEmpty()) {
	          // no necessary to set $format if there is no response required 
				  object.setEnableJson(false);
				  object.setEnableVerbosejson(false);
       		  }
			  else if (strPath.endsWith(EnumDefaultTransactions.URL_PATH_PROPERTY) || 
					  	strPath.contains(EnumDefaultTransactions.URL_PATH_LINKS)) {
				  object.setEnableJson(true);
				  object.setEnableVerbosejson(false);
			  }
			  else {
				  object.setEnableJson(true);
				  object.setEnableVerbosejson(true);
			  }
					  
			  if ( hasDuplicateTransaction(object) ) {
				  errMessage = "There is the same transaction definded already, please specify the new method or resource path!";
				  MessageDialog.openError(parentShell, "VSM Assistant", errMessage);
				  cbMethod.setFocus();
				  return false;
			  }
			  return true;			  
		  }
		  else {
			  MessageDialog.openError(parentShell, "VSM Assistant", errMessage);
			  curControl.setFocus();
			  return false;
		  }

	  }

	  private void initTranscationInformation(VSITransactionObject object) {
		  
		  cbMethod.setText(object.getOperation());
		  cbResourcePath.setEnabled(bAdd);
		  cbMethod.setEnabled(bAdd);			  

		  if (object.getPath().isEmpty())
			  cbResourcePath.setText(URL_PATH_SEPERATOR);
		  else
			  cbResourcePath.setText(object.getPath());	
		  
		  if (false == object.getDescription().isEmpty())
			  txtDescription.setText(object.getDescription());
		  
		  if ( object.getResponseBody().isEmpty())
			  buildDefaultResponseBody();
		  else
			  txtResponseBody.setText(object.getResponseBody());			  
		  
		  txtResponseBody.setEditable(bAdd);
		  btnResetResponse.setVisible(bAdd);
	  }
	  
	  private boolean hasDuplicateTransaction(VSITransactionObject object){
		
			String method = object.getOperation();
			String resPath = object.getPath();
			for (int i = 0; i < transTable.getItemCount(); i++) {
				TableItem item = transTable.getItem(i); 
		   		VSITransactionObject curObject = (VSITransactionObject) item.getData();
		   		if (curObject.getID()!= object.getID()) {
		       		if ( resPath.equals(curObject.getPath()) &&
		       				method.equals(curObject.getOperation())) {
		       			return true;
		       		}
		   		}       		
			}						
			return false; //no duplicate transaction			
	  }
			
	  private void buildAvailableResourcePath(String method){
		 
		  String[] items = enumTranscation.buildAvailableResourcePath(method);		  
		  if (items != null) {
			  cbResourcePath.setItems(items);	
			  cbResourcePath.select(0);			  
		  }
		  
	  }

	  private void buildDefaultDescription() {
			 
		  String strMethod = cbMethod.getText(); 
		  String resourcePath = cbResourcePath.getText().trim(); 		  
		  if (txtDescription.getText().trim().isEmpty()){
			  	String description = enumTranscation.buildDefaultDescription(strMethod, resourcePath);
			  	txtDescription.setText(description);
		 	}
		  
	  }
	  
	  private void buildDefaultResponseBody(){

		  String strMethod = cbMethod.getText(); 
		  String resourcePath = cbResourcePath.getText().trim(); 		  
		  String strResponse = enumTranscation.buildDefaultResponseBody(strMethod, resourcePath, odataVersion);		  		  
		  txtResponseBody.setText(strResponse);
	  }
	  
	  @Override
	  protected void okPressed() {		  
		  bSaved = saveInputs();
	      if ( bSaved ) 
	    	  super.okPressed();
	  }

	  @Override
	  public boolean close() {
	      if ( bSaved ) 
			  return super.close();

		  if( MessageDialog.openConfirm(parentShell, "ODME Assistant", 
				  	"The information are not saved, do you like to continue?") )
			  return super.close();
		  
		  return false;
	  }
	  
	  public VSITransactionObject getTransaction() {
		  return object;
	  }
	  
	} 