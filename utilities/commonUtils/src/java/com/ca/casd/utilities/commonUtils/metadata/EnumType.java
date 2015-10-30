/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.metadata;

import java.util.ArrayList;
import java.util.List;

public class EnumType {

	private String name;
	private String underlyingType;
	private List<EnumMember>members = new ArrayList<EnumMember>();
	private boolean isflags = false;
	
	public EnumType () {
		
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}

	public void addMember(EnumMember member) {		
		this.members.add(member);
	}	
	
	public void setMembers( List<EnumMember> members) {
		this.members = members;
	}
	
	public List<EnumMember> getMembers() {
		return this.members;
	}
	
	public EnumMember getMemberByName(String name) {	
		for (EnumMember member : members)
			if (member.getName().equals(name))
				return member;
		
		return null;
	}

	public void setUnderlyingType(String underlyingType) {
		this.underlyingType = underlyingType;
	}
	
	public String getUnderlyingType() {
		return this.underlyingType;
	}
	
	public void setFlags(boolean isflags) {
		this.isflags = isflags;
	}

	public boolean isFlags() {
		return isflags;
	}
	
}
