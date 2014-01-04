package emulib.plugins.cpu;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class DisassembledInstructionTest {
    private DisassembledInstruction instruction;
    private static final String DEFAULT_MNEMO = "mnemo";
    private static final String DEFAULT_OPCODE = "opcode";
    private static final int DEFAULT_ADDRESS = 100;

    @Before
    public void setUp(){
        instruction = new DisassembledInstruction(
                DEFAULT_ADDRESS,
                DEFAULT_MNEMO,
                DEFAULT_OPCODE);
    }

    @Test
    public void testGetDefaultValues() {
        assertEquals(DEFAULT_ADDRESS, instruction.getAddress());
        assertEquals(DEFAULT_MNEMO, instruction.getMnemo());
        assertEquals(DEFAULT_OPCODE, instruction.getOpCode());
    }

    @Test
    public void testOverwriteInstruction() {
        String otherMnemo = DEFAULT_MNEMO + "AAA";
        String otherOpcode = DEFAULT_OPCODE + "BBB";

        instruction.setInstruction(otherMnemo, otherOpcode);
        assertEquals(otherMnemo, instruction.getMnemo());
        assertEquals(otherOpcode, instruction.getOpCode());
    }

    @Test
    public void testOverwriteAddress() {
        int otherAddress = DEFAULT_ADDRESS + 222;

        instruction.setAddress(otherAddress);
        assertEquals(otherAddress, instruction.getAddress());
    }


}
