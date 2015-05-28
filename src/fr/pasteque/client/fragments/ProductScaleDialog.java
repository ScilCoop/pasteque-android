package fr.pasteque.client.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;

import fr.pasteque.client.R;
import fr.pasteque.client.models.Product;

public class ProductScaleDialog extends DialogFragment {

    public static String TAG = "ProdScaleDFRAG";

    private final static String PRODUCT_ARG = "prod_arg";
    private Context mContext;
    private Listener mListener;
    private Product mProd;

    public interface Listener {
        void onPsdPositiveClick(Product p, double weight);
    }

    public static ProductScaleDialog newInstance(Product p) {
        Bundle args = new Bundle();
        args.putSerializable(PRODUCT_ARG, p);
        ProductScaleDialog dial = new ProductScaleDialog();
        dial.setArguments(args);
        return dial;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " should implement ProductScaleDialog.Listener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mProd = (Product) getArguments().getSerializable(PRODUCT_ARG);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        final EditText input = new EditText(mContext);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        alertDialogBuilder.setView(input);
        alertDialogBuilder.setTitle(mProd.getLabel());
        alertDialogBuilder
                .setView(input)
                .setIcon(R.drawable.scale)
                .setMessage(R.string.scaled_products_info)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
					public void onClick(DialogInterface dialog, int id) {
                        String getString = input.getText().toString();
                        if (!TextUtils.isEmpty(getString)) {
                            double weight = Double.valueOf(getString);
                            mListener.onPsdPositiveClick(mProd, weight);
                        }
                    }
                })
                .setNegativeButton(R.string.scaled_products_cancel, new DialogInterface.OnClickListener() {
                    @Override
					public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialogBuilder.create();
    }
}
