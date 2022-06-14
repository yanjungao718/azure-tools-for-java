package com.microsoft.azure.toolkit.ide.guidance.input;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.guidance.Context;
import com.microsoft.azure.toolkit.ide.guidance.Guidance;
import com.microsoft.azure.toolkit.ide.guidance.config.InputConfig;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class InputContext {
    @Nonnull
    private final Context context;
    @Nonnull
    private final Map<String, String> paramMapping;
    @Nonnull
    private final Map<String, String> resultMapping;

    public InputContext(@Nonnull final InputConfig config, @Nonnull final Context context) {
        this.context = context;
        this.paramMapping = Optional.ofNullable(config.getParamMapping()).orElse(Collections.emptyMap());
        this.resultMapping = Optional.ofNullable(config.getResultMapping()).orElse(Collections.emptyMap());
    }

    @Nonnull
    public Project getProject() {
        return context.getProject();
    }

    public Guidance getGuidance() {
        return context.getGuidance();
    }

    public Object getParameter(@Nonnull final String key) {
        final String mappedKey = paramMapping.getOrDefault(key, key);
        return context.getProperty(mappedKey);
    }

    public void applyResult(@Nonnull final String key, @Nonnull final Object value) {
        final String mappedKey = resultMapping.getOrDefault(key, key);
        context.setProperty(mappedKey, value);
    }

    public void addPropertyListener(String key, Consumer<Object> listener) {
        final String mappedKey = paramMapping.getOrDefault(key, key);
        context.addPropertyListener(mappedKey, listener);
    }

    public void removePropertyListener(String key, Consumer<Object> listener) {
        final String mappedKey = paramMapping.getOrDefault(key, key);
        context.removePropertyListener(mappedKey, listener);
    }
}
