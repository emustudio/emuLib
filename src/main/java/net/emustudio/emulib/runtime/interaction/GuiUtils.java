package net.emustudio.emulib.runtime.interaction;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.font.TextAttribute;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class GuiUtils {

    /**
     * Adds a KeyListener to given component recursively.
     *
     * @param component GUI component
     * @param listener  KeyListener object
     */
    public static void addKeyListener(Component component, KeyListener listener) {
        component.addKeyListener(listener);
        if (component instanceof Container) {
            Container cont = (Container) component;
            Component[] children = cont.getComponents();
            for (Component child : children) {
                addKeyListener(child, listener);
            }
        }
    }

    /**
     * Removes given KeyListener from a component recursively.
     *
     * @param component GUI component
     * @param listener  KeyListener object
     */
    public static void removeKeyListener(Component component, KeyListener listener) {
        component.removeKeyListener(listener);
        if (component instanceof Container) {
            Container cont = (Container) component;
            Component[] children = cont.getComponents();
            for (Component child : children) {
                removeKeyListener(child, listener);
            }
        }
    }

    /**
     * Loads a true-type font from a resource
     *
     * @param path          Resource path
     * @param size          Default font size
     * @param resourceClass class where to look for resources
     * @return loaded font, or Font.MONOSPACED if the font could not be loaded
     */
    public static Font loadFontResource(String path, Class<?> resourceClass, int size) {
        Map<TextAttribute, Object> attrs = new HashMap<>();
        attrs.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);

        try (InputStream fin = resourceClass.getResourceAsStream(path)) {
            Font font = Font
                    .createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(fin))
                    .deriveFont(Font.PLAIN, size)
                    .deriveFont(attrs);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font;
        } catch (Exception e) {
            return new Font(Font.MONOSPACED, Font.PLAIN, size);
        }
    }
}
