package org.tumixlabs.payment;

import java.io.IOException;

public class PaymentInitiationException extends IOException {
public PaymentInitiationException(String message){
    super(message);
}
}
