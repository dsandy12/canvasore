package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */



import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * representation of a list of all user groups in a specific course
 */
public class CanvasUserGroups extends ArrayList<CanvasUserGroup> {
	@Serial
    private static final long serialVersionUID = 1L;

	/**
	 * constructor - initialize the user groups by querying canvas
	 * @param course_id - the associated course ID
	 */
	public CanvasUserGroups(String course_id) {
        try {
            JsonArray jsonArray;
            jsonArray = (JsonArray) RequesterSso.apiGetRequest("courses/"+course_id+"/groups?per_page=100");
            if (jsonArray != null) init(jsonArray);
        } catch (IOException ex) {
            Logger.getLogger(Assignments.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
	/* 
	 * helper function to initialize each group based on information from canvas
	 */
    private void init(JsonArray array) {
        try {
            for (JsonAbstractValue jsonGroup:array) {
                // retrieve the users for a specific group
                JsonArray members = (JsonArray)RequesterSso.apiGetRequest("groups/"+jsonGroup.getValue("id")+"/users?per_page=100");
                if (members != null) {
                    CanvasUserGroup group = new CanvasUserGroup();
                    group.setId(jsonGroup.getValue("id"));
                    for (JsonAbstractValue member:members) {
                        group.add(member.getValue("id"));                        
                    }
                    add(group);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Assignments.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
            
    /**
     *  returns the group id associated with a specific user id 
     * <p>
     *  Note: this assumes that there is only one group set within canvas
    */
    public CanvasUserGroup getAssociatedGroup(String userId) {
        for (CanvasUserGroup group:this) {
            if (group.contains(userId)) {
                return group;
            }
        }
        return null;
    }
}
