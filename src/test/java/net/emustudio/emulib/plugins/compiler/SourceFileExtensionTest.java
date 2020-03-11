/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2020  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.plugins.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

public class SourceFileExtensionTest {
    
    @Test
    public void testSetGetExtensionAndDescription() {
        SourceFileExtension instance = new SourceFileExtension("bmp", "descr");
        assertEquals("bmp", instance.getExtension());
        assertEquals("descr", instance.getDescription());
        assertEquals("descr (*.bmp)", instance.getFormattedDescription());
    }

}
