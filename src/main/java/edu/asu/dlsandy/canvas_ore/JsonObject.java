package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serial;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * Representation of a Json object element.  This is a concrete implementation of the JavaAbstractValue class.
 */
public class JsonObject extends TreeMap<String, JsonAbstractValue> implements JsonAbstractValue {
	@Serial
    private static final long serialVersionUID = 1L;

	@Override
	/* 
	 * diagnostic function - dumps the contents of the json object to the system output device
	 */
    public void dump(int indent) {
        Set<Map.Entry<String,JsonAbstractValue>> set = this.entrySet();
        Iterator<Map.Entry<String, JsonAbstractValue>> it = set.iterator();
        for (int i=0;i<indent;i++) System.out.print(" ");
        System.out.println("{");
        while (it.hasNext()) {
            for (int i=0;i<indent+3;i++) System.out.print(" ");
            Map.Entry<String,JsonAbstractValue> me = it.next();
            JsonAbstractValue av = me.getValue();
            System.out.print(me.getKey());
            System.out.print(":");
            av.dump(indent+3);
        }
        for (int i=0;i<indent;i++) System.out.print(" ");
        System.out.println("{");        
    }
    

    @Override
    /*
     * returns a string value of the specified element where the specifier
     * is of the form:
     * 		Empty - return the entire object
     * 		key,
     * 		key[index].specifier,
     * 		key.specifier
     */
    public String getValue(String specifier) {
        if (isEmpty()) return "";
        
        if (specifier.isEmpty()) {
            // The specifier is empty - return values for all records in the
            // object (this should not be normal)
            Set<Map.Entry<String,JsonAbstractValue>> set = this.entrySet();
            Iterator<Map.Entry<String, JsonAbstractValue>> it = set.iterator();
            StringBuilder sb = new StringBuilder();
            while (it.hasNext()) {
                Map.Entry<String,JsonAbstractValue> me = it.next();
                JsonAbstractValue av = me.getValue();
                sb.append(me.getKey());
                sb.append(":");
                sb.append(av.getValue(""));
                sb.append("\n");
            }
            return sb.toString();
        }
        // use the leftmost part of the specifier as the key
        String[] key = specifier.split("[.]",2);
        if (containsKey(key[0])) {
            StringBuilder sb = new StringBuilder();
            if (key.length>1) {
                return sb.append(get(key[0]).getValue(key[1])).toString();
            } else {
            	if (get(key[0]).getValue("")!=null)	return sb.append(get(key[0]).getValue("")).toString();                
            	return null;
            }
        } 
        return null;
    }

    @Override
    /*
     * returns a boolean value of the specified element where the specifier
     * is of the form:
     * 		Empty - return the entire object
     * 		key,
     * 		key[index].specifier,
     * 		key.specifier
     */
    public boolean getBoolean(String specifier) {
        if (isEmpty()) return false;
        // use the leftmost part of the specifier as the key
        String[] key = specifier.split("[.]",2);
        if (containsKey(key[0])) {
            if (key.length>1) return get(key[0]).getBoolean(key[1]);
            return get(key[0]).getBoolean("");
        }
        return false;
    }

    @Override
    /*
     * returns a handle where the specifier is of the form:
     * 		key
     */
    public String getHandle(String specifier) {
        return getValue(specifier);
    }

    @Override
    /*
     * returns an integer value of the specified element where the specifier
     * is of the form:
     * 		Empty - return the entire object
     * 		key,
     * 		key[index].specifier,
     * 		key.specifier
     */
    public int getInteger(String specifier) {
        if (isEmpty()) return 0;
        // use the leftmost part of the specifier as the key
        String[] key = specifier.split("[.]",2);
        if (containsKey(key[0])) {
            if (key.length>1) return get(key[0]).getInteger(key[1]);
            return get(key[0]).getInteger("");
        }
        return 0;
    }
 
    @Override
    /*
     * returns a double value of the specified element where the specifier
     * is of the form:
     * 		Empty - return the entire object
     * 		key,
     * 		key[index].specifier,
     * 		key.specifier
     */
    public double getDouble(String specifier) {
        if (isEmpty()) return 0.0;
        // use the leftmost part of the specifier as the key
        String[] key = specifier.split("[.]",2);
        if (containsKey(key[0])) {
            if (key.length>1) return get(key[0]).getDouble(key[1]);
            return get(key[0]).getDouble("");
        }
        return 0.0;
    }
    
    @Override
    /* 
     * write the object to a file specified by hte BufferedWriter parameter.
     */
    public void writeToFile(BufferedWriter br) {
        try {
            br.append('{');
            boolean isFirst = true;
            for (Map.Entry<String, JsonAbstractValue> entry: entrySet()) {
                if (!isFirst) br.append(',');
                isFirst = false;
                br.append('"');
                br.append(entry.getKey());
                br.append("\":");
                entry.getValue().writeToFile(br);
            }
            br.append('}');
        } catch (IOException ignored) {
        }
    }
}
