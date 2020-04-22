package edu.asu.dlsandy;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


/**
 * a representation of a single Canvas LMS question bank
 */
public class CanvasQuestionBank {
    String  id;            
    int     question_count;      
    String  title; 
    
    /**
     * constructor - initialize the instance from the specified Json Object.
     */
    CanvasQuestionBank(JsonObject obj) {
        // construct the object from the provided json object
        id = obj.getValue("id");
        question_count = obj.getInteger("assessment_question_count");
        title = obj.getValue("title");
    }

    /**
     * constructor - initialize the instance from the Canvas LMS apu using 
     * the specified course ID and bank ID
     */
    public CanvasQuestionBank(String course_id, String bank_id)  {      
        // there is no api call to get one question bank - so get them all, then return the 
        // one we are interested in.
        CanvasQuestionBanks banks = new CanvasQuestionBanks(course_id);
        CanvasQuestionBank bank = banks.getById(bank_id);
        if (bank!= null) {
        	id = bank.id;
        	question_count = bank.question_count;
        	title = bank.title;
        }
    }
    
    /**
     * returns the canvas id of the question bank
     */
    public String getId() {return id;}

    /**
     * returns the title of the question bank
     */
    public String getTitle() {return title;}

    /**
     * returns the number of questions in the bank
     */
    public int    getQuestionCount() {return question_count;}
}
