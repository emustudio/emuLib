/*
 * UniversalFileFilter.java
 *
 * Copyright (C) 2013 Peter Jakubčo
 * KISS, YAGNI, DRY
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
package emulib.runtime;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.filechooser.FileFilter;

public class UniversalFileFilter extends FileFilter {
    private final Set<String> extensions = new LinkedHashSet<>();
    private String description;

    public void addExtension(String extension) {
        String tmp = extension;
        while (tmp.startsWith(".")) {
            tmp = tmp.substring(1);
        }
        extensions.add(tmp);
    }

    public String getFirstExtension() {
        if (!extensions.isEmpty()) {
            return extensions.iterator().next();
        }
        return null;
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String ext = getExtension(f);
        if (ext != null) {
            for (String extension : extensions) {
                if (extension.equals(ext) || extension.equals("*")) {
                    return true;
                }
            }
        } else {
            for (String extension : extensions) {
                if (extension.equals("*")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getExtension(File file) {
        String extension = null;
        String s = file.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            extension = s.substring(i + 1).toLowerCase();
        }
        return extension;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get extensions count within this filter.
     *
     * @return number of extensions acceptable by this filter
     */
    public int getExtensionsCount() {
        return extensions.size();
    }
    
}
