package edu.asu.dlsandy;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents an implementation of a list of Canvas Question Bank objects 
 */
public class CanvasQuestionBanks extends ArrayList<CanvasQuestionBank> {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor - initialize the object instance by loading the required information 
	 * from canvas.
	 * @param course_id - the canvas course id to load the information from
	 */
	public CanvasQuestionBanks(String course_id)  {
        String strResponse;
        try {
            strResponse = RequesterSso.httpGetRequest("https://canvas.asu.edu/courses/"+course_id+"/question_banks");
            if (strResponse == null) {
            	return;
            }

            // convert the JSON-coded response into JSON objects
            JsonResultFactory rf = new JsonResultFactory();
            JsonArray ary = (JsonArray)rf.build(strResponse);        
            if (ary != null) {
          		init(ary);
            }
        } catch (IOException ex) {
        }
        
    }  
    
	/*
	 * helper function to initialize the question banks from the given JSON array
	 */
    private void init(JsonArray ary) {
        // loop for each object in the array
    	for (JsonAbstractValue obj:ary) {
    		if (((JsonObject)obj).containsKey("assessment_question_bank")) {
    			// unwrap this object
    			JsonObject inner_object = (JsonObject)((JsonObject)obj).get("assessment_question_bank");
    			// create the question bank
    			CanvasQuestionBank bank = new CanvasQuestionBank(inner_object);
                // add the new object
    			add(bank);        		
        	}
        }
    }   
    
    /**
     * Returns the question bank object with the specified ID
     * @param id - the id of the question bank to return
     * @return the question bank with the matching id, or null if no match is found
     */
    public CanvasQuestionBank getById(String id) {
    	for (CanvasQuestionBank b:this) {
    		if (b.getId().equals(id)) {
    			return b;
    		}
    	}
    	return null;
    }
}
