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
 * a representation of a list of canvas courses
 */
public class CanvasCourses extends ArrayList<CanvasCourse> {
	private static final long serialVersionUID = 1L;

	/**
     * constructor - initialize from a canvas request of all courses associated with the canvas user
     */
    public CanvasCourses()  {
        try {
            JsonArray courses;
            courses = (JsonArray) RequesterSso.apiGetRequest("courses?per_page=100");
            if (courses != null) {
                for (JsonAbstractValue obj:courses) {
                    // create and add the courses, configuring them from the canvas request data
                    CanvasCourse course = new CanvasCourse((JsonObject)obj);
                    add(course);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CanvasCourses.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
