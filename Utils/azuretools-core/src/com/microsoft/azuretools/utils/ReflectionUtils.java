/*
 * Copyright (c) Microsoft Corporation
 *
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azuretools.utils;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

/**
 * Utils of java reflection.
 *
 * @author qianjinshen
 * @date 2020-09-27 09:40:12
 */
public class ReflectionUtils {

    public static <T> T getDeclaredFieldValue(Object object, String fieldName, Class<T> fieldClazz) throws NoSuchFieldException, IllegalAccessException {
        Preconditions.checkNotNull(object);
        Preconditions.checkArgument(StringUtils.isNoneBlank(fieldName), "fieldName can not be blank.");
        Class objectClazz = object.getClass();
        Field field = getDeclaredFieldIncludingSupperClazz(objectClazz, fieldName);
        field.setAccessible(true);
        T fieldValue = (T) field.get(object);
        return fieldValue;
    }

    private static Field getDeclaredFieldIncludingSupperClazz(Class clazz, String fieldName) throws NoSuchFieldException {
        Class superClazz = clazz;
        while (superClazz != Object.class) {
            try {
                Field field = superClazz.getDeclaredField(fieldName);
                return field;
            } catch (NoSuchFieldException e) {
                superClazz = superClazz.getSuperclass();
                if (superClazz == Object.class) {
                    throw e;
                }
            }
        }
        return null;
    }
}
