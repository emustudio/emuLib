package net.emustudio.emulib.plugins.debugger;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class PresentationTest {
    @Test public void symbolicRowsAndNavigationRemainIndependentOfMutableInputs() {
        List<String> tags = new ArrayList<>(List.of("ram/output/hello"));
        List<NavLink> links = new ArrayList<>(List.of(new NavLink("Earlier output", tags.get(0))));
        ViewRow row = new ViewRow("Tape", "Output", "empty", "hello", true, tags, links);
        List<ViewRow> rows = new ArrayList<>(List.of(row));
        StepView view = new StepView("PRINT hello", "print", OptionalInt.of(7), rows);

        tags.clear();
        links.clear();
        rows.clear();

        assertEquals("hello", view.rows.get(0).after);
        assertEquals(List.of("ram/output/hello"), row.tags);
        assertEquals("ram/output/hello", row.links.get(0).tag);
        assertThrows(UnsupportedOperationException.class, () -> view.rows.clear());
        assertThrows(UnsupportedOperationException.class, () -> row.tags.clear());
        assertThrows(UnsupportedOperationException.class, () -> row.links.clear());
    }

    @Test public void alternateFormatsArePassiveAndDefensivelyCopied() {
        List<ValueView.Format> formats = new ArrayList<>(List.of(new ValueView.Format("Symbol", "hello")));
        ValueView value = new ValueView("hello", formats, true);

        formats.clear();

        assertEquals("hello", value.formats.get(0).text);
        assertTrue(value.changed);
        assertThrows(UnsupportedOperationException.class, () -> value.formats.clear());
    }
}
