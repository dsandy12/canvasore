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
 * Represents a list of all quiz submissions for a specific quiz.
 */
public class CanvasQuizSubmissions extends ArrayList<CanvasQuizSubmission> {
	private static final long serialVersionUID = 1L;

	/**
	 * constructor - initialize the list by querying the canvas LMS api.
	 * @param course_id - the id of the course
	 * @param quiz_id - the id of the quiz
	 */
	public CanvasQuizSubmissions(String course_id, String quiz_id)  {
    	try {
            JsonObject obj;
            obj = (JsonObject) RequesterSso.apiGetRequest("courses/"+course_id+"/quizzes/"+quiz_id+"/submissions?per_page=100");
            if (obj != null) {
            	if (obj.containsKey("quiz_submissions")) {
            		JsonArray ary = (JsonArray)obj.get("quiz_submissions");
            		for (JsonAbstractValue json_submission:ary) {
                        CanvasQuizSubmission question = new CanvasQuizSubmission((JsonObject)json_submission);
                        add(question);
                    }
            	}
            }
        } catch (IOException ex) {
            Logger.getLogger(Assignments.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  
}
