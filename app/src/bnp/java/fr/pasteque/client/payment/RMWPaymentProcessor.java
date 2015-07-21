package fr.pasteque.client.payment;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import fr.pasteque.client.R;
import fr.pasteque.client.models.Payment;
import fr.pasteque.client.payment.rmw.RmwSession;
import fr.pasteque.client.utils.TrackedActivity;

/**
 * This PaymentProcessor behaves as a Merchant Server from the 1st scenario point of view.
 *
 */
public class RMWPaymentProcessor extends FlavorPaymentProcessor {
	private RmwSession session;
    private ProgressDialog paymentDialog;
	
	protected RMWPaymentProcessor(TrackedActivity parentActivity,
			PaymentListener listener, Payment payment) {
		super(parentActivity, listener, payment);

		//TODO: As you can guess, null is a WRONG value...
		//(String username, String password, String consumerKey, String consumerSecret, String enterpriseCode, 
		//String merchantName, String pspMerchantId, String psp, String mcc)
		session = new RmwSession("RMWSP/Atos WL", "TestRMW!?1", "34sdg8m7DoXrnBufMlNOMhuYPkIa", "IRri_Jnh7c6nwoKdDOg0mQH0Auca", 
		        "464_entc_745", "Atos cash register", "4148775", "02", "7829");
		paymentDialog = new ProgressDialog(parentActivity);
	}

	@Override
	public void handleIntent(int requestCode, int resultCode, Intent data) {
		// Nothing to do here, communication is done sync in initiatePayment
	}

	@Override
	public Status initiatePayment() {
	    // Request the customer subscriberCode...
	    AlertDialog dialog;
	    
	    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(parentActivity);
	    dialogBuilder.setTitle("RMW Payment");
	    dialogBuilder.setMessage("Please fill in your subscriber code or email");
	    final EditText loginView = new EditText(parentActivity);
	    dialogBuilder.setView(loginView);
	    
	    dialogBuilder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Start playing with RMW...
                String login = loginView.getText().toString();
                new RMWTask().execute(login);
                paymentDialog.setCancelable(false);
                paymentDialog.setMessage(parentActivity.getString(R.string.card_payment_startup));
                paymentDialog.show();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Nothing to do here
            }
        });

	    dialog = dialogBuilder.create();
	    dialog.show();
	    
		return Status.PENDING;
	}
	
	private static final String LOG_TAG = "Pasteque/RMW";
	
	private class RMWTask extends AsyncTask<String, Void, Void> {

	    private String errorMessage = null;
	    private boolean paymentSuccess = false;
	    private String statusMessage;
	    
	    @Override
	    protected void onPostExecute(Void values) {
	        if (errorMessage != null)
                Toast.makeText(parentActivity, errorMessage, Toast.LENGTH_SHORT).show();
	        if (paymentSuccess)
                listener.registerPayment(payment);
	        paymentDialog.dismiss();
	    }
	    
	    @Override
	    protected void onProgressUpdate(Void... values) {
	        paymentDialog.setMessage(statusMessage);
	    }
	    
        @Override
        protected Void doInBackground(String... params) {
            String login = params[0];
            JSONObject result = null;
            Log.e(LOG_TAG, "Hello RMW");
            try {
                statusMessage = "Checking available cards...";
                publishProgress();
                if (login.contains("@"))
                    result = session.getCardAssigned(login, null);
                else
                    result = session.getCardAssigned(null, login);
                
                Log.e(LOG_TAG, result.toString());   
                
                if (result != null && "3000".equals(result.getString("resultCode"))) {
                    
                    JSONArray cards = result.getJSONArray("getCardAssignedList");
                    if (cards.length() == 0) {
                        Log.e(LOG_TAG, "No card assigned !");
                        // What ? is that possible ?
                    }
                    JSONObject card = cards.getJSONObject(0);
                    
                    String subscriberCode = card.getString("subscriberCode");
                    
                    // Request the payment then !
                    statusMessage = "Requesting transaction...";
                    publishProgress();
                    Log.e(LOG_TAG, "Calling closeTransaction");
                    JSONObject transactionResult = session.closeTransactionWithCreditCard(subscriberCode, String.valueOf(payment.getInnerId()), payment.getAmount(), payment.getCurrency());
                    Log.e(LOG_TAG, transactionResult.toString());
                    if (transactionResult != null && "0000".equals(transactionResult.getString("resultCode"))) {
                        String transactionId = transactionResult.getString("transactionId");
                        statusMessage = "Confirming transaction...";
                        publishProgress();
                        Log.e(LOG_TAG, "Calling merchantConfirmation");
                        JSONObject confirmResult = session.merchantConfirmation(subscriberCode, transactionId, true);
                        Log.e(LOG_TAG, confirmResult.toString());
                        if (confirmResult != null && "0000".equals(confirmResult.getString("resultCode"))) {
                            paymentSuccess = true;
                        } else {
                            errorMessage = result.getString("resultDescription");
                        }
                    } else {
                        errorMessage = result.getString("resultDescription");
                    }
                } else {
                    Log.e(LOG_TAG, "Got an error in getCardAssigned ?");
                    errorMessage = result.getString("resultDescription");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
	}
}
