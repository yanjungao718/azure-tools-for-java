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

package com.microsoft.tooling.msservices.serviceexplorer.azure.appservice.file;

import com.microsoft.azure.toolkit.lib.appservice.file.AppServiceFile;
import com.microsoft.azure.toolkit.lib.appservice.file.AppServiceFileService;
import com.microsoft.azuretools.azurecommons.helpers.AzureCmdException;
import com.microsoft.tooling.msservices.serviceexplorer.AzureRefreshableNode;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.WebAppModule;

public class AppServiceFileNode extends AzureRefreshableNode implements IAppServiceFileNode {
    private static final String MODULE_ID = WebAppModule.class.getName();
    private static final String ICON_PATH = "Slot_16.png";

    private final AppServiceFileService fileService;
    private final IAppServiceFileNode parentNode;
    private final AppServiceFile file;

    public AppServiceFileNode(final AppServiceFile file, final IAppServiceFileNode parent, AppServiceFileService service) {
        super(file.getName(), file.getName(), (Node) parent, AppServiceFile.getIcon(file));
        this.file = file;
        this.parentNode = parent;
        this.fileService = service;
    }

    @Override
    public void removeNode(final String sid, final String name, Node node) {
    }

    @Override
    protected void refreshItems() throws AzureCmdException {
        if (this.file.getType() != AppServiceFile.Type.DIRECTORY) {
            return;
        }
        this.fileService.getFilesInDirectory(this.getPath()).stream()
                        .map(file -> new AppServiceFileNode(file, this, fileService))
                        .forEach(this::addChildNode);
    }

    @Override
    public String getPath() {
        return this.parentNode.getPath() + "/" + this.file.getName();
    }
}
