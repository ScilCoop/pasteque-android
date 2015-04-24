package fr.pasteque.client;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.pasteque.client.data.CatalogData;
import fr.pasteque.client.data.ImagesData;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.TicketLine;

class BarcodeListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Product> mList;

    public BarcodeListAdapter(Context context) {
        mContext = context;
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
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                Toast.makeText(mContext, p.getBarcode(), Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }
}

/*
** Maybe do a base Tab DialogFragment ?
*/
public class ManualInput extends DialogFragment {

    private Context mContext;
    private BarcodeListAdapter mMatchingItems;
    private Boolean mNotFoundToast;

    private final TextWatcher mBarcodeInputTW = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            ManualInput.this.readBarcode(s.toString());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    static public ManualInput newInstance(Ticket activityTicket) {
        ManualInput frag = new ManualInput();
        /******  TODO: WRONG IMPLEMENTATION, PLEASE CORRECT THIS *******/
        /******  This is because it needs to go through all function of the TicketInput activity *******/
        Bundle args = new Bundle();
        args.putSerializable("activity_ticket", activityTicket);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
        mContext = getActivity();
        mMatchingItems = new BarcodeListAdapter(mContext);
        mNotFoundToast = true;
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

        tabs.getTabWidget().setDividerDrawable(R.color.popupBackgroundText);

        int nbrTab = tabWidget.getTabCount();
        if (BuildConfig.DEBUG && nbrTab != 2) {
            throw new AssertionError();
        }
        for (int j = 0; j < nbrTab; ++j) {
            View tabView = tabWidget.getChildTabViewAt(j);
            TextView tabTitle = (TextView) tabView.findViewById(android.R.id.title);
            if (tabTitle != null) {
                tabView.setBackgroundResource(R.drawable.tab_selector);
                tabTitle.setTextColor(getResources().getColor(R.color.popupBackgroundText));
                tabTitle.setTypeface(null, Typeface.BOLD);
            }
        }

        // Setting up buttons
        View.OnClickListener negativeClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManualInput.this.getDialog().cancel();
            }
        };
        layout.findViewById(R.id.tab1_btn_negative).setOnClickListener(negativeClick);
        layout.findViewById(R.id.tab2_btn_negative).setOnClickListener(negativeClick);

        EditText input = ((EditText) layout.findViewById(R.id.tab2_barcode_input));
        input.addTextChangedListener(mBarcodeInputTW);

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
