/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.console;

import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azuretools.core.utils.PluginUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

public class AzureHyperlinkTracker implements IPatternMatchListenerDelegate {
    @Override
    public void connect(TextConsole console) {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void matchFound(PatternMatchEvent event) {
        if (event.getSource() instanceof TextConsole) {
            try {
                final TextConsole console = (TextConsole) event.getSource();
                final int start = event.getOffset();
                final int length = event.getLength();
                IHyperlink link = new AzureHyperlink(console.getDocument().get(start, length));
                console.addHyperlink(link, start, length);
            } catch (BadLocationException e) {
                AzureMessager.getMessager().error(e, "Cannot access link in azure console.");
            }
        }
    }

    private static class AzureHyperlink implements IHyperlink {

        private String url;

        AzureHyperlink(String url) {
            this.url = url;
        }

        @Override
        public void linkExited() {
        }

        @Override
        public void linkEntered() {
            // TODO(andxu): record telemetry
        }

        @Override
        public void linkActivated() {
            PluginUtil.openLinkInBrowser(url);
        }
    }
}
