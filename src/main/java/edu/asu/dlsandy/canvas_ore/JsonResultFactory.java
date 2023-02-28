package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


/**
 * Represents an abstract factory class that builds JsonAbstractValues based on a Json-formatted string
 */
public class JsonResultFactory {
    int stringPosition;
    String str;

    /* 
     * helper function used by builder to extract a double-quoted string
     */
    private String getString() {
        if (str.charAt(stringPosition)!='\"') return null;
        stringPosition++;
        int start = stringPosition;
        boolean ignoreNext = false;
        while (stringPosition <str.length()) {
            if ((str.charAt(stringPosition) == '\"') && (!ignoreNext)) {
                String result = str.substring(start, stringPosition);
                stringPosition++;
                return result;
            }
            ignoreNext = str.charAt(stringPosition) == '\\';
            stringPosition++;
        }
        return null;
    }
    
    /*
     * helper function used by builder to extract a string that is delimited by 
     * JSON ending delimiters.
     */
    private String getRaw() {
        int start = stringPosition;
        while (stringPosition <str.length()) {
            if ((str.charAt(stringPosition) == ',') || (str.charAt(stringPosition) == '}') ||
                    (str.charAt(stringPosition) == ']')) {
                return str.substring(start, stringPosition);
            } 
            stringPosition++;
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
        this.str = str.trim();
        stringPosition = 0;
        return builder();
    }
    
    /*
     * helper class to build the JsonAbstractValue
     */
    private JsonAbstractValue builder() {
        if (str.isEmpty()) return null;
        
        if (str.charAt(stringPosition)=='[') {
            stringPosition++;
            // here if we need to create a value set
            JsonArray cs = new JsonArray();
            while ((stringPosition <str.length())&&((str.charAt(stringPosition)=='"')||str.charAt(stringPosition)=='{')) {
                // create and build the object or string
                JsonAbstractValue obj = builder();
                if (obj==null) {
                    System.err.println("null object returned at "+ stringPosition);
                    return null;
                }
                cs.add(obj);

                // next character should either be a comma or an end brace
                if (stringPosition >=str.length()) {
                    System.err.println("unexpected end of string");
                    return null;
                }
                if (str.charAt(stringPosition)==']') break;
                if (str.charAt(stringPosition)==',') stringPosition++;
            }
            if (str.charAt(stringPosition)!=']') {
                System.err.println("']' expected but none found"+ stringPosition);
                return null;
            }
            stringPosition++;
            return cs;
        }

        if (str.charAt(stringPosition)=='{') {
            stringPosition++;
            // here if we need to create a canvas object
            JsonObject co = new JsonObject();
            
            // check for an empty object.
            if ((stringPosition <str.length()+1)&&(str.charAt(stringPosition)=='}')) {
                stringPosition +=2;
                return co;
            }
            
            while (stringPosition <str.length()) {
                String key = getString();
                if (key == null) return null;
                if (stringPosition >=str.length()) {
                    System.err.println("unexpected end of string");
                    return null;
                }
                if (str.charAt(stringPosition)!=':') {
                    System.err.println("keyword separator expected.  None found");
                    return null;
                }
                stringPosition++;
                // create and build the value
                JsonAbstractValue obj = builder();
                if (obj==null) return null;
                co.put(key,obj);

                // next character should either be a comma or an end brace
                if (stringPosition >=str.length()) {
                    System.err.println("Unexpected end of string");
                    return null;
                }
                if (str.charAt(stringPosition)=='}') break;
                if (str.charAt(stringPosition)==',') stringPosition++;
            }
            if (str.charAt(stringPosition)!='}') return null;
            stringPosition++;
            return co;
        }
        
        // here if the line is a value primitive
        if (str.charAt(stringPosition)=='"') {
            String s = getString();
            if (s==null) {
                System.err.println("Null string returned");
                return null;
            }
            return new JsonValue(s);
        } 
        // here if the value primitive is not quoted
        String s = getRaw();
        if (s==null) {
            System.err.println("Null raw value returned");
            return null;
        }
        return new JsonValue(s);
        
        
    } 
}
