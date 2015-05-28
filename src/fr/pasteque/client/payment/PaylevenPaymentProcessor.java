package fr.pasteque.client.payment;

import java.util.Map;

import android.content.Intent;

import com.payleven.payment.api.OpenTransactionDetailsCompletedStatus;
import com.payleven.payment.api.PaylevenApi;
import com.payleven.payment.api.PaylevenResponseListener;
import com.payleven.payment.api.PaymentCompletedStatus;
import com.payleven.payment.api.TransactionRequest;
import com.payleven.payment.api.TransactionRequestBuilder;

import fr.pasteque.client.Configure;
import fr.pasteque.client.Error;
import fr.pasteque.client.R;
import fr.pasteque.client.models.Payment;
import fr.pasteque.client.utils.TrackedActivity;


public class PaylevenPaymentProcessor extends PaymentProcessor {

    private static final String PAYLEVEN_API_KEY = "edaffb929bd34aa78122b2d15a36a5c7";
    
	public PaylevenPaymentProcessor(TrackedActivity parentActivity, PaymentListener listener, Payment payment) {
		super(parentActivity, listener, payment);
        if (!"payleven".equals(Configure.getCardProcessor(parentActivity))) {
        	throw new RuntimeException("Payleven is disabled in configuration");
        }
        
        // Init Payleven API
        PaylevenApi.configure(PAYLEVEN_API_KEY);
	}
	
	@Override
	public void handleIntent(int requestCode, int resultCode, Intent data) {
        PaylevenApi.handleIntent(requestCode, data, new PaylevenResultHandler());
	}
	
	@Override
	public Status initiatePayment() {
		int amountInCents = (int) Math.round(payment.getAmount() * 100);
        TransactionRequestBuilder builder = new TransactionRequestBuilder(amountInCents, payment.getCurrency());
        TransactionRequest request = builder.createTransactionRequest();
        String orderId = String.valueOf(payment.getInnerId());
        PaylevenApi.initiatePayment(parentActivity, orderId, request);
        return Status.PENDING;
	}

    private class PaylevenResultHandler implements PaylevenResponseListener {
        public void onPaymentFinished(String orderId,
                                      TransactionRequest originalRequest, Map<String, String> result,
                                      PaymentCompletedStatus status) {

    		PaylevenPaymentProcessor processor = PaylevenPaymentProcessor.this;
    		
            switch (status) {
                case AMOUNT_TOO_LOW:
                    Error.showError(R.string.payment_card_rejected, processor.parentActivity);
                    break;
                case API_KEY_DISABLED:
                case API_KEY_NOT_FOUND:
                case API_KEY_VERIFICATION_ERROR:
                    Error.showError(R.string.err_payleven_key, processor.parentActivity);
                    break;
                case ANOTHER_API_CALL_IN_PROGRESS:
                    Error.showError(R.string.err_payleven_concurrent_call, processor.parentActivity);
                    break;
                case API_SERVICE_ERROR:
                case API_SERVICE_FAILED:
                case ERROR:
                case PAYMENT_ALREADY_EXISTS:
                    Error.showError(R.string.err_payleven_general, processor.parentActivity);
                    break;
                case CARD_AUTHORIZATION_ERROR:
                    Error.showError(R.string.payment_card_rejected, processor.parentActivity);
                    break;
                case INVALID_CURRENCY:
                case WRONG_COUNTRY_CODE:
                    Error.showError(R.string.err_payleven_forbidden, processor.parentActivity);
                    break;
                case SUCCESS:
                	listener.registerPayment(processor.payment);
                    break;
            }
        }

        public void onNoPaylevenResponse(Intent data) {
        }

        public void onOpenTransactionDetailsFinished(String orderId,
                                                     Map<String, String> transactionData,
                                                     OpenTransactionDetailsCompletedStatus status) {
        }

        public void onOpenSalesHistoryFinished() {
        }
    }
}
