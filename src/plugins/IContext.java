/**
 *
 * IContext.java
 * 
 * (c) Copyright 2008-2009 P. Jakubčo
 * 
 * KISS, YAGNI
 * 
 */

package plugins;


public interface IContext {

    /**
     * Return unique ID of this context. This can be any string identifying
     * concrete context. Usually it is related with kind of context
     * (e.g. "cpu8080","mitsSIO-2",...) . Other plugins can identify the
     * context by recognizing of its ID.
     * @return ID of this context.
     */
    public String getID ();
    
    /**
     * Hash string (doesn't matter how long) is computed from names of all
     * methods. Hash is computed from a string that consists of alphabetically
     * sorted names of all context methods with their return type and parameter
     * types.
     * 
     * A single method is written in the following way:
     * "type name(parameter_type1,parameter_type2,...)"
     * 
     * Methods are then sorted alphabetically in ascending, and joined together
     * with separator ";", like this:
     * "method1;method2;..."
     * 
     * For example: "String getID();void setRAM(int,int)" 
     * 
     * Then the string has to be hashed by md5 hash.
     * 
     * The hash is used for identifying the context by other plugins.
     * 
     * @return hash of all context methods
     */
    public String getHash();

}

