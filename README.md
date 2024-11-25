  public void someFunction() throws PaymentInitiationException {
        //paymentservice takes two args: api_key and boolean false-if you are in live environment or true if you are using test environment
        PaymentService service = new PaymentService("your_intasend_api_secret_key", false);
        service.initiatePayment("amount", "phone_number making the payment in international format(254)");
    }
