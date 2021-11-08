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

package com.microsoft.azuretools.plugins.tasks

import com.microsoft.azuretools.plugins.configs.JdkUrlConfigurable
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.net.ssl.HttpsURLConnection

class FetchAdoptOpenJdkAndSetJdkUrlTask extends DefaultTask {
    boolean setOnlyForEmpty = true

    JdkUrlConfigurable conf

    String adoptOpenJdkApi

    @TaskAction
    def fetchAndSet() {
        if (conf == null) {
            return
        }

        if (setOnlyForEmpty && conf.jdkUrl != null && !conf.jdkUrl.isEmpty()) {
            return
        }

        def httpcon = new URL(adoptOpenJdkApi).openConnection()
        httpcon.addRequestProperty("User-Agent", "Mozilla")
        httpcon.addRequestProperty("accept", "application/json")

        def adoptOpenJdk = new JsonSlurper().parseText(httpcon.getInputStream().getText())

        def latestRelease = adoptOpenJdk["releases"][0]
        def jdkUrl = "https://api.adoptium.net/v3/binary/version/" +
                latestRelease +
                "/windows/x64/jdk/hotspot/normal/eclipse?project=jdk"

        HttpURLConnection jdkUrlCon = new URL(jdkUrl).openConnection()
        jdkUrlCon.setInstanceFollowRedirects(false)
        jdkUrlCon.setRequestMethod("HEAD")
        if (jdkUrlCon.getResponseCode() == 307) {
            conf.jdkUrl = jdkUrlCon.getHeaderField("Location")
        } else {
            conf.jdkUrl = jdkUrl
        }
    }
}
