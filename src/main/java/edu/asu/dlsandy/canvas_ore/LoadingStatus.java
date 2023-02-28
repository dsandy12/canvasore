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
	
	public synchronized void setStatus(String mainOperationDescription, String subOperationDescription, double percentDone) {
		if (mainOperationDescription!=null) this.mainOperationDescription = mainOperationDescription;
		if (subOperationDescription!=null) this.subOperationDescription = subOperationDescription;
		if (percentDone>=0) this.percentDone = percentDone;
		this.changed = true;
	}

	public synchronized String getMainOperationDescription() {
		return mainOperationDescription;
	}

	public synchronized String getSubOperationDescription() {
		return subOperationDescription;
	}

	public synchronized double getPercentDone() {
		return percentDone;
	}

	public synchronized boolean isChanged() {
		return changed;
	}
}
