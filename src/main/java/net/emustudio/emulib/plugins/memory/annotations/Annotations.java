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

@ThreadSafe
public class Annotations implements MemoryAnnotations {
    @GuardedBy("rwl")
    private final Map<Integer, Set<Annotation>> annotations = new HashMap<>();
    private final ReadWriteLockSupport rwl = new ReadWriteLockSupport();

    @Override
    public void clear() {
        rwl.lockWrite(annotations::clear);
    }

    @Override
    public void removeAll(Class<?> annotationClass) {
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

    @Override
    public void removeAll(long pluginId) {
        rwl.lockWrite(() -> {
            Set<Integer> toRemove = new HashSet<>();
            for (Map.Entry<Integer, Set<Annotation>> entry : annotations.entrySet()) {
                Set<Annotation> atLocation = entry.getValue();

                Set<Annotation> toRemoveAtLocation = atLocation
                        .stream()
                        .filter(v -> v.getPluginId() == pluginId)
                        .collect(Collectors.toSet());
                atLocation.removeAll(toRemoveAtLocation);

                if (atLocation.isEmpty()) {
                    toRemove.add(entry.getKey());
                }
            }
            toRemove.forEach(annotations::remove);
        });
    }

    @Override
    public void removeAll(long pluginId, int location) {
        rwl.lockWrite(() -> {
            Set<Annotation> atLocation = Optional
                    .ofNullable(annotations.get(location))
                    .orElse(Collections.emptySet());

            Set<Annotation> toRemove = atLocation
                    .stream()
                    .filter(v -> v.getPluginId() == pluginId)
                    .collect(Collectors.toSet());
            atLocation.removeAll(toRemove);

            if (atLocation.isEmpty()) {
                annotations.remove(location);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Annotation> Map<Integer, Set<T>> getAll(long pluginId, Class<? extends T> annotationClass) {
        return rwl.lockRead(() -> {
            Map<Integer, Set<T>> result = new HashMap<>();

            for (Map.Entry<Integer, Set<Annotation>> entry : annotations.entrySet()) {
                Set<T> keyValues = entry
                        .getValue()
                        .stream()
                        .filter(v -> v.getPluginId() == pluginId)
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

    @Override
    public Map<Integer, Set<Annotation>> getAll(long pluginId) {
        return rwl.lockRead(() -> {
            Map<Integer, Set<Annotation>> result = new HashMap<>();

            for (Map.Entry<Integer, Set<Annotation>> entry : annotations.entrySet()) {
                Set<Annotation> keyValues = entry
                        .getValue()
                        .stream()
                        .filter(v -> v.getPluginId() == pluginId)
                        .collect(Collectors.toSet());
                if (!keyValues.isEmpty()) {
                    result.put(entry.getKey(), keyValues);
                }
            }
            return result;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Annotation> Set<T> get(long pluginId, int location, Class<? extends T> annotationClass) {
        return rwl.lockRead(() -> {
            Set<T> result = new HashSet<>();
            Set<Annotation> atLocation = Optional
                    .ofNullable(annotations.get(location))
                    .orElse(Collections.emptySet());

            atLocation.forEach(a -> {
                if (a.getClass().equals(annotationClass) && a.getPluginId() == pluginId) {
                    result.add((T) a);
                }
            });
            return result;
        });
    }

    @Override
    public Set<Annotation> get(long pluginId, int location) {
        return rwl.lockRead(() -> {
            Set<Annotation> result = new HashSet<>();
            Set<Annotation> atLocation = Optional
                    .ofNullable(annotations.get(location))
                    .orElse(Collections.emptySet());

            atLocation.forEach(a -> {
                if (a.getPluginId() == pluginId) {
                    result.add(a);
                }
            });
            return result;
        });
    }

    @Override
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
