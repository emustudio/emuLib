package emulib.plugins.cpu;

import emulib.plugins.cpu.stubs.AbstractDisassemblerStub;
import org.junit.Test;
import static org.junit.Assert.*;

public class AbstractDisassemblerTest {

    @Test(expected = IndexOutOfBoundsException.class)
    public void testNegativeLocation() {
        AbstractDisassembler instance = new AbstractDisassemblerStub();
        instance.getPreviousInstructionPosition(-1);
    }

    /*
       The algorithm works as expected only if instructions in memory
       location starting from any `N >= 0` to `instrPos` are always the
       same (excluding those starting at 0 to `N`).

       Example:
       Suppose we have instructions at positions:
           0, 1, 2, 3, 5, 8, 9
       If we want to know previous instruction at memory location L=6,
       the algorithm must search the instruction space starting before the
       location, because it doesn't know the accurate instruction positions.

       It starts at some location `N >= 0` but `N < L` and traverses all
       the instructions using `getNextInstructionPosition()` method until
       it encounters wanted location, which is `L=6` in our case.
       However it never reach the position, because instruction at position
       6 is just second byte of the instruction at location 5.

       The algorithm in the next iteration encounters location > L by what
       it is obvious that:
         1. the algorithm cannot go further, we already passed the L
            (the location is equal to 8 now)
         2. previous instruction position is 5 which in fact equals to
            instruction located at L (the second byte).

       From this situation it is clear that the algorithm must return
       instruction position which lies before 5, which is 3.

       When the algorithm does not work:
       Instructions are parseable objects composed from one or more bytes.
       When we start to parse an instruction at location `N`, the parser can
       recognize some instruction. But if we start to parse the instruction at
       location `N + 1`, we would end up with totally different instruction.

       Therefore, when the algorithm jumps back (at location `N`), it can only
       hope that during the traversing it finds correct previous instruction.
       A good idea might be, and empirically it works very good, to make long
       enough jumps (`L - N` is big enough) and traverse until the actual
       location equals to location `L`, and we say that the traverse has
       succeeded.

       If the traverse did not succeed, we will increase the length of the
       jump (increase `L - N`), and then jump back again, repeating the process
       until either the traverse succeeds or if `N < 0`.

       If `N < 0` the result of the algorithm will be most likely wrong.
    */
    @Test
    public void testPreviousInstruction() {
        AbstractDisassembler instance = new AbstractDisassemblerStub();

        int max = AbstractDisassemblerStub.INSTR_POS.length;
        for (int index = 1; index < max; index++) {
            int instrLen = AbstractDisassemblerStub.INSTR_LEN[index];

            int diff = 0;
            if (instrLen > 1) {
                diff = 1; // this should force to find beginning of the
                          // current instruction before the previous one is
                          // being located
            }

            int previousInstrPos = AbstractDisassemblerStub.INSTR_POS[index - 1];
            int instrPos = AbstractDisassemblerStub.INSTR_POS[index] + diff;

            assertEquals(
                    previousInstrPos,
                    instance.getPreviousInstructionPosition(instrPos)
            );
        }
    }

}
