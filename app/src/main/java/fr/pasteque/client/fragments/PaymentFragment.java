package fr.pasteque.client.fragments;

import java.io.IOError;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import fr.pasteque.client.data.Data;
import fr.pasteque.client.utils.Error;
import fr.pasteque.client.interfaces.PaymentEditListener;
import fr.pasteque.client.R;
import fr.pasteque.client.models.Customer;
import fr.pasteque.client.models.Payment;
import fr.pasteque.client.models.PaymentMode;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.payment.FlavorPaymentProcessor;
import fr.pasteque.client.payment.PaymentProcessor;
import fr.pasteque.client.payment.PaymentProcessor.Status;
import fr.pasteque.client.activities.TrackedActivity;
import fr.pasteque.client.widgets.NumKeyboard;
import fr.pasteque.client.widgets.PaymentModeItem;
import fr.pasteque.client.widgets.PaymentModesAdapter;
import fr.pasteque.client.widgets.PaymentsAdapter;

public class PaymentFragment extends ViewPageFragment
        implements PaymentEditListener,
        Handler.Callback {

    public interface Listener {
        void onPfPrintReceipt(Receipt r);

        void onPfCustomerListClick();

        Receipt onPfSaveReceipt(ArrayList<Payment> p);

        void onPfFinished();
    }

    private static final String LOG_TAG = "Pasteque/PayFrag";

    // Serialize string
    private static final String PAYMENT_STATE = "payments";
    private static final String OPEN_STATE = "open";
    private static final String TOTAL_PRICE_STATE = "price";
    private static final String CUSTOMER_STATE = "current_customer";

    private Listener mListener;
    // Data
    private boolean mbIsCashDrawerOpen;
    private PaymentMode mCurrentMode;
    private ArrayList<Payment> mPaymentsListContent;
    private double mTotalPrice;
    private Customer mCustomer;
    private double mTicketPrepaid;
    // Views
    private Gallery mPaymentModes;
    private EditText mInput;
    private NumKeyboard mNumberPad;
    private ListView mPaymentsList;
    private TextView mRemaining;
    private TextView mGiveBack;
    private LinearLayout mCusInfo;
    private TextView mCusPrepaid;
    private TextView mCusDebt;
    private TextView mCusDebtMax;

    private PaymentProcessor mCurrentProcessor;

    @SuppressWarnings("unused") // Used via class reflection
    public static PaymentFragment newInstance(int pageNumber) {
        PaymentFragment frag = new PaymentFragment();
        ViewPageFragment.initPageNumber(pageNumber, frag);
        return frag;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        if (mCurrentProcessor != null)
            mCurrentProcessor.handleIntent(requestCode, resultCode, data);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PaymentFragment Listener!");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reuseData(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.payment_zone, container, false);
        mPaymentModes = (Gallery) layout.findViewById(R.id.payment_modes);
        List<PaymentMode> modes = Data.PaymentMode.paymentModes(mContext);
        mPaymentModes.setAdapter(new PaymentModesAdapter(modes));
        mPaymentModes.setOnItemSelectedListener(new PaymentModeItemSelectedListener());
        mPaymentModes.setSelection(0, false);
        mCurrentMode = modes.get(0);

        mInput = (EditText) layout.findViewById(R.id.input);
        mInput.setInputType(InputType.TYPE_NULL); // Should be TextView.
        mNumberPad = (NumKeyboard) layout.findViewById(R.id.numkeyboard);
        mNumberPad.setKeyHandler(new Handler(this));

        mPaymentsList = (ListView) layout.findViewById(R.id.payments_list);
        mPaymentsList.setAdapter(new PaymentsAdapter(mPaymentsListContent, this));

        mRemaining = (TextView) layout.findViewById(R.id.ticket_remaining);
        mGiveBack = (TextView) layout.findViewById(R.id.give_back);

        mCusInfo = (LinearLayout) layout.findViewById(R.id.user_characteristic);
        mCusPrepaid = (TextView) layout.findViewById(R.id.custPrepaidAmount);
        mCusDebt = (TextView) layout.findViewById(R.id.currentDebt);
        mCusDebtMax = (TextView) layout.findViewById(R.id.mountMax);

        LinearLayout customerList = (LinearLayout) layout.findViewById(R.id.customers_list);
        customerList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPfCustomerListClick();
            }
        });

        updateView();
        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PAYMENT_STATE, mPaymentsListContent);
        outState.putBoolean(OPEN_STATE, mbIsCashDrawerOpen);
        outState.putDouble(TOTAL_PRICE_STATE, mTotalPrice);
        outState.putSerializable(CUSTOMER_STATE, mCustomer);
    }

    public void setCurrentCustomer(Customer customer) {
        mCustomer = customer;
    }

    public void setTotalPrice(double totalPrice) {
        mTotalPrice = totalPrice;
    }

    public void setTicketPrepaid(double ticketPrepaid) {
        mTicketPrepaid = ticketPrepaid;
    }

    public void updateView() {
        updateInputView();
        updateRemainingView();
        updateGiveBackView();
        updateCustomerView();
    }

    public void resetInput() {
        mNumberPad.clear();
        updateInputView();
        updateGiveBackView();
    }

    public void resetPaymentList() {
        mPaymentsListContent.clear();
        ((PaymentsAdapter) mPaymentsList.getAdapter()).notifyDataSetChanged();
        updateView();
    }

    /*
     *  INTERFACE
     */

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case NumKeyboard.KEY_ENTER:
                validatePayment();
                break;
            default:
                updateInputView();
                mInput.setSelection(mInput.getText().toString().length());
                updateGiveBackView();
                break;

        }
        return true;
    }

    @Override
    public void deletePayment(Payment p) {
        mPaymentsListContent.remove(p);
        ((PaymentsAdapter) mPaymentsList.getAdapter()).notifyDataSetChanged();
        updateRemainingView();
    }

    /*
     *  PRIVATE
     */

    private double getRemaining() {
        double paid = 0.0;
        for (Payment p : mPaymentsListContent) {
            paid += p.getAmount();
        }
        return mTotalPrice - paid;
    }

    private void reuseData(Bundle savedState) {
        if (savedState == null) {
            mPaymentsListContent = new ArrayList<>();
            mbIsCashDrawerOpen = false;
            mTotalPrice = 0;
            mCustomer = null;
        } else {
            @SuppressWarnings("unchecked") ArrayList<Payment> sw
                    = (ArrayList<Payment>) savedState.getSerializable(PAYMENT_STATE);
            mPaymentsListContent = sw;
            mbIsCashDrawerOpen = savedState.getBoolean(OPEN_STATE);
            mTotalPrice = savedState.getDouble(TOTAL_PRICE_STATE);
            // Might be better to implement it as mCustomer = mListener.getCurrentCustomer(); in onCreate
            mCustomer = (Customer) savedState.getSerializable(CUSTOMER_STATE);
        }
    }

    private void updateInputView() {
        mInput.setHint(String.format("%.2f", getRemaining()));
        mInput.setText(mNumberPad.getRawValue());
    }

    private void updateRemainingView() {
        double remaining = getRemaining();
        String strRemaining = getString(R.string.ticket_remaining, remaining);
        mRemaining.setText(strRemaining);
    }

    private void updateGiveBackView() {
        double overflow = mNumberPad.getValue() - getRemaining();
        PaymentMode retMode = mCurrentMode.getReturnMode(overflow, mContext);
        String back = null;
        if (retMode != null) {
            Formatter f = new Formatter();
            back = f.format("%s %.2f€", retMode.getBackLabel(),
                    overflow).toString();
        }
        mGiveBack.setText(back);
        if (mCurrentMode.isCustAssigned()
                && mCustomer == null) {
            mGiveBack.setText(R.string.payment_no_customer);
        } else {
            if (mCurrentMode.isDebt()
                    && mCustomer != null) {
                double debt = mCustomer.getCurrDebt();
                for (Payment p : mPaymentsListContent) {
                    if (p.getMode().isDebt()) {
                        debt += p.getAmount();
                    }
                }
                double maxDebt = mCustomer.getMaxDebt();
                String debtStr = this.getString(R.string.payment_debt,
                        debt, maxDebt);
                mGiveBack.setText(debtStr);
            } else if (mCurrentMode.isPrepaid()
                    && mCustomer != null) {
                double prepaid = getRemainingPrepaid();
                String strPrepaid = this.getString(R.string.payment_prepaid,
                        prepaid);
                mGiveBack.setText(strPrepaid);
            }
        }
    }

    private void updateCustomerView() {
        int visibility = View.GONE;
        if (mCustomer != null) {
            visibility = View.VISIBLE;
            mCusPrepaid.setText(String.valueOf(mCustomer.getPrepaid()));
            mCusDebt.setText(String.valueOf(mCustomer.getCurrDebt()));
            mCusDebtMax.setText(String.valueOf(mCustomer.getMaxDebt()));
        }
        int total = mCusInfo.getChildCount();
        for (int i = 0; i < total; ++i) {
            mCusInfo.getChildAt(i).setVisibility(visibility);
        }
    }

    private double getRemainingPrepaid() {
        if (mCustomer != null) {
            double prepaid = mCustomer.getPrepaid();
            // Substract prepaid payments
            for (Payment p : mPaymentsListContent) {
                if (p.getMode().isPrepaid()) {
                    prepaid -= p.getAmount();
                }
            }
            // Add ordered refills
            prepaid += mTicketPrepaid;
            return prepaid;
        } else {
            return 0.0;
        }
    }

    /**
     * Get entered amount. If money is given back, amount is the final sum
     * (not the given one).
     */
    private double getAmount() {
        double remaining = this.getRemaining();
        double amount = remaining;
        if (mInput.getText().length() > 0) {
            amount = Double.parseDouble(mInput.getText().toString());
        }
        // Use remaining when money is given back
        double overflow = amount - remaining;
        if (overflow > 0.0) {
            for (PaymentMode.Return ret : mCurrentMode.getRules()) {
                if (ret.appliesFor(overflow)) {
                    if (ret.hasReturnMode()) {
                        amount = remaining;
                    }
                    break;
                }
            }
        }
        return amount;
    }

    private double getGiven() {
        double given = getRemaining();
        if (mInput.getText().length() > 0) {
            given = Double.parseDouble(mInput.getText().toString());
        }
        return given;
    }

    private void validatePayment() {
        if (mCurrentMode != null) {
            double remaining = getRemaining();
            // Get amount from entered value (default is remaining)
            double amount = getAmount();
            // Check for debt and cust assignment
            if (mCurrentMode.isCustAssigned()
                    && mCustomer == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(R.string.payment_no_customer);
                builder.setNeutralButton(android.R.string.ok, null);
                builder.show();
                return;
            }
            if (mCurrentMode.isDebt()) {
                double debt = mCustomer.getCurrDebt();
                for (Payment p : mPaymentsListContent) {
                    if (p.getMode().isDebt()) {
                        debt += p.getAmount();
                    }
                }
                if (debt + amount > mCustomer.getMaxDebt()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage(R.string.payment_debt_exceeded);
                    builder.setNeutralButton(android.R.string.ok, null);
                    builder.show();
                    return;
                }
            }
            if (mCurrentMode.isPrepaid()) {
                double prepaid = this.getRemainingPrepaid();
                if (prepaid < amount) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage(R.string.payment_no_enough_prepaid);
                    builder.setNeutralButton(android.R.string.ok, null);
                    builder.show();
                    return;
                }
            }
            boolean proceed = true;
            if (remaining - amount < 0.005) {
                // Confirm payment end
                proceed = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(R.string.confirm_payment_end)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                proceedPayment();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
            if (proceed) {
                proceedPayment();
            }
        }
    }

    /**
     * Register the payment.
     *
     * @return True if payment is registered, false if an operation is pending.
     */
    private boolean proceedPayment() {
        double amount = this.getAmount();
        Payment p = new Payment(mCurrentMode, amount, getGiven());

        // If we have a processor for this payment type, forward to it
        PaymentProcessor.PaymentListener listener = new PaymentProcessor.PaymentListener() {

            @Override
            public void registerPayment(Payment p) {
                PaymentFragment.this.registerPayment(p);
            }
        };

        mCurrentProcessor = FlavorPaymentProcessor.getProcessor((TrackedActivity) this.getActivity(), listener, p);
        if (mCurrentProcessor != null) {
            PaymentProcessor.Status paymentStatus = mCurrentProcessor.initiatePayment();

            if (paymentStatus == Status.PENDING)
                return false;
        }
        this.registerPayment(p);
        this.mPaymentModes.setSelection(0);
        return true;
    }

    /**
     * Add a payment to the registered ones and update ui
     * (update remaining or close payment)
     */
    private void registerPayment(Payment p) {
        mPaymentsListContent.add(p);
        ((PaymentsAdapter) mPaymentsList.getAdapter()).notifyDataSetChanged();
        double remaining = getRemaining();
        if (remaining < 0.005) {
            closePayment();
        } else {
            updateRemainingView();
            resetInput();
            Toast.makeText(mContext, R.string.payment_done, Toast.LENGTH_SHORT).show();
        }
        mCurrentProcessor = null;
    }

    /**
     * Save ticket and return to a new one
     */
    private void closePayment() {
        Receipt r = mListener.onPfSaveReceipt(new ArrayList<>(mPaymentsListContent));

        // Update customer debt
        boolean custDirty = false;
        if (mCustomer != null) {
            for (Payment p : mPaymentsListContent) {
                if (p.getMode().isDebt()) {
                    mCustomer.addDebt(p.getAmount());
                    custDirty = true;
                }
            }
            if (getRemainingPrepaid() != mCustomer.getPrepaid()) {
                mCustomer.setPrepaid(this.getRemainingPrepaid());
                custDirty = true;
            }
        }
        if (custDirty) {
            int index = Data.Customer.customers.indexOf(mCustomer);
            Data.Customer.customers.remove(index);
            Data.Customer.customers.add(index, mCustomer);
            try {
                Data.Customer.save(mContext);
            } catch (IOError e) {
                Log.e(LOG_TAG, "Unable to save customers", e);
                Error.showError(R.string.err_save_customers, (TrackedActivity) getActivity());
            }
        }
        mListener.onPfPrintReceipt(r);
        finish();
    }

    public void finish() {
        mListener.onPfFinished();
    }

    /*
     *  LISTENERS
     */

    private class PaymentModeItemSelectedListener
            implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mCurrentMode = ((PaymentModeItem) view).getMode();
            PaymentFragment.this.updateView();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }
}
