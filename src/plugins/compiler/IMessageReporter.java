/**
 * IMessageReporter.java
 * 
 * (c) Copyright 2008-2009, P.Jakubčo
 * 
 * KISS, YAGNI
 * 
 */
package plugins.compiler;

/**
 * Interface for reporting messages while running compilation process. It is
 * used for sending compiling messages to main module, e.g. warnings, errors, etc.
 * These messages are showed in bottom text area in panel "source code" in the
 * main module.
 */
public interface IMessageReporter {

	public static final int TYPE_WARNING = 1;
	public static final int TYPE_ERROR   = 2;
	public static final int TYPE_INFO    = 3;
	
    /**
     * Method reports some message to a main module.
     * @param message   message to report
     * @param type      type of the message (one of the
     *                  <code>TYPE_WARNING</code>, <code>TYPE_ERROR</code>,
     *                  <code>TYPE_INFO</code>)
     */
    public void report (String message, int type);

    /**
     * Method reports some message to a main module with location information.
     * 
     * @param row       row in the source code that is related to the message
     * @param column    column in the source code that is related to the message
     * @param message   message to report
     * @param type      type of the message (one of the
     *                  <code>TYPE_WARNING</code>, <code>TYPE_ERROR</code>,
     *                  <code>TYPE_INFO</code>)
     */
    public void report (int row, int column, String message, int type);

}

