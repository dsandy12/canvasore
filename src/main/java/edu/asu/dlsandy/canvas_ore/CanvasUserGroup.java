package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.io.Serial;
import java.util.ArrayList;

/**
 * Representation of a canvas user group
 */
public class CanvasUserGroup extends ArrayList<String>{
	@Serial
    private static final long serialVersionUID = 1L;
	String groupId;
	
	/**
	 * set the ID for the group
	 */
    void setId(String groupId) {this.groupId = groupId;}

    /**
     * get the canvas ID for the group
     */
    String getId() {return groupId;}            
}
