/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.tree;

import com.intellij.openapi.actionSystem.DefaultActionGroup;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.function.Function;

public class AzureNode<T> extends DefaultMutableTreeNode {
         private DefaultActionGroup actionGroup = new DefaultActionGroup();

        private T data;

        public AzureNode(T t) {
                this(t, e -> e.toString(), null);
        }

        public AzureNode(T t, Function<T, String> textGetter) {
                this(t, textGetter, null);
        }

        public AzureNode(T t, Icon icon) {
                this(t, e -> e.toString(), icon);
        }

        public AzureNode(T t, Function<T, String> textGetter, Icon icon) {
                this.data = t;
                com.intellij.ide.util.treeView.NodeDescriptor nodeDescriptor = new AzureNodeDescriptor<>(t, textGetter, icon);
                setUserObject(nodeDescriptor);
        }

        public T getData() {
                return this.data;
        }

        public AzureNodeDescriptor getNodeDescriptor() {
                return (AzureNodeDescriptor) getUserObject();
        }

        public DefaultActionGroup getActionGroup() {
                return this.actionGroup;
        }

}
