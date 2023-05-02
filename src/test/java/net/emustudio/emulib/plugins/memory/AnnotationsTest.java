/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
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
package net.emustudio.emulib.plugins.memory;

import net.emustudio.emulib.plugins.compiler.SourceCodePosition;
import net.emustudio.emulib.plugins.memory.annotations.*;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class AnnotationsTest {

    @Test
    public void testAddThenRemoveAllByPluginId() {
        Annotations annotations = new Annotations();

        annotations.add(100, new BreakpointAnnotation(0L));
        annotations.add(100, new BreakpointAnnotation(1L));

        annotations.removeAll(0L);
        Map<Integer, Set<BreakpointAnnotation>> result = annotations.getAll(BreakpointAnnotation.class);
        assertEquals(1, result.size());
        assertTrue(result.get(100).contains(new BreakpointAnnotation(1L)));
    }

    @Test
    public void testAddThenRemoveAllByPluginIdDifferentClasses() {
        Annotations annotations = new Annotations();

        annotations.add(100, new BreakpointAnnotation(0L));
        annotations.add(100, new TextAnnotation(0L, "Hello"));
        annotations.add(100, new SourceCodeAnnotation(0L, new SourceCodePosition(1, 2, "")));

        annotations.removeAll(0L);
        Set<Annotation> result = annotations.get(0L, 100);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testRemoveAllByPluginIdMemoryPosition() {
        Annotations annotations = new Annotations();

        annotations.add(100, new BreakpointAnnotation(0L));
        annotations.add(99, new BreakpointAnnotation(0L));
        annotations.removeAll(0L, 100);

        Set<BreakpointAnnotation> breakpoints = annotations.get(99, BreakpointAnnotation.class);
        assertEquals(1, breakpoints.size());
        assertTrue(breakpoints.contains(new BreakpointAnnotation(0L)));
    }

    @Test
    public void testAnnotationsAreNotDuplicatedByOnePlugin() {
        Annotations annotations = new Annotations();

        annotations.add(100, new BreakpointAnnotation(0L));
        annotations.add(100, new BreakpointAnnotation(0L));

        assertEquals(1, annotations.getAll(BreakpointAnnotation.class).size());

        Set<BreakpointAnnotation> breakpoints = annotations.get(100, BreakpointAnnotation.class);
        assertEquals(1, breakpoints.size());
    }

    @Test
    public void testAnnotationsCanBeDuplicatedByTwoPlugins() {
        Annotations annotations = new Annotations();

        annotations.add(100, new BreakpointAnnotation(0L));
        annotations.add(100, new BreakpointAnnotation(1L));

        assertEquals(1, annotations.getAll(BreakpointAnnotation.class).size());

        Set<BreakpointAnnotation> breakpoints = annotations.get(100, BreakpointAnnotation.class);
        assertEquals(2, breakpoints.size());
    }

    @Test
    public void testAddMultipleAnnotationClasses() {
        Annotations annotations = new Annotations();
        annotations.add(100, new BreakpointAnnotation(0L));
        annotations.add(100, new TextAnnotation(0L, "Hello"));
        annotations.add(100, new SourceCodeAnnotation(0L, new SourceCodePosition(1, 2, "")));

        Set<Annotation> result = annotations.get(0L, 100);
        assertEquals(3, result.size());
    }

    @Test
    public void testAddMultipleAnnotationClassesOnDifferentLocations() {
        Annotations annotations = new Annotations();
        annotations.add(1, new BreakpointAnnotation(0L));
        annotations.add(2, new BreakpointAnnotation(0L));
        annotations.add(3, new BreakpointAnnotation(0L));
        annotations.add(1, new TextAnnotation(0L, "H"));
        annotations.add(2, new TextAnnotation(0L, "e"));
        annotations.add(4, new TextAnnotation(0L, "l"));
        annotations.add(1, new SourceCodeAnnotation(0L, new SourceCodePosition(1, 2, "")));
        annotations.add(2, new SourceCodeAnnotation(0L, new SourceCodePosition(1, 3, "")));
        annotations.add(5, new SourceCodeAnnotation(0L, new SourceCodePosition(1, 4, "")));

        Map<Integer, Set<Annotation>> all = annotations.getAll(0L);

        Set<Annotation> at1 = all.get(1);
        assertTrue(at1.contains(new BreakpointAnnotation(0L)));
        assertTrue(at1.contains(new TextAnnotation(0L, "H")));
        assertTrue(at1.contains(new SourceCodeAnnotation(0L, new SourceCodePosition(1, 2, ""))));

        Set<Annotation> at2 = all.get(2);
        assertTrue(at2.contains(new BreakpointAnnotation(0L)));
        assertTrue(at2.contains(new TextAnnotation(0L, "e")));
        assertTrue(at2.contains(new SourceCodeAnnotation(0L, new SourceCodePosition(1, 3, ""))));

        Set<Annotation> at3 = all.get(3);
        assertTrue(at3.contains(new BreakpointAnnotation(0L)));

        Set<Annotation> at4 = all.get(4);
        assertTrue(at4.contains(new TextAnnotation(0L, "l")));

        Set<Annotation> at5 = all.get(5);
        assertTrue(at5.contains(new SourceCodeAnnotation(0L, new SourceCodePosition(1, 4, ""))));
    }

    @Test
    public void testGetAllByPluginIdAndClass() {
        Annotations annotations = new Annotations();
        annotations.add(1, new BreakpointAnnotation(0L));
        annotations.add(1, new BreakpointAnnotation(1L));
        annotations.add(1, new TextAnnotation(0L, "H"));
        annotations.add(2, new TextAnnotation(0L, "e"));

        Map<Integer, Set<BreakpointAnnotation>> bp0 = annotations.getAll(0L, BreakpointAnnotation.class);
        Map<Integer, Set<BreakpointAnnotation>> bp1 = annotations.getAll(1L, BreakpointAnnotation.class);
        Map<Integer, Set<TextAnnotation>> da0 = annotations.getAll(0L, TextAnnotation.class);

        assertEquals(1, bp0.get(1).size());
        assertEquals(1, bp1.get(1).size());
        assertEquals(1, da0.get(1).size());
        assertEquals(1, da0.get(2).size());
    }

    @Test
    public void testGetByPluginIdMemoryPositionAndClass() {
        Annotations annotations = new Annotations();
        annotations.add(1, new BreakpointAnnotation(0L));
        annotations.add(1, new BreakpointAnnotation(1L));
        annotations.add(20, new BreakpointAnnotation(1L));
        annotations.add(20, new BreakpointAnnotation(0L));
        annotations.add(1, new TextAnnotation(0L, "Hello"));

        Set<BreakpointAnnotation> bp = annotations.get(0L, 1, BreakpointAnnotation.class);
        assertEquals(1, bp.size());
        assertEquals(new BreakpointAnnotation(0L), bp.iterator().next());
    }
}
