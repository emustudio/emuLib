package emulib.plugins.cpu.stubs;

import emulib.plugins.cpu.AbstractDisassembler;
import emulib.plugins.cpu.DisassembledInstruction;
import emulib.plugins.cpu.InvalidInstructionException;

public class AbstractDisassemblerStub extends AbstractDisassembler {
    public static final int[] NEXT_INSTR_POS = new int[] {
        1, 2, 3, 5, 5, 8, 8, 8, 9, 11, 11, 13, 13, 14, 17, 17, 17
    };
    public static final int[] PREV_INSTR_POS = new int[] {
        0, 0, 1, 2, 2, 3, 3, 3, 5, 8, 8, 9, 9, 11, 13, 13, 13
    };
    public static final int[] INSTR_POS = new int[] {
        0, 1, 2, 3, 5, 8, 9, 11, 13, 14, 17
    };
    public static final int[] INSTR_LEN = new int[] {
        1, 1, 1, 2, 3, 1, 2,  2,  1,  3,  1
    };

    public AbstractDisassemblerStub() {
        super(null);
    }

    @Override
    public DisassembledInstruction disassemble(int memoryPosition) throws InvalidInstructionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNextInstructionPosition(int memoryPosition) throws IndexOutOfBoundsException {
        return NEXT_INSTR_POS[memoryPosition];
    }
    
}
