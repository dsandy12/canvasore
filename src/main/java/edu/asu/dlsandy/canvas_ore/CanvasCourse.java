package edu.asu.dlsandy.canvas_ore;
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
    private final String account_id;
    private final String name;
    private final String course_code;
    private final CanvasEnrollments enrollments;
    private final GregorianCalendar start_date;
    private final String id;
    
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
            start_date.set(2000, Calendar.JANUARY,1);
        } else {
            String[] date_parts = canvas_date.split("[-T]");
            //noinspection MagicConstant
            start_date.set(Integer.parseInt(date_parts[0]),
                    Integer.parseInt(date_parts[1]),
                    Integer.parseInt(date_parts[2]));
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
