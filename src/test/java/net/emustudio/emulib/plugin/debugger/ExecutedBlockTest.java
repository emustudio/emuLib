package net.emustudio.emulib.plugin.debugger;

import net.emustudio.emulib.plugin.cpu.DecodedInstruction;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ExecutedBlockTest {
    private static final DecodedInstruction FIRST = DecodedInstruction.of(7, 1, "first", "01");
    private static final DecodedInstruction LAST = DecodedInstruction.of(8, 1, "last", "02");
    private static final ExecutedBlock.Transition FINAL = new ExecutedBlock.Transition(8, 100);

    @Test public void factsAreDefensivelyCopiedAndUnmodifiable() {
        List<DecodedInstruction> instructions = new ArrayList<>(List.of(FIRST, LAST));
        List<ExecutedBlock.Transition> transitions = new ArrayList<>(List.of(new ExecutedBlock.Transition(7, 8), FINAL));
        ExecutedBlock block = new ExecutedBlock(instructions, transitions, 8, 100);
        instructions.clear();
        transitions.clear();

        assertEquals(List.of(FIRST, LAST), block.instructions());
        assertEquals(List.of(new ExecutedBlock.Transition(7, 8), FINAL), block.transitions());
        assertEquals(8, block.lastAddress());
        assertEquals(100, block.nextAddress());
        assertThrows(UnsupportedOperationException.class, () -> block.instructions().clear());
        assertThrows(UnsupportedOperationException.class, () -> block.transitions().clear());
    }

    @Test public void finalInstructionNeedNotBeLastInTheSuppliedFactsList() {
        ExecutedBlock block = new ExecutedBlock(List.of(LAST, FIRST), List.of(FINAL), 8, 100);

        assertSame(LAST, block.instructions().get(0));
        assertEquals(8, block.lastAddress());
        assertEquals(List.of(FINAL), block.transitions());
    }

    @Test public void emptyInstructionsOrMissingFinalInstructionAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ExecutedBlock(List.of(), List.of(), 8, 100));
        assertThrows(IllegalArgumentException.class, () -> new ExecutedBlock(List.of(FIRST), List.of(FINAL), 8, 100));
    }

    @Test public void missingFinalTransitionOrUnknownTransitionSourceAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExecutedBlock(List.of(FIRST, LAST), List.of(new ExecutedBlock.Transition(7, 8)), 8, 100));
        assertThrows(IllegalArgumentException.class,
                () -> new ExecutedBlock(List.of(FIRST, LAST), List.of(new ExecutedBlock.Transition(99, 8), FINAL), 8, 100));
    }

    @Test public void transitionValuesCompareByBothEndpoints() {
        ExecutedBlock.Transition transition = new ExecutedBlock.Transition(7, 8);

        assertEquals(7, transition.from());
        assertEquals(8, transition.to());
        assertEquals(transition, new ExecutedBlock.Transition(7, 8));
        assertEquals(transition.hashCode(), new ExecutedBlock.Transition(7, 8).hashCode());
        assertNotEquals(transition, new ExecutedBlock.Transition(8, 7));
        assertNotEquals(transition, null);
    }
}
