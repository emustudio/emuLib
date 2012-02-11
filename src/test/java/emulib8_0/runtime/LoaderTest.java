/*
 * LoaderTest.java
 * 
 * (c) Copyright 2010-2011, P. Jakubčo <pjakubco@gmail.com>
 *
 * KISS, YAGNI
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

package emulib8_0runtime;

import emulib8_0.runtime.Loader;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author vbmacher
 */
public class LoaderTest extends TestCase {

    public LoaderTest(String testName) {
        super(testName);
    }

   /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( LoaderTest.class );
    }

    /**
     * Test of getInstance method, of class Loader.
     */
    public void testGetInstance() {
        System.out.println("getInstance");
        Loader expResult = Loader.getInstance();
        Loader result = Loader.getInstance();
        assertEquals(expResult, result);
    }

    /**
     * Test of loadJAR method, of class Loader.
     */
    public void testLoadJAR() {
/*        System.out.println("loadJAR\n(Supposed existing: D:\\8080-cpu.jar."
                + " Please DO NOT use SecurityManager now!)\n");
        String filename = "D:\\8080-cpu.jar";
        Loader instance = Loader.getInstance();
        ArrayList<Class<?>> result = instance.loadJAR(filename);
        assertNotNull(result);*/
    }

}
