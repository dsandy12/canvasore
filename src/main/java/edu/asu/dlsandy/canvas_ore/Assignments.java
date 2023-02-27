package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */



import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  The representation of a list of assignments as stored in Canvas LMS.
 */
public class Assignments extends ArrayList<Assignment> implements ProvidesLoadingStatus {
	private static final long serialVersionUID = 1L;
	private final LoadingStatus loadingStatus = new LoadingStatus();
	private String course_id;
	private final String assignment_group;
	
    /**
     * Constructor for the assignments.  Assignments are initialized from data from Canvas
     * @param course_id - the course ID to load assignments from
     * @param assignment_group - the name of the assignment group to load the assignments from
     */
	public Assignments(String course_id, String assignment_group) {
    	this.course_id = course_id;
    	this.assignment_group = assignment_group;
    	try {
            JsonArray jsonArray;
            jsonArray = (JsonArray) RequesterSso.apiGetRequest("courses/"+course_id+"/assignments?per_page=100");
            if (jsonArray != null) init(jsonArray);
        }catch (IOException ex) {
            Logger.getLogger(Assignments.class.getName()).log(Level.SEVERE, null, ex);
        }    	    			
	}
	
    /**
     * Constructor for the assignments.  Assignments are initialized from the specified JsonArray object
     * @param jsonArray - json array that contains the initialization data
     * @param assignment_group - the name of the associated assignment group the assignments belong to.
     */
    public Assignments(JsonArray jsonArray, String assignment_group) {
    	this.assignment_group = assignment_group;
    	if (jsonArray != null) init(jsonArray);        
    }
    
    /**
     * Initializes the assignments and loads any additional data from Canvas.  Because this
     * process can take a long time, a separate loader thread and progress window are used to 
     * display progress.
     * 
     * @param jsonArray - an json array that contains the assignment information.
     */
    private void init(JsonArray jsonArray) {
    	// Loader thread to initialize each assignment and update the operation status 
    	final class LoaderThread extends Thread {
    		public void run() {
    	    	double pct = 0;
    			for (JsonAbstractValue obj:jsonArray) {
    	            // create and add the courses, configuring them from the canvas request data
    	            Assignment assignment = new Assignment((JsonObject)obj);
    	            add(assignment);
    	        	pct = pct +  1.0/(double)jsonArray.size();
    	        	synchronized (loadingStatus) {
    	        		loadingStatus.setChanged(true);
    	        		loadingStatus.setPercentDone(pct);
    	        		loadingStatus.setSubOperationDescription(assignment.getName());
    	        	}
    	        }
	        	synchronized (loadingStatus) {
	        		loadingStatus.setChanged(true);
	        		loadingStatus.setPercentDone(1.0);
	        	}
    		}
    	}
    	// initialize the progress bar and wait for the loading operatoin to complete
    	loadingStatus.setMainOperationDescription("Loading Assignments for Group: "+assignment_group);
    	ProgressDlg progress = new ProgressDlg(loadingStatus);
    	LoaderThread loader = new LoaderThread();
    	loader.start();
    	progress.showAndWait();
    	
    	// if the dialog box is closed before loading completes, stop the
    	// loader thread
    	synchronized(loadingStatus) {
    		if (loadingStatus.getPercentDone()<1.0) {
    			loader.interrupt();
    		}
    	}
    	// wait for the loader to die
    	try {
			loader.join();
		} catch (InterruptedException e) {
		}
    }
    
    /**
     * returns the index number of the assignment with the specified name
     */
    public int getIndexByName(String name) {
        for (int i=0; i<size(); i++) {
            Assignment assignment = get(i);
            if (assignment.getName().equals(name)) {
                return i;
            }
        } 
        return -1;
    }
    
    /**
     * get the assignment object with the specified name
     */
    public Assignment getAssignmentByName(String name) {
        for (Assignment a:this) {
            if (a.getName().equals(name)) return a;
        } 
        return null;
    }

    /**
     * load all the grades from canvas for every assignment associated with this instance
     */
    public boolean loadGrades() {
        boolean result = true;
        for (Assignment a:this) {
            result |= a.loadGrades();
        } 
        return result;
    }
    
    /**
     * returns the loading status object for this instance
     */
    public LoadingStatus getLoadingStatus() {return loadingStatus;}

    /**
     * returns the course id for this instance
     */
    public String getCourseId() {return course_id;}
}
