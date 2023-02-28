package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */



import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Representation of a list of canvas assignment submissions
 */
public class CanvasSubmissions extends ArrayList<CanvasSubmission> {
	@Serial
    private static final long serialVersionUID = 1L;

	/**
	 * constructor - create and initialize the list by querying the canvas LMS api
	 * @param course_id - the canvas id of the associated course
	 * @param assignment_id - the canvas id of the associated assignment
	 */
	public CanvasSubmissions(String course_id,String assignment_id)  {
        try {
            JsonArray jsonArray;
            jsonArray = (JsonArray) RequesterSso.apiGetRequest("courses/"+course_id+"/assignments/"+assignment_id+"/submissions?include[]=rubric_assessment;per_page=100");
            if (jsonArray != null) init(jsonArray);
        } catch (IOException ex) {
            Logger.getLogger(Assignments.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  
    
    /*
     * helper function to create and initialize all the submission objects
     */
    private void init(JsonArray json_submissions) {
        for (JsonAbstractValue json_submission:json_submissions) {
            CanvasSubmission submission = new CanvasSubmission((JsonObject)json_submission);
            add(submission);
        }
    }    
}
