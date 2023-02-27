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
 * Representation of a list of quiz questions associated with a student's submission
 */
public class CanvasQuizQuestions extends ArrayList<CanvasQuizQuestion> {
	private static final long serialVersionUID = 1L;

	/**
	 * constructor - load the quiz questions associated with a specific submission from the 
	 *               canvas LMS API.
	 * @param sid - the submission to get the quiz question for
	 */
	public CanvasQuizQuestions(String sid)  {
        try {
            JsonObject obj;
            obj = (JsonObject) RequesterSso.apiGetRequest("quiz_submissions/"+sid+"/questions?per_page=100");
            if (obj != null) {
          		if (obj.containsKey("quiz_submission_questions")) {
          			init((JsonArray)obj.get("quiz_submission_questions"));
          		}
            }
        } catch (IOException ex) {
            Logger.getLogger(Assignments.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  
    
    /*
     * Initialize each question from the JsonArray returned from Canvas
     */
    private void init(JsonArray ary) {
        for (JsonAbstractValue av:ary) {
            CanvasQuizQuestion question = new CanvasQuizQuestion((JsonObject)av);
            add(question);
        }
    }    
}
