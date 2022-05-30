/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.common.launch;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;

public class LaunchConfigurationUtils {
    private static final ObjectMapper mapper = new ObjectMapper();
    static{
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T getFromConfiguration(ILaunchConfiguration config, @Nonnull Class<T> classOfT) {
        try {
            T obj = classOfT.getDeclaredConstructor().newInstance();
            for (Field field : FieldUtils.getAllFields(classOfT)) {
                Object value = null;
                // only three types are supported now
                if (field.getType().equals(String.class)) {
                    value = config.getAttribute(field.getName(), StringUtils.EMPTY);
                } else if (field.getType().equals(Boolean.class)) {
                    value = config.getAttribute(field.getName(), false);
                } else if (field.getType().equals(Integer.class)) {
                    value = config.getAttribute(field.getName(), 0);
                } else {
                    final String json = config.getAttribute(field.getName(), StringUtils.EMPTY);
                    value = StringUtils.isEmpty(json) ? null : mapper.readValue(json, field.getType());
                }
                if (value != null) {
                    FieldUtils.writeField(field, obj, value, true);
                }
            }
            return obj;
        } catch (NoSuchMethodException | SecurityException | InvocationTargetException | InstantiationException
                | IllegalAccessException | CoreException | JsonProcessingException ex) {
            throw new AzureToolkitRuntimeException("Cannot use reflections on class:" + classOfT.getSimpleName(), ex);
        }
    }

    public static <T> void saveToConfiguration(@Nonnull T obj, ILaunchConfigurationWorkingCopy config) {
        try {
            for (Field field : FieldUtils.getAllFields(obj.getClass())) {
                Object value = FieldUtils.readField(field, obj, true);
                if (value == null) {
                    continue;
                } else if (value instanceof String || value instanceof Boolean || value instanceof Integer) {
                    config.setAttribute(field.getName(), value);
                } else {
                    config.setAttribute(field.getName(), mapper.writeValueAsString(value));
                }
            }
        } catch (SecurityException | IllegalAccessException | JsonProcessingException ex) {
            throw new AzureToolkitRuntimeException("Cannot use reflections on class:" + obj.getClass().getSimpleName(), ex);
        }
    }
}
