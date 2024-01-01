package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */



import java.io.Serial;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Representation of a Canvas Rubric.  This class also contains a map of user scores
 * against each rubric row which can be used for rubric-related outcome assessment
 */
public class CanvasRubric {
    final ArrayList<RubricRow> rows;
    // a map of maps.  The first key is the student id.  The second key
    // is the rubric criteria.  There should be no duplicate grades for a specific
    // criteria, so this should work fine.
    TreeMap<String,TreeMap<String,Double>> student_scores;

    // a map of maps.  The first key is the student id.  The second key
    // is the rubric criteria.  There should be no duplicate grades for a specific
    // criteria, so this should work fine.
    TreeMap<String,TreeMap<String,String>> student_kpi_ratings;
    /**
     * Representation of a single row in a canvas rubric.  This contains an ordered
     * set of ratings where the rubric ratings that are worth the most points come 
     * before those worth fewer points.
     */
    public static class RubricRow extends ArrayList<RubricCell> {
		@Serial
        private static final long serialVersionUID = 1L;
		final double points;
        final String id;
        final String description;
        final String long_description;
        
        /**
         * add a new rubric rating to the criteria, maintaining the 
         * order of the list
         * @param rc - the rating to add
         */
        public void addOrdered(RubricCell rc) {
            for (int i=0;i<size();i++) {
                if (rc.getPoints()>get(i).getPoints()) {
                    // insert the new item before this item
                    add(i,rc);
                    return;
                }
            }
            // add the item at the end of the list
            add(size(),rc);
        }
        
        /**
         * constructor - initialize the rubric criteria from the given json object
         * @param obj - a json object that contains the initialization data
         */
        public RubricRow(JsonObject obj) {
            // initialize the rubric row with the information contained within
            // the json object.
            points = obj.getDouble("points");
            id = obj.getValue("id");
            description = obj.getValue("description");
            long_description = obj.getValue("long_description");
            JsonArray ratings = (JsonArray)obj.get("ratings");
            if (ratings != null) {
                for (JsonAbstractValue rating : ratings) {
                    RubricCell rr = new RubricCell((JsonObject) rating);
                    addOrdered(rr);
                }
            }
        }

        /**
         * returns the points for this rubric row
         */
        public double getPoints() {return points;}

        /**
         * returns the id of the associated Canvas LMS object associated with
         * this rubric row.
         */
        public String getId() {return id;}

        /**
         * returns the description for this rubric row
         */
        public String getDescription() {return description;}

        /**
         * returns the long description for this rubric row
         */
        public String getLongDescription() {return long_description;}
    }
    
    /**
     * Representation of a single cell within a Canvas Rubric
     */
    public static class RubricCell {
        final double points;
        final String id;
        final String description;
        final String long_description;

        String competency_level;
        
        /**
         * constructor - initialize from the provided JsonObject
         */
        public RubricCell(JsonObject obj) {
            points = obj.getDouble("points");
            id     = obj.getValue("id");
            description = obj.getValue("description");
            long_description = obj.getValue("long_description");

            // attempt to assign the competency level for this rubric item based on
            // the information coded in the description field.
            // first, take care of default values based on the name of the field
            competency_level = "unknown";
            switch (description.toLowerCase()) {
                case ("full marks"), ("full credit"), ("proficient") -> competency_level = "E";
                case ("partial marks"), ("partial credit"), ("competent"), ("commpetent") -> competency_level = "M";
                case ("no marks"), ("minimal marks"), ("minimal credit"), ("insufficient"), ("not proficient"), ("novice") ->
                        competency_level = "I";
                case ("missing"), ("not attempted") -> competency_level = "X";
            }
            // now look for hashtags in the long description - these will override the
            // rating name if the hashtag exists
            if (long_description.toLowerCase().contains("#exceeds_competency")) {
                competency_level = "E";
            } else if (long_description.toLowerCase().contains("#meets_competency")) {
                competency_level = "M";
            } else if (long_description.toLowerCase().contains("#insufficient_competency")) {
                competency_level = "I";
            } else if (long_description.toLowerCase().contains("#not_attempted")) {
                competency_level = "X";
            }

                if (competency_level.equals("unknown")) {
                System.out.println(description);
            }
        }
        
        /**
         * returns the points associated with this rating
         */
        public double getPoints() {return points;}

        /**
         * returns the Canvas LMS id associated with this rubric cell
         */
        public String getId() {return id;}

        /**
         * returns the description for this rubric cell
         */
        public String getDescription() {return description;}

        /**
         * returns the long description for this rubric cell
         */
        public String getLongDescription() {return long_description;}

        /**
         * returns the competency level for the rubric cell
         */
        public String getCompetencyLevel() {return competency_level;}
    }

    /**
     * constructor - initialize the rubric instance from information in the provided 
     *               JsonArray.
     * @param ary - a JsonArray that contains configuration information for the rubric
     */
    public CanvasRubric(JsonArray ary) {
        rows = new ArrayList<>();
        if (ary==null) return;
        for (JsonAbstractValue jsonAbstractValue : ary) {
            RubricRow rr = new RubricRow((JsonObject) jsonAbstractValue);
            rows.add(rr);
        }
        student_scores = new TreeMap<>();
        student_kpi_ratings = new TreeMap<>();
    }
    
