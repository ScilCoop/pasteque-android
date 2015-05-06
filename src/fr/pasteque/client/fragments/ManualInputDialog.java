package fr.pasteque.client.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.pasteque.client.BuildConfig;
import fr.pasteque.client.R;
import fr.pasteque.client.data.CatalogData;
import fr.pasteque.client.data.ImagesData;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.Product;


/**
 * The activity that creates an instance of this dialog fragment
 * must implement MIDialogListener to get results
 */
public class ManualInputDialog extends DialogFragment {
    // TODO: Maybe do a base Tab DialogFragment ?

    public interface MIDialogListener {
        /**
         * Called when creating a product in manual input
         *
         * @param product is the newly created product
         */
        public void onMIProductCreated(Product product);

        /**
         * Called when picking an item in the list.
         *
         * @param product is the selected scanned product
         */
        public void onMIProductPick(Product product);
    }

    private class BarcodeListAdapter extends BaseAdapter {

        private List<Product> mList;

        public BarcodeListAdapter() {
            mList = new ArrayList<Product>();
        }

        public void addItem(Product p) {
            mList.add(p);
        }

        public void clearItems() {
            mList.clear();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Product p = mList.get(position);
            if (convertView == null) {
                // Create the view
                LayoutInflater inflater = (LayoutInflater) ManualInputDialog.this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.barcode_list_item, parent, false);
            }
            // Reuse the view
            // TODO: put this try catch in a static func in Product class
            try {
                Bitmap img;
                if (p.hasImage() && null != (img = ImagesData.getProductImage(mContext, p.getId()))) {
                    ((ImageView) convertView.findViewById(R.id.product_img)).setImageBitmap(img);
                } else {
                    ((ImageView) convertView.findViewById(R.id.product_img)).setImageResource(R.drawable.ic_placeholder_img);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            TextView label = (((TextView) convertView.findViewById(R.id.product_label)));
            label.setText(p.getLabel());

            convertView.findViewById(R.id.btn_product_select).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ManualInputDialog.this.mListener.onMIProductPick(p);
                    ManualInputDialog.this.dismiss();
                }
            });
            return convertView;
        }
    }

    private class OnProductCreatedClick implements View.OnClickListener {
        private EditText mLabel;
        private EditText mPrice;
        private Spinner mVAT;

        OnProductCreatedClick(View layout) {
            mLabel = (EditText) layout.findViewById(R.id.tab1_product_title);
            mPrice = (EditText) layout.findViewById(R.id.tab1_edit_tariff);
            mVAT = (Spinner) layout.findViewById(R.id.tab1_spin_vat);
        }

        @Override
        public void onClick(View v) {
            String label = mLabel.getText().toString().trim();
            String sPrice = mPrice.getText().toString();
            if (label.isEmpty()) {
                mLabel.setError(getString(R.string.manualinput_error_empty));
            }
            Boolean bValid;
            if ((bValid = sPrice.isEmpty()) || (bValid = sPrice.equals("."))) {
                mPrice.setError(getString(R.string.manualinput_error_number));
            }
            // TODO: Implement VAT
            if (!label.isEmpty() && !bValid) {
                Double price = Double.parseDouble(sPrice);
                Product p = new Product(null, label, "", price,
                        "004", 0.0, false, false);
                ManualInputDialog.this.mListener.onMIProductCreated(p);
                ManualInputDialog.this.dismiss();
            }
        }
    }

    /* START OF CLASS ManualInput */
    private MIDialogListener mListener;

    private Context mContext;
    private Boolean mNotFoundToast;
    private BarcodeListAdapter mMatchingItems;

    private final TextWatcher BARCODE_INPUT_TW = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            ManualInputDialog.this.readBarcode(s.toString());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
        mContext = getActivity();
        mNotFoundToast = true;
        mMatchingItems = new BarcodeListAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View layout = inflater.inflate(R.layout.manual_input, container, false);

        // Editing layout
        TabHost tabs = (TabHost) layout.findViewById(android.R.id.tabhost);

        tabs.setup();

        TabHost.TabSpec tabpage1 = tabs.newTabSpec("tab1");
        tabpage1.setContent(R.id.input_manual);
        tabpage1.setIndicator(getString(R.string.manualinput_title));

        TabHost.TabSpec tabpage2 = tabs.newTabSpec("tab2");
        tabpage2.setContent(R.id.input_barcode);
        tabpage2.setIndicator(getString(R.string.barcodeinput_title));

        tabs.addTab(tabpage1);
        tabs.addTab(tabpage2);

        TabWidget tabWidget = tabs.getTabWidget();

        tabs.getTabWidget().setDividerDrawable(R.color.popup_outer_txt);

        int nbrTab = tabWidget.getTabCount();
        if (BuildConfig.DEBUG && nbrTab != 2) {
            throw new AssertionError();
        }
        for (int j = 0; j < nbrTab; ++j) {
            View tabView = tabWidget.getChildTabViewAt(j);
            TextView tabTitle = (TextView) tabView.findViewById(android.R.id.title);
            if (tabTitle != null) {
                tabView.setBackgroundResource(R.drawable.tab_selector);
                tabTitle.setTextColor(getResources().getColor(R.color.popup_outer_txt));
                tabTitle.setTypeface(null, Typeface.BOLD);
            }
        }

        // Setting up buttons
        layout.findViewById(R.id.tab1_btn_positive).setOnClickListener(new OnProductCreatedClick(layout));

        View.OnClickListener negativeClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManualInputDialog.this.getDialog().cancel();
            }
        };
        layout.findViewById(R.id.tab1_btn_negative).setOnClickListener(negativeClick);
        layout.findViewById(R.id.tab2_btn_negative).setOnClickListener(negativeClick);

        EditText input = ((EditText) layout.findViewById(R.id.tab2_barcode_input));
        input.addTextChangedListener(BARCODE_INPUT_TW);

        // Dynamic list view
        ListView MatchedListView = (ListView) layout.findViewById(R.id.tab2_scanned_products);
        MatchedListView.setAdapter(mMatchingItems);
        return layout;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MIDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MIDialogListener");
        }
    }

    private void readBarcode(String code) {
        mMatchingItems.clearItems();
        if (!code.isEmpty()) {
            Catalog cat = CatalogData.catalog(mContext);
            List<Product> pList = cat.getProductLikeBarcode(code);
            if (pList.size() > 0) {
                mNotFoundToast = true;
                for (Product p : pList) {
                    mMatchingItems.addItem(p);
                }
            } else if (mNotFoundToast == true) {
                mNotFoundToast = false;
                String text = this.getString(R.string.barcode_not_found, code);
                Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
            }
        }
        mMatchingItems.notifyDataSetChanged();
    }
}
