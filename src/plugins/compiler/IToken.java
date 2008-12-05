package plugins.compiler;


// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.6883133D-96AC-348C-C43B-792E40EA519E]
// </editor-fold> 
/**
 * Interface that identifies a token.
 */
public interface IToken {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.748E993F-3A84-6FF2-7A76-4D35235316A4]
    // </editor-fold> 
    /**
     * Token is a reserved word.
     */
    public static final int RESERVED = 0x100;

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.125FBF4E-DFB4-A397-5138-5CFB0B343749]
    // </editor-fold> 
    /**
     * Token is a preprocessor keyword.
     */
    public static final int PREPROCESSOR = 0x200;

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.C363B79F-19BC-4ED8-1DCC-4852CF2D7D85]
    // </editor-fold> 
    /**
     * Token is a CPU register.
     */
    public static final int REGISTER = 0x300;

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.109F6CE8-2397-94CE-7343-F1509B0AF1A3]
    // </editor-fold> 
    /**
     * Token is a separator (e.g. ' ', tab, '\n', ...).
     */
    public static final int SEPARATOR = 0x400;

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.DFC00FBE-B02E-0787-C0DE-2051C80D82FD]
    // </editor-fold> 
    /**
     * Token is a operator (e.g. +, -, *, /, ...).
     */
    public static final int OPERATOR = 0x500;

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.56BFAE4E-E687-8742-3B66-8BAC46E1233B]
    // </editor-fold> 
    /**
     * Token is a comment. Most assemblers used semicolon (";") as start of a comment.
     */
    public static final int COMMENT = 0x600;

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.39F35822-D1DD-DA5B-24D3-0904726FCAA4]
    // </editor-fold> 
    /**
     * Token is a literal (e.g. number, string, char, ...).
     */
    public static final int LITERAL = 0x700;

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.9CB29ED8-EC7A-0ACD-C350-00849CC146E3]
    // </editor-fold> 
    /**
     * Token is an identifier (e.g. name of variable, name of macro...).
     */
    public static final int IDENTIFIER = 0x800;

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.C3C3D921-8455-6FBE-047B-4C8DC1684EBC]
    // </editor-fold> 
    /**
     * Token is an label identifier.
     */
    public static final int LABEL = 0x900;

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.543D10D1-5804-952E-0450-3C55D1030936]
    // </editor-fold> 
    /**
     * Token is of uknown type.
     */
    public static final int ERROR = 1;

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.A122E1E2-9EBB-2B0C-D1BE-15C190D29D00]
    // </editor-fold> 
    /**
     * Token represents end-of-file. This token should be the last found token.
     */
    public static final int TEOF = 0;

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.45E913C6-C7CA-4478-DC6A-47B14B0DDF40]
    // </editor-fold> 
    /**
     * Gets ID of the token. ID should be used for identifying not the type of 
     * the token (e.g. reserved word, etc.) but for concrete token of given
     * token type got from <code>getType()</code> method. E.g. "mvi" is concrete
     * token of type <code>RESERVED</code> and this method should return ID
     * for "mvi" token. This is primary method for identifying tokens.
     * 
     * @return ID of the token
     */
    public int getID ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.BA5C4793-04C4-7C8D-8DF6-51D062263A99]
    // </editor-fold> 
    /**
     * Gets type of the token. Type is represented by pre-defined constants in
     * this interface (e.g. reserved words, preprocessor, ...).
     * @return type of the token
     */
    public int getType ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.4E0BD0CE-3B7E-8D3A-DA9F-175FF1F5147C]
    // </editor-fold> 
    /**
     * Gets parsed text of the token taken from source code.
     * @return textual representation of token
     */
    public String getText ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.CA7F1FAA-EB13-52F5-2D1C-4B6CA27C88F5]
    // </editor-fold> 
    /**
     * Gets error message that belongs to this token. This should be either
     * real error message (if the token is erroneous), or <code>null</code>.
     * @return error message of this token
     */
    public String getErrorString ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.41BD63CC-F5AF-22F5-3F48-3174EE6BC6F7]
    // </editor-fold> 
    /**
     * Gets 0-based line (row) number of start of the token.
     * @return start line number of the token
     */
    public int getLine ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.84DDBB63-3ACF-7A80-4072-7A3547AB5991]
    // </editor-fold> 
    /**
     * Gets 0-based column number of start of the token.
     * @return start column number of the token
     */
    public int getColumn ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.36948C2B-611F-6F7B-5276-5F60FFA098BE]
    // </editor-fold> 
    /**
     * Gets 0-based char number of start of the token.
     * @return starting char number of the token
     */
    public int getCharBegin ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.86F5E24F-FFE6-D2DD-0458-6F823733261C]
    // </editor-fold> 
    /**
     * Gets 0-based char number of end of the token (where ends last char
     * of the token).
     * @return ending char number of the token
     */
    public int getCharEnd ();

}

