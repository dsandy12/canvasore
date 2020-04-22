package edu.asu.dlsandy;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


/**
 * Represents an abstract factory class that builds JsonAbstractValues based on a Json-formatted string
 */
public class JsonResultFactory {
    int strpos;
    String str;

    /* 
     * helper function used by builder to extract a double quoted string 
     */
    private String getString() {
        if (str.charAt(strpos)!='\"') return null;
        strpos ++;
        int start = strpos;
        boolean ignoreNext = false;
        while (strpos<str.length()) {
            if ((str.charAt(strpos) == '\"') && (!ignoreNext)) {
                String result = str.substring(start,strpos);
                strpos++;
                return result;
            }
            ignoreNext = false;
            if (str.charAt(strpos)=='\\') ignoreNext = true;
            strpos++;
        }
        return null;
    }
    
    /*
     * helper function used by builder to extract a string that is delimited by 
     * JSON ending delimiters.
     */
    private String getRaw() {
        int start = strpos;
        while (strpos<str.length()) {
            if ((str.charAt(strpos) == ',') || (str.charAt(strpos) == '}') ||
                    (str.charAt(strpos) == ']')) {
                String result = str.substring(start,strpos);
                return result;
            } 
            strpos++;
        }
        return null;
    }

    /**
     * entry point for the builder.  Builds a JsonAbstractValue based on the input string
     * @param str - the JSON formatted string that specifies the structure to build
     * @return A JsonAbstractValue structure that matches the input string
     */
    public JsonAbstractValue build(String str) {
        // trim leading and trailing whitespace
        this.str = new String(str.trim());
        strpos = 0;
        return builder();
    }
    
    /*
     * helper class to build the JsonAbstractValue
     */
    private JsonAbstractValue builder() {
        if (str.isEmpty()) return null;
        
        if (str.charAt(strpos)=='[') {
            strpos++;
            // here if we need to create a value set
            JsonArray cs = new JsonArray();
            while ((strpos<str.length())&&((str.charAt(strpos)=='"')||str.charAt(strpos)=='{')) {
                // create and build the object or string
                JsonAbstractValue obj = builder();
                if (obj==null) {
                    System.err.println("null object returned at "+String.valueOf(strpos));
                    return null;
                }
                cs.add(obj);

                // next character should either be a comma or an end brace
                if (strpos>=str.length()) {
                    System.err.println("unexpected end of string");
                    return null;
                }
                if (str.charAt(strpos)==']') break;
                if (str.charAt(strpos)==',') strpos++;                
            }
            if (str.charAt(strpos)!=']') {
                System.err.println("']' expected but none found"+String.valueOf(strpos));               
                return null;
            }
            strpos++;
            return cs;
        }

        if (str.charAt(strpos)=='{') {
            strpos++;
            // here if we need to create a canvas object
            JsonObject co = new JsonObject();
            
            // check for an empty object.
            if ((strpos<str.length()+1)&&(str.charAt(strpos)=='}')) {
                strpos+=2;
                return co;
            }
            
            while (strpos<str.length()) {
                String key = getString();
                if (key == null) return null;
                if (strpos>=str.length()) {
                    System.err.println("unexpected end of string");
                    return null;
                }
                if (str.charAt(strpos)!=':') {
                    System.err.println("keyword separator expected.  None found");
                    return null;
                }
                strpos++;
                // create and build the value
                JsonAbstractValue obj = builder();
                if (obj==null) return null;
                co.put(key,obj);

                // next character should either be a comma or an end brace
                if (strpos>=str.length()) {
                    System.err.println("Unexpected end of string");
                    return null;
                }
                if (str.charAt(strpos)=='}') break;
                if (str.charAt(strpos)==',') strpos++;                
            }
            if (str.charAt(strpos)!='}') return null;
            strpos++;
            return co;
        }
        
        // here if the line is a value primitive
        if (str.charAt(strpos)=='"') {
            String s = getString();
            if (s==null) {
                System.err.println("Null string returned");
                return null;
            }
            JsonValue cv = new JsonValue(s);
            return cv;
        } 
        // here if the value primitive is not quoted
        String s = getRaw();
        if (s==null) {
            System.err.println("Null raw value returned");
            return null;
        }
        JsonValue cv = new JsonValue(s);
        return cv;
        
        
    } 
}
