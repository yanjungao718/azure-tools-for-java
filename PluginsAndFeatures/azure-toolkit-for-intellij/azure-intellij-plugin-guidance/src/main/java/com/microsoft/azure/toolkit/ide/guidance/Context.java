/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.guidance;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.guidance.task.GuidanceTaskProvider;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Context {
    private final Guidance guidance;
    private final Project project;
    public Map<String, Object> parameters = new HashMap<>();

    public Context(@Nonnull final Guidance guidance){
        this.guidance = guidance;
        this.project = guidance.getProject();
    }

    public Object getProperty(String key) {
        return parameters.get(key);
    }

    public void setProperty(String key, Object value) {
        parameters.put(key, value);
    }
}
