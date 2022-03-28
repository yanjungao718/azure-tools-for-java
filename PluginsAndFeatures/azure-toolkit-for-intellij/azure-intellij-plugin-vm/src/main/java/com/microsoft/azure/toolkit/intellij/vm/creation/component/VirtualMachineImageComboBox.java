/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.vm.creation.component;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.compute.AzureCompute;
import com.microsoft.azure.toolkit.lib.compute.virtualmachine.VmImage;
import lombok.Setter;
import org.apache.commons.collections4.ListUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class VirtualMachineImageComboBox extends AzureComboBox<VmImage> {
    @Setter
    private Subscription subscription;
    @Setter
    private Region region;
    private VmImage customImage;

    @Override
    protected ExtendableTextComponent.Extension getExtension() {
        return ExtendableTextComponent.Extension.create(AllIcons.General.OpenDisk, "Select marketplace image", this::selectImage);
    }

    private void selectImage() {
        if (region == null || subscription == null) {
            AzureMessager.getMessager().warning("PLease select subscription&region first");
        }
        final VirtualMachineImageDialog dialog = new VirtualMachineImageDialog(subscription, region);
        if (dialog.showAndGet()) {
            customImage = dialog.getValue();
            refreshItems();
            setValue(customImage);
        }
    }

    @Nonnull
    @Override
    protected List<VmImage> loadItems() throws Exception {
        final List<VmImage> knownImages = AzureCompute.getKnownImages();
        return Optional.ofNullable(customImage)
            .map(image -> ListUtils.union(knownImages, Collections.singletonList(image)))
            .orElse(knownImages);
    }

    @Override
    protected String getItemText(Object item) {
        return item instanceof VmImage ? String.format("%s %s", ((VmImage) item).getOffer(), ((VmImage) item).getSku()) : super.getItemText(item);
    }
}
