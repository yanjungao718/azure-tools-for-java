package com.microsoft.azure.toolkit.ide.guidance.input;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.guidance.Context;
import com.microsoft.azure.toolkit.ide.guidance.Guidance;
import com.microsoft.azure.toolkit.ide.guidance.config.InputConfig;

import javax.annotation.Nonnull;
import java.util.Map;

public class InputContext {
    @Nonnull
    private final Context context;
    @Nonnull
    private final Map<String, String> paramMapping;
    @Nonnull
    private final Map<String, String> resultMapping;

    public InputContext(@Nonnull final InputConfig config, @Nonnull final Context context) {
        this.context = context;
        this.paramMapping = config.getParamMapping();
        this.resultMapping = config.getResultMapping();
    }

    @Nonnull
    public Project getProject() {
        return context.getProject();
    }

    public Guidance getGuidance() {
        return context.getGuidance();
    }

    public Object getParameter(@Nonnull final String key) {
        final String mappedKey = paramMapping.containsKey(key) ? paramMapping.get(key) : key;
        return context.getProperty(mappedKey);
    }

    public void applyResult(@Nonnull final String key, @Nonnull final Object value) {
        final String mappedKey = resultMapping.getOrDefault(key, key);
        context.setProperty(mappedKey, value);
    }
}
