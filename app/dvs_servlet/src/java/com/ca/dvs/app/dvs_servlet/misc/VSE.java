/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.app.dvs_servlet.misc;

import java.util.ArrayList;
import java.util.List;

public class VSE {

	private String name;
	private List<VirtualService> virtualSvcs = new ArrayList<VirtualService>();

	public VSE () {
		
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}

	public List<VirtualService> getVirtualSvcs() {
		return virtualSvcs;
	}

	public void setVirtualSvcs(List<VirtualService> virtualSvcs) {
		this.virtualSvcs = virtualSvcs;
	}

}
