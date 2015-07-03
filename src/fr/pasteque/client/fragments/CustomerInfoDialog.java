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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.pasteque.client.Configure;
import fr.pasteque.client.Error;
import fr.pasteque.client.R;
import fr.pasteque.client.data.CustomerData;
import fr.pasteque.client.models.Customer;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.sync.SyncUtils;
import fr.pasteque.client.utils.TrackedActivity;
import fr.pasteque.client.utils.URLTextGetter;
import fr.pasteque.client.widgets.CustomerTicketHistoryAdapter;
import fr.pasteque.client.widgets.ProgressPopup;

public class CustomerInfoDialog extends DialogFragment
        implements View.OnClickListener {

    public static final String TAG = CustomerSelectDialog.class.getSimpleName();
    private static final String EDITABLE_ARG = "EDITABLE_ARG";
    private static final String CUSTOMER_ARG = "CUSTOMER_ARG";
    private static final int DATAHANDLER_CUSTOMER = 1;
    private static final int DATAHANDLER_HISTORY = 2;

    // Data
    private Context mCtx;
    private Listener mListener;
    private TrackedActivity mParentActivity;
    private Customer mNewCustomer;
    private boolean mbEditable;
    private boolean mbShowHistory;
    private Customer mCustomer;
    private List<Ticket> mHistoryData;
    private CustomerTicketHistoryAdapter mAdapter;
    // View
    private EditText mName;
    private EditText mZipCode;
    private EditText mPhone1;
    private EditText mMail;
    private EditText mDescription;
    private ProgressPopup mPopup;
    private ListView mTicketList;

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
        mHistoryData = new ArrayList<>();
        mbShowHistory = (Configure.getSyncMode(mCtx) == Configure.AUTO_SYNC_MODE
                && mCustomer != null);
        if (mbShowHistory) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Map<String, String> params = SyncUtils.initParams(mCtx, "TicketsAPI", "search");
                    params.put("customerId", mCustomer.getId());
                    URLTextGetter.getText(SyncUtils.apiUrl(mCtx), null, params,
                            new DataHandler(CustomerInfoDialog.this), DATAHANDLER_HISTORY);
                }
            }).run();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.customer_info, null);

        mName = (EditText) layout.findViewById(R.id.name);
        mName.setEnabled(mbEditable);
        mZipCode = (EditText) layout.findViewById(R.id.zip_code);
        mZipCode.setEnabled(mbEditable);
        mPhone1 = (EditText) layout.findViewById(R.id.phone);
        mPhone1.setEnabled(mbEditable);
        mMail = (EditText) layout.findViewById(R.id.email);
        mMail.setEnabled(mbEditable);
        mDescription = (EditText) layout.findViewById(R.id.description);
        mDescription.setEnabled(mbEditable);
        mAdapter = new CustomerTicketHistoryAdapter(mCtx, mHistoryData);
        //TODO: handle when empty list
        TextView tv = new TextView(mCtx);
        tv.setText(R.string.customerinfo_empty_history);
        mTicketList = (ListView) layout.findViewById(R.id.customer_ticket_history);
        //mTicketList.setEmptyView(tv);
        mTicketList.setAdapter(mAdapter);
        if (!mbShowHistory) {
            mTicketList.setVisibility(View.GONE);
            layout.findViewById(R.id.ticket_history_label).setVisibility(View.GONE);
            layout.findViewById(R.id.ticket_history_sep).setVisibility(View.GONE);
        }
        if (mCustomer != null) {
            mName.setText(mCustomer.getFirstName());
            mZipCode.setText(mCustomer.getZipCode());
            mPhone1.setText(mCustomer.getPhone1());
            mMail.setText(mCustomer.getMail());
        }

        Button positive = (Button) layout.findViewById(R.id.btn_positive);
        if (!mbEditable) {
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) positive.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            positive.setText(R.string.ok);
            positive.setLayoutParams(params);
        }
        positive.setOnClickListener(this);

        Button negative = (Button) layout.findViewById(R.id.btn_negative);
        if (!mbEditable) negative.setVisibility(View.GONE);
        negative.findViewById(R.id.btn_negative).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        Button capture = (Button) layout.findViewById(R.id.btn_capture);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Capture picture
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
    public void onStart() {
        super.onStart();
        int dialogWidth = (int) getResources().getDimension(R.dimen.customerInfoWidth);
        int dialogHeight = WindowManager.LayoutParams.WRAP_CONTENT;

        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
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
        String firstNameStr = mName.getText().toString();
        String lastNameStr = mName.getText().toString();
        String address1Str = "";
        String address2Str = "";
        String zipCodeStr = mZipCode.getText().toString();
        String cityStr = "";
        String departmentStr = "";
        String countryStr = "";
        String phone1Str = mPhone1.getText().toString();
        String phone2Str = "";
        String mailStr = mMail.getText().toString();
        String faxStr = "";
        if (lastNameStr.equals("") || firstNameStr.equals("")) {
            Toast.makeText(mCtx, getString(R.string.emptyField), Toast.LENGTH_SHORT).show();
        } else if (!mailStr.equals("") && !isEmailValid(mailStr)) {
            Toast.makeText(mCtx, getString(R.string.badMail), Toast.LENGTH_SHORT).show();
        } else if (!phone1Str.equals("") && !isPhoneValid(phone1Str)) {
            Toast.makeText(mCtx, getString(R.string.badPhone), Toast.LENGTH_SHORT).show();
        } else {
            //noinspection UnnecessaryLocalVariable
            String dispName = lastNameStr;
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
            URLTextGetter.getText(SyncUtils.apiUrl(mCtx), null, postBody,
                    new DataHandler(this), DATAHANDLER_CUSTOMER);
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

    private void parseHistory(JSONObject result) {
        try {
            JSONArray array = result.getJSONArray("content");
            int length = array.length();
            for (int i = 0; i < length; ++i) {
                JSONObject o = array.getJSONObject(i);
                mHistoryData.add(Ticket.fromJSON(mCtx, o));
            }
            //Todo: Remove this
            Toast.makeText(mCtx, "History updated", Toast.LENGTH_SHORT).show();
            mParentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing history", e);
            Error.showError(R.string.err_search_customer_history, mParentActivity);
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
                            showError(self, msg.arg1);
                        } else {
                            parseContent(self, msg.arg1, result);
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
                    break;
            }
        }

        private static void showError(CustomerInfoDialog self, int who) {
            switch (who) {
                case DATAHANDLER_CUSTOMER:
                    Error.showError(R.string.err_save_online_customer, self.mParentActivity);
                    break;
                case DATAHANDLER_HISTORY:
                    Error.showError(R.string.err_search_customer_history, self.mParentActivity);
                    break;
            }
        }

        private static void parseContent(CustomerInfoDialog self, int who, JSONObject result) {
            switch (who) {
                case DATAHANDLER_CUSTOMER:
                    self.parseCustomer(result);
                    break;
                case DATAHANDLER_HISTORY:
                    self.parseHistory(result);
                    break;
            }
        }
    }
}