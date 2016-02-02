package io.mpos.ui.shared.util;

import io.mpos.transactions.TransactionType;
import io.mpos.transactions.parameters.TransactionParameters;

/**
 * Created by abhijith on 1/29/16.
 */
public class TransactionParametersHelper {

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
}
