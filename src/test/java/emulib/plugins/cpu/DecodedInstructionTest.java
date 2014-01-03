package emulib.plugins.cpu;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class DecodedInstructionTest {
    private DecodedInstruction instruction;

    @Before
    public void setUp() {
        instruction = new DecodedInstruction();
    }

    @Test
    public void testHasKeyForStringAndConstant() {
        instruction.add(0, "A", 5);
        assertTrue(instruction.hasKey(0));
    }

    @Test
    public void testHasKeyForBits() {
        instruction.add(0, new byte[] {});
        assertTrue(instruction.hasKey(0));
    }

    @Test
    public void testNullStringForInvalidKey() {
        assertNull(instruction.getString(0));
    }

    @Test
    public void testNullBitsForInvalidKey() {
        assertNull(instruction.getBits(0));
        assertNull(instruction.getBits(0, true));
        assertNull(instruction.getBits(0, false));
    }

    @Test
    public void testNegativeConstantForInvalidKey() {
        assertEquals(-1, instruction.get(0));
    }

    @Test
    public void testGetStringAndConstant() {
        instruction.add(0, "A", 5);
        assertEquals("A", instruction.getString(0));
        assertEquals(5, instruction.get(0));
    }

    @Test
    public void testGetBits() {
        byte[] bits = new byte[] {5,6};
        instruction.add(0, bits);
        assertArrayEquals(bits, instruction.getBits(0));
    }

    @Test
    public void testGetReversedBits() {
        byte[] bits = new byte[] {5,6};
        instruction.add(0, bits);
        assertArrayEquals(new byte[] {6,5}, instruction.getBits(0, true));
    }

    @Test
    public void testOverwriteAbilityForStringAndConstant() {
        instruction.add(0, "A", 5);
        instruction.add(0, "B", 6);
        assertEquals(6, instruction.get(0));
        assertEquals("B", instruction.getString(0));
    }

    @Test
    public void testSingleKeyForMultipleAdditions() {
        instruction.add(0, "A", 5);
        instruction.add(0, "A", 6);
        assertEquals(1, instruction.getKeys().size());
    }

    @Test
    public void testSingleKeyForMultipleAdditios() {
        byte[] image = new byte[] {};
        instruction.setImage(image);
        assertSame(image, instruction.getImage());
    }

}
