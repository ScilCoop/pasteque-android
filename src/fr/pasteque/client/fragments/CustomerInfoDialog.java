package fr.pasteque.client.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;

import fr.pasteque.client.*;
import fr.pasteque.client.Error;
import fr.pasteque.client.data.CustomerData;
import fr.pasteque.client.models.Customer;
import fr.pasteque.client.sync.SyncUtils;
import fr.pasteque.client.utils.TrackedActivity;
import fr.pasteque.client.utils.URLTextGetter;
import fr.pasteque.client.widgets.ProgressPopup;

public class CustomerInfoDialog extends DialogFragment
        implements View.OnClickListener {

    public static final String TAG = CustomerSelectDialog.class.getSimpleName();
    private static final String EDITABLE_ARG = "EDITABLE_ARG";
    private static final String CUSTOMER_ARG = "CUSTOMER_ARG";

    // Data
    private Context mCtx;
    private Listener mListener;
    private TrackedActivity mParentActivity;
    private Customer mNewCustomer;
    private boolean mbEditable;
    private Customer mCustomer;
    // View
    private EditText mLastName;
    private EditText mFirstName;
    private EditText mAddress1;
    private EditText mAddress2;
    private EditText mZipCode;
    private EditText mCountry;
    private EditText mCity;
    private EditText mPhone1;
    private EditText mMail;
    private ProgressPopup mPopup;

    public interface Listener {
        void onCustomerCreated(Customer customer);
    }

    public static CustomerInfoDialog newInstance(boolean editable, @Nullable Customer c) {
        Bundle args = new Bundle();
        args.putBoolean(EDITABLE_ARG, editable);
        args.putSerializable(CUSTOMER_ARG, c);
        CustomerInfoDialog dial = new CustomerInfoDialog();
        dial.setArguments(args);
        return dial;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCtx = getActivity();
        mCustomer = (Customer) getArguments().getSerializable(CUSTOMER_ARG);
        mbEditable = getArguments().getBoolean(EDITABLE_ARG);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.customer_info, null);

        mFirstName = (EditText) layout.findViewById(R.id.first_name);
        mFirstName.setEnabled(mbEditable);
        mLastName = (EditText) layout.findViewById(R.id.last_name);
        mLastName.setEnabled(mbEditable);
        mAddress1 = (EditText) layout.findViewById(R.id.address1);
        mAddress1.setEnabled(mbEditable);
        mAddress2 = (EditText) layout.findViewById(R.id.address2);
        mAddress2.setEnabled(mbEditable);
        mZipCode = (EditText) layout.findViewById(R.id.zip_code);
        mZipCode.setEnabled(mbEditable);
        mCity = (EditText) layout.findViewById(R.id.city);
        mCity.setEnabled(mbEditable);
        mCountry = (EditText) layout.findViewById(R.id.country);
        mCountry.setEnabled(mbEditable);
        mPhone1 = (EditText) layout.findViewById(R.id.phone);
        mPhone1.setEnabled(mbEditable);
        mMail = (EditText) layout.findViewById(R.id.email);
        mMail.setEnabled(mbEditable);

        if (mCustomer != null) {
            mFirstName.setText(mCustomer.getFirstName());
            mLastName.setText(mCustomer.getLastName());
            mAddress1.setText(mCustomer.getAddress1());
            mAddress2.setText(mCustomer.getAddress2());
            mZipCode.setText(mCustomer.getZipCode());
            mCity.setText(mCustomer.getCity());
            mCountry.setText(mCustomer.getCountry());
            mPhone1.setText(mCustomer.getPhone1());
            mMail.setText(mCustomer.getMail());
        }

        Button positive = (Button) layout.findViewById(R.id.btn_positive);
        if (!mbEditable) positive.setText(R.string.ok);
        positive.setOnClickListener(this);

        Button negative = (Button) layout.findViewById(R.id.btn_negative);
        if (!mbEditable) negative.setVisibility(View.GONE);
        negative.findViewById(R.id.btn_negative).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return layout;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dial = super.onCreateDialog(savedInstanceState);
        dial.setCanceledOnTouchOutside(true);
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dial;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mParentActivity = (TrackedActivity) activity;
        } catch (ClassCastException e) {
            throw new RuntimeException(TAG + "parent activity must extend TrackedActivity");
        }
    }

    public void setDialogListener(Listener listener) {
        mListener = listener;
    }

    public void show(FragmentManager manager) {
        show(manager, TAG);
    }

    @Override
    public void onClick(View v) {
        //TODO: Handle customer edition
        if (!mbEditable) {
            getDialog().dismiss();
            return;
        }
        String lastNameStr = mLastName.getText().toString();
        String firstNameStr = mFirstName.getText().toString();
        String address1Str = mAddress1.getText().toString();
        String address2Str = mAddress2.getText().toString();
        String zipCodeStr = mZipCode.getText().toString();
        String cityStr = mCity.getText().toString();
        String departmentStr = null;
        String countryStr = mCountry.getText().toString();
        String phone1Str = mPhone1.getText().toString();
        String phone2Str = null;
        String mailStr = mMail.getText().toString();
        String faxStr = null;
        if (lastNameStr.equals("") || firstNameStr.equals("")) {
            Toast.makeText(mCtx, getString(R.string.emptyField), Toast.LENGTH_SHORT).show();
        } else if (!mailStr.equals("") && !isEmailValid(mailStr)) {
            Toast.makeText(mCtx, getString(R.string.badMail), Toast.LENGTH_SHORT).show();
        } else if (!phone1Str.equals("") && !isPhoneValid(phone1Str)) {
            Toast.makeText(mCtx, getString(R.string.badPhone), Toast.LENGTH_SHORT).show();
        } else {
            String dispName = lastNameStr + " " + firstNameStr;

            Customer c = new Customer(null, dispName, "",
                    firstNameStr, lastNameStr, address1Str,
                    address2Str, zipCodeStr, cityStr, departmentStr,
                    countryStr, mailStr, phone1Str, phone2Str, faxStr,
                    0.0, 0.0, 0.0, "0");
            if (Configure.getSyncMode(mCtx) == Configure.AUTO_SYNC_MODE) {
                uploadCustomer(c);
            } else {
                storeLocalCustomer(c);
            }
        }
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPhoneValid(CharSequence phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    private void storeLocalCustomer(Customer c) {
        // Generates local temp unique id
        c.setId("new customer:" + UUID.randomUUID().toString());
        CustomerData.addCreatedCustomer(c);
        try {
            CustomerData.save(mCtx);
        } catch (IOException ioe) {
            Log.w(TAG, "Unable to save customers");
            Error.showError(getString(R.string.err_save_local_customer), mParentActivity);
        }
        if (mListener != null) {
            mListener.onCustomerCreated(c);
        }
        getDialog().dismiss();
    }

    /**
     * Uploads client to server
     * Called in sync mode
     *
     * @param c is the customer to upload
     */
    private void uploadCustomer(Customer c) {
        Map<String, String> postBody = SyncUtils.initParams(mCtx, "CustomersAPI", "save");
        try {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(c.toJSON());
            postBody.put("customer", jsonArray.toString());
            URLTextGetter.getText(SyncUtils.apiUrl(mCtx), null, postBody, new DataHandler(this));
            mPopup = new ProgressPopup(mCtx);
            mPopup.setIndeterminate(true);
            mPopup.setMessage(getString(R.string.saving_customer_message));
            mPopup.show();
            mNewCustomer = c;
        } catch (JSONException e) {
            Log.e(TAG, "Unable to json new customer", e);
            Error.showError(R.string.err_save_online_customer, mParentActivity);
        }
    }

    private void parseCustomer(JSONObject resp) {
        try {
            JSONObject o = resp.getJSONObject("content");
            JSONArray ids = o.getJSONArray("saved");
            String id = ids.getString(0);
            mNewCustomer.setId(id);
            // Update local customer list
            CustomerData.customers.add(mNewCustomer);
            try {
                CustomerData.save(mCtx);
            } catch (IOException ioe) {
                Log.e(TAG, "Unable to save customers");
                Error.showError(R.string.err_save_local_customer, mParentActivity);
            }
            if (mListener != null) {
                mListener.onCustomerCreated(mNewCustomer);
            }
            mNewCustomer = null;
            getDialog().dismiss();
        } catch (JSONException e) {
            Log.e(TAG, "Error while parsing customer result", e);
            Error.showError(R.string.err_save_local_customer, mParentActivity);
        }
    }

    private static class DataHandler extends Handler {
        WeakReference<CustomerInfoDialog> mSelfRef;

        public DataHandler(CustomerInfoDialog self) {
            mSelfRef = new WeakReference<>(self);
        }

        @Override
        public void handleMessage(Message msg) {
            CustomerInfoDialog self = mSelfRef.get();

            if (self == null) return;
            if (self.mPopup != null) {
                self.mPopup.dismiss();
                self.mPopup = null;
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
                            Log.i(TAG, "Server error " + error);
                            Error.showError(R.string.err_save_online_customer, self.mParentActivity);
                        } else {
                            self.parseCustomer(result);
                        }
                    } catch (JSONException e) {
                        Log.w(TAG, "Json error: " + content, e);
                        Error.showError(R.string.err_json_read, self.mParentActivity);
                    }
                    break;
                case URLTextGetter.ERROR:
                    Log.e(TAG, "URLTextGetter error", (Exception) msg.obj);
                    Error.showError(R.string.err_server_error, self.mParentActivity);
                    break;
                case URLTextGetter.STATUS_NOK:
                    Log.e(TAG, "URLTextGetter nok", (Exception) msg.obj);
                    Error.showError(R.string.err_server_error, self.mParentActivity);
            }
        }
    }
}
