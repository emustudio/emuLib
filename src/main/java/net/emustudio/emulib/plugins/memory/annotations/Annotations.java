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
package net.emustudio.emulib.plugins.memory.annotations;

import net.emustudio.emulib.runtime.helpers.ReadWriteLockSupport;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Memory annotations.
 * <p>
 * Each memory cell (at one location) can be annotated with one or more annotations. An annotation is a class extending
 * {@link net.emustudio.emulib.plugins.memory.annotations.Annotation} abstract class, which must implement hashCode and
 * equals methods properly. That way it is guaranteed multiple annotations can be put on single location, but they won't
 * get duplicated.
 * <p>
 * Accessible from memory context, but some methods ({@link #clear()}, {@link #removeAll(Class)}) are needed by the root
 * memory class. The reason for not allowing to ignore source plugin ID in the context is to be able to forbid by one
 * plugin to remove annotations of another plugin. The root memory plugin class is accessible only to emuStudio, which
 * can ultimately decide about all annotations since it controls all plugins already.
 */
@ThreadSafe
public class Annotations {
    @GuardedBy("rwl")
    private final Map<Integer, Set<Annotation>> annotations = new HashMap<>();
    private final ReadWriteLockSupport rwl = new ReadWriteLockSupport();

    /**
     * Clears all annotations
     */
    protected void clear() {
        rwl.lockWrite(annotations::clear);
    }

    /**
     * Removes all annotations of given annotation class
     *
     * @param annotationClass annotation class
     */
    protected void removeAll(Class<?> annotationClass) {
        class P {
            final Integer k;
            final Set<Annotation> v;

            P(Integer k, Set<Annotation> v) {
                this.k = k;
                this.v = v;
            }
        }

        rwl.lockWrite(() -> {
            Set<P> toRemove = new HashSet<>();
            for (Map.Entry<Integer, Set<Annotation>> entry : annotations.entrySet()) {
                Set<Annotation> toRemoveAtLocation = entry
                        .getValue()
                        .stream()
                        .filter(e -> e.getClass().equals(annotationClass))
                        .collect(Collectors.toSet());

                if (!toRemoveAtLocation.isEmpty()) {
                    toRemove.add(new P(entry.getKey(), toRemoveAtLocation));
                }
            }
            for (P p : toRemove) {
                Set<Annotation> annotationsSet = annotations.get(p.k);
                annotationsSet.removeAll(p.v);
                if (annotationsSet.isEmpty()) {
                    annotations.remove(p.k);
                }
            }
        });
    }

    /**
     * Removes all annotations for given sourcePluginId.
     * <p>
     * Each plugin which sets memory annotations is responsible for cleaning them.
     *
     * @param sourcePluginId source plugin ID
     */
    public void removeAll(long sourcePluginId) {
        rwl.lockWrite(() -> {
            Set<Integer> toRemove = new HashSet<>();
            for (Map.Entry<Integer, Set<Annotation>> entry : annotations.entrySet()) {
                Set<Annotation> atLocation = entry.getValue();

                Set<Annotation> toRemoveAtLocation = atLocation
                        .stream()
                        .filter(v -> v.getSourcePluginId() == sourcePluginId)
                        .collect(Collectors.toSet());
                atLocation.removeAll(toRemoveAtLocation);

                if (atLocation.isEmpty()) {
                    toRemove.add(entry.getKey());
                }
            }
            toRemove.forEach(annotations::remove);
        });
    }

    /**
     * Removes all annotations at given memory location set by given plugin ID
     *
     * @param sourcePluginId source plugin ID
     * @param location       memory location
     */
    public void removeAll(long sourcePluginId, int location) {
        rwl.lockWrite(() -> {
            Set<Annotation> atLocation = Optional
                    .ofNullable(annotations.get(location))
                    .orElse(Collections.emptySet());

            Set<Annotation> toRemove = atLocation
                    .stream()
                    .filter(v -> v.getSourcePluginId() == sourcePluginId)
                    .collect(Collectors.toSet());
            atLocation.removeAll(toRemove);

            if (atLocation.isEmpty()) {
                annotations.remove(location);
            }
        });
    }

    /**
     * Get all annotations of given type
     *
     * @param annotationClass annotation class (of type T)
     * @param <T>             type of annotation
     * @return annotations of type T (always non-null)
     */
    @SuppressWarnings("unchecked")
    public <T extends Annotation> Map<Integer, Set<T>> getAll(Class<? extends T> annotationClass) {
        return rwl.lockRead(() -> {
            Map<Integer, Set<T>> result = new HashMap<>();

            for (Map.Entry<Integer, Set<Annotation>> entry : annotations.entrySet()) {
                Set<T> keyValues = entry
                        .getValue()
                        .stream()
                        .filter(v -> (v.getClass().equals(annotationClass)))
                        .map(v -> (T) v)
                        .collect(Collectors.toSet());
                if (!keyValues.isEmpty()) {
                    result.put(entry.getKey(), keyValues);
                }
            }
            return result;
        });
    }

    /**
     * Get all annotations of given type coming from given source plugin ID
     *
     * @param sourcePluginId  source plugin ID
     * @param annotationClass annotation class (of type T)
     * @param <T>             type of annotation
     * @return annotations of type T coming from given source plugin ID (always non-null)
     */
    @SuppressWarnings("unchecked")
    public <T extends Annotation> Map<Integer, Set<T>> getAll(long sourcePluginId, Class<? extends T> annotationClass) {
        return rwl.lockRead(() -> {
            Map<Integer, Set<T>> result = new HashMap<>();

            for (Map.Entry<Integer, Set<Annotation>> entry : annotations.entrySet()) {
                Set<T> keyValues = entry
                        .getValue()
                        .stream()
                        .filter(v -> v.getSourcePluginId() == sourcePluginId)
                        .filter(v -> (v.getClass().equals(annotationClass)))
                        .map(v -> (T) v)
                        .collect(Collectors.toSet());
                if (!keyValues.isEmpty()) {
                    result.put(entry.getKey(), keyValues);
                }
            }
            return result;
        });
    }

    /**
     * Get all annotations coming from given source plugin ID
     *
     * @param sourcePluginId source plugin ID
     * @return annotations coming from given source plugin ID (always non-null)
     */
    public Map<Integer, Set<Annotation>> getAll(long sourcePluginId) {
        return rwl.lockRead(() -> {
            Map<Integer, Set<Annotation>> result = new HashMap<>();

            for (Map.Entry<Integer, Set<Annotation>> entry : annotations.entrySet()) {
                Set<Annotation> keyValues = entry
                        .getValue()
                        .stream()
                        .filter(v -> v.getSourcePluginId() == sourcePluginId)
                        .collect(Collectors.toSet());
                if (!keyValues.isEmpty()) {
                    result.put(entry.getKey(), keyValues);
                }
            }
            return result;
        });
    }

    /**
     * Get annotations at given location (and of given type)
     *
     * @param location        memory location
     * @param annotationClass annotation class (of type T)
     * @param <T>             type of annotation
     * @return annotations set at given memory location (always non-null)
     */
    @SuppressWarnings("unchecked")
    public <T extends Annotation> Set<T> get(int location, Class<? extends T> annotationClass) {
        return rwl.lockRead(() -> {
            Set<T> result = new HashSet<>();
            Set<Annotation> atLocation = Optional
                    .ofNullable(annotations.get(location))
                    .orElse(Collections.emptySet());

            atLocation.forEach(a -> {
                if (a.getClass().equals(annotationClass)) {
                    result.add((T) a);
                }
            });
            return result;
        });
    }

    /**
     * Get annotations at given location set by given source plugin ID (and of given type)
     *
     * @param sourcePluginId  source plugin ID
     * @param location        memory location
     * @param annotationClass annotation class (of type T)
     * @param <T>             type of annotation
     * @return annotations set at given memory location by given source plugin ID (always non-null)
     */
    @SuppressWarnings("unchecked")
    public <T extends Annotation> Set<T> get(long sourcePluginId, int location, Class<? extends T> annotationClass) {
        return rwl.lockRead(() -> {
            Set<T> result = new HashSet<>();
            Set<Annotation> atLocation = Optional
                    .ofNullable(annotations.get(location))
                    .orElse(Collections.emptySet());

            atLocation.forEach(a -> {
                if (a.getClass().equals(annotationClass) && a.getSourcePluginId() == sourcePluginId) {
                    result.add((T) a);
                }
            });
            return result;
        });
    }

    /**
     * Get annotations at given location set by given source plugin ID
     *
     * @param sourcePluginId source plugin ID
     * @param location       memory location
     * @return set of annotations at given location set by given source plugin ID (always non-null)
     */
    public Set<Annotation> get(long sourcePluginId, int location) {
        return rwl.lockRead(() -> {
            Set<Annotation> result = new HashSet<>();
            Set<Annotation> atLocation = Optional
                    .ofNullable(annotations.get(location))
                    .orElse(Collections.emptySet());

            atLocation.forEach(a -> {
                if (a.getSourcePluginId() == sourcePluginId) {
                    result.add(a);
                }
            });
            return result;
        });
    }

    /**
     * Adds an annotation at given memory location.
     * The same annotations will not be duplicated.
     *
     * @param location   memory location
     * @param annotation annotation
     */
    public void add(int location, Annotation annotation) {
        rwl.lockWrite(() -> {
            Set<Annotation> atLocation = Optional
                    .ofNullable(annotations.get(location))
                    .orElse(new HashSet<>());

            atLocation.add(annotation);
            annotations.put(location, atLocation);
        });
    }
}
