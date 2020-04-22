package edu.asu.dlsandy;
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
 * Representation of a list of canvas student enrollments
 */
public class CanvasStudentEnrollmentList extends ArrayList<String> {
	private static final long serialVersionUID = 1L;

	/**
	 * constructor - initialize the list by querying the canvas student enrollments
	 *    for the specified course id
	 * @param course_id - the canvas course id to query
	 */
	public CanvasStudentEnrollmentList(String course_id) {
        try {
            JsonArray jsonArray;
            jsonArray = (JsonArray)RequesterSso.apiGetRequest("courses/"+course_id+"/enrollments?type[]=StudentEnrollment");
            if (jsonArray != null) {
                for (JsonAbstractValue obj:jsonArray) {
                	if (obj.getValue("enrollment_state").equalsIgnoreCase("active")) add(obj.getValue("user_id"));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Assignments.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
