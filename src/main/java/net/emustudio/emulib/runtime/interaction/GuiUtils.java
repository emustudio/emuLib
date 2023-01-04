package net.emustudio.emulib.runtime.interaction;

import java.awt.*;
import java.awt.event.KeyListener;

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
}
