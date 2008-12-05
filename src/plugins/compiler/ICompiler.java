package plugins.compiler;

import java.io.Reader; 
import plugins.memory.IMemoryContext;
import plugins.IPlugin; 
import plugins.ISettingsHandler; 

// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.CFF12729-6437-6F64-97AA-EEF100F11420]
// </editor-fold> 
/**
 * This interface is the core for compiler plugin types. These plugins
 * should implement this interface once and only once.
 */
public interface ICompiler extends IPlugin {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.A3B94BCD-5C35-4093-86BA-36715167D176]
    // </editor-fold> 
    /**
     * Perform initialization process of a compiler. This method is called
     * immediately after plugins are loaded into memory.
     * @param sHandler settings handler object. Compiler can use this for
     *                 accessing/storing/removing its settings.
     */
    public void initialize (ISettingsHandler sHandler);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.6E45DDE4-F3A1-2DCF-097F-F34F1568D163]
    // </editor-fold>
    /**
     * Compile a file either to output file or to a memory. Output file name
     * should compiler derive from input file name.
     * @param fileName  name of input file (source code)
     * @param mem       memory context object - it is used if compiler compiles
     *                  the source into memory. Compiler should check this
     *                  parameter for <code>null</code>.
     * @param use_mem   boolean value indicating whether the compiler should
     *                  compile source into memory (<code>true</code> - yes,
     *                  <code>false</code> - no).
     */
    public boolean compile (String fileName, IMemoryContext mem, boolean use_mem);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.8DF55EEE-152B-7ECD-C9D6-40859CF9CB3E]
    // </editor-fold>
    /**
     * Return lexical analyzer of the compiler. It is used in main module for
     * syntax highlighting and ofcourse in compile process. At first time it is
     * called after initialization process, before main window is showed.
     * Compiler plugin can create lexical analyzer object inside the
     * implementation of this method and then return it. It is sure that 
     * <code>compile</code> method won't be called before the call of this method.
     * @param in        <code>Reader</code> object of the document - source code.
     * @param reporter  object for reporting messages (warnings, errors, ...). 
     *                  This object is implemented in main module and is connected
     *                  to text area in panel "source code" in the main module.
     *                  Plugin should use this 
     * @return lexical analyzer object
     */
    public ILexer getLexer (Reader in, IMessageReporter reporter);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.BD07C612-13F9-3433-B8DB-CE4A0226787E]
    // </editor-fold> 
    /**
     * Gets starting address of compiled source. It is (or can be) called only
     * after compilation process. It should return the first occurence of compiled
     * program. The word "address" can be replaced by a term "offset from 0". One
     * step in address has size of one byte. It means that return value should
     * not be related to operating memory units and should not deliberate
     * techniques used in operating memory, e.g. banking.
     * @return address of program's first occurence
     */
    public int getProgramStartAddress ();

}

