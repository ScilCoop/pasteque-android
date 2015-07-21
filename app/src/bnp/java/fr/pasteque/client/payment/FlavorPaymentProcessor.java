package fr.pasteque.client.payment;

import fr.pasteque.client.models.Payment;
import fr.pasteque.client.utils.TrackedActivity;

public abstract class FlavorPaymentProcessor extends PaymentProcessor {

    protected FlavorPaymentProcessor(TrackedActivity parentActivity, PaymentListener listener, Payment payment) {
        super(parentActivity, listener, payment);
    }

    public static PaymentProcessor getProcessor(TrackedActivity parentActivity, PaymentListener listener, Payment payment) {
        PaymentProcessor processor = PaymentProcessor.getProcessor(parentActivity, listener, payment);
		if (processor == null && "RMW".equals(payment.getMode().getCode())) {
            return new RMWPaymentProcessor(parentActivity, listener, payment);
        }
        return processor;
    }
}
