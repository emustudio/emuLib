/*
 * KISS, YAGNI, DRY
 *
 * Copyright (C) 2011-2014, Peter Jakubčo
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * and open the template in the editor.
 */

package emulib.plugins.cpu;

/**
 * This abstract class implements some fundamental methods of Disassembler
 * interface. It can be used as foundation for implementing own disassembler.
 */
public abstract class AbstractDisassembler implements Disassembler {
    protected final Decoder decoder;

    public AbstractDisassembler(Decoder decoder) {
        this.decoder = decoder;
    }

    /**
     * Method returns memory position of the previous instruction.
     *
     * This method relies hlighly upon von-Neuman sequential model of the computation.
     *
     * It uses ad-hoc jump 15 bytes back to check the previous instruction is of the correct length. If the ad-hoc
     * method was not used, the more bytes instructions wouldn't be catched correctly. The theory is that instruction
     * lengths are catched as correctly as big the reserve is.
     *
     * @param location The memory location of current instruction
     * @return memory position of the previous instruction. If no instruction can be found (the position is invalid),
     * exception is thrown.
     * @throws IndexOutOfBoundsException if the location is 0 or negative
     */
    @Override
    public int getPreviousInstructionPosition(int location) throws IndexOutOfBoundsException {
        int loc, oldLoc;
        int tryBytes = 15;

        if (location <= 0) {
            throw new IndexOutOfBoundsException();
        }

        loc = location - tryBytes;
        while (loc < 0) {
            tryBytes--;
            loc = location - tryBytes;
        }
        oldLoc = loc;

        int prevOldLoc = loc;
        while ((loc = getNextInstructionPosition(loc)) != location) {
            if (loc > location) {
                tryBytes++;
                if (location - tryBytes < 0) {
                    oldLoc = prevOldLoc;
                    break;
                }
                loc = location - tryBytes;
            }
            prevOldLoc = oldLoc;
            oldLoc = loc;
        }
        return oldLoc;
    }

}
