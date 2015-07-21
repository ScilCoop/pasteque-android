package fr.pasteque.client.payment.rmw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.JsonWriter;
import android.util.Log;
import fr.pasteque.client.utils.Base64;

public class RmwSession {
    // Local parameters : enterpriseCode, username, password ?
    // consumer key and consumer secret, someone ?
    
    private static final String LOG_TAG = "Pasteque/RmwSession";
    private static final String AUTH_URL = "https://e2e.rmw.mms.accenture.com/sso/token";
    private static final String MOBILE_WS_URL = "https://e2e.rmw.mms.accenture.com/pay-mlv/1.0/";
    private static final String PAY_WS_URL = "https://e2e.rmw.mms.accenture.com/pay-tm/1.0/";

    private Calendar tokenExpiry;
    private String token;
    private String consumerKey;
    private String consumerSecret;
    private String enterpriseCode;
    private String username;
    private String password;
    private String pspMerchantId;
    private String psp; // Who knows…
    private String mcc;
    private String merchantName;
    
    public RmwSession(String username, String password, String consumerKey, String consumerSecret, String enterpriseCode, String merchantName, String pspMerchantId, String psp, String mcc) {
        this.username = username;
        this.password = password;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.enterpriseCode = enterpriseCode;
        this.merchantName = merchantName;
        this.pspMerchantId = pspMerchantId;
        this.psp = psp;
        this.mcc = mcc;
    }
    
    private String getAuthorizationBasicHeader() {
        return "Basic " + Base64.encodeBytes((consumerKey + ":" + consumerSecret).getBytes());
    }
    
    private void getToken() {
        // Build a new http request
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(AUTH_URL);
        
        try {
            post.setHeader("Authorization", this.getAuthorizationBasicHeader());
            List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>(3);
            parameters.add(new BasicNameValuePair("grant_type", "password"));
            parameters.add(new BasicNameValuePair("username", username));
            parameters.add(new BasicNameValuePair("password", password));
            post.setEntity(new UrlEncodedFormEntity(parameters));
        
            HttpResponse response = client.execute(post);
            JSONObject json = parseAnswer(response);
            
            Log.e(LOG_TAG, "Got a token ? " + json.toString());
            this.token = json.getString("access_token");
            this.tokenExpiry = Calendar.getInstance();
            tokenExpiry.roll(Calendar.SECOND, json.getInt("expires_in"));
            
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private static JSONObject parseAnswer(HttpResponse response) throws IOException, JSONException {

        // UGLY CODE FOR JSON PARSING
        
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String allJson = rd.readLine();
        String line = rd.readLine();
        while (line != null) {
            allJson = allJson + line;
            line = rd.readLine();
        }
        
        return new JSONObject(allJson);
    }
    
    private void signQuery(HttpRequest request) {
        if (this.token == null || this.tokenExpiry.before(Calendar.getInstance())) {
            this.getToken();
        }
        
        request.setHeader("Authorization", "Bearer " + this.token);
    }
    
    // get token (username, password) ==> token
    // warning : check token life...
    
    // Returning a JSONObject so far, can't do better yet
    public JSONObject getCardAssigned(String email, String subscriberCode) throws IOException, JSONException {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(MOBILE_WS_URL + "coupon/retrieve-assigned");

        signQuery(post);
        
        // Build the JSON query
        StringWriter targetString = new StringWriter();
        JsonWriter writer = new JsonWriter(targetString);
        writer.beginObject();
        if (subscriberCode != null && !"".equals(subscriberCode))
            writer.name("subscriberCode").value(subscriberCode);
        if (email != null && !"".equals(email))
            writer.name("eMail").value(email);
        writer.name("enterpriseCode").value(enterpriseCode);
        writer.name("statusList").beginObject().name("statusName").beginArray().value("ASSIGNED").value("RECEIVED").endArray().endObject();
        writer.endObject();

        writer.close();
        
        Log.e(LOG_TAG, PAY_WS_URL + "coupon/retrieve-assigned");
        Log.e(LOG_TAG, targetString.toString());
        Log.e(LOG_TAG, this.token);
        
        StringEntity targetEntity = new StringEntity(targetString.toString()); 
        targetEntity.setContentType("application/json");
        post.setEntity(targetEntity);
        
        HttpResponse response = client.execute(post);

        JSONObject json = parseAnswer(response);
        
        return json;
    }
    
    public JSONObject closeTransactionWithCreditCard(String subscriberCode, String orderId, double amount, Currency currency) throws IOException, JSONException {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(PAY_WS_URL + "transaction/closeTransactionWithCreditCard");

        signQuery(post);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd+HH:mm", Locale.FRANCE);
        
        // Build the JSON query
        StringWriter targetString = new StringWriter();
        JsonWriter writer = new JsonWriter(targetString);
        writer.beginObject();
        writer.name("subscriberCode").value(subscriberCode);
        writer.name("enterpriseCode").value(enterpriseCode);
        writer.name("merchantName").value(merchantName);
        writer.name("mcc").value(mcc);
        writer.name("amountNet").value(String.valueOf(Math.round(amount * 100))).name("exponentNet").value("2");
        writer.name("currencyNet").value(currency.getCurrencyCode());
        writer.name("amountFull").value(String.valueOf(Math.round(amount * 100))).name("exponentFull").value("2");
        writer.name("currencyFull").value(currency.getCurrencyCode());
        writer.name("operationType").value("CAP");
        writer.name("purchaseDate").value(dateFormat.format(Calendar.getInstance().getTime()));
        writer.name("channelId").value("03");   /* Proximity */
        writer.name("orderId").value(orderId);
        writer.name("pspMerchantId").value(pspMerchantId);
        writer.name("pspId").value(psp);
        writer.endObject();

        writer.close();
        
        Log.e(LOG_TAG, PAY_WS_URL + "transaction/closeTransactionWithCreditCard");
        Log.e(LOG_TAG, targetString.toString());
        Log.e(LOG_TAG, this.token);
        
        StringEntity targetEntity = new StringEntity(targetString.toString()); 
        targetEntity.setContentType("application/json");
        post.setEntity(targetEntity);

        HttpResponse response = client.execute(post);

        JSONObject json = parseAnswer(response);
        
        return json;
    }
    
    // merchantConfirmation (bearerToken, transactionId, subscriberCode, 
    // enterpriseCode, tillFeedback
    
    public JSONObject merchantConfirmation(String subscriberCode, String transactionId, boolean tillFeedback) throws IOException, JSONException {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(PAY_WS_URL + "payment/merchantConfirmation");

        signQuery(post);
        
        // Build the JSON query
        StringWriter targetString = new StringWriter();
        JsonWriter writer = new JsonWriter(targetString);
        writer.beginObject();
        writer.name("subscriberCode").value(subscriberCode);
        writer.name("enterpriseCode").value(enterpriseCode);
        writer.name("transactionId").value(transactionId);
        writer.name("tillFeedback").value(tillFeedback ? "Y" : "N");

        writer.endObject();
        
        writer.close();
        
        StringEntity targetEntity = new StringEntity(targetString.toString()); 
        targetEntity.setContentType("application/json");
        post.setEntity(targetEntity);

        HttpResponse response = client.execute(post);

        JSONObject json = parseAnswer(response);
        
        return json;
    }

    
}
