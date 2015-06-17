package fr.pasteque.client.payment;

import android.content.Intent;
import fr.pasteque.client.Configure;
import fr.pasteque.client.models.Payment;
import fr.pasteque.client.utils.TrackedActivity;

public abstract class PaymentProcessor {

	protected TrackedActivity parentActivity;
	
	protected Payment payment;
	
	protected PaymentListener listener;
	
	public enum Status {
		VALIDATED,
		PENDING
	}

	protected PaymentProcessor (TrackedActivity parentActivity, PaymentListener listener, Payment payment) {
		this.parentActivity= parentActivity;
		this.listener = listener;
		this.payment = payment;
	}

	public abstract void handleIntent(int requestCode, int resultCode,
            Intent data);

	public abstract Status initiatePayment();

	public static PaymentProcessor getProcessor(TrackedActivity parentActivity, PaymentListener listener, Payment payment) { 
		if ("magcard".equals(payment.getMode().getCode())) {
			String cardProcessor = Configure.getCardProcessor(parentActivity);
			if ("none".equals(cardProcessor)) {
				return null;
			}
			else if ("payleven".equals(cardProcessor))
				return new PaylevenPaymentProcessor(parentActivity, listener, payment);
			else
				// Atos is "generic"
				return new AtosPaymentProcessor(parentActivity, listener, payment);
/*		} else if ("bnp_rmw".equals(payment.getMode().getCode())) {
			return new RMWPaymentProcessor(parentActivity, listener, payment);*/
		}
		return null;
	}
	
	public interface PaymentListener {
		abstract void registerPayment(Payment p);
	}
}
