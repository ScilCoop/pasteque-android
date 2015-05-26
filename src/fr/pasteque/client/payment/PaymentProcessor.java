package fr.pasteque.client.payment;

import android.content.Intent;
import fr.pasteque.client.ProceedPayment;
import fr.pasteque.client.models.Payment;
import fr.pasteque.client.models.PaymentMode;

public abstract class PaymentProcessor {

	protected ProceedPayment parentActivity;
	
	protected Payment payment;
	
	public enum Status {
		VALIDATED,
		PENDING
	}
	
	protected PaymentProcessor (ProceedPayment parentActivity, Payment payment) {
		this.parentActivity = parentActivity;
		this.payment = payment;
	}
	
	public abstract void handleIntent(int requestCode, int resultCode,
            Intent data);
	
	public abstract Status initiatePayment();
	
	
	public static PaymentProcessor getProcessor(ProceedPayment parentActivity, PaymentMode mode, Payment payment) {
		if ("PAYLEVEN".equals(mode.getCode())) {
			return new PaylevenPaymentProcessor(parentActivity, payment);
		}
		return null;
	}
}
