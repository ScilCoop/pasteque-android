package fr.pasteque.client.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.pasteque.client.Configure;
import fr.pasteque.client.R;
import fr.pasteque.client.interfaces.TicketLineEditListener;
import fr.pasteque.client.TicketSelect;
import fr.pasteque.client.data.Data;
import fr.pasteque.client.data.DataSavable.SessionData;
import fr.pasteque.client.data.TariffAreaData;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.Category;
import fr.pasteque.client.models.CompositionInstance;
import fr.pasteque.client.models.Customer;
import fr.pasteque.client.models.Discount;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.TariffArea;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.TicketLine;
import fr.pasteque.client.sync.TicketUpdater;
import fr.pasteque.client.utils.ScreenUtils;
import fr.pasteque.client.utils.exception.DataCorruptedException;
import fr.pasteque.client.widgets.SessionTicketsAdapter;
import fr.pasteque.client.widgets.TariffAreasAdapter;
import fr.pasteque.client.widgets.TicketLinesAdapter;

public class TicketFragment extends ViewPageFragment
        implements TicketLineEditListener,
        TicketLineEditDialog.Listener {
    
    public interface Listener {
        void onTfCheckInClick();

        void onTfCheckOutClick();
    }

    public static final int CHECKIN_STATE = 0;
    public static final int CHECKOUT_STATE = 1;

    private static final String LOG_TAG = "Pasteque/TicketInfo";
    // Serialize string
    private static final String TICKET_DATA = "ticket";
    private static final String PAGE_STATE = "page_state";

    //Data
    private Listener mListener;
    private Ticket mTicketData;
    private int mCurrentState;
    private boolean mbEditable;
    private boolean mbSimpleMode;
    //View
    private TextView mTitle;
    private TextView mCustomer;
    private TextView mTotal;
    private TextView mTariffArea;
    private ImageView mCustomerImg;
    private ImageButton mNewBtn;
    private ImageButton mDeleteBtn;
    private ListView mTicketLineList;
    private ImageButton mCheckInCart;
    private ImageButton mCheckOutCart;
    private RelativeLayout mCustomerBtn;
    private TextView mDiscount;
    private ViewGroup mDiscountHolder;
    private ImageButton mDeleteDiscBtn;

    @SuppressWarnings("unused") // Used via class reflection
    public static TicketFragment newInstance(int pageNumber) {
        TicketFragment frag = new TicketFragment();
        ViewPageFragment.initPageNumber(pageNumber, frag);
        return frag;
    }

    /*
     *  PUBLIC
     */

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement TicketFragment Listener!");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reuseData(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.ticket_information, container, false);
        layout.setPadding(1, 0, 1, 0);
        mTitle = (TextView) layout.findViewById(R.id.ticket_label);
        mCustomer = (TextView) layout.findViewById(R.id.ticket_customer_name);
        mTotal = (TextView) layout.findViewById(R.id.ticket_total);
        mTariffArea = (TextView) layout.findViewById(R.id.ticket_area);
        mCustomerImg = (ImageView) layout.findViewById(R.id.ticket_customer_img);
        mNewBtn = (ImageButton) layout.findViewById(R.id.ticket_new);
        mDeleteBtn = (ImageButton) layout.findViewById(R.id.ticket_delete);
        mCheckInCart = (ImageButton) layout.findViewById(R.id.btn_cart_back);
        mDiscount = (TextView) layout.findViewById(R.id.ticket_discount);
        mDiscountHolder = (ViewGroup) layout.findViewById(R.id.ticket_discount_holder);
        mDeleteDiscBtn = (ImageButton) layout.findViewById(R.id.ticket_discount_delete);
        mCheckOutCart = (ImageButton) layout.findViewById(R.id.pay);
        mCustomerBtn = (RelativeLayout) layout.findViewById(R.id.ticket_customer);

        mTicketLineList = (ListView) layout.findViewById(R.id.ticket_content);
        mTicketLineList.setAdapter(new TicketLinesAdapter(mTicketData, this, mbEditable));
        
        mDeleteDiscBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDiscount();
            }
        });
        
        mTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTicketClick(v);
            }
        });
        mNewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTicketClick(v);
            }
        });
        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTicketClick(v);
            }
        });
        mCheckInCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onTfCheckInClick();
            }
        });
        mCheckOutCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onTfCheckOutClick();
            }
        });
        mCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomerInfoDialog dial = CustomerInfoDialog.newInstance(false, mTicketData.getCustomer());
                dial.show(getFragmentManager());
            }
        });

        updateTicketMode();
        updatePageState();

        //TODO: Implement line 89 TARIFF AREA
        // Check presence of tariff areas
        if (TariffAreaData.areas.size() == 0) {
            //layout.findViewById(R.id.change_area).setVisibility(View.GONE);
            mTariffArea.setVisibility(View.GONE);
        }
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateViewNoSave();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(TICKET_DATA, mTicketData);
        outState.putInt(PAGE_STATE, mCurrentState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TicketSelect.CODE_TICKET:
                switch (resultCode) {
                    case Activity.RESULT_CANCELED:
                        break;
                    case Activity.RESULT_OK:
                        switchTicket(Data.Session.currentSession(mContext).getCurrentTicket());
                        break;
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Configure.getTicketsMode(mContext) == Configure.RESTAURANT_MODE
                && Configure.getSyncMode(mContext) == Configure.AUTO_SYNC_MODE) {
            TicketUpdater.getInstance().execute(mContext, null,
                    TicketUpdater.TICKETSERVICE_SEND
                            | TicketUpdater.TICKETSERVICE_ONE, mTicketData);
        }
    }

    public TariffArea getTariffArea() {
        return mTicketData.getTariffArea();
    }

    public double getTicketPrice() {
        return mTicketData.getTicketFinalPrice();
    }

    public Customer getCustomer() {
        return mTicketData.getCustomer();
    }

    public Ticket getTicketData() {
        return mTicketData;
    }

    // This prepaid is what's registered in the ticket
    public double getTicketPrepaid() {
        double prepaid = 0;
        Catalog cat = Data.Catalog.catalog(mContext);
        Category prepaidCat = cat.getPrepaidCategory();
        for (TicketLine l : mTicketData.getLines()) {
            Product p = l.getProduct();
            if (prepaidCat != null
                    && cat.getProducts(prepaidCat).contains(p)) {
                prepaid += p.getPriceIncTax() * l.getQuantity();
            }
        }
        return prepaid;
    }

    public void setState(int state) {
        mCurrentState = state;
        mbEditable = (mCurrentState == CHECKIN_STATE);
    }

    public void setCustomer(Customer customer) {
        mTicketData.setCustomer(customer);
    }

    public void updateView() {
        updateViewNoSave();
        saveSession();
    }

    public void updateViewNoSave() {
        // Update ticket info
        String total = getString(R.string.ticket_total,
                mTicketData.getTicketFinalPrice());
        String label = getString(R.string.ticket_label,
                mTicketData.getTicketId());
        mTitle.setText(label);
        mTotal.setText(total);
        if (mTicketData.getDiscountRate() != 0) {
            mDiscount.setText(mTicketData.getDiscountRateString());
            mDiscountHolder.setVisibility(View.VISIBLE);
        } else {
            mDiscountHolder.setVisibility(View.GONE);
        }
        // Update customer info
        Customer c = mTicketData.getCustomer();
        if (c != null) {
            String name;
            if (c.getPrepaid() > 0.005) {
                name = getString(R.string.customer_prepaid_label,
                        c.getName(), c.getPrepaid());
            } else {
                name = c.getName();
            }
            mCustomer.setText(name);
            mCustomer.setVisibility(View.VISIBLE);
            mCustomerImg.setVisibility(View.VISIBLE);
        } else {
            mCustomer.setVisibility(View.GONE);
            mCustomerImg.setVisibility(View.GONE);
        }
        // Update tariff area info
        if (mTicketData.getTariffArea() == null) {
            mTariffArea.setText(R.string.default_tariff_area);
        } else {
            mTariffArea.setText(mTicketData.getTariffArea().getLabel());
        }
        updatePageState();
    }

    public void updatePageState() {
        mCheckInCart.setEnabled(mCurrentState == CHECKOUT_STATE);
        mCheckOutCart.setEnabled(mCurrentState == CHECKIN_STATE);
        mNewBtn.setEnabled(!mbSimpleMode && mCurrentState == CHECKIN_STATE);
        mDeleteBtn.setEnabled(!mbSimpleMode && mCurrentState == CHECKIN_STATE);
        mDeleteDiscBtn.setEnabled(mTicketData.getDiscountRate() != Discount.DEFAULT_DISCOUNT_RATE);
        TicketLinesAdapter adp = ((TicketLinesAdapter) mTicketLineList.getAdapter());
        adp.setEditable(mbEditable);
        adp.notifyDataSetChanged();
    }

    public int addProduct(Product p) {
        // Simply return pos if you want to make the list view focus on the modified item;
        int pos = mTicketData.addProduct(p);
        return (pos == mTicketLineList.getCount()) ? (pos) : (-1);
    }

    public int addProduct(CompositionInstance compo) {
        return mTicketData.addProduct(compo);
    }

    public void addScaledProduct(Product p, double scale) {
        mTicketData.addScaledProduct(p, scale);
    }

    public void setDiscountRate(double rate) {
        mTicketData.setDiscountRate(rate);
    }
    
    public double getDiscountRate(double rate) {
        return mTicketData.getDiscountRate();
    }
    
    public void removeDiscount() {
        mTicketData.setDiscountRate(Discount.DEFAULT_DISCOUNT_RATE);
        updateView();
    }
    
    public void switchTicket(Ticket t) {
        mTicketData = t;
        mTicketLineList.setAdapter(new TicketLinesAdapter(mTicketData, this, mbEditable));
        Data.Session.currentSession(mContext).setCurrentTicket(t);
        updateView();
    }

    public void scrollDown() {
        scrollTo(mTicketLineList.getCount() - 1);
    }

    public void scrollTo(final int position) {
        if (position < 0) return;
        mTicketLineList.post(new Runnable() {
            @Override
            public void run() {
                mTicketLineList.setSelection(position);
            }
        });
    }

    /*
     *  INTERFACES
     */

    @Override
    public void addQty(TicketLine l) {
        mTicketData.adjustQuantity(l, 1);
        updateView();
    }

    @Override
    public void remQty(TicketLine l) {
        mTicketData.adjustQuantity(l, -1);
        updateView();
    }

    /**
     * Modifies the weight of the product by asking the user a new one
     *
     * @param l the ticket's line
     */
    @Override
    public void mdfyQty(final TicketLine l) {
        Product p = l.getProduct();
        if (p.isScaled()) {
            ProductScaleDialog dial = ProductScaleDialog.newInstance(p);
            dial.setDialogListener(new ProductScaleDialog.Listener() {
                @Override
                public void onPsdPositiveClick(Product p, double weight) {
                    mTicketData.adjustScale(l, weight);
                    updateView();
                }
            });
            dial.show(getFragmentManager(), ProductScaleDialog.TAG);
        }
    }

    @Override
    public void editProduct(final TicketLine l) {
        TicketLineEditDialog dial = TicketLineEditDialog.newInstance(l);
        dial.setDialogListener(this);
        dial.show(getFragmentManager(), TicketLineEditDialog.TAG);
    }

    @Override
    public void delete(TicketLine l) {
        mTicketData.removeLine(l);
        updateView();
    }

    @Override
    public void onTicketLineEdited() {
        updateView();
    }

    /*
     *  PRIVATES
     */

    private void reuseData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mTicketData = Data.Session.currentSession(mContext).getCurrentTicket();
            if (mTicketData == null) {
                mTicketData = new Ticket();
            }
            setState(CHECKIN_STATE);
        } else {
            mTicketData = (Ticket) savedInstanceState.getSerializable(TICKET_DATA);
            setState(savedInstanceState.getInt(PAGE_STATE));
        }
    }

    private void updateTicketMode() {
        // Ticket Modes. If simple, disable add/remove btn and ticket switch
        mbSimpleMode = Configure.getTicketsMode(mContext) == Configure.SIMPLE_MODE;
        mTitle.setClickable(!mbSimpleMode);
    }

    /*
     *  BUTTON CLICK
     */

    private void switchTicketClick(View v) {
        // Send current ticket data in connected mode
        if (Configure.getSyncMode(mContext) == Configure.AUTO_SYNC_MODE) {
            TicketUpdater.getInstance().execute(getActivity().getApplicationContext(),
                    null,
                    TicketUpdater.TICKETSERVICE_SEND
                            | TicketUpdater.TICKETSERVICE_ONE, mTicketData);
        }
        // Open ticket picker
        switch (Configure.getTicketsMode(mContext)) {
            case Configure.STANDARD_MODE:
                // Open selector popup
                try {
                    final ListPopupWindow popup = new ListPopupWindow(mContext);
                    ListAdapter adapter = new SessionTicketsAdapter(mContext);
                    popup.setAnchorView(mTitle);
                    popup.setAdapter(adapter);
                    popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {
                            // TODO: handle connected mode on switch
                            Ticket t = Data.Session.currentSession(mContext).getTickets().get(position);
                            switchTicket(t);
                            popup.dismiss();
                        }

                        public void onNothingSelected(AdapterView v) {
                        }
                    });
                    popup.setWidth(ScreenUtils.inToPx(2, mContext));
                    int ticketsCount = adapter.getCount();
                    int height = (int) (ScreenUtils.dipToPx(SessionTicketsAdapter.HEIGHT_DIP *
                            Math.min(5, ticketsCount), mContext) + mTitle.getHeight() / 2 + 0.5f);
                    popup.setHeight(height);
                    popup.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Configure.RESTAURANT_MODE:
                // Open restaurant activity
                Intent i = new Intent(mContext, TicketSelect.class);
                startActivityForResult(i, TicketSelect.CODE_TICKET);
                break;
            default:
                //NOT AVAILABLE IN SIMPLE_MODE
                Log.wtf(LOG_TAG, "Switch Ticket is not available mode " + Configure.getTicketsMode(mContext));
        }
    }

    private void addTicketClick(View v) {
        Session currSession = Data.Session.currentSession(mContext);
        currSession.newTicket();
        switchTicket(currSession.getCurrentTicket());
    }

    private void deleteTicketClick(View v) {
        // Show confirmation
        AlertDialog.Builder b = new AlertDialog.Builder(mContext);
        b.setTitle(getString(R.string.delete_ticket_title));
        String message = getResources().getQuantityString(
                R.plurals.delete_ticket_message,
                mTicketData.getArticlesCount(), mTicketData.getArticlesCount());
        b.setMessage(message);
        b.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Session currSession = Data.Session.currentSession(mContext);
                Ticket current = currSession.getCurrentTicket();
                for (Ticket t : currSession.getTickets()) {
                    if (t.getTicketId().equals(current.getTicketId())) {
                        currSession.getTickets().remove(t);
                        break;
                    }
                }
                if (currSession.getTickets().size() == 0) {
                    currSession.newTicket();
                } else {
                    currSession.setCurrentTicket(currSession.getTickets().get(currSession.getTickets().size() - 1));
                }
                switchTicket(currSession.getCurrentTicket());
            }
        });
        b.setNegativeButton(android.R.string.no, null);
        b.show();
    }

    private void switchAreaClick(View v) {
        // Open tariff area popup
        final ListPopupWindow popup = new ListPopupWindow(mContext);
        final List<TariffArea> data = new ArrayList<TariffArea>();
        data.add(null);
        data.addAll(TariffAreaData.areas);
        ListAdapter adapter = new TariffAreasAdapter(data);
        popup.setAnchorView(mTariffArea);
        popup.setAdapter(adapter);
        popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // TODO: handle connected mode on switch
                TariffArea area = data.get(position);
                mTicketData.setTariffArea(area);
                updateView();
                popup.dismiss();
            }

            public void onNothingSelected(AdapterView v) {
            }
        });
        popup.setWidth(ScreenUtils.inToPx(2, mContext));
        int areaCount = adapter.getCount();
        int height = (int) (ScreenUtils.dipToPx(TariffAreasAdapter.HEIGHT_DIP * Math.min(5, areaCount), mContext) + mTitle.getHeight() / 2 + 0.5f);
        popup.setHeight(height);
        popup.show();
    }

    private void saveSession() {
        try {
            Data.Session.save(mContext);
        } catch (IOError|DataCorruptedException e) {
            Log.e(LOG_TAG, "Unable to save session", e);
        }
    }
}
