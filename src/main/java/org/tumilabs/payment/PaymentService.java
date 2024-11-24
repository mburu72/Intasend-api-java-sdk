package org.tumilabs.payment;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PaymentService handles interactions with the Intasend payment API.
 * It provides methods to initiate payments and handle responses.
 */
public class PaymentService {
    private static final Logger logger = Logger.getLogger(PaymentService.class.getName());

    private String api_key;
    public Boolean test;
    /**
     * Constructs a PaymentService object with the specified API key.
     *
     * @param api_key The API key used for authenticating requests to the payment API.
     */
    public PaymentService(String api_key, Boolean test_env){
        this.api_key = api_key;
        this.test = test_env;
    }


    /**
     * Initiates an MPESA STK push payment by sending a request to the Intasend API.
     * The payment initiation process involves providing an amount and a phone number.
     *
   * @return A message indicating the result of the payment initiation, e.g., "OK" for success.
     * @throws PaymentInitiationException If the payment initiation fails due to network errors or API response errors.
     */
    private String env(Boolean test_env){
        if (test_env){
            logger.setLevel(Level.FINE);
            logger.info("Running in test environment");
            return "https://sandbox.intasend.com/api/v1/payment/mpesa-stk-push/";
        }else {
            logger.info("Running in live environment");
            return "https://payment.intasend.com/api/v1/payment/mpesa-stk-push/";
        }
    }
    public String initiatePayment(String amount, String phoneNumber) throws PaymentInitiationException {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"amount\":\"" + amount + "\",\"phone_number\":\"" + phoneNumber + "\"}");
        Request request = new Request.Builder()
                .url(env(test))
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("Authorization", "Bearer " + api_key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.message();
            } else {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                String errorMessage = parseErrorResponse(errorBody);
                throw new PaymentInitiationException("API Error: " + errorMessage + " Response: " + errorBody);
            }
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

    /**
     * Parses the error response from the Intasend API and extracts the relevant error message.
     * The error message is extracted from the JSON response, which typically contains an array of error objects.
     *
     * @param errorBody The raw error response body returned by the API.
     * @return A string containing the extracted error message.
     */
    private String parseErrorResponse(String errorBody) {
        try {
            JSONObject errorJson = new JSONObject(errorBody);
            if (errorJson.has("errors")) {
                JSONArray errors = errorJson.getJSONArray("errors");
                if (errors.length() > 0) {
                    JSONObject firstError = errors.getJSONObject(0);
                    String code = firstError.optString("code", "unknown_error");  // Error code (default to "unknown_error")
                    String detail = firstError.optString("detail", "No details provided");  // Error details (default to "No details provided")
                    return String.format("Code: %s, Detail: %s", code, detail);
                }
            }
        } catch (Exception e) {
            return "Failed to parse error response: " + errorBody;
        }
        return "Unknown API error occurred";
    }
}
