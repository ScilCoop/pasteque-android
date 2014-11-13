package fr.pasteque.client;

import android.app.Activity;
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
import fr.pasteque.client.widgets.CustomersAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import fr.pasteque.client.sync.SyncSend;
/**
 * Created by jdelagorce on 09/11/2014.
 */
public class CustomerCreate extends Activity implements View.OnClickListener {
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
    private Handler listener;


    /*
    private ListView list;*/
    private Button registerCustomer = null;
    private boolean nullable;
    private Customer cust;
    private ListView list;


    private EditText lastName;
    private EditText firstName;
    private EditText address1;
    private EditText address2;
    private EditText zipCode;
    private EditText city;
    private EditText department;
    private EditText country;
    private EditText mail;
    private EditText phone1;
    private EditText phone2;
    private EditText fax;

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
                this.lastName = (EditText) this.findViewById(R.id.lastNameCustomer);
                String lastNameStr = this.lastName.getText().toString();
                this.firstName = (EditText) this.findViewById(R.id.firstNameCustomer);
                String firstNameStr = this.firstName.getText().toString();
                this.address1 = (EditText) this.findViewById(R.id.address1Customer);
                String address1Str = this.address1.getText().toString();
                this.address2 = (EditText) this.findViewById(R.id.address2Customer);
                String address2Str = this.address2.getText().toString();
                this.zipCode = (EditText) this.findViewById(R.id.zipCodeCustomer);
                String zipCodeStr = this.zipCode.getText().toString();
                this.city = (EditText) this.findViewById(R.id.cityCustomer);
                String cityStr = this.city.getText().toString();
                this.department = (EditText) this.findViewById(R.id.departmentsCustomer);
                String departmentStr = this.department.getText().toString();
                this.country = (EditText) this.findViewById(R.id.countryCustomer);
                String countryStr = this.country.getText().toString();
                this.phone1 = (EditText) this.findViewById(R.id.phoneCustomer);
                String phone1Str = this.phone1.getText().toString();
                this.phone2 = (EditText) this.findViewById(R.id.handPhoneCustomer);
                String phone2Str = this.phone2.getText().toString();
                this.mail = (EditText) this.findViewById(R.id.mailCustomer);
                String mailStr = this.mail.getText().toString();
                this.fax = (EditText) this.findViewById(R.id.faxCustomer);
                String faxStr = this.fax.getText().toString();

                if (lastNameStr.equals("") || firstNameStr.equals("")) {

                    Toast.makeText(context, this.getString(R.string.emptyField),
                            Toast.LENGTH_SHORT).show();
/*
                    newCustomer.toJSON();
*/
                } else {
                    if (!mailStr.equals("") && !isEmailValid(mailStr)) {
                        Toast.makeText(context, this.getString(R.string.badMail),
                                Toast.LENGTH_SHORT).show();
                    } else if (!phone1Str.equals("") && !isPhoneValid(phone1Str)) {
                        Toast.makeText(context, this.getString(R.string.badPhone),
                                Toast.LENGTH_SHORT).show();
                    } else if (!phone2Str.equals("") && !isPhoneValid(phone2Str)) {
                        Toast.makeText(context, this.getString(R.string.badPhone),
                                Toast.LENGTH_SHORT).show();
                    } else if (!faxStr.equals("") && !isPhoneValid(faxStr)) {
                        Toast.makeText(context, this.getString(R.string.badFax),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        String dispName = lastNameStr + " " + firstNameStr;
                        Customer newCustomer = new Customer("", dispName, lastNameStr, "", firstNameStr, address1Str,
                                address2Str, zipCodeStr, cityStr, departmentStr, countryStr,
                                phone1Str, phone2Str, mailStr, faxStr, 0.0, 0.0, 0.0);
                        Map<String, String> postBody = SyncUtils.initParams(this.context,
                                "CustomersAPI", "save");
                        postBody.put("customer", newCustomer.toString());
                       /* postBody.put("dispName", dispName);
                        postBody.put("card", JSONObject.NULL.toString());
                        postBody.put("addr1", this.address1.toString());
                        postBody.put("addr2", this.address2.toString());
                        postBody.put("zipCode", this.zipCode.toString());
                        postBody.put("city", this.city.toString());
                        postBody.put("region", this.department.toString());
                        postBody.put("country", this.country.toString());
                        postBody.put("email", this.mail.toString());
                        postBody.put("phone1", this.phone1.toString());
                        postBody.put("phone2", this.phone2.toString());
                        postBody.put("fax", this.fax.toString());
                        postBody.put("prepaid", JSONObject.NULL.toString());
                        postBody.put("maxDebt", JSONObject.NULL.toString());
                        postBody.put("currDebt", JSONObject.NULL.toString()); */
                        URLTextGetter.getText(SyncUtils.apiUrl(this.context), null,
                                postBody, new DataHandler(DataHandler.TYPE_RECEIPTS));
                    }
                }


                break;
        }

    }
    private void parseCustomer(JSONObject resp) {
        try {
            JSONObject o = resp.getJSONObject("content");
            Customer customer = Customer.fromJSON(o);
            // Update our cash for tickets (maybe id is set)
            this.cust = customer;
            SyncUtils.notifyListener(this.listener, CUSTOMER_SYNC_DONE, customer);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error while parsing customer result", e);
            SyncUtils.notifyListener(this.listener, CUSTOMER_SYNC_FAILED, resp);
            return;
        }
    }
    private class DataHandler extends Handler {

        private static final int TYPE_RECEIPTS = 1;
        private static final int TYPE_CASH = 2;
        private Handler listener;

        private int type;

        public DataHandler(int type) {
            this.type = type;
        }

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
                            SyncUtils.notifyListener(listener, SYNC_ERROR, error);
                            finish();
                        } else {
                            parseCustomer(result);
                        }
                    } catch (JSONException e) {
                        SyncUtils.notifyListener(listener, SYNC_ERROR, content);
                        finish();
                    }
                    break;
                case URLTextGetter.ERROR:
                    Log.e(LOG_TAG, "URLTextGetter error", (Exception) msg.obj);
                case URLTextGetter.STATUS_NOK:
                    SyncUtils.notifyListener(listener, CONNECTION_FAILED, msg.obj);
                    finish();
                    return;
            }
        }




    }

}

