package edu.asu.dlsandy;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * representation of a canvas user object
 */
public class CanvasUser {
	String id;
	String name;
	/**
	 * constructor - initialize the user information by querying the canvas LMS API
	 * @param user_id - the canvas user id to query
	 */
	public CanvasUser(String user_id)  {
        try {
            JsonObject obj;
            obj = (JsonObject)RequesterSso.apiGetRequest("users/"+user_id+"/profile?per_page=100");
            if (obj != null) {
                id = obj.getValue("id");
                name = obj.getValue("name");
            }
        } catch (IOException ex) {
            Logger.getLogger(Assignments.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  
    
	/**
	 * returns the id of the user
	 */
    String getId() {return id;}

    /**
     * returns the name of the user
     */
    String getName() {return name;}
}

