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

package com.microsoft.intellij.helpers;

import com.google.common.base.Preconditions;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.microsoft.tooling.msservices.serviceexplorer.AzureIconSymbol;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AzureIconLoader {

    private static final Map<AzureIconSymbol, Icon> ICON_MAP = new ConcurrentHashMap<>();

    static {
        loadIDEIcons();
    }

    public static Icon loadIcon(AzureIconSymbol iconSymbol) {
        Preconditions.checkNotNull(iconSymbol);
        Preconditions.checkArgument(StringUtils.isNotBlank(iconSymbol.getPath()), "iconPath can not be blank.");
        return ICON_MAP.computeIfAbsent(iconSymbol, k -> IconLoader.getIcon(iconSymbol.getPath()));
    }

    private static void loadIDEIcons() {
        loadIcon(AzureIconSymbol.Common.REFRESH, AllIcons.Actions.Refresh);
        loadIcon(AzureIconSymbol.Common.CREATE, AllIcons.Welcome.CreateNewProject);
        loadIcon(AzureIconSymbol.Common.DELETE, AllIcons.Actions.GC);
        loadIcon(AzureIconSymbol.Common.RESTART, AllIcons.Actions.Restart);
        loadIcon(AzureIconSymbol.Common.SHOW_PROPERTIES, AllIcons.Actions.Properties);
    }

    private static Icon loadIcon(AzureIconSymbol iconSymbol, Icon customIcon) {
        Preconditions.checkNotNull(iconSymbol);
        Preconditions.checkArgument(StringUtils.isNotBlank(iconSymbol.getPath()), "iconPath can not be blank.");
        Preconditions.checkNotNull(customIcon);
        return ICON_MAP.computeIfAbsent(iconSymbol, k -> customIcon);
    }

}
