/*
 * SimpleDisassembler.java
 *
 * KISS, YAGNI
 * 
 *  Copyright (C) 2011 vbmacher
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

package emuLib8.plugins.cpu;

/**
 * This abstract class implements some fundamental methods of IDisassembler
 * interface. It can be used as foundation for implementing own disassembler.
 *
 * @author vbmacher
 */
public abstract class SimpleDisassembler implements IDisassembler {

    protected ICPU cpu;

    /**
     * The constructor creates an instance of the SimpleDisassembler class
     *
     * @param cpu CPU object
     */
    public SimpleDisassembler(ICPU cpu) {
        this.cpu = cpu;
    }


    /**
     * Method returns memory address of previous instruction.
     *
     * This method relies hlighly upon von-Neuman sequential model of
     * computation.
     *
     * @param location The memory location of current instruction
     * @return memory location of previous instruction. If no instruction can be
     * found (the location is invalid), negative value is returned.
     */
    @Override
    public int getPreviousInstructionLocation(int location) {
        int loc, oldLoc;
        int tryBytes = 1;

        if (location <= 0)
            return location;

        IDisassembler dis = cpu.getDisassembler();

        loc = location - tryBytes;
        oldLoc = loc;
        while ((loc = dis.getNextInstructionLocation(loc)) != oldLoc) {
            if (loc == location)
                return oldLoc;
            if (loc > location) {
                tryBytes++;
                oldLoc = loc = location - tryBytes;
                if (oldLoc < 0)
                    return oldLoc;
            }
            oldLoc = loc;
        }
        return oldLoc;
    }

}
