/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.common.console;

import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessager;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.function.Supplier;

public class AzureAsyncConsoleJob extends WorkspaceJob {
    private Supplier<IStatus> supplier;

    private IAzureMessager messager;

    public AzureAsyncConsoleJob(String name) {
        super(name);
    }

    public void setSupplier(Supplier<IStatus> supplier) {
        this.supplier = supplier;
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) {
        return checkCanceled(monitor, Mono.fromSupplier(supplier), () -> Status.CANCEL_STATUS);
    }

    protected static <T> T checkCanceled(IProgressMonitor monitor, Mono<? extends T> mono, Supplier<T> supplier) {
        if (monitor == null) {
            return mono.block();
        }
        final Mono<T> cancelMono = Flux.interval(Duration.ofSeconds(1)).map(ignore -> monitor.isCanceled())
                .any(cancel -> cancel).map(ignore -> supplier.get()).subscribeOn(Schedulers.boundedElastic());
        return Mono.firstWithSignal(cancelMono, mono.subscribeOn(Schedulers.boundedElastic())).block();
    }

    public IAzureMessager getMessager() {
        return messager;
    }

    public void setMessager(IAzureMessager messager) {
        this.messager = messager;
    }
}
