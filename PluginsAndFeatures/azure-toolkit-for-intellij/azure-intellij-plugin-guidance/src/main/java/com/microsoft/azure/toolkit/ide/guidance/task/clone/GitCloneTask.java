package com.microsoft.azure.toolkit.ide.guidance.task.clone;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.ide.impl.ProjectUtil;
import com.microsoft.azure.toolkit.ide.guidance.Context;
import com.microsoft.azure.toolkit.ide.guidance.Guidance;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceConfigManager;
import com.microsoft.azure.toolkit.ide.guidance.InputComponent;
import com.microsoft.azure.toolkit.ide.guidance.Task;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import static com.microsoft.azure.toolkit.ide.guidance.GuidanceConfigManager.GETTING_START_CONFIGURATION_NAME;

public class GitCloneTask implements Task {
    private final Guidance guidance;

    public GitCloneTask(Guidance guidance) {
        this.guidance = guidance;
    }

    @Override
    public boolean isDone() {
        // Check whether project was clone to local
        final File file = new File(guidance.getProject().getBasePath(), GETTING_START_CONFIGURATION_NAME);
        return file.exists();
    }

    @Override
    public InputComponent getInput() {
        return new CloneTaskInputPanel(guidance);
    }

    @Override
    public void execute(Context context) {
        final String projectPath = (String) context.getProperty("directory");
        final String gitUrl = "https://github.com/spring-guides/gs-spring-boot.git";
        try {
            Git.cloneRepository()
                .setURI(gitUrl)
                .setDirectory(Paths.get(projectPath).toFile())
                .call();
            // Copy get start file to path
            copyConfigurationToWorkspace(projectPath);
            ProjectUtil.openOrImport(Paths.get(projectPath, "complete"), OpenProjectTask.newProject());
        } catch (final Exception ex) {
            AzureMessager.getMessager().error(ex);
            throw new AzureToolkitRuntimeException(ex);
        }
    }

    private void copyConfigurationToWorkspace(final String projectPath) throws IOException {
        try (final InputStream inputStream = GuidanceConfigManager.class.getResourceAsStream(guidance.getUri())) {
            final File complete = new File(projectPath, "complete");
            FileUtils.copyInputStreamToFile(inputStream, new File(complete, GuidanceConfigManager.GETTING_START_CONFIGURATION_NAME));
        } catch (final IOException e) {
            throw e;
        }
    }
}
