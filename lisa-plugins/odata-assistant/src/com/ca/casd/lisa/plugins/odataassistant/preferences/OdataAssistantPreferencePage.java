/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.lisa.plugins.odataassistant.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import com.ca.casd.lisa.plugins.odataassistant.Activator;
import com.ca.casd.lisa.plugins.odataassistant.views.OdataAssistant;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class OdataAssistantPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public static int		MINI_DEPTH_OF_RESOURCE_PATH 	= 0;
	public static int		MAX_DEPTH_OF_RESOURCE_PATH 		= 10;

	public static int		maxDepthOfResourcePath 			= 4;		//default is 4, max is 10
	public static boolean	enableGenerateTransactions		= true; 	

	private static IntegerFieldEditor 	fieldMaxNumberEditor; 
	private static BooleanFieldEditor 	enablePopulateTransactionEditor; 
	
	public static boolean 	isFirstTime = true;
	
	public OdataAssistantPreferencePage() {
		
		super(GRID);
		if (Activator.getDefault() != null)
			setPreferenceStore(Activator.getDefault().getPreferenceStore());
		//setDescription("Set the limit number of the depth for the resource path [0 - 10]");

	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		
	    Composite container = getFieldEditorParent();
	
		getFieldEditorParent().setLayout(new GridLayout(1, false));
		
		SpacerFieldEditor spacer1 = new SpacerFieldEditor(container);
		addField(spacer1);
		SpacerFieldEditor spacer2 = new SpacerFieldEditor(container);
		addField(spacer2);
		
		enablePopulateTransactionEditor = new BooleanFieldEditor(PreferenceConstants.P_ENABLE_POPULATE_TRANSACTIONS, "&Enable to Populate Transactions", container); 
		enablePopulateTransactionEditor.setEnabled(true, container);
		addField( enablePopulateTransactionEditor );

		fieldMaxNumberEditor = new IntegerFieldEditor(PreferenceConstants.P_MAX_NUMBER_PATH, "&Maximum depth of the resource path [0 - 10]:", container);		
		fieldMaxNumberEditor.setValidRange(MINI_DEPTH_OF_RESOURCE_PATH, MAX_DEPTH_OF_RESOURCE_PATH);
		addField( fieldMaxNumberEditor );
				
	}
	
	protected void checkState() {		
        super.checkState();
	}
	
	protected void initialize() {
		super.initialize();	
		if (isFirstTime) {
			isFirstTime = false;
			performDefaults();
		}
		else {
			fieldMaxNumberEditor.setEnabled(enablePopulateTransactionEditor.getBooleanValue(), getFieldEditorParent());
		}
	}
	
	public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getProperty().equals(FieldEditor.VALUE)) {
            checkState();
        }
        
        if (event.getSource() instanceof BooleanFieldEditor) {
		    BooleanFieldEditor booleanEditor = (BooleanFieldEditor) event.getSource();
		    if (booleanEditor.getPreferenceName().equals(PreferenceConstants.P_ENABLE_POPULATE_TRANSACTIONS))
		    	fieldMaxNumberEditor.setEnabled(booleanEditor.getBooleanValue(), getFieldEditorParent());
		}
        
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	public boolean performOk() {	
		maxDepthOfResourcePath = fieldMaxNumberEditor.getIntValue(); 
		enableGenerateTransactions = enablePopulateTransactionEditor.getBooleanValue();
		
		OdataAssistant.enableTransactionTable(enableGenerateTransactions);
		
		return super.performOk();
	}
	
	public void performDefaults() {		
		super.performDefaults();		
		fieldMaxNumberEditor.setEnabled(enablePopulateTransactionEditor.getBooleanValue(), getFieldEditorParent());		
	}

	public class SpacerFieldEditor extends FieldEditor {
        public SpacerFieldEditor(Composite parent) {
                super("", "", parent);
        }

        @Override
        protected void adjustForNumColumns(int numColumns) {
        	// do nothing
        }

        @Override
        protected void doFillIntoGrid(Composite parent, int numColumns) {
                getLabelControl(parent);
        }

        @Override
        protected void doLoad() {
        	// do nothing
        }

        @Override
        protected void doLoadDefault() {
        	// do nothing
        }

        @Override
        protected void doStore() {
        	// do nothing
        }

        @Override
        public int getNumberOfControls() {
        	return 0;
        }
}
	
}