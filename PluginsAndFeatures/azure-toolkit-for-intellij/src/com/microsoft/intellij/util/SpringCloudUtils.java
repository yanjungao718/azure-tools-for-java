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

package com.microsoft.intellij.util;

import com.microsoft.azure.management.appplatform.v2019_05_01_preview.AppResourceProperties;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.DeploymentInstance;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.DeploymentResourceProperties;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.DeploymentSettings;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.PersistentDisk;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.TestKeys;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.UserSourceInfo;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.UserSourceType;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.implementation.AppPlatformManager;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.implementation.AppResourceInner;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.implementation.DeploymentResourceInner;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.implementation.ResourceUploadDefinitionInner;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.intellij.runner.springcloud.SpringCloudConstants;
import com.microsoft.intellij.runner.springcloud.SpringCloudModel;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SpringCloudUtils {
    public static AppResourceInner activeDeployment(AppResourceInner appResourceInner, DeploymentResourceInner deploymentResourceInner,
                                                    SpringCloudModel springCloudModel) throws IOException {
        final AppPlatformManager appPlatformManager = getAppPlatformManager(springCloudModel.getSubscriptionId());
        final AppResourceProperties appResourceProperties = appResourceInner.properties()
                .withActiveDeploymentName(deploymentResourceInner.name())
                .withPublicProperty(springCloudModel.isPublic());
        return appPlatformManager.apps().inner().update(springCloudModel.getResourceGroup(),
                springCloudModel.getClusterName(), springCloudModel.getAppName(), appResourceProperties);
    }

    public static DeploymentResourceInner createOrUpdateDeployment(SpringCloudModel springCloudModel,
                                                                   UserSourceInfo userSourceInfo) throws IOException {
        final AppPlatformManager appPlatformManager = getAppPlatformManager(springCloudModel.getSubscriptionId());
        final AppResourceInner appResourceInner = appPlatformManager.apps().inner().get(springCloudModel.getResourceGroup(),
                springCloudModel.getClusterName(), springCloudModel.getAppName());
        // get or create default deployment
        // Get existing deployment properties or new one
        final String targetDeployment;
        DeploymentResourceProperties deploymentResourceProperties;
        if (StringUtils.isEmpty(appResourceInner.properties().activeDeploymentName())) {
            // Create deployment properties
            targetDeployment = SpringCloudConstants.DEFAULT_DEPLOYMENT_NAME;
            deploymentResourceProperties = new DeploymentResourceProperties();
        } else {
            // Get existing deployment properties
            targetDeployment = appResourceInner.properties().activeDeploymentName();
            deploymentResourceProperties = appPlatformManager.deployments().inner().get(springCloudModel.getResourceGroup(),
                    springCloudModel.getClusterName(), springCloudModel.getAppName(), targetDeployment).properties();
        }
        deploymentResourceProperties = updateDeploymentProperties(deploymentResourceProperties, springCloudModel).withSource(userSourceInfo);

        return appPlatformManager.deployments().inner().createOrUpdate(springCloudModel.getResourceGroup(), springCloudModel.getClusterName(),
                springCloudModel.getAppName(), targetDeployment, deploymentResourceProperties);
    }

    public static AppResourceInner createOrUpdateSpringCloudApp(SpringCloudModel springCloudModel) throws IOException {
        final AppPlatformManager appPlatformManager = getAppPlatformManager(springCloudModel.getSubscriptionId());
        final AppResourceInner appResourceInner = appPlatformManager.apps().inner().get(springCloudModel.getResourceGroup(),
                springCloudModel.getClusterName(), springCloudModel.getAppName());
        final AppResourceProperties appResourceProperties = updateAppResourceProperties(appResourceInner == null ?
                new AppResourceProperties() : appResourceInner.properties(), springCloudModel);
        // Service didn't support update app with PUT (createOrUpdate)
        return appResourceInner == null ? appPlatformManager.apps().inner().createOrUpdate(springCloudModel.getResourceGroup(),
                springCloudModel.getClusterName(), springCloudModel.getAppName(), appResourceProperties) :
                appPlatformManager.apps().inner().update(springCloudModel.getResourceGroup(), springCloudModel.getClusterName(),
                        springCloudModel.getAppName(), appResourceProperties);
    }

    public static UserSourceInfo deployArtifact(SpringCloudModel springCloudModel) throws IOException, URISyntaxException, StorageException {
        // Upload artifact to correspond url
        final AppPlatformManager appPlatformManager = getAppPlatformManager(springCloudModel.getSubscriptionId());
        final ResourceUploadDefinitionInner resourceUploadDefinition = appPlatformManager.apps().inner()
                .getResourceUploadUrl(springCloudModel.getResourceGroup(), springCloudModel.getClusterName(), springCloudModel.getAppName());
        uploadFileToStorage(new File(springCloudModel.getArtifactPath()), resourceUploadDefinition.uploadUrl());
        final UserSourceInfo userSourceInfo = new UserSourceInfo();
        // There are some issues with server side resourceUpload logic
        // Use uploadUrl instead of relativePath
        userSourceInfo.withType(UserSourceType.JAR).withRelativePath(resourceUploadDefinition.relativePath());
        return userSourceInfo;
    }

    private static final String LOG_STREAMING_ENDPOINT = "%s/api/logstream/apps/%s/instances/%s?follow=%b";

    public static Map<String, InputStream> getLogStreamReaderList(String subscriptionId, String resourceGroup, String cluster, String appName) throws IOException {
        final Map<String, InputStream> result = new HashMap<>();
        final AppPlatformManager appPlatformManager = getAppPlatformManager(subscriptionId);
        final AppResourceInner appResourceInner = appPlatformManager.apps().inner().get(resourceGroup, cluster, appName);
        final DeploymentResourceInner activeDeployment = appPlatformManager.deployments().inner().get(resourceGroup, cluster, appName, appResourceInner.properties().activeDeploymentName());
        for (final DeploymentInstance instance : activeDeployment.properties().instances()) {
            result.put(instance.name(), getLogStream(subscriptionId, resourceGroup, cluster, appName, instance.name(), 0, 10, 0, true));
        }
        return result;
    }

    public static InputStream getLogStream(String subscriptionId, String resourceGroup, String cluster, String appName,
                                           String instanceName, int sinceSeconds, int tailLines, int limitBytes, boolean follow) throws IOException {
        final AppPlatformManager manager = getAppPlatformManager(subscriptionId);
        final TestKeys testKeys = manager.services().listTestKeysAsync(resourceGroup, cluster).toBlocking().first();
        String endpoint = String.format(LOG_STREAMING_ENDPOINT,
                testKeys.primaryTestEndpoint().replace(".test", ""), appName, instanceName, follow);
        if (sinceSeconds > 0) {
            endpoint += "&sinceSeconds=" + sinceSeconds;
        }
        if (tailLines > 0) {
            endpoint += "&tailLines=" + tailLines;
        }
        if (limitBytes > 0) {
            endpoint += "&limitBytes=" + limitBytes;
        }
        System.out.println(endpoint);
        final String userName = "primary";
        final String password = testKeys.primaryKey();
        HttpURLConnection connection;
        try {
            final URL url = new URL(endpoint);
            connection = (HttpURLConnection) url.openConnection();
            final String userpass = userName + ":" + password;
            final String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
            connection.setRequestProperty("Authorization", basicAuth);

            connection.setReadTimeout(50000);
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return connection.getInputStream();
            }
            throw new RuntimeException("get log stream reader failed");
        } catch (Exception e) {
            throw new RuntimeException("get log stream reader failed", e);
        }
    }

    private static DeploymentResourceProperties updateDeploymentProperties(DeploymentResourceProperties deploymentProperties, SpringCloudModel springCloudModel) {
        DeploymentSettings deploymentSettings = deploymentProperties.deploymentSettings() == null ? new DeploymentSettings() : deploymentProperties.deploymentSettings();
        deploymentSettings = deploymentSettings.withJvmOptions(springCloudModel.getJvmOptions())
                .withEnvironmentVariables(springCloudModel.getEnvironment())
                .withCpu(springCloudModel.getCpu())
                .withInstanceCount(springCloudModel.getInstanceCount())
                .withMemoryInGB(springCloudModel.getMemoryInGB())
                .withRuntimeVersion(springCloudModel.getRuntimeVersion());
        return deploymentProperties.withDeploymentSettings(deploymentSettings);
    }

    private static AppResourceProperties updateAppResourceProperties(AppResourceProperties appResourceProperties, SpringCloudModel springCloudModel) {
        // Enable persistent disk with default parameters
        appResourceProperties = appResourceProperties == null ? new AppResourceProperties() : appResourceProperties;
        if (appResourceProperties.persistentDisk() == null && springCloudModel.isEnablePersistentStorage()) {
            appResourceProperties = appResourceProperties.withPersistentDisk(getDefaultPersistentDisk());
        }
        // As we can't set public policy to an app without active deployment
        if (StringUtils.isNotEmpty(appResourceProperties.activeDeploymentName())) {
            appResourceProperties = appResourceProperties.withPublicProperty(springCloudModel.isPublic());
        }
        return appResourceProperties;
    }

    private static PersistentDisk getDefaultPersistentDisk() {
        final PersistentDisk persistentDisk = new PersistentDisk();
        persistentDisk.withMountPath(SpringCloudConstants.DEFAULT_PERSISTENT_DISK_MOUNT_PATH)
                .withSizeInGB(SpringCloudConstants.DEFAULT_PERSISTENT_DISK_SIZE);
        return persistentDisk;
    }

    private static void uploadFileToStorage(File file, String sasUrl) throws IOException, StorageException, URISyntaxException {
        final CloudFile cloudFile = new CloudFile(new URI(sasUrl));
        cloudFile.uploadFromFile(file.getPath());
    }

    private static AppPlatformManager getAppPlatformManager(String subscriptionId) throws IOException {
        return AuthMethodManager.getInstance().getAzureSpringCloudClient(subscriptionId);
    }
}
