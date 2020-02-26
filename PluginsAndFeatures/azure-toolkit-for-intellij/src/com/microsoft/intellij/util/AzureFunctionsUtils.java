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
 *
 */

package com.microsoft.intellij.util;

import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.microsoft.azure.common.exceptions.AzureExecutionException;
import com.microsoft.azure.common.function.template.FunctionTemplate;
import com.microsoft.intellij.runner.functions.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AzureFunctionsUtils {
    public static void applyKeyValueToLocalSettingFile(File localSettingFile, String key, String value) throws IOException {
        if (!localSettingFile.getParentFile().isDirectory()) {
            throw new IOException("Cannot save file to a non-existing directory: " + localSettingFile.getParent());
        }
        final JsonObject localSettingRoot = localSettingFile.exists() ?
                JsonUtils.readJsonFile(localSettingFile) : new JsonObject();
        if (localSettingRoot.has("IsEncrypted")) {
            localSettingRoot.add("IsEncrypted", new JsonPrimitive(false));
        }
        JsonObject appSettings = localSettingRoot.getAsJsonObject("Values");
        if (appSettings == null) {
            appSettings = new JsonObject();
            localSettingRoot.add("Values", appSettings);
        }

        appSettings.addProperty(key, value);
        JsonUtils.writeJsonToFile(localSettingFile, localSettingRoot);
    }

    public static String normalizeClassName(String className) {
        if (StringUtils.isBlank(className)) {
            return "untitled";
        }
        final String trimClassName = className.endsWith(".java") ? className.substring(0, className.length() - 5) : className;
        return trimClassName.replaceAll("[^\\.a-zA-Z0-9\\_\\$]+", "").replace('.', '_');
    }

    public static File createMavenProjectToTempFolder(final String groupId, final String artifactId,
            final String version, final String packageName)
            throws IOException, InterruptedException {
        final boolean isWindows = CommandUtils.isWindows();
        final List<File> mvnCmds = resolvePathForCommand(isWindows ? "mvn.cmd" : "mvn");
        if (!mvnCmds.isEmpty()) {
            final File mvnCmd = mvnCmds.get(0);
            final File folder = Files.createTempDir();
            final Map<String, String> maps = new LinkedHashMap<>();
            maps.put("-DarchetypeGroupId", "com.microsoft.azure");
            maps.put("-DarchetypeArtifactId", "azure-functions-archetype");
            maps.put("-DgroupId", groupId);
            maps.put("-DartifactId", artifactId);
            maps.put("-Dversion", version);
            maps.put("-Dpackage", packageName);
            final String args = maps.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(" "));
            CommandUtils.executeMultipleLineOutput(mvnCmd.getAbsolutePath() + " archetype:generate" + " -B " + args, folder);

            return folder;
        }
        return null;
    }

    public static String substituteParametersInTemplate(final FunctionTemplate template, final Map<String, String> params)
            throws AzureExecutionException {
        String ret = template.getFiles().get("function.java");
        for (final Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null) {
                throw new AzureExecutionException("Required property:" + entry.getKey() + " is missing");
            }
            ret = ret.replace(String.format("$%s$", entry.getKey()), entry.getValue());
        }
        return ret;
    }

    private static List<File> resolvePathForCommand(final String command)
            throws IOException, InterruptedException {
        return extractFileFromOutput(CommandUtils.executeMultipleLineOutput((CommandUtils.isWindows() ? "where " : "which ") + command, null));
    }

    private static List<File> extractFileFromOutput(final String[] outputStrings) {
        final List<File> list = new ArrayList<>();
        for (final String outputLine : outputStrings) {
            if (StringUtils.isBlank(outputLine)) {
                continue;
            }

            final File file = new File(outputLine.replaceAll("\\r|\\n", ""));
            if (!file.exists() || !file.isFile()) {
                continue;
            }

            list.add(file);
        }
        return list;
    }
}
