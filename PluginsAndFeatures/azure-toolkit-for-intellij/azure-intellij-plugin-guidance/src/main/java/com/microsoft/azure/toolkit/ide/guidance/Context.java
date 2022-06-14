/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.guidance;

import com.intellij.openapi.project.Project;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Getter
public class Context {
    private final Guidance guidance;
    private final Project project;
    private final Map<String, Object> parameters = new HashMap<>();

    private final Map<String, List<Consumer<Object>>> listenerMap = new HashMap<>();


    public Context(@Nonnull final Guidance guidance) {
        this.guidance = guidance;
        this.project = guidance.getProject();
    }

    public Object getProperty(String key) {
        return parameters.get(key);
    }

    public void setProperty(String key, Object value) {
        final Object origin = parameters.get(key);
        if (!Objects.equals(origin, value)) {
            parameters.put(key, value);
            Optional.ofNullable(listenerMap.get(key)).ifPresent(list -> list.forEach(c -> c.accept(value)));
        }
    }

    public void addPropertyListener(String key, Consumer<Object> listener) {
        final List<Consumer<Object>> consumers = listenerMap.computeIfAbsent(key, ignore -> new ArrayList<>());
        consumers.add(listener);
    }

    public void removePropertyListener(String key, Consumer<Object> listener) {
        Optional.ofNullable(listenerMap.get(key)).ifPresent(list -> list.remove(listener));
    }
}
