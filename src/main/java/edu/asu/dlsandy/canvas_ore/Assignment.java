/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */

package edu.asu.dlsandy.canvas_ore;

import java.util.TreeMap;

/**
 *  The representation of a single course assignment as stored in Canvas LMS.
 */
public class Assignment {
    private String name;
    private String id;
    private String course_id;
    private String due_at;
    private double points_possible;
    private CanvasRubric rubric;  // TODO: allow the assignment to have more than one rubric
    private boolean is_quiz;
    private String quiz_id;

    // If this is a group assignment, boolean flag indicating whether or not
    // students will be graded individually.
    private boolean grade_group_students_individually; 
    
    // The ID of the assignment's group set, if this is a group assignment. For
    // group discussions, set group_category_id on the discussion topic, not the
    // linked assignment.
    private String group_category_id;
    private static CanvasUserGroups teams = null;
    private static boolean loadedTeams = false;
    private CanvasQuiz quiz;
    
    // a tree map with the student id as the key and the assignment score as the value
    private TreeMap<String,Double> grades;
    
	private LoadingStatus loadingStatus = new LoadingStatus();

    /**
     * Constructor for the assignment.
     * @param obj - a JsonObject of a single assignment as received from the Canvas LMS
     */
    public Assignment(JsonObject obj) {
        // initialize the course from the given canvas object.
        name = obj.getValue("name");
        id = obj.getValue("id");
        due_at = obj.getValue("due_at");
        points_possible = obj.getDouble("points_possible");
        rubric = new CanvasRubric((JsonArray)obj.get("rubric"));
        course_id = obj.getValue("course_id");
        grade_group_students_individually = obj.getBoolean("grade_group_students_individually");
        group_category_id = obj.getValue("group_category_id");
        if (obj.getValue("submission_types").equals("online_quiz")) {
        	is_quiz = true;
        	quiz_id = obj.getValue("quiz_id");
        	quiz = new CanvasQuiz(course_id,quiz_id);
        } else {
        	is_quiz = false;
        	quiz_id = "";
        }
        grades = new TreeMap<>();
    }
    
    /**
     * returns true if the assignment is a quiz, otherwise false.
     */
    public boolean isQuiz() {return is_quiz; }
    
    /**
     * returns a string representation of the quiz id for the assignment.
     * if the assignment is not a quiz, an empty string is returned.
     */
    public String getQuizId() {return quiz_id; }
    
    /**
     * returns the name of the assignment
     */
    public String getName() { return name; }
    
    /**
     * returns the Canvas ID of the assignment
     */
    public String getId() {return id;}
    
    /**
     * returns a Canvas date string representing the due date of the assignment
     */
    public String dueAt() {return due_at;}
    
    /**
     * returns the maximum number of points possible for the assignment
     */
    public double getPointsPossible() {return points_possible;}
    
    /**
     * returns a CanvasRubric object for the rubric associated with this assignment.  If
     * no rubric exists, null is returned. 
     * 
     * TODO: allow the assignment to have more than one rubric
     */
    public CanvasRubric getRubric() {return rubric;}
    
    /**
     * returns a CanvasQuiz object for the quiz associated with this assignment.  If no
     * quiz is associated with the assignment, returns null.
     */
    public CanvasQuiz getQuiz() {return quiz;}
    
