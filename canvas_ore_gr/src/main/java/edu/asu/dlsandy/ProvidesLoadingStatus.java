package edu.asu.dlsandy;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


/**
 * Interface for object that will provide loading status to the progress bar
 */
public interface ProvidesLoadingStatus {
	public LoadingStatus getLoadingStatus();
}
