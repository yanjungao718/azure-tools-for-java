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

package com.microsoft.azure.toolkit.lib.appservice.file;

import com.google.common.base.Joiner;
import com.microsoft.azure.management.appservice.WebAppBase;
import com.microsoft.azure.management.appservice.implementation.AppServiceManager;
import com.microsoft.rest.RestClient;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AppServiceFileService {

    private final AppServiceFileClient client;
    private final WebAppBase app;

    private AppServiceFileService(final WebAppBase app, AppServiceFileClient client) {
        this.app = app;
        this.client = client;
    }

    public static AppServiceFileService forApp(final WebAppBase app) {
        final AppServiceFileClient client = getClient(app);
        return new AppServiceFileService(app, client);
    }

    public List<? extends AppServiceFile> getFilesInDirectory(String path) {
        return ((KuduFileClient) this.client).getFilesInDirectory(path).toBlocking().first();
    }

    private static AppServiceFileClient getClient(WebAppBase webAppBase) {
        if (webAppBase.defaultHostName() == null) {
            throw new UnsupportedOperationException("Cannot initialize kudu vfs client before web app is created");
        } else {
            String host = webAppBase.defaultHostName().toLowerCase().replace("http://", "").replace("https://", "");
            final String[] parts = host.split("\\.", 2);
            host = Joiner.on('.').join(parts[0], "scm", parts[1]);
            final AppServiceManager manager = webAppBase.manager();
            final RestClient restClient = getRestClient(manager);
            return restClient.newBuilder()
                             .withBaseUrl("https://" + host)
                             .withConnectionTimeout(3L, TimeUnit.MINUTES)
                             .withReadTimeout(3L, TimeUnit.MINUTES)
                             .build()
                             .retrofit()
                             .create(KuduFileClient.class);
        }
    }

    @SneakyThrows
    private static RestClient getRestClient(final AppServiceManager manager) {
        //TODO: @wangmi find a proper way to get rest client.
        final Method method = manager.getClass().getDeclaredMethod("restClient");
        method.setAccessible(true);
        return (RestClient) method.invoke(manager);
    }

}