    /**
     * Load the grades associated with the assignment.  Returns true on success, otherwise false.
     */
    public boolean loadGrades() {
    	final class LoaderThread extends Thread {
    		public void run() {
    	        // load the submissions associated with the assignment, including rubric
    	        // assessments
	        	synchronized (loadingStatus) {
	        		loadingStatus.setChanged(true);
	        		loadingStatus.setSubOperationDescription("Loading Submissions");
	        	}
    	        CanvasSubmissions submissions = new CanvasSubmissions(course_id,id);
    	        
    	        // load the teams
    	        if (!loadedTeams) {
    	        	synchronized (loadingStatus) {
    	        		loadingStatus.setChanged(true);
    	        		loadingStatus.setSubOperationDescription("Loading Team Information");
    	        	}
    	            if ((group_category_id!=null)&&(!group_category_id.toLowerCase().equals("null"))) {
    	                teams = new CanvasUserGroups(course_id);
    	            }
    	            loadedTeams = true;
    	        }
    	        CanvasUserGroup  team;

    	        double pct = 0;
    	        double step = 1.0/(double)submissions.size();

    	        // loop through each submission
    	        for (CanvasSubmission submission:submissions) {
    	        	synchronized (loadingStatus) {
    	        		loadingStatus.setChanged(true);
    	        		loadingStatus.setSubOperationDescription("Processing Scores for Submission : "+submission.getId());
    	        		loadingStatus.setPercentDone(pct);
    	        	}
    	        	pct = pct+step;
    	        	
    	            // ignore submissions that are not the most recently graded
    	            if (submission.getGradeMatches()== false) continue; 
    	            
    	            // take care of assignment grades
    	            if ((teams!=null)&&(grade_group_students_individually==false)) {
    	                // if this is a group project, get a list of the students in the group
    	                // and add grade items for each student in the group
    	                team = teams.getAssociatedGroup(submission.getUserId());
    	                if (team!=null) {
    	                    grades.put(submission.getUserId(), submission.getScore());                    
    	                    rubric.setRubricScores(submission.getUserId(),submission.getRubricScores());
    	                }
    	            } else {
    	                // otherwise, add a grade item for this student alone
    	                grades.put(submission.getUserId(), submission.getScore());                    
    	                rubric.setRubricScores(submission.getUserId(),submission.getRubricScores());
    	            }
    	        }
    	            
	        	synchronized (loadingStatus) {
	        		loadingStatus.setChanged(true);
	        		loadingStatus.setPercentDone(1.0);
	        	}
    		}
    	}
    	boolean result = true;
        // clear any existing grades
        grades.clear();

    	loadingStatus.setMainOperationDescription("Loading Results for Assignment: "+name);
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
    	
    	// if the assignment is a quiz, load the question bank specific scores
        if (is_quiz) quiz.loadGrades(course_id);

        return result;
    }

    /**
     * get the outcome points for the specified student and outcome association.
     * 
     * @param oa - the outcome association (eg. assignment, rubric item or question bank) 
     *             to get points for
     * @param student_id - the canvas student id to get the points for
     * @return - the number of points the student scored toward the specific outcome 
     *           related to this assignment.
     */
    public double getStudentOutcomePoints(OutcomeAssociation oa, String student_id) {
        // return if student did not submit an assignment
        if (!grades.containsKey(student_id)) return 0;

        // if this evaluation is for this assignment, process the assignment
        if ((oa==null)||((oa.getRubricCriterion() == null) && (oa.getQuestionGroup() == null))) {
            return grades.get(student_id);
        }
 
        if (oa.getRubricCriterion() != null) {
	        // return the points for the rubric - note that if the student's
	        // assigned grade does not match the sum of the rubric scores, then the 
	        // rubric score will be scaled to match the assignment grade.  This
	        // case occurs for group assignments where a student has been assigned a
	        // grade lower than their peers.
	        if (rubric.getStudentPointSum(student_id)==0) return 0;
	        double scale = grades.get(student_id)/rubric.getStudentPointSum(student_id); 
	        return rubric.getStudentOutcomePoints(oa.getRubricCriterion(),student_id)*scale;
        }
        
          if (is_quiz) {
        	  return quiz.getQuestionGroups().getStudentOutcomePoints(oa,student_id);
          }
          return 0.0;
    }

    /**
     * get the maximum outcome points that a student could earn for the specified outcome association.
     * 
     * @param oa - the outcome association (eg. assignment, rubric item or question bank) 
     *             to get points for
     * @return - the number of points that could be earned toward the specific outcome 
     *           related to this assignment.
     */
    public double getMaximumOutcomePoints(OutcomeAssociation oa) {
        // if this evaluation is for this assignment, process the assignment
        if ((oa == null) ||((oa.getRubricCriterion() == null) && (oa.getQuestionGroup()==null))) {
            return points_possible;
        }

        if (oa.getRubricCriterion() != null) {
        	// return the points for the rubric criteria
        	return rubric.getMaximumOutcomePoints(oa.getRubricCriterion());
        }
        return quiz.getQuestionGroups().getMaximumOutcomePoints(oa);
    }
    
    /**
     * returns true if this assignment contains the specified rubric criterion name
     * within its associated rubric (if any)
     */
    public boolean contains(String rubricCriterionName) {
        return rubric.contains(rubricCriterionName);
    }
}

