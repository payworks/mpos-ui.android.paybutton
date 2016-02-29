/*
 * mpos-ui : http://www.payworksmobile.com
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 payworks GmbH
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
package io.mpos.ui.shared.util;

import io.mpos.accessories.AccessoryFamily;
import io.mpos.accessories.parameters.AccessoryParameters;
import io.mpos.transactions.TransactionType;
import io.mpos.transactions.parameters.TransactionParameters;

public class ParametersHelper {

    public static TransactionParameters getTransactionParametersWithNewCustomerIdentifier(TransactionParameters params, String customIdentifier) {

        if (params.getType() == TransactionType.CHARGE) {
            return new TransactionParameters.Builder().
                    charge(params.getAmount(), params.getCurrency()).
                    subject(params.getSubject()).
                    customIdentifier(customIdentifier).
                    applicationFee(params.getApplicationFee()).
                    metadata(params.getMetadata()).
                    statementDescriptor(params.getStatementDescriptor()).
                    build();
        }

        //Refund
        return new TransactionParameters.Builder().
                refund(params.getReferencedTransactionIdentifier()).
                subject(params.getSubject()).
                customIdentifier(customIdentifier).
                build();
    }

    public static AccessoryParameters getAccessoryParametersForAccessoryFamily(AccessoryFamily accessoryFamily) {
        if(accessoryFamily == null) {
            return null;
        }

        switch (accessoryFamily) {
            case UNKNOWN:
                return null;
            case MIURA_MPI:
                return new AccessoryParameters.Builder(AccessoryFamily.MIURA_MPI).bluetooth().build();
            case VERIFONE_E105:
                return new AccessoryParameters.Builder(AccessoryFamily.VERIFONE_E105).audioJack().build();
            case BBPOS_WISE:
                return new AccessoryParameters.Builder(AccessoryFamily.BBPOS_WISE).bluetooth().build();
            case BBPOS_CHIPPER:
                return new AccessoryParameters.Builder(AccessoryFamily.BBPOS_CHIPPER).audioJack().build();
            case SEWOO:
                return new AccessoryParameters.Builder(AccessoryFamily.SEWOO).bluetooth().build();
            case MOCK:
                return new AccessoryParameters.Builder(AccessoryFamily.MOCK).mocked().build();
        }
        return null;
    }
}
