/*
 * mpos-ui : http://www.payworksmobile.com
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 payworks GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.ui.helper;

import android.support.test.espresso.IdlingResource;

import io.mpos.ui.paybutton.controller.StatefulTransactionProviderProxy;

public class StatefulTransactionProviderProxyIdlingResource implements IdlingResource {

    ResourceCallback mCallback;

    boolean mIsWaitingForSignature;
    boolean mIsWaitingForAppSelection;

    boolean mNotified = false;

    public StatefulTransactionProviderProxyIdlingResource() {
        this(false, false);
    }

    public StatefulTransactionProviderProxyIdlingResource(boolean isWaitingForSignature, boolean isWaitingForAppSelection) {
        mIsWaitingForAppSelection = isWaitingForAppSelection;
        mIsWaitingForSignature = isWaitingForSignature;
    }

    @Override
    public String getName() {
        return "StatefulTransactionProviderProxyIdlingResource idling resource";
    }

    @Override
    public boolean isIdleNow() {
        boolean idle = false;

        if(mNotified) {
            idle = true;
        } else if(mIsWaitingForSignature) {
            idle = StatefulTransactionProviderProxy.getInstance().isAwaitingSignature();
        } else if(mIsWaitingForAppSelection) {
            idle = StatefulTransactionProviderProxy.getInstance().isAwaitingApplicationSelection();
        } else {
            idle = !StatefulTransactionProviderProxy.getInstance().isTransactionOnGoing();
        }

        if(idle && mCallback != null) {
            mCallback.onTransitionToIdle();
            mNotified = true;
        }

        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        mCallback = resourceCallback;
    }
}
