package edu.asu.dlsandy;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.util.ArrayList;

/**
 * a representation of all enrollments in a course
 */
public class CanvasEnrollments extends ArrayList<String> {    
	private static final long serialVersionUID = 1L;

	/**
     * constructor - initialize the enrollments from a JSON array that was received from
     *               the Canvas LMS API
     */
    public CanvasEnrollments(JsonArray enrollments) {
        if (enrollments!=null) {
            for (int enrollment_idx = 0; enrollment_idx<enrollments.size();enrollment_idx++) {
                // only concern ourselves with courses where the user is a TA or teacher
                JsonObject enrollment = (JsonObject)enrollments.get(enrollment_idx);
                add(enrollment.getValue("type"));
            }
        }        
    }
}
