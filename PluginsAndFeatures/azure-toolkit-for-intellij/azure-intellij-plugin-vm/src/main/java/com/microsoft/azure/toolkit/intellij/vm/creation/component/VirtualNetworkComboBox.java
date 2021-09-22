package com.microsoft.azure.toolkit.intellij.vm.creation.component;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.compute.network.AzureNetwork;
import com.microsoft.azure.toolkit.lib.compute.network.DraftNetwork;
import com.microsoft.azure.toolkit.lib.compute.network.Network;
import lombok.Setter;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class VirtualNetworkComboBox extends AzureComboBox<Network> {
    private DraftNetwork draftNetwork;
    private Subscription subscription;
    @Setter
    private ResourceGroup resourceGroup;
    @Setter
    private Region region;

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
        this.refreshItems();
    }

    @Nullable
    @Override
    protected ExtendableTextComponent.Extension getExtension() {
        return ExtendableTextComponent.Extension.create(AllIcons.General.Add, "Create new virtual network", this::createVirtualNetwork);
    }

    private void createVirtualNetwork() {
        if (!ObjectUtils.allNotNull(resourceGroup, region, subscription)) {
            AzureMessager.getMessager().warning("To create new virtual network, please select subscription, resource group and region first");
            return;
        }
        final VirtualNetworkDialog dialog = new VirtualNetworkDialog(subscription.getId(), resourceGroup.getName(), region);
        if (dialog.showAndGet()) {
            draftNetwork = dialog.getData();
            this.addItem(draftNetwork);
            setValue(draftNetwork);
        }
    }

    @Override
    protected String getItemText(Object item) {
        return item instanceof Network ? ((Network) item).name() : super.getItemText(item);
    }

    @Nonnull
    @Override
    protected List<? extends Network> loadItems() throws Exception {
        final List<Network> networks = Optional.ofNullable(subscription).map(subscription -> Azure.az(AzureNetwork.class).list(subscription.getId())).orElse(Collections.emptyList());
        return draftNetwork == null ? networks : ListUtils.union(List.of(draftNetwork), networks);
    }
}
