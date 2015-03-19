/**
 * PAYWORKS GMBH ("COMPANY") CONFIDENTIAL
 * Copyright (c) 2015 payworks GmbH, All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of COMPANY. The intellectual and technical concepts contained
 * herein are proprietary to COMPANY and may be covered by European or foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from COMPANY.  Access to the source code contained herein is hereby forbidden to anyone except current COMPANY employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes
 * information that is confidential and/or proprietary, and is a trade secret, of COMPANY.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE,
 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF COMPANY IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package io.ui;

import android.support.test.espresso.IdlingResource;

import io.mpos.ui.paybutton.controller.StatefulTransactionProviderProxy;

public class TransactionProviderControllerIdlingResource implements IdlingResource {

    ResourceCallback mCallback;

    boolean mIsWaitingForSignature;
    boolean mIsWaitingForAppSelection;

    boolean mNotified = false;

    public TransactionProviderControllerIdlingResource() {
        this(false, false);
    }

    public TransactionProviderControllerIdlingResource(boolean isWaitingForSignature, boolean isWaitingForAppSelection) {
        mIsWaitingForAppSelection = isWaitingForAppSelection;
        mIsWaitingForSignature = isWaitingForSignature;
    }

    @Override
    public String getName() {
        return "TransactionProviderController idling resource";
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
            idle = !StatefulTransactionProviderProxy.getInstance().isPaymentOnGoing();
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