    /**
     * return the rubric cell for the specified position in the rubric
     * @param row - the rubric row of the cell
     * @param col - the rubric column of the cell
     * @return the RubricCell located at the specified row/column of the rubric
     */
    public RubricCell get(int row, int col) {
        if (row>rows.size()) return null;
        if (col>rows.get(row).size()) return null;
        return rows.get(row).get(col);
    }
    
    /**
     * returns the number of points that can be earned for the specified rubric row
     */
    public double getRowPoints(int row) {if (row>rows.size()) return 0; return rows.get(row).getPoints();}
    
    /**
     * returns the Canvas ID for the specified rubric row
     */
    public String getRowId(int row) {if (row>rows.size()) return ""; return rows.get(row).getId();}

    /**
     * returns the description for the specified row
     */
    public String getRowDescription(int row) {if (row>rows.size()) return ""; return rows.get(row).getDescription();}

    /**
     * returns the long description for the specified rubric row
     */
    public String getRowLongDescription(int row) {if (row>rows.size()) return ""; return rows.get(row).getLongDescription();}

    /**
     * returns the number of rows in the rubric
     */
    public int getRowCount() {return rows.size();}
    
    /**
     * set the user scores for each rubric row based on information provided
     * @param user - the user to set the scores for
     * @param scores - a map that contains the rubric row ID as the primary key 
     *               and the score for the row as the value
     */
    public void setRubricScores(String user, TreeMap<String,Double> scores) {
        // loop for each entry in the map
    	for(Map.Entry<String,Double> entry : scores.entrySet()) {
            String rubric_row_id = entry.getKey();
            double rubric_row_score = entry.getValue();
            // if the student scores map does not yet have an entry for this user, create it
            if (!student_scores.containsKey(user)) {
                student_scores.put(user, new TreeMap<>());
            }
            // add the student score to the map
            student_scores.get(user).put(rubric_row_id, rubric_row_score);
        }
    }

    /**
     * set the user rubric-based kpi rating for each rubric row based on information provided
     * @param user - the user to set the scores for
     * @param ratings - a map that contains the rubric row ID as the primary key
     *               and the id for the rubric rating as the value
     */
    public void setRubricRatings(String user, TreeMap<String,String> ratings) {
        // loop for each entry in the map
        for(Map.Entry<String,String> entry : ratings.entrySet()) {
            String rubric_row_id = entry.getKey();
            String rubric_row_rating_id = entry.getValue();

            // if the student scores map does not yet have an entry for this user, create it
            if (!student_kpi_ratings.containsKey(user)) {
                student_kpi_ratings.put(user, new TreeMap<>());
            }
            // search the rubric for the associated rating within this rubric
            for (RubricRow row : rows) {
                if (!row.getId().equals(rubric_row_id)) continue;
                for (RubricCell rating: row) {
                    if (rating.getId().equals(rubric_row_rating_id)) {
                        student_kpi_ratings.get(user).put(rubric_row_id, rating.getCompetencyLevel());
                    }
                }
            }
        }
    }

    /**
     * returns the total number of points the specified user earned from the rubric.
     * This method assumes that the user scores have already been set using
     * SetRubricScores()
     * @param user - the user id to get the points for
     * @return - the total points earned by the student
     */
    public double getStudentPointSum(String user) {
        if (!student_scores.containsKey(user)) return 0;
        
        double result = 0;
        // loop through the map for all entries that correspond to the student
        for(Map.Entry<String,Double> entry : student_scores.get(user).entrySet()) {
            result += entry.getValue();
        }
        return result;
    }

    /**
     * returns the canvas rubric criterion (row) id for the criterion with a name that 
     * matches the specified name
     * @param name - the name to find a match for
     * @return - the canvas rubric criterion id
     */
    public String getCriterionIdFromName(String name) {
        for (RubricRow criterion:rows) {
            if (criterion.getDescription().equals(name)) return criterion.getId();
        }
        return null;
    }
    
    /**
     * return the student score for the rubric row with the specified name
     * @param rubric_row_name - the name of the rubric row
     * @param user_id - the canvas student id
     * @return the points the student earned
     */
    public double getStudentOutcomePoints(String rubric_row_name, String user_id) {
        String criterion = getCriterionIdFromName(rubric_row_name);
        if (criterion== null) return 0;
        if (!student_scores.containsKey(user_id)) return 0;
        return student_scores.get(user_id).get(criterion);
    }

    /**
     * return the student kpi attainment for the rubric row with the specified name
     * @param rubric_row_name - the name of the rubric row
     * @param user_id - the canvas student id
     * @return the points the student earned
     */
    public String getStudentOutcomeKpiAttainment(String rubric_row_name, String user_id) {
        String criterion = getCriterionIdFromName(rubric_row_name);
        if (criterion== null) return "X";
        if (!student_kpi_ratings.containsKey(user_id)) return "X";
        return student_kpi_ratings.get(user_id).get(criterion);
    }

    /**
     * return the maximum possible score for the rubric row with the specified name
     * @param rubric_row_name - the name of the rubric row
     * @return the maximum possible points
     */
    public double getMaximumOutcomePoints(String rubric_row_name) {
        for (RubricRow criterion:rows) {
            if (criterion.getDescription().equals(rubric_row_name)) return criterion.getPoints();
        }
        return 0.0;
    }
    
    /**
     * returns true of the rubric contains a row with a description that matches the specified name
     */
    public boolean contains(String rubric_row_name) {
        for (RubricRow criterion:rows) {
            if (criterion.getDescription().equals(rubric_row_name)) {
                return true;
            }
        }
        return false;                
    }
}