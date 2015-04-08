package fr.pasteque.client;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Button;
import android.widget.Toast;
import android.util.Patterns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.pasteque.client.data.CustomerData;
import fr.pasteque.client.data.SessionData;
import fr.pasteque.client.models.Cash;
import fr.pasteque.client.models.Customer;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.User;
import fr.pasteque.client.sync.SyncUtils;
import fr.pasteque.client.utils.URLTextGetter;
import fr.pasteque.client.utils.TrackedActivity;
import fr.pasteque.client.widgets.CustomersAdapter;
import fr.pasteque.client.widgets.ProgressPopup;
import fr.pasteque.client.sync.SyncSend;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomerCreate extends TrackedActivity implements View.OnClickListener {
    private static final String LOG_TAG = "Pasteque/CustomerSelect";
    public static final int CODE_CUSTOMER = 3;

    public static void setup(boolean nullable) {
        nullableInitializer = nullable;
    }

    private static boolean nullableInitializer;
    private final Context context = this;

    public static final int SYNC_ERROR = -8;
    public static final int CONNECTION_FAILED = -2;
    public static final int CUSTOMER_SYNC_DONE = -5;
    public static final int CUSTOMER_SYNC_FAILED = -6;

    private Button registerCustomer = null;
    private boolean nullable;
    private ListView list;
    private ProgressPopup syncPopup;

    private EditText lastName;
    private EditText firstName;
    private EditText address1;
    private EditText address2;
    private EditText zipCode;
    private EditText city;
    private EditText country;
    private EditText mail;
    private EditText phone1;
    private EditText phone2;
    private Customer newCustomer;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if (state != null) {
            this.nullable = state.getBoolean("nullable");
        } else {
            this.nullable = nullableInitializer;
        }
        setContentView(R.layout.customer_create);
        registerCustomer = (Button) findViewById(R.id.RegisterCustomer);
        registerCustomer.setOnClickListener(this);
        this.lastName = (EditText) this.findViewById(R.id.lastNameCustomer);
        this.firstName = (EditText) this.findViewById(R.id.firstNameCustomer);
        this.address1 = (EditText) this.findViewById(R.id.address1Customer);
        this.address2 = (EditText) this.findViewById(R.id.address2Customer);
        this.zipCode = (EditText) this.findViewById(R.id.zipCodeCustomer);
        this.city = (EditText) this.findViewById(R.id.cityCustomer);
        this.country = (EditText) this.findViewById(R.id.countryCustomer);
        this.phone1 = (EditText) this.findViewById(R.id.phoneCustomer);
        this.phone2 = (EditText) this.findViewById(R.id.handPhoneCustomer);
        this.mail = (EditText) this.findViewById(R.id.mailCustomer);
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    boolean isPhoneValid(CharSequence phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
        case R.id.RegisterCustomer:
            String lastNameStr = this.lastName.getText().toString();
            String firstNameStr = this.firstName.getText().toString();
            String address1Str = this.address1.getText().toString();
            String address2Str = this.address2.getText().toString();
            String zipCodeStr = this.zipCode.getText().toString();
            String cityStr = this.city.getText().toString();
            String departmentStr = null;
            String countryStr = this.country.getText().toString();
            String phone1Str = this.phone1.getText().toString();
            String phone2Str = this.phone2.getText().toString();
            String mailStr = this.mail.getText().toString();
            String faxStr = null;
            if (lastNameStr.equals("") || firstNameStr.equals("")) {
                Toast.makeText(context, this.getString(R.string.emptyField),
                        Toast.LENGTH_SHORT).show();
            } else if (!mailStr.equals("") && !isEmailValid(mailStr)) {
                Toast.makeText(context, this.getString(R.string.badMail),
                        Toast.LENGTH_SHORT).show();
            } else if (!phone1Str.equals("") && !isPhoneValid(phone1Str)) {
                Toast.makeText(context, this.getString(R.string.badPhone),
                        Toast.LENGTH_SHORT).show();
            } else if (!phone2Str.equals("") && !isPhoneValid(phone2Str)) {
                Toast.makeText(context, this.getString(R.string.badPhone),
                        Toast.LENGTH_SHORT).show();
            } else {
                String dispName = lastNameStr + " " + firstNameStr;
                this.newCustomer = new Customer(null, dispName,
                        lastNameStr, "", firstNameStr, address1Str,
                        address2Str, zipCodeStr, cityStr, departmentStr,
                        countryStr, phone1Str, phone2Str, mailStr, faxStr,
                        0.0, 0.0, 0.0, "0");
                Map<String, String> postBody = SyncUtils.initParams(this,
                        "CustomersAPI", "save");
                // Feel the magic (uncomment for real code)
                JSONObject resp = new JSONObject();
                JSONObject content = new JSONObject();
                JSONArray ids = new JSONArray();
                try {
                    ids.put("newCustomer" + String.valueOf(Math.random()));
                    content.put("saved", ids);
                    resp.put("content", content);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                this.parseCustomer(resp);
                /*try {
                    postBody.put("customer", newCustomer.toJSON().toString());
                    URLTextGetter.getText(SyncUtils.apiUrl(this), null,
                            postBody, new DataHandler());
                    this.syncPopup = new ProgressPopup(this);
                    this.syncPopup.setIndeterminate(true);
                    this.syncPopup.setMessage(this.getString(R.string.saving_customer_message));
                    this.syncPopup.show();
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Unable to jsonify new customer", e);
                    // TODO: feedback
                    }*/
            }
            break;
        }
    }

    private void parseCustomer(JSONObject resp) {
        try {
            JSONObject o = resp.getJSONObject("content");
            JSONArray ids = o.getJSONArray("saved");
            String id = ids.getString(0);
            this.newCustomer.setId(id);
            // Update local customer list
            CustomerData.customers.add(this.newCustomer);
            try {
                CustomerData.save(this);
            } catch (IOException ioe) {
                Log.w(LOG_TAG, "Unable to save customers");
                // TODO: error feedback
            }
            // Assign current ticket to new customer and return
            SessionData.currentSession(this).getCurrentTicket().setCustomer(this.newCustomer);
            this.finish();
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error while parsing customer result", e);
            return;
        }
    }
    private class DataHandler extends Handler {

        private Handler listener;

        private String getError(String response) {
            try {
                JSONObject o = new JSONObject(response);
                if (o.has("error")) {
                    return o.getString("error");
                }
            } catch (JSONException e) {
            }
            return null;
        }

        @Override
        public void handleMessage(Message msg) {
            if (CustomerCreate.this.syncPopup != null) {
                CustomerCreate.this.syncPopup.dismiss();
                CustomerCreate.this.syncPopup = null;
            }
            switch (msg.what) {
            case URLTextGetter.SUCCESS:
                // Parse content
                String content = (String) msg.obj;
                try {
                    JSONObject result = new JSONObject(content);
                    String status = result.getString("status");
                    if (!status.equals("ok")) {
                        JSONObject err = result.getJSONObject("content");
                        String error = err.getString("code");
                        Log.i(LOG_TAG, "Server error " + error);
                        Error.showError(R.string.err_server_error,
                                CustomerCreate.this);
                    } else {
                        parseCustomer(result);
                    }
                } catch (JSONException e) {
                    Log.w(LOG_TAG, "Server response error: received " + content,
                            e);
                    Error.showError(R.string.err_server_error,
                            CustomerCreate.this);
                }
                break;
            case URLTextGetter.ERROR:
                Log.e(LOG_TAG, "URLTextGetter error", (Exception) msg.obj);
            case URLTextGetter.STATUS_NOK:
                Error.showError(R.string.err_server_error, CustomerCreate.this);
                return;
            }
        }
    }

}
