package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


/**
 * A class that represents the status of loading a complex request
 * from Canvas.  This class is updated by the object's loading
 * method and can be read by other classes to determine the status.
 * 
 */
public class LoadingStatus {
	private String mainOperationDescription="";
	private String subOperationDescription="";
	private double percentDone=0.0;
	private boolean changed = false;
	
	public String getMainOperationDescription() {
		return mainOperationDescription;
	}
	
	public void setMainOperationDescription(String mainOperationDescription) {
		this.mainOperationDescription = mainOperationDescription;
	}
	
	public String getSubOperationDescription() {
		return subOperationDescription;
	}
	
	public void setSubOperationDescription(String subOperationDescription) {
		this.subOperationDescription = subOperationDescription;
	}
	
	public double getPercentDone() {
		return percentDone;
	}
	
	public void setPercentDone(double percentDone) {
		this.percentDone = percentDone;
	}
	
	public boolean isChanged() {
		return changed;
	}
	
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
}
