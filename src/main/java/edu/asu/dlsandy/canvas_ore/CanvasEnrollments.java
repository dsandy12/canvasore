package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.io.Serial;
import java.util.ArrayList;

/**
 * a representation of all enrollments in a course
 */
public class CanvasEnrollments extends ArrayList<String> {    
	@Serial
    private static final long serialVersionUID = 1L;

	/**
     * constructor - initialize the enrollments from a JSON array that was received from
     *               the Canvas LMS API
     */
    public CanvasEnrollments(JsonArray enrollments) {
        if (enrollments!=null) {
            for (JsonAbstractValue jsonAbstractValue : enrollments) {
                // only concern ourselves with courses where the user is a TA or teacher
                JsonObject enrollment = (JsonObject) jsonAbstractValue;
                add(enrollment.getValue("type"));
            }
        }        
    }
}
