package plugins.compiler;


// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.842B5076-0B69-82AF-CF78-8824C5BE6F80]
// </editor-fold> 
/**
 * Interface for reporting messages while running compilation process. It is
 * used for sending compiling messages to main module, e.g. warnings, errors, etc.
 * These messsges are showed in bottom text area in panel "source code" in the
 * main module.
 */
public interface IMessageReporter {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.86287B67-CDA7-2F89-CF56-C11C5A800C5E]
    // </editor-fold> 
    /**
     * Method reports some message to a main module.
     * @param message message to report
     */
    public void report (String message);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.C32FAFE6-8CBA-581D-9683-9E7CCD2BCD2C]
    // </editor-fold> 
    /**
     * Method reports some message to a main module with location information.
     * Location information should contain formatted string of the position
     * in the source code (e.g. [row, column]) that is related to reported
     * message.
     * @param message message to report
     */
    public void report (String location, String message);

}

