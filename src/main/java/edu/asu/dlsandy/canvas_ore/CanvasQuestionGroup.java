package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Representation of a single Canvas question group
 */
public class CanvasQuestionGroup {
	String id;
	String name;
	String quiz_id;
	int pick_count;
	double question_points;
	String bank_id;
	int position;
	CanvasQuestionBank bank;
    // a map of scores for this group.  The primary key is the student id.  
    TreeMap<String,Double> student_scores;  
    private LoadingStatus loadingStatus = new LoadingStatus();

    /**
     * constructor - initialize the object by reading information from Canvas LMS
     * @param cid - the course ID associated with the question group
     * @param qid - the quiz ID associated with the question group
     * @param gid - the group ID for the question group
     */
    public CanvasQuestionGroup(String cid, String qid, String gid) {
		// first, run the query to get the quiz info
	    try {
	        JsonObject obj;
	        obj = (JsonObject) RequesterSso.apiGetRequest("courses/"+cid+"/quizzes/"+qid+"/groups/"+gid);
	
	        // initialize the question bank from the given canvas json object.
	        name = obj.getValue("name");
	        id = obj.getValue("id");
	        quiz_id = obj.getValue("quiz_id");
	        pick_count = obj.getInteger("pick_count");
	        position = obj.getInteger("position");
	        question_points = obj.getDouble("question_points");
	        bank_id = obj.getValue("assessment_question_bank_id");
	        if ((bank_id!=null) && (!bank_id.equalsIgnoreCase("null"))) {
	        	bank = new CanvasQuestionBank(cid, bank_id);
	        }
	    } catch (IOException ex) {
	        Logger.getLogger("QuestionGroup").log(Level.SEVERE, null, ex);
	    }
	    student_scores = new TreeMap<String,Double>();
	}        
	
    /**
     * returns the name of the question group
     */
	public String getName() {return name;}
	
	/**
	 * returns the number of questions to pick from the group
	 */
	public int getPickCount() {return pick_count;}
	
	/**
	 * returns the position of the group relative to other groups/questions in the quiz
	 */
	public int getPosition() {return position;}
	
	/**
	 * returns the number of points that can be earned per question
	 */
	public double getPointsPerQuestion() {return question_points;}
	
	/**
	 * returns the id of the quiz bank associated with the group (if there is one), 
	 * otherwise, null.
	 */
	public String getBankId() {return bank_id;}
	
	/**
	 * returns the id of the quiz group
	 */
	public String getId() {return id;}
	
	/**
	 * returns the question bank associated with this group if one exists,
	 * otherwise, null.
	 */
	public CanvasQuestionBank getBank() {return bank;}; 
	
	/**
	 * load grades associated with questions in this question group.  Because
	 * this operation could take a long time, a progress bar is displayed while loading
	 * @param submissions - all student quiz submissions for the associated quiz
	 */
	public boolean loadGrades(CanvasQuizSubmissions submissions) {
		/* 
		 * a helper thread for loading the data and updating the status bar
		 */
    	final class LoaderThread extends Thread {
    		public void run() {
    			double pct = 0.0;
    			double step = 1.0/(double)submissions.size();
    			
    		    // loop through each submission
    			for (CanvasQuizSubmission qsub:submissions) {
    		        // ignore submissions that don't match the kept score
    		        if (qsub.getScore()!=qsub.getKeptScore()) {
    		        	pct = pct+step;
    		        	continue; 
    		        }
    		        
    	        	synchronized (loadingStatus) {
    	        		loadingStatus.setChanged(true);
    	        		loadingStatus.setPercentDone(pct);
    	        		loadingStatus.setSubOperationDescription("Processing Scores for Submission: " + qsub.getId());
    	        	}
    	        	pct = pct+step;
    	        	
    	        	// load the questions for this submission
    		        CanvasQuizQuestions qq = new CanvasQuizQuestions(qsub.getId());

    		        // loop for each question
    		        double sum = 0.0;
    		        for (CanvasQuizQuestion q:qq) {
    		        	if (q.getQuizGroupId().equals(id)) {
    		        		if (q.getCorrect()==null) {
    		        			// assume that the question was not answered
    		        		} else {
    		        			if (q.getCorrect().equalsIgnoreCase("true")) sum = sum + question_points;
    		        		}
    		        	}
    		        }    		
    		        student_scores.put(qsub.getUserId(),sum);
    		    }
    	            
	        	synchronized (loadingStatus) {
	        		loadingStatus.setChanged(true);
	        		loadingStatus.setPercentDone(1.0);
	        	}
    		}
    	}
		boolean result = true;  
	    student_scores.clear();
	    
    	loadingStatus.setMainOperationDescription("Processing Question Bank: " + name);
    	ProgressDlg progress = new ProgressDlg(loadingStatus);
    	LoaderThread loader = new LoaderThread();
    	loader.start();
    	progress.showAndWait();
  
    	// if the dialog box is closed before loading completes, stop the
    	// loader thread
    	synchronized(loadingStatus) {
    		if (loadingStatus.getPercentDone()<1.0) {
    			loader.interrupt();
    		}
    	}
    	
	    return result;
	} 
	
	/**
	 * returns the number of points that the specified student earned from questions
	 * in this question bank.
	 * @param student_id - the ID of the student
	 * @return the number of points earned
	 */
    public double getStudentOutcomePoints(String student_id) {
        if (student_scores.containsKey(student_id)) {
        	return student_scores.get(student_id);
        }
    	return 0;
    }

    /**
     * returns the total number of points that a student could earn associated with
     * questions from this question group.
     */
    public double getMaximumOutcomePoints() {
        return question_points*pick_count;
    }  
}

