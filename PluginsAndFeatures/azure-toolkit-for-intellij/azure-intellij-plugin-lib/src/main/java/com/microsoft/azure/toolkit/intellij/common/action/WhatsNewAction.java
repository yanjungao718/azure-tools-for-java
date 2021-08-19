/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.action;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.microsoft.azure.toolkit.intellij.common.messager.IntellijOpenInBrowserMessageAction;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhatsNewAction extends AnAction {
    private static final String WHATSNEW_URL = "https://github.com/microsoft/azure-tools-for-java/tree/develop/PluginsAndFeatures/" +
            "azure-toolkit-for-intellij/azure-intellij-plugin-lib/src/main/resources/whatsnew.md";
    private static final String AZURE_TOOLKIT_FOR_JAVA = "What's New in Azure Toolkit for Java";
    private static final String AZURE_TOOLKIT_WHATS_NEW = "AzureToolkit.WhatsNew";
    private static final String FAILED_TO_LOAD_WHATS_NEW = "Failed to load what's new document";
    private static final String VERSION_PATTERN = "<!-- Version: (.*) -->";
    private static final String CONTENT_PATH = "/whatsnew.md";
    private static final Key<String> CONTENT_KEY = new Key<>("WHATS_NEW_IN_AZURE_TOOLKIT");

    @Override
    public void actionPerformed(@NotNull final AnActionEvent event) {
        showWhatsNew(true, event.getProject());
    }

    @AzureOperation(name = "common.load_whatsnew", type = AzureOperation.Type.ACTION)
    public static synchronized void showWhatsNew(boolean manually, @NotNull Project project) {
        final String content = getWhatsNewContent();
        final DefaultArtifactVersion currentVersion = getVersion(content);
        final DefaultArtifactVersion lastVersion = getLastVersion();
        if (manually || compare(currentVersion, lastVersion)) {
            saveVersion(currentVersion);
            doShow(content, currentVersion, manually, project);
        }
    }

    private static void doShow(String content, DefaultArtifactVersion version, boolean manually, @NotNull Project project) {
        final FileEditorManager manager = FileEditorManager.getInstance(project);
        final VirtualFile file = Arrays.stream(manager.getOpenFiles())
                .filter(f -> StringUtils.equals(f.getUserData(CONTENT_KEY), CONTENT_PATH))
                .findFirst().orElse(createVirtualFile(content));
        AzureTaskManager.getInstance().runAndWait(() -> {
            if (manager.getProject().isDisposed()) {
                return;
            }
            final FileEditor[] editors = manager.openFile(file, true, true);
            if (editors.length > 0) { // opened as markdown in editor when markdown plugin enabled.
                for (final FileEditor fileEditor : editors) {
                    if (fileEditor instanceof SplitFileEditor) {
                        // Switch to markdown preview panel
                        ((SplitFileEditor<?, ?>) fileEditor).triggerLayoutChange(SplitFileEditor.SplitEditorLayout.SECOND, true);
                    }
                }
            } else if (manually) {
                BrowserUtil.browse(WHATSNEW_URL);
            } else {
                final String message = String.format("Azure Toolkit for Java is updated to <b><u>%s</u></b>", version.toString());
                final String title = "Azure Toolkit for Java Updated";
                final IntellijOpenInBrowserMessageAction changelog = new IntellijOpenInBrowserMessageAction("What's New", WHATSNEW_URL);
                AzureMessager.getMessager().info(message, title, changelog);
            }
        });
    }

    @Nonnull
    private static LightVirtualFile createVirtualFile(String content) {
        final FileType fileType = FileTypeManagerEx.getInstance().getFileTypeByExtension("md");
        final LightVirtualFile virtualFile = new LightVirtualFile(AZURE_TOOLKIT_FOR_JAVA);
        virtualFile.setFileType(fileType);
        virtualFile.setContent(null, content, true);
        virtualFile.putUserData(CONTENT_KEY, CONTENT_PATH);
        virtualFile.setWritable(false);
        return virtualFile;
    }

    @Nullable
    private static String getWhatsNewContent() {
        try (final InputStream html = WhatsNewAction.class.getResourceAsStream(CONTENT_PATH)) {
            if (html != null) {
                return new String(StreamUtil.readBytes(html), StandardCharsets.UTF_8);
            }
        } catch (final IOException e) {
            throw new AzureToolkitRuntimeException(FAILED_TO_LOAD_WHATS_NEW, e);
        }
        throw new AzureToolkitRuntimeException(FAILED_TO_LOAD_WHATS_NEW);
    }

    /**
     * @return true if {@code versionA} is a newer version or {@code versionB} is null, false otherwise.
     */
    private static boolean compare(DefaultArtifactVersion versionA, DefaultArtifactVersion versionB) {
        return versionB == null || (versionA != null && versionA.compareTo(versionB) >= 0);
    }

    private static void saveVersion(DefaultArtifactVersion version) {
        PropertiesComponent.getInstance().setValue(AZURE_TOOLKIT_WHATS_NEW, version.toString());
    }

    @Nullable
    private static DefaultArtifactVersion getLastVersion() {
        final String shownVersionValue = PropertiesComponent.getInstance().getValue(AZURE_TOOLKIT_WHATS_NEW);
        return StringUtils.isEmpty(shownVersionValue) ? null : new DefaultArtifactVersion(shownVersionValue);
    }

    @NotNull
    private static DefaultArtifactVersion getVersion(String content) {
        try (final Scanner scanner = new Scanner(content)) {
            // Read the first comment line to get the whats new version
            final String versionLine = scanner.nextLine();
            final Matcher matcher = Pattern.compile(VERSION_PATTERN).matcher(versionLine);
            return matcher.matches() ? new DefaultArtifactVersion(matcher.group(1)) : new DefaultArtifactVersion("0");
        }
    }
}
