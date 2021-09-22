package com.microsoft.azure.toolkit.intellij.storage.component;

import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.storage.service.AzureStorageAccount;
import com.microsoft.azure.toolkit.lib.storage.service.StorageAccount;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AzureStorageAccountComboBox extends AzureComboBox<StorageAccount> {
    private Subscription subscription;

    public void setSubscription(final Subscription subscription) {
        this.subscription = subscription;
        this.refreshItems();
    }

    @Override
    protected String getItemText(Object item) {
        return item instanceof StorageAccount ? ((StorageAccount) item).name() : super.getItemText(item);
    }

    @Nonnull
    @Override
    protected List<? extends StorageAccount> loadItems() {
        return Optional.ofNullable(subscription)
                .map(subscription -> Azure.az(AzureStorageAccount.class).subscription(subscription.getId()).list())
                .orElse(Collections.emptyList());
    }
}
