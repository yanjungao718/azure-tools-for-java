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

package com.microsoft.azure.toolkit.lib.common.task;

import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.functions.Func2;
import rx.plugins.RxJavaHooks;

import java.util.Objects;

public class AzureRxTaskManager {
    private static boolean registered = false;

    @SuppressWarnings("rawtypes")
    public static synchronized void register() {
        if (registered) {
            throw new IllegalStateException("rx task manager has already been registered.");
        }
        registered = true;
        final Func2<Observable, Observable.OnSubscribe, Observable.OnSubscribe> oldObservableStartHooks = RxJavaHooks.getOnObservableStart();
        final Func2<Completable, Completable.OnSubscribe, Completable.OnSubscribe> oldCompletableStartHooks = RxJavaHooks.getOnCompletableStart();
        final Func2<Single, Single.OnSubscribe, Single.OnSubscribe> oldSingleStartHooks = RxJavaHooks.getOnSingleStart();
        RxJavaHooks.setOnObservableStart((observable, onStart) -> {
            final AzureTaskContext.Node context = AzureTaskContext.current().derive();
            final Observable.OnSubscribe<?> withClosure = (subscriber) -> AzureTaskContext.run(() -> onStart.call(subscriber), context);
            if (Objects.isNull(oldObservableStartHooks)) {
                return withClosure;
            }
            return oldObservableStartHooks.call(observable, withClosure);
        });
        RxJavaHooks.setOnCompletableStart((completable, onStart) -> {
            final AzureTaskContext.Node context = AzureTaskContext.current().derive();
            final Completable.OnSubscribe withClosure = (subscriber) -> AzureTaskContext.run(() -> onStart.call(subscriber), context);
            if (Objects.isNull(oldCompletableStartHooks)) {
                return withClosure;
            }
            return oldCompletableStartHooks.call(completable, withClosure);
        });
        RxJavaHooks.setOnSingleStart((single, onStart) -> {
            final AzureTaskContext.Node context = AzureTaskContext.current().derive();
            final Single.OnSubscribe<?> withClosure = (subscriber) -> AzureTaskContext.run(() -> onStart.call(subscriber), context);
            if (Objects.isNull(oldSingleStartHooks)) {
                return withClosure;
            }
            return oldSingleStartHooks.call(single, withClosure);
        });
    }
}
