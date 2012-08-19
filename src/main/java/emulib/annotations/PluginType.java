/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package emulib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author vbmacher
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PluginType {
    String title();
    String copyright();
    String version();
    String description();
}
