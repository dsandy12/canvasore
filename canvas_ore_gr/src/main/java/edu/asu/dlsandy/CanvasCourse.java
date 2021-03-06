package edu.asu.dlsandy;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *  The representation of a single canvas course.
 */
public class CanvasCourse {
    private String account_id;
    private String name;
    private String course_code;
    private CanvasEnrollments enrollments;
    private GregorianCalendar start_date;
    private String id;
    
    /**
     * Constructor for the course.  
     * @param obj - a JsonObject that represents the course information
     */
    public CanvasCourse(JsonObject obj) {
        // initialize the course from the given JSON object.
        id = obj.getValue("id");
        name = obj.getValue("name");
        account_id = obj.getValue("account_id");
        course_code = obj.getValue("course_code");
        String canvas_date = obj.getValue("start_at");
        start_date = new GregorianCalendar();
        if (canvas_date == null) {
            start_date.set(2000,1,1);                    
        } else {
            String[] date_parts = canvas_date.split("[-T]");
            start_date.set(Integer.valueOf(date_parts[0]),
                    Integer.valueOf(date_parts[1]),
                    Integer.valueOf(date_parts[2]));  
        }
        enrollments = new CanvasEnrollments((JsonArray)obj.get("enrollments"));
    }
    
    /**
     * returns the name of the course
     */
    public String getName() { return name; }
    
    /**
     * returns teh account id associated with the course
     */
    public String getAccountId() {return account_id;}
    
    /**
     * returns the start date associated with the course
     */
    public Calendar getStartDate() {return start_date;}
    
    /**
     * returns the enrollments associated with the course
     */
    public CanvasEnrollments getEnrollments() {return enrollments;}
    
    /**
     * returns the course code for the course
     */
    public String getCourseCode() {return course_code;}

    /**
     * returns the canvas ID for the course
     */
    public String getId() {return id;}
}
