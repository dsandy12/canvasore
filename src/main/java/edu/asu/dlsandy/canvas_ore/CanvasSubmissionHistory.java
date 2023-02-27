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
 * Representation of a canvas submission history object.  The submission history object when related 
 * related to quiz assignments includes a list of all questions the student was asked as well as 
 * the score that they received on each question. For more information, consult the canvas LMS API
 */
public class CanvasSubmissionHistory extends ArrayList<CanvasSubmissionHistoryRecord> {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor - initialize the instance using information from a canvas query
	 * @param course_id - the canvas course id 
	 * @param assignment_id - the canvas assignment id
	 * @param user_id - the canvas student id
	 */
	public CanvasSubmissionHistory(String course_id,String assignment_id, String user_id)  {
        try {
        	// get the submission object with history information included
            JsonObject obj;
            obj = (JsonObject) RequesterSso.apiGetRequest("courses/"+course_id+"/assignments/"+assignment_id+"/submissions/"+user_id+"?include[]=submission_history");
            if (obj != null) {
            	// if the object includes "submission_history" open that record
            	if (obj.containsKey("submission_history")) {
            		JsonArray history_array = (JsonArray)obj.get("submission_history");

            		for (JsonAbstractValue av:history_array) {
            			if (av != null) {
            				if (((JsonObject)av).containsKey("submission_data")) {
            					init((JsonArray)((JsonObject)av).get("submission_data"));
            				}
            			}
            		}
            	}
            }
        } catch (IOException ex) {
            Logger.getLogger(Assignments.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  
    
	/*
	 * create and initialize all the sumission data records from the provided JSON array
	 */
    private void init(JsonArray json_submission_data) {
        for (JsonAbstractValue json_submission_data_record:json_submission_data) {
            CanvasSubmissionHistoryRecord record = new CanvasSubmissionHistoryRecord((JsonObject)json_submission_data_record);
            add(record);
        }
    }    
}
