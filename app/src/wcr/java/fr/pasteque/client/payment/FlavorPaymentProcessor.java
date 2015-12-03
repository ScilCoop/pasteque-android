package fr.pasteque.client.payment;

import fr.pasteque.client.models.Payment;
import fr.pasteque.client.utils.TrackedActivity;

public abstract class FlavorPaymentProcessor extends PaymentProcessor {

    protected FlavorPaymentProcessor(TrackedActivity parentActivity, PaymentListener listener, Payment payment) {
        super(parentActivity, listener, payment);
    }

}
