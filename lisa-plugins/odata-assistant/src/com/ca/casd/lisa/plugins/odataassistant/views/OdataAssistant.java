/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.lisa.plugins.odataassistant.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.raml.model.Raml;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.w3c.dom.Document;

import com.ca.casd.utilities.commonUtils.CommonDefs;
import com.ca.casd.utilities.commonUtils.log.Log;
import com.ca.casd.utilities.commonUtils.log.LogException;
import com.ca.casd.utilities.commonUtils.metadata.Metadata;
import com.ca.casd.utilities.commonUtils.metadata.MetadataParser;
import com.ca.casd.lisa.plugins.odataassistant.preferences.OdataAssistantPreferencePage;
import com.ca.casd.lisa.plugins.odataassistant.utils.EnumDefaultTransactions;
import com.ca.dvs.utilities.lisamar.LisaMarObject;
import com.ca.dvs.utilities.lisamar.LisaMarUtil;
import com.ca.dvs.utilities.lisamar.VSITransactionObject;
import com.ca.dvs.utilities.lisamar.VSMObject;
import com.ca.dvs.utilities.raml.EDM;
import com.ca.dvs.utilities.raml.RamlUtil;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class OdataAssistant extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.ca.casd.lisa.plugins.odataassistant.views.OdataAssistant";

	private static final String MESSAGE_FONT_NAME = "Arial";
	private static final String LABEL_STATUS_ERROR = "Error:";
	private static final String LABEL_STATUS_INFORMATION = "Information:";
	private static final String LABEL_STATUS_WARNING = "Warning:";
	
	private static String 		URL_PATH_SEPERATOR = "/";
	private static String 		viewTitle = "DVS Assistant";

	private static String 		MESSAGE_TXTVSMNAME = "{{Enter Virtual Service Name}}";
	private static String 		MESSAGE_TXTBASEURL = "{{Enter Base URL}}";
	private static String 		MESSAGE_TXTPORTNUM = "{{Enter Port#}}";
	private static String 		MESSAGE_TXTVSMPATH = "{{Specify a location to save VSM}}";
	private static String 		MESSAGE_TXTEDMFILE = "{{Specify a service model file to load}}";
	
	//Odata Versions
	private static String		ODATA_VSERSION_3 = "OData Version 3";
	private static String		ODATA_VSERSION_4 = "OData Version 4";	
	
	// transactions table
	private static int 			COLUMN_ACTION_EDIT = 0;
	private static int 			COLUMN_ACTION_COPY = 1;
	private static int 			COLUMN_ACTION_DELETE = 2;
	private static int 			COLUMN_METHOD = 3;
	private static int 			COLUMN_PATH = 4;
	private static int 			COLUMN_DESCRIPTION = 5;

	private static final String[] columnTitles = { "", "", "", "Method", "Resource Path", "Description"};
	
	private static Composite 	 composite;
	
	private TableViewer 		tableViewer;
	private Action 				saveAction;
		
	private static Text 		txtVSMName;
	private static Text 		txtPort;
	private static Text 		txtBaseUrl;
	
	private static Text 		txtVSMPath;
	private static Text 		txtEDMFile;
	
    private static Table 		table;
	private static Group 		vsiGroup;
    
    private static Label 		lblTaskStatus;
    private static Text 		txtTaskStatus;
	private static Composite 	statusComposite;
	
	private static Combo 		cbOdataVersion;
	
	private static Button 		btnRadioAEDM;
	private static Button 		btnRadioRAML;
   
    private static Image 		editImage;
    private static Image 		deleteImage;
    private static Image 		copyImage;
	
	private static Metadata 	metadata;
	private String 				entityDataModelFile = "";
	Map<String, Map<String, Object>> sampleData = null;

	private String 				curEntityDataFile = "";	
	private int 				curOataVersion = 0;	
	
	private static final String LOG_PATH_FILE_NAME = System.getProperty("user.home")
			  + System.getProperty("file.separator")
			  + "ca"
			  + System.getProperty("file.separator")
			  + "com.ca.casd.lisa.plugins.odataassistant"
			  + System.getProperty("file.separator")
			  + "logs"
			  + System.getProperty("file.separator")
			  + "default.log";

	private static final String LOG_LAYOUT_PATTERN = "%d{ISO8601}, %p, %m, %C(%L) %n";

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		
		public Object[] getElements(Object parent) {
			return new String[] {};
		}
				
	}
	
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public OdataAssistant() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
		composite = parent;
		
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		data.heightHint = 70; // 80
		data.widthHint = 400;

	    editImage = PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJ_FILE).createImage();//IMG_OBJ_FILE
	    deleteImage = PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_TOOL_DELETE).createImage(); //IMG_ELCL_REMOVE
	    copyImage = PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJ_ADD).createImage(); 
	    	  
		parent.setLayout(new GridLayout(1, false));
		
		Composite contentComposite = new Composite(parent, SWT.NONE);
	    GridLayout contentLayout = new GridLayout(2, false);
	    contentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
	    contentComposite.setLayout(contentLayout);
	    
		createVSModelNameSection(contentComposite, data);
		
	    createVSMLocationSection(contentComposite, data);
	    
	    createHttpSection(contentComposite, data);
	    
	    createLoadEDMSection(contentComposite, data);
	    
	    createVSImageSection(parent, data);

		createProgressStatusSection(parent, data);
	    
	    addControlsListener();

	    try {
			Log.setLogPathFileName(LOG_PATH_FILE_NAME);	 
			Log.setLogLayoutPattern(LOG_LAYOUT_PATTERN);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(tableViewer.getControl(), "com.ca.casd.lisa.plugins.odataassistant.viewer");
		makeActions();
		contributeToActionBars();
		
		enableTransactionTable(enableGenerateTransactions());
		
		resetInputFields();
				
	}

	private void createVSModelNameSection(final Composite parent, final GridData data){
		
		Group vsmGroup = new Group(parent, SWT.SHADOW_IN);
		vsmGroup.setLayout(new GridLayout(3, false));
		vsmGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		vsmGroup.setText("VS Model");

		Label lblVSMName = new Label(vsmGroup, SWT.NONE);
		lblVSMName.setText("Name:");
		lblVSMName.setLocation(20,20);
		lblVSMName.pack();		
		txtVSMName = new Text(vsmGroup, SWT.BORDER);
		txtVSMName.setMessage(MESSAGE_TXTVSMNAME);
		txtVSMName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
			}
		});
		txtVSMName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		String versionItems[] = { ODATA_VSERSION_3, ODATA_VSERSION_4};
	    cbOdataVersion = new Combo(vsmGroup, SWT.READ_ONLY);	//SWT.READ_ONLY
	    cbOdataVersion.setToolTipText("Choose OData version");
		cbOdataVersion.setItems(versionItems);
		curOataVersion = 1;
		cbOdataVersion.select(curOataVersion);

		vsmGroup.pack();
	}

	private void createHttpSection(final Composite parent, final GridData data){
		
	    // Virtual HTTP/S Listener
		Group httpGroup = new Group(parent, SWT.SHADOW_IN);
		httpGroup.setLayout(new GridLayout(2, false));
		httpGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		httpGroup.setText("Virtual HTTP/S Listener");
	 
		Label labelPort = new Label(httpGroup, SWT.NONE);
		labelPort.setText("Listen port:");
		labelPort.setLocation(20,20);
		labelPort.pack();
		txtPort = new Text(httpGroup, SWT.BORDER);
		txtPort.setMessage(MESSAGE_TXTPORTNUM);
		txtPort.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
			}
		});

		Label labelUrl = new Label(httpGroup, SWT.NONE);
		labelUrl.setText("Base URL:");
		labelUrl.setLocation(20,20);
		labelUrl.pack();
		txtBaseUrl = new Text(httpGroup, SWT.BORDER);
		txtBaseUrl.setMessage(MESSAGE_TXTBASEURL);
		txtBaseUrl.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
			}
		});
		txtBaseUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));	
		httpGroup.setLayoutData(data);
		httpGroup.pack();
		
	}

	private void createVSMLocationSection(final Composite parent, final GridData data){
	    // Save VSM To		
		Group vsmLocGroup = new Group(parent, SWT.SHADOW_IN);
		vsmLocGroup.setLayout(new GridLayout(2, false));
		vsmLocGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		vsmLocGroup.setText("Save VSM File To");
		
		txtVSMPath = new Text(vsmLocGroup, SWT.BORDER);
		txtVSMPath.setMessage(MESSAGE_TXTVSMPATH);
		txtVSMPath.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
			}
		});
		txtVSMPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

	    Button btnVSMBrowser = new Button(vsmLocGroup, SWT.PUSH);
	    btnVSMBrowser.setText("Browse");
	    btnVSMBrowser.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {	    		
	    		DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
	    		String txtPath = txtVSMPath.getText().trim();
	    		if (!txtPath.isEmpty()) {
		    		dialog.setFilterPath(txtPath);
	    		}
	    		dialog.open();
	        	
	    		txtVSMPath.setText(dialog.getFilterPath());	    		
	    	}
	    });		   
	    btnVSMBrowser.pack();
	    vsmLocGroup.pack();
	}

	private void createLoadEDMSection(final Composite parent, final GridData data){
	    // Specify EDM file		
		Group loadEDMGroup = new Group(parent, SWT.SHADOW_IN);
		loadEDMGroup.setLayout(new GridLayout(2, false));
		loadEDMGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		loadEDMGroup.setText("Specify Service Model File");
		
		txtEDMFile = new Text(loadEDMGroup, SWT.BORDER);
		txtEDMFile.setMessage(MESSAGE_TXTEDMFILE);
		txtEDMFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

	    Button btnBrowser = new Button(loadEDMGroup, SWT.PUSH);
	    btnBrowser.setText("Browse");
	    btnBrowser.pack();
	    
	    Composite btnComposite = new Composite(loadEDMGroup, SWT.NONE);
	    GridLayout lblLayout = new GridLayout(2, true);
	    btnComposite.setLayoutData(new GridData(SWT.NONE, SWT.TOP, false, false));
	    btnComposite.setLayout(lblLayout);	    
	    btnRadioAEDM = new Button(btnComposite, SWT.RADIO); 
	    btnRadioAEDM.setText("AEDM");
	    btnRadioAEDM.setToolTipText("generate MAR based on AEDM");
	    btnRadioRAML = new Button(btnComposite, SWT.RADIO); 
	    btnRadioAEDM.setSelection(true);
	    btnRadioRAML.setText("Raml");
	    btnRadioRAML.setToolTipText("generate MAR from Raml");
	    
	    btnComposite.pack();
	    loadEDMGroup.setLayoutData(data);
	    loadEDMGroup.pack();
	    
	    btnBrowser.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		boolean isRaml = false;
	    		if (btnRadioRAML.getSelection())
	    			isRaml = true;
	    		
	    		String edmfile = getModelFile(isRaml);
	    		if (edmfile.isEmpty())
	    			return;
	    		txtEDMFile.setText(edmfile);
	    	}
	    });		   
	    
	}

	private void createProgressStatusSection(final Composite container, final GridData data){
		
		statusComposite = new Composite(container, SWT.NONE);
	    GridLayout contentLayout = new GridLayout(3, false);
	    statusComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
	    statusComposite.setLayout(contentLayout);
	    
	    Composite lblComposite = new Composite(statusComposite, SWT.NONE);
	    GridLayout lblLayout = new GridLayout(1, true);
	    lblComposite.setLayoutData(new GridData(SWT.NONE, SWT.TOP, false, false));
	    lblComposite.setLayout(lblLayout);	    
		lblTaskStatus = new Label(lblComposite, SWT.NONE);
		lblTaskStatus.setText("Status:");
		lblTaskStatus.setLocation(20,20);
        FontData defaultFont = new FontData(MESSAGE_FONT_NAME, 10, SWT.BOLD);
        Font txtFont = new Font(container.getDisplay(), defaultFont);
        lblTaskStatus.setFont(txtFont);
        
        Composite ctrComposite = new Composite(statusComposite, SWT.NONE);
	    GridLayout layout = new GridLayout(1, true);
	    ctrComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	    ctrComposite.setLayout(layout);
		GridData ctrLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		ctrLayoutData.heightHint = 40;
		txtTaskStatus = new Text(ctrComposite, SWT.READ_ONLY|SWT.MULTI|SWT.WRAP);
		txtTaskStatus.setLayoutData(ctrLayoutData);	
		txtTaskStatus.setText("{{Show status here}}");
		
		lblTaskStatus.setVisible(false);
		txtTaskStatus.setVisible(false);
		
	    Button btnGenerateVS = new Button(statusComposite, SWT.PUSH);
	    btnGenerateVS.setText("Generate VS");
	    btnGenerateVS.setToolTipText("Create Virtual Service");
	    btnGenerateVS.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
				generateVS();
	    	}
	    });		   

	}
	
	private void addControlsListener() {
		
		// once EMD file is changed, need to clean the existing transations and reload EMD
		txtEDMFile.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String newEdm = txtEDMFile.getText().trim();
				if (false == curEntityDataFile.equalsIgnoreCase(newEdm)) {
					if (table.getItemCount() > 0) {
						if ( false == showConfirmationMessage("The current transactions will be lost if you change to use different service model file, do you like to continue?") ){
							txtEDMFile.setText(curEntityDataFile);
							return;
						}
						else {
							table.removeAll();
							showProgressStatus(false, "", "");
						}
					}
				}
				curEntityDataFile = newEdm;
			}
		});
		
	    table.addMouseListener(new MouseAdapter() {
	    	
	    	public void mouseDoubleClick(MouseEvent event) {	
	    		
	    		Point pt = new Point(event.x, event.y);
	    		// Determine which row was selected
	    		final TableItem item = table.getItem(pt);
	    		if ( item != null)
	    			launchViewAndEditTransaction(item);
			}
	    	
		});	
	    
	    cbOdataVersion.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				int newOdataVersion = cbOdataVersion.getSelectionIndex();
				if (newOdataVersion != curOataVersion) {
					if (table.getItemCount() > 0) {
						if ( false == showConfirmationMessage("The current transactions will be lost if you change OData version, do you like to continue?") ){
							cbOdataVersion.select(curOataVersion);
							return;
						}
						else {
							table.removeAll();
							showProgressStatus(false, "", "");
						}
					}
				}
				curOataVersion = newOdataVersion;
			}
		});
	    
	    SelectionListener aedmSelectionListener = new SelectionAdapter () {
	    	public void widgetSelected(SelectionEvent event) {
	            Button button = ((Button) event.widget);
	            System.out.print(button.getText());
	            System.out.println(" selected = " + button.getSelection());
				if (table.getItemCount() > 0) {
					showMessage("The current transactions will be lost and you need to specify the proper service model file");
					table.removeAll();
				}
				// clean edm information
				entityDataModelFile = "";
				curEntityDataFile = "";
				txtEDMFile.setText("");
				showProgressStatus(false, "", "");
	        };
	    };
	    btnRadioAEDM.addSelectionListener(aedmSelectionListener); 
	    btnRadioRAML.addSelectionListener(aedmSelectionListener); 

	}

	private void launchViewAndEditTransaction(TableItem item){

		VSITransactionObject object = (VSITransactionObject) item.getData();
   		int  resPathDepth = getLimitDepthOfResourcePath();
   		String odataVersion = getOdataVersion();
        AddUpdateTransactionDialog transactionDlg = new AddUpdateTransactionDialog(composite.getShell(), object, table, metadata, resPathDepth, odataVersion, false);
		transactionDlg.create("View/Edit a Transaction",
                "View and edit the transaction information");
		
		if (transactionDlg.open() == Window.OK) {
			object = transactionDlg.getTransaction();
			updateTransactionInTable(object, item);
            table.setSelection(table.getItemCount()-1);
            table.setFocus();					
		}		
	}
	
	private void resetInputFields() {
		txtVSMName.setText("");
		txtPort.setText("80");
		txtBaseUrl.setText("");
	    txtVSMPath.setText(System.getProperty("user.home") + System.getProperty("file.separator") + "dvs");
	    table.deselectAll();
	    table.removeAll();   
	    txtVSMName.setFocus();
	    
		entityDataModelFile = "";
		curEntityDataFile = "";
	}
	
	private void createVSImageSection(final Composite parent, final GridData data){
		
		vsiGroup = new Group(parent, SWT.SHADOW_IN);
		vsiGroup.setLayout(new GridLayout(1, false));
	    GridData gridData = new GridData(GridData.FILL_BOTH);
	    gridData.horizontalSpan = 2;
	    vsiGroup.setLayoutData(gridData);
	    
		vsiGroup.setText("Virtual Service Transactions");
		
        table = new Table(vsiGroup, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL );
        tableViewer = buildAndLayoutTable(table);        
        
        appendTableMenu(parent, table);
        
    	table.addListener(SWT.MouseDown, new Listener() {
    		@Override
    		public void handleEvent(Event event) {
    			Point pt = new Point(event.x, event.y);
    			TableItem item = table.getItem(pt);
    			if (item == null)
    				return;
    			for (int i = 0; i < table.getColumnCount(); i++) {
    				Rectangle rect = item.getBounds(i);
    				if (rect.contains(pt)) {
    					int index = table.indexOf(item); 
    					if (i == COLUMN_ACTION_EDIT){
    						launchViewAndEditTransaction(item);
    						break;
    					}
    					else if (i == COLUMN_ACTION_COPY){
    		        		VSITransactionObject object = VSITransactionObject.clone((VSITransactionObject) item.getData());
    		        		object.setID(table.getItemCount());
    		    			object.setBaseURL(getCurrentBaseUrl()); // get the latest Base URL 
    		        		int  resPathDepth = getLimitDepthOfResourcePath();
    		           		String odataVersion = getOdataVersion();
    		        		AddUpdateTransactionDialog transactionDlg = new AddUpdateTransactionDialog(parent.getShell(), object, table, metadata, resPathDepth, odataVersion, true);
    						transactionDlg.create("Clone Transaction",
    		                        "Specify the transaction information in VS Model");
    						
    						if (transactionDlg.open() == Window.OK) {
    							object = transactionDlg.getTransaction();					
    							addTransactionInTable(object);
    			                table.setSelection(table.getItemCount()-1);
    			                table.setFocus();
    						}
    						break;
    					}
    					else if (i == COLUMN_ACTION_DELETE){
    						table.remove(index);
       						break;
    					}
    				}
    			}
    		}
    	});
	
	}
	
	/**
	 * 
	 * @param table
	 * @return
	 */
	private TableViewer buildAndLayoutTable(final Table table) {
		
	    TableViewer tableViewer = new TableViewer(table);
	   
        for (int i = 0; i < columnTitles.length; i++) {
            TableColumn column = new TableColumn(table, SWT.LEFT);
            column.setText(columnTitles[i]);
            column.pack();
        }

        /* Resize the columns */
        table.getColumn(COLUMN_ACTION_EDIT).setWidth(20);
        table.getColumn(COLUMN_ACTION_EDIT).setResizable(false);
        table.getColumn(COLUMN_ACTION_EDIT).setToolTipText("View and edit a transaction");
        table.getColumn(COLUMN_ACTION_COPY).setWidth(20);
        table.getColumn(COLUMN_ACTION_COPY).setResizable(false);
        table.getColumn(COLUMN_ACTION_COPY).setToolTipText("Clone a transaction");
        table.getColumn(COLUMN_ACTION_DELETE).setWidth(20);
        table.getColumn(COLUMN_ACTION_DELETE).setResizable(false);
        table.getColumn(COLUMN_ACTION_DELETE).setToolTipText("Delete a transaction");
        table.getColumn(COLUMN_METHOD).setWidth(100);
        table.getColumn(COLUMN_PATH).setWidth(400);
        table.getColumn(COLUMN_DESCRIPTION).setWidth(600);          
        
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        table.setLayoutData(data);
                        
	    return tableViewer;
	}
		
	/**
	 * 
	 * @param parent
	 * @param table
	 */
	private void appendTableMenu(final Composite parent, final Table table){
		
        Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
        
        // populate transactions beased on EDM 
        MenuItem itemAll = new MenuItem(menu, SWT.PUSH);
        itemAll.setText("Populate All Transactions");
        itemAll.addListener(SWT.Selection, new Listener() {
        	public void handleEvent(Event event) {
        		
        		if (table.getItemCount() > 0) {
					if ( false == showConfirmationMessage("IMPORTANT: this action will remove the existing transactions and populate transactions based on the specified EDM file, do you like to continue?") ){
						return;
					}
        		}
        		
    			if ( checkBaseInputObject() == false)
        			return;
    			
    			populateTransactionTable();
    			
         	}
        });

        // create a new transaction
        MenuItem itemNew = new MenuItem(menu, SWT.PUSH);
        itemNew.setText("Create a Transaction");
        itemNew.addListener(SWT.Selection, new Listener() {
        	public void handleEvent(Event event) {
        		
         		if ( checkBaseInputObject() == false)
        			return;
        		        		
           		String basePath = getCurrentBaseUrl();
    			VSITransactionObject object = VSITransactionObject.create(table.getItemCount());
    			object.setOperation(VSITransactionObject.DEFAULT_OPTIONS[0]);
    			object.setBaseURL(basePath);    			
        		int  resPathDepth = getLimitDepthOfResourcePath();
           		String odataVersion = getOdataVersion();
        		AddUpdateTransactionDialog transactionDlg = new AddUpdateTransactionDialog(parent.getShell(), object, table, metadata, resPathDepth, odataVersion, true);
				transactionDlg.create("Create Transaction",
                        "Specify the transaction information in VS Model");
				if (transactionDlg.open() == Window.OK) {
					object = transactionDlg.getTransaction();					
					addTransactionInTable(object);
	                table.setSelection(table.getItemCount()-1);
	                table.setFocus();
				}				               
        	}
        });
        
        table.setMenu(menu);       
	}


	private void populateTransactionTable() {

		// Clear the existing transaction in Table 
		String msgString = "";
		
		table.removeAll();
		tableViewer.refresh();
		showProgressStatus(true, LABEL_STATUS_INFORMATION, "Populating transactions, please wait...");
		
		setWaitingCursor(true);
		
		String basePath = getCurrentBaseUrl();
		int    resPathDepth = getLimitDepthOfResourcePath();
   		String odataVersion = getOdataVersion();
		EnumDefaultTransactions enumTrans = new EnumDefaultTransactions(basePath, resPathDepth, metadata, odataVersion);
		ArrayList<VSITransactionObject> transObjects = enumTrans.populateAvailableTransaction();
		if (transObjects.size() > 0) { 
    		for (int i=0; i<transObjects.size(); i++) {
    			VSITransactionObject object = transObjects.get(i);
				addTransactionInTable(object);
			}				               
            table.setSelection(0);
            table.setFocus();
            
            msgString = transObjects.size() + " transactions have been populated. ";
    		Log.write().info(msgString);
    		
           	showProgressStatus(true, LABEL_STATUS_INFORMATION, msgString);
    		setWaitingCursor(false);

		}
		
		if ( enumTrans.populateAvailableTransactionError())
		{
			try {
				msgString = "There are error(s) to populate transactions, please check the log file for detail\n" + Log.getLogPathFileName();
				//showErrorMessage( msgString );
	           	showProgressStatus(true, LABEL_STATUS_ERROR, msgString);
			} catch (LogException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.write().error(e.getMessage());
			}  
			finally {
				setWaitingCursor(false);
			}		
		}
		
	}

	private void addTransactionInTable(final VSITransactionObject object) {
        TableItem item = new TableItem(table, SWT.LEFT | SWT.WRAP | SWT.MULTI);
        item.setImage(COLUMN_ACTION_EDIT, editImage);
        item.setImage(COLUMN_ACTION_COPY, copyImage);
        item.setImage(COLUMN_ACTION_DELETE, deleteImage);	                
        item.setText(COLUMN_ACTION_EDIT, "");
        item.setText(COLUMN_ACTION_COPY, "");
        item.setText(COLUMN_ACTION_DELETE, "");
        item.setText(COLUMN_METHOD, object.getOperation());
        item.setText(COLUMN_PATH, object.getPath());
        item.setText(COLUMN_DESCRIPTION, object.getDescription());
        item.setData(object);		
	}
	
	private void updateTransactionInTable(final VSITransactionObject object, final TableItem item) {
        item.setText(COLUMN_METHOD, object.getOperation());
        item.setText(COLUMN_PATH, object.getPath());
        item.setText(COLUMN_DESCRIPTION, object.getDescription());
        item.setData(object);		
	}
	
	private boolean checkBaseInputObject() {
		
		String inputStr = txtVSMName.getText().trim();		
		if (inputStr.isEmpty()) {
			showErrorMessage("Please input Virtual Service Name!");	
			txtVSMName.setFocus();
			return false;
		}
		
		inputStr = txtVSMPath.getText().trim();		
		if (inputStr.isEmpty()) {
			showErrorMessage("Please specify the location to store VSM file!");	
			txtVSMPath.setFocus();
			return false;
		}
		
		inputStr = txtPort.getText().trim();		
		if (inputStr.isEmpty()) {
			showErrorMessage("Please specify the port number!");	
			txtPort.setFocus();
			return false;
		}

		String baseURL = txtBaseUrl.getText().trim();		
		if (baseURL.isEmpty()) {
			showErrorMessage("Please specify the base URL!");	
			txtBaseUrl.setFocus();
			return false;
		}
		if (false == baseURL.startsWith(URL_PATH_SEPERATOR)) {
			showErrorMessage("The base URL has to start with '" + URL_PATH_SEPERATOR + "'" );	
			txtBaseUrl.setFocus();
			return false;			
		}

		String edmFile = txtEDMFile.getText().trim();		
		if (edmFile.isEmpty()) {
			showErrorMessage("Please specify Endity Data Model file!");	
			txtEDMFile.setFocus();
			return false;
		}

		if (false == loadEntityDataModel(edmFile)) { 
			try {
				String msgString = "Failed to load '" + edmFile + "'. For detail, please check the log: " + Log.getLogPathFileName();
				//showMessage( msgString );
	           	showProgressStatus(true, LABEL_STATUS_ERROR, msgString);
			} catch (LogException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.write().error(e.getMessage());
    			showMessage("Failed to load '" + edmFile + "', \n\nPlease specify a valid entity data model file");    			
			}  
			
			txtEDMFile.setFocus();
			return false;        				
		}
		
		return true;
	}
	
	private boolean checkValidInputObject() throws IOException{

		if (false == checkBaseInputObject())
			return false;
		
		/* 
		if ( enableGenerateTransactions()) {
			if (table.getItemCount()==0){
				showErrorMessage("There is no transaction defined for Virtual Service!");	
				table.setFocus();
				return false;			
			}
		}
		*/
		
		return true;
	}
	
	private boolean generateVS() {

		System.out.println(this.getClass().getSimpleName() + " @generateVS() method called");

		try {
			if (false == checkValidInputObject())
				return false;
		
			String msgString = "";
			
			VSMObject vsmobj = new VSMObject(txtVSMName.getText().trim());
			vsmobj.setDataStoreURL("");
			vsmobj.setDataStoreType("H2");
			vsmobj.setDatabaseUser("test");
			vsmobj.setDatabasePassword("test");
			vsmobj.setHttpBaseURL(getCurrentBaseUrl());
			vsmobj.setHttpPort(txtPort.getText().trim());
			vsmobj.setVSMPath(txtVSMPath.getText().trim());
			vsmobj.setEDMFile(entityDataModelFile);
			vsmobj.setOdataVersion(getOdataVersion());
			
			//VSI should be always in the folder {{LISA_PROJ_ROOT}}/VServices/Images/
			String vsiFile = "{{LISA_PROJ_ROOT}}/VServices/Images/" + vsmobj.getVSMName() + ".vsi"; 
	    	vsmobj.setVSIFile(vsiFile);
	
	    	if ( enableGenerateTransactions() ) {
	    		ArrayList<VSITransactionObject> transactions = getTransactionListInTable();
	    		vsmobj.addTranscations(transactions);
	    	}
	    	
			String vsmFile = vsmobj.getVSMPath() + File.separator + vsmobj.getVSMName() + ".vsm"; 
	    	File file = new File(vsmFile);
	    	if (file.exists()){
	    		boolean bSave = MessageDialog.openConfirm(composite.getShell(), viewTitle, "The VSM file exists already, do you like to overwrite " + vsmFile );
	    		if (bSave == false) {
	    			txtVSMName.setFocus();
	    			return false;
	    		}
	    	}
	    	   	
			showProgressStatus(true, LABEL_STATUS_INFORMATION, "Generating VSM Project, please wait...");
			setWaitingCursor(true);
	
	    	LisaMarObject marObject = LisaMarUtil.generateLisaProject(vsmobj, metadata, sampleData, true);
	    	String marFile = marObject.getLisafile();
	    	Map<String, Object> marErrors = marObject.getErrors();
	    	Map<String, Object> marWarnings = marObject.getWarnings();
			setWaitingCursor(false);
	    	
	    	if (marFile.isEmpty()) {
	    		msgString = "Failed to create VS, please check the log file for the details\n" + Log.getLogPathFileName();
	        	showProgressStatus(true, LABEL_STATUS_ERROR, msgString);
	        	return false;
	    	}
	    	
	    	String status = LABEL_STATUS_INFORMATION;
			msgString = "The VS Model has been saved as " + marFile ;
    		if (marErrors != null && marErrors.size() > 0) {
    			status = LABEL_STATUS_ERROR;
    			msgString += ". Please check the log file for the error details\n" ;
     		}
    		else if (marWarnings != null && marWarnings.size() > 0) {
    			status = LABEL_STATUS_WARNING;
    			msgString += ". Please check the log file for the error or warning details\n" ;
    		}
    		else {
    			status = LABEL_STATUS_INFORMATION;
    			msgString += ". Please check the log file for the details\n" ;
    		}
    		
			msgString += Log.getLogPathFileName();
			
	       	showProgressStatus(true, status, msgString);
   	   	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.write().error(e.getMessage());
		}
		
		return true;
    	
	}

	private ArrayList<VSITransactionObject> getTransactionListInTable(){
		
		ArrayList<VSITransactionObject> transcations = new ArrayList<VSITransactionObject>();	

		for (int index=0; index<table.getItemCount(); index++){
			TableItem item = table.getItem(index);
			VSITransactionObject object = (VSITransactionObject)item.getData();
			object.setBaseURL(getCurrentBaseUrl()); // get the latest Base URL 
			transcations.add(object);
		}
		
		return transcations;
	}		
	
	private void showProgressStatus(final boolean showProgress, final String status, final String statusMessage) {
		
		lblTaskStatus.setText(status);
		txtTaskStatus.setText(statusMessage);

		lblTaskStatus.setVisible(showProgress);
		txtTaskStatus.setVisible(showProgress);
		
		Color lblColor;
		Color txtColor;		
		if ( status.equals(LABEL_STATUS_ERROR) ) {
			lblColor = composite.getDisplay().getSystemColor(SWT.COLOR_RED);
			txtColor = composite.getDisplay().getSystemColor(SWT.COLOR_RED);
		}
		else if ( status.equals(LABEL_STATUS_WARNING) ) {
			lblColor = composite.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW);
			txtColor = composite.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW);
		}
		else {
			lblColor = composite.getDisplay().getSystemColor(SWT.COLOR_BLUE);
			txtColor = composite.getDisplay().getSystemColor(SWT.COLOR_BLUE);
		}
        lblTaskStatus.setForeground(lblColor);
		txtTaskStatus.setForeground(txtColor);

		lblTaskStatus.update();
		txtTaskStatus.update();
		statusComposite.layout(true);		
		statusComposite.update();
		
	}
	
	private void setWaitingCursor(final boolean waiting) {
		Cursor cursor = null;
		if (waiting)
			cursor = new Cursor(composite.getDisplay(), SWT.CURSOR_WAIT);
		else	
			cursor = new Cursor(composite.getDisplay(), SWT.CURSOR_ARROW);
		
		composite.getShell().setCursor(cursor);

	}
			
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new Separator());
		manager.add(saveAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(saveAction);
	}

	private void makeActions() {
				
		//Action: Save to VSM (can overwrite a existing VSM)
		saveAction = new Action() {
			public void run() {
				generateVS();
			}
		};
		saveAction.setText("Create Virtual Service");
		saveAction.setToolTipText("Create Virtual Service");
		saveAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
	}

	private String getCurrentBaseUrl() {	
		String baseUrl = txtBaseUrl.getText().trim();
		if ( baseUrl.endsWith(File.separator) || baseUrl.endsWith("/"))
			baseUrl = baseUrl.substring(0, baseUrl.length()-1);
		return baseUrl;
	}
	
	private void showMessage(String message) {
		MessageDialog.openInformation(composite.getShell(), viewTitle, message);
	}

	public void showErrorMessage(String message) {
		MessageDialog.openError(composite.getShell(), viewTitle, message);
	}
	
	private boolean showConfirmationMessage(String message) {
		return MessageDialog.openConfirm(composite.getShell(), viewTitle, message);
	}

	private boolean loadEntityDataModel(String edmfile){
		boolean isRaml = false;
		if (btnRadioRAML.getSelection())
			isRaml = true;
		
		if (edmfile.isEmpty()) {			
			edmfile = getModelFile(isRaml);			
			if (edmfile.isEmpty())
				return false;
    		txtEDMFile.setText(edmfile);
		}
		
    	try {    		
    		MetadataParser cmsmdParser = new MetadataParser();
			cmsmdParser.parseXmlFile(entityDataModelFile);
			metadata = cmsmdParser.getMetadata();
	    	//cmsmdParser.dump();
			
 			return true;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.write().error(e.getMessage());
		}
		
		return false;
	}
	
	private int getLimitDepthOfResourcePath() {	
		return OdataAssistantPreferencePage.maxDepthOfResourcePath + 1;
	}
	
	private boolean enableGenerateTransactions() {	
		return OdataAssistantPreferencePage.enableGenerateTransactions;
	}
	
	private String getOdataVersion() {
		String odataVersion = cbOdataVersion.getText();
		if (odataVersion.equals(ODATA_VSERSION_4))
			odataVersion = CommonDefs.VALUE_ODATA_VERSION_4;		//ODATA_VSERSION_4
		else
			odataVersion = CommonDefs.VALUE_ODATA_VERSION_3;		//ODATA_VSERSION_3
		
		return odataVersion;

	}
	
	private String getModelFile(boolean isRaml) {
		
		FileDialog dialog = new FileDialog(composite.getShell());
		String[] ext = isRaml ? new String[]{"*.raml"} : new String[]{"*.xml"};
		dialog.setFilterExtensions(ext);
		dialog.open();
		
		if (dialog.getFileName().isEmpty())
			return "";
		
		String edmfile = dialog.getFilterPath() + File.separator + dialog.getFileName();
		FileInputStream ramlFileStream = null;
		
		try {
			if (isRaml) {
				File  ramlFile	= new File(edmfile);			
				ramlFileStream = new FileInputStream(ramlFile.getAbsolutePath());
				FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
				RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
				Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
				
	 			EDM edm = new EDM(raml);
				Document docAEDM = edm.getDocument();			
				
				sampleData = RamlUtil.getSampleData(raml, ramlFile.getParentFile());
				entityDataModelFile = LisaMarUtil.writeDomToFile(docAEDM);
				
				// get the information from raml and update the input fields
				// Get service name, base URL and port from RAML
				String strVsName = raml.getTitle().replaceAll("\\s", ""); 
				String strVsBaseURL = raml.getBasePath();				
				URI uri = new URI(raml.getBaseUri());
				int intVsPort = uri.getPort();
				txtVSMName.setText(strVsName);
				txtBaseUrl.setText(strVsBaseURL);
				txtPort.setText(String.valueOf(intVsPort));									
			}
			else {
				entityDataModelFile = edmfile;
	    	}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			edmfile = "";
			e.printStackTrace();
			Log.write().error(e.getMessage());
		}
		finally {
			safeClose(ramlFileStream); 
		}
		
		return edmfile;
	}

	/**
	 * Passing the focus request to VSM name text field.
	 */
	public void setFocus() {
		txtVSMName.setFocus();		
	}

	public static void enableTransactionTable(final boolean enabled) {
		table.setEnabled(enabled);
	}
	
	public static void safeClose(FileInputStream fis) {
		if (fis != null) {
		    try {
		      fis.close();
		    } 
		    catch (IOException e) {
				Log.write().error(e.getMessage());
		    }
	  	}
	}
	
}