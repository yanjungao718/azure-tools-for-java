package com.microsoft.azure.toolkit.ide.guidance.task;

import com.microsoft.azure.toolkit.ide.guidance.Context;
import com.microsoft.azure.toolkit.ide.guidance.config.TaskConfig;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessager;
import com.microsoft.azure.toolkit.lib.common.operation.OperationContext;

import javax.annotation.Nonnull;
import java.util.Map;

public abstract class AbstractTask {
    protected final String name;
    protected final String description;
    protected final Map<String, String> paramMapping;
    protected final Map<String, String> resultMapping;

    private final Context context;

    public AbstractTask(@Nonnull final TaskConfig config, @Nonnull final Context context) {
        this.name = config.getName();
        this.description = config.getDescription();
        this.paramMapping = config.getParamMapping();
        this.resultMapping = config.getResultMapping();

        this.context = context;
    }

    public void init() {

    }

    public boolean isComplete() {
        return false;
    }

    public void execute(IAzureMessager messager) throws Exception {
        final IAzureMessager currentMessager = AzureMessager.getMessager();
        OperationContext.current().setMessager(messager);
        try {
            execute();
        } finally {
            OperationContext.current().setMessager(currentMessager);
        }
    }

    public abstract void execute() throws Exception;

    protected Object getParameterFromContext(@Nonnull final String key) {
        final String mappedKey = paramMapping.containsKey(key) ? paramMapping.get(key) : key;
        return context.getProperty(mappedKey);
    }

    protected void setValueInContext(@Nonnull final String key, @Nonnull final Object value) {
        final String mappedKey = resultMapping.getOrDefault(key, key);
        context.setProperty(mappedKey, value);
    }
}
