/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.aad;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ApplicationCodeTemplateTest {
    @Test
    public void applicationPropertiesTemplate() throws IOException {
        var replaced = ApplicationCodeTemplate.ApplicationProperties.render("actual-tenant-id",
                "actual-client-id",
                "actual-client-secret",
                "actual-group-names");
        Assert.assertTrue(replaced.contains("azure.activedirectory.tenant-id=actual-tenant-id"));
        Assert.assertTrue(replaced.contains("azure.activedirectory.client-id=actual-client-id"));
        Assert.assertTrue(replaced.contains("azure.activedirectory.client-secret=actual-client-secret"));
        Assert.assertTrue(replaced.contains("azure.activedirectory.user-group.allowed-group-names=actual-group-names"));
    }
}
