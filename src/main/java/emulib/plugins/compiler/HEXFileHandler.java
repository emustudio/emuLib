/*
 * HEXFileHandler.java
 *
 * Created on Sobota, 2007, október 13, 16:21
 * KISS, YAGNI, DRY
 *
 * Copyright (C) 2007-2012, Peter Jakubčo
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package emulib.plugins.compiler;

import emulib.plugins.memory.IMemoryContext;
import emulib.runtime.StaticDialogs;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class generate 16 bit Intel hex file.
 *
 * @author vbmacher
 */
public class HEXFileHandler {
    private HashMap<Integer, String> program;
    private int nextAddress;
    
    /** Creates a new instance of HEXFileHandler */
    public HEXFileHandler() {
        this.program = new HashMap<Integer, String>();
        nextAddress = 0;
    }
    
    /**
     * Put code on next address
     * if element exist on the address, then is rewritten
     * @param code
     */
    public void putCode(String code) {
        program.put(nextAddress,code);
        nextAddress += (code.length()/2);
    }
    
    private String getCode(int address) {
        return (String)program.get(address);
    }
    
    /**
     * Set the next address where the next value will be assigned.
     *
     * @param address
     */
    public void setNextAddress(int address) {
        nextAddress = address;
    }
    
    private String checksum(String lin) {
        int sum = 0, chsum = 0;
        for (int i =0; i < lin.length()-1; i += 2)
            sum += Integer.parseInt(lin.substring(i,i+2),16);
        sum %= 0x100;
        // :
        // 10 00 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
        // 16 0  8  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0
        // 16+8 = 24
        // 0x100 -24 +1 = 256 - 24 +1 = 232 +1 = 0xe8 + 1 = 0xe9
        // 0xe9 je zevraj zle, ma byt 0xe8
        chsum = 0x100 - sum; //+1;
        return String.format("%1$02X",chsum);
    }
    
    /**
     * Keys of the HashMap have to represent adresses
     * and values have to represent compiled code.
     * Method copies all elements from param HashMap
     * to internal data member.
     *
     * @param ha sub-table with addresses and codes
     */
    public void addTable(HashMap<Integer,String> ha) {
        ArrayList<Integer> adrs = new ArrayList<Integer>(ha.keySet());
        int largestAdr = nextAddress;
        Iterator<Integer> e = adrs.iterator();
        while (e.hasNext()) {
            nextAddress = (Integer)e.next();
            String cd = (String)ha.get(nextAddress);
            program.put(nextAddress,cd);
            nextAddress += (cd.length()/2);
            if (nextAddress > largestAdr) 
                largestAdr = nextAddress;
        }
        nextAddress = largestAdr;
    }
    
    /**
     * Get the HashMap representing the program hex code.
     *
     * @return HashMap representing the program hex code
     */
    public HashMap<Integer, String> getTable() {
        return this.program;
    }
    
    // generate hex file
    private String generateHEX() {
        String lines = "";       // all lines
        String lineAddress = ""; // starting line address
        String line = "";        // line data
        int address = 0;         // current address in hex file
        int bytesCount = 0;      // current count of data bytes on single line
        
        ArrayList<Integer> adrs = new ArrayList<Integer>(program.keySet());
        Collections.sort(adrs);

        // for all code elements (they won't be separated)
        Iterator<Integer> e = adrs.iterator();
        while (e.hasNext()) {
            int adr = (Integer)e.next();
            
            // is line very beginning ?
            if (lineAddress.equals("")) {
                address = adr;
                lineAddress = String.format("%1$04X",address);
            }

            // if element's address do not equal suggested (natural computed)
            // address or line is full
            if ((address != adr) || (bytesCount >= 16)) {
                String lin = String.format("%1$02X", bytesCount) + lineAddress
                        + "00" + line;
                lines += ":"+ lin + checksum(lin) + "\n";
                bytesCount = 0;
                line = "";
                address = adr;
                lineAddress = String.format("%1$04X",address);
            }

            // code have to be stored as number of separate pairs of hex digits
            String cd = (String)program.get(adr);
            
            // cd hasn't to be longer than 16-bytesCount
            while ((cd.length()+line.length()) > 32) {
                int len = 32 - line.length(); // kolko este treba
                line += cd.substring(0,len);
                cd = cd.substring(len,cd.length());
                
                address += (len / 2); // compute next address
                bytesCount += (len / 2);
            
                // save line
                String lin = String.format("%1$02X",bytesCount) + lineAddress
                        + "00" + line;
                lines += ":"+ lin + checksum(lin) + "\n";
                bytesCount = 0;
                line = "";
                lineAddress = String.format("%1$04X",address);
            }
            if (cd.length() > 0) {
                line += cd;
                address += (cd.length() / 2); // compute next address
                bytesCount += (cd.length() / 2);
            }
        }
        if (line.equals("") == false) {
            String lin = String.format("%1$02X",bytesCount) + lineAddress
                    + "00" + line;
            lines += ":"+ lin + checksum(lin) + "\n";
        }
        lines += ":00000001FF\n";
        return lines;
    }
    
    /**
     * Method is similar to generateHex() method in that way, that
     * compiled program is also transformed into chunk of bytes, but
     * not to hex file but to the operating memory.
     * 
     * @param mem context of operating memory
     * @return true if the hex file was successfully loaded, false otherwise
     */
    public boolean loadIntoMemory(IMemoryContext mem) {
        if (mem.getDataType() != Short.class) {
            StaticDialogs.showErrorMessage("Incompatible operating memory type!"
                    + "\n\nThis compiler can't load file into this memory.");
            return false;
        }
        ArrayList<Integer> adrs = new ArrayList<Integer>(program.keySet());
        Collections.sort(adrs);
        Iterator<Integer> e = adrs.iterator();
        while (e.hasNext()) {
            int adr = (Integer)e.next();
            String code = this.getCode(adr);
            for (int i = 0, j = 0; i < code.length()-1; i+=2, j++) {
                String hexCode = code.substring(i, i+2);
                short num = (short)((Short.decode("0x" + hexCode)) & 0xFF);
                mem.write(adr+j, num);
            }
        }
        return true;
    }

    
    /**
     * Generates a Intel Hex file based on the cached program HashMap.
     *
     * @param filename file name where to store the hex file
     * @throws java.io.IOException
     */
    public void generateFile(String filename) throws java.io.IOException{
        String fileData = generateHEX();

        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
        out.write(fileData);
        out.close();
    }

    /**
     * Get the program starting address (the first address that has occured
     * in the program HashMap).
     *
     * @return program starting memory location
     */
    public int getProgramStart() {
        ArrayList<Integer> adrs = new ArrayList<Integer>(program.keySet());
        Collections.sort(adrs);
        if (adrs.isEmpty() == false)
            return (Integer)adrs.get(0);
        else return 0;
    }
    

}
