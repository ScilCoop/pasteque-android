package fr.pasteque.client.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.pasteque.client.Configure;
import fr.pasteque.client.R;
import fr.pasteque.client.TicketLineEditListener;
import fr.pasteque.client.TicketSelect;
import fr.pasteque.client.data.CatalogData;
import fr.pasteque.client.data.ImagesData;
import fr.pasteque.client.data.SessionData;
import fr.pasteque.client.data.TariffAreaData;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.Category;
import fr.pasteque.client.models.CompositionInstance;
import fr.pasteque.client.models.Customer;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.TariffArea;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.TicketLine;
import fr.pasteque.client.sync.TicketUpdater;
import fr.pasteque.client.utils.ScreenUtils;
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
    //View
    private TextView mTitle;
    private TextView mCustomer;
    private TextView mTotal;
    private TextView mTariffArea;
    private ImageView mCustomerImg;
    private ImageButton mNewBtn;
    private ImageButton mDeleteBtn;
    private ListView mContentList;
    private ImageButton mCheckInCart;
    private ImageButton mCheckOutCart;

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
        mCustomer = (TextView) layout.findViewById(R.id.ticket_customer);
        mTotal = (TextView) layout.findViewById(R.id.ticket_total);
        mTariffArea = (TextView) layout.findViewById(R.id.ticket_area);
        mCustomerImg = (ImageView) layout.findViewById(R.id.ticket_customer_img);
        mNewBtn = (ImageButton) layout.findViewById(R.id.ticket_new);
        mDeleteBtn = (ImageButton) layout.findViewById(R.id.ticket_delete);
        mCheckInCart = (ImageButton) layout.findViewById(R.id.btn_cart_back);
        mCheckOutCart = (ImageButton) layout.findViewById(R.id.pay);

        mContentList = (ListView) layout.findViewById(R.id.ticket_content);
        mContentList.setAdapter(new TicketLinesAdapter(mTicketData, this, mbEditable));

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
        updateTicketMode();
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
        updateView();
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
                        switchTicket(SessionData.currentSession(mContext).getCurrentTicket());
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

    public double getTotalPrice() {
        return mTicketData.getTotalPrice();
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
        Catalog cat = CatalogData.catalog(mContext);
        Category prepaidCat = cat.getPrepaidCategory();
        for (TicketLine l : mTicketData.getLines()) {
            Product p = l.getProduct();
            if (prepaidCat != null
                    && cat.getProducts(prepaidCat).contains(p)) {
                prepaid += p.getTaxedPrice() * l.getQuantity();
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
        // Update ticket info
        String total = getString(R.string.ticket_total,
                mTicketData.getTotalPrice());
        String label = getString(R.string.ticket_label,
                mTicketData.getLabel());
        mTitle.setText(label);
        mTotal.setText(total);
        // Update customer info
        if (mTicketData.getCustomer() != null) {
            String name;
            Customer c = mTicketData.getCustomer();
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
        ((TicketLinesAdapter) mContentList.getAdapter()).notifyDataSetChanged();
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
        mNewBtn.setEnabled(mCurrentState == CHECKIN_STATE);
        mDeleteBtn.setEnabled(mCurrentState == CHECKIN_STATE);
        TicketLinesAdapter adp = ((TicketLinesAdapter) mContentList.getAdapter());
        adp.setEditable(mbEditable);
        adp.notifyDataSetChanged();
    }

    public void addProduct(Product p) {
        mTicketData.addProduct(p);
    }

    public void addProduct(CompositionInstance compo) {
        mTicketData.addProduct(compo);
    }

    public void addScaledProduct(Product p, double scale) {
        mTicketData.addScaledProduct(p, scale);
    }

    public void switchTicket(Ticket t) {
        mTicketData = t;
        mContentList.setAdapter(new TicketLinesAdapter(mTicketData, this, mbEditable));
        SessionData.currentSession(mContext).setCurrentTicket(t);
        updateView();
        try {
            SessionData.saveSession(mContext);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to save session", e);
        }
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        final EditText input = new EditText(mContext);
        input.setInputType(InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL |
                InputType.TYPE_NUMBER_FLAG_SIGNED);
        alertDialogBuilder.setView(input);
        alertDialogBuilder.setTitle(p.getLabel());
        alertDialogBuilder
                .setView(input)
                .setIcon(R.drawable.scale)
                .setMessage(R.string.scaled_products_info)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
					public void onClick(DialogInterface dialog, int id) {
                        String recup = input.getText().toString();
                        double scale = Double.valueOf(recup);
                        mTicketData.adjustScale(l, scale);
                        updateView();
                    }
                })
                .setNegativeButton(R.string.scaled_products_cancel, new DialogInterface.OnClickListener() {
                    @Override
					public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
            mTicketData = SessionData.currentSession(mContext).getCurrentTicket();
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
        // Ticket Modes. If simple, disable add/remove btn
        if (Configure.getTicketsMode(mContext) == Configure.SIMPLE_MODE) {
            mTitle.setClickable(false);
            mNewBtn.setEnabled(false);
            mDeleteBtn.setEnabled(false);
        } else {
            mTitle.setClickable(true);
            mNewBtn.setEnabled(true);
            mDeleteBtn.setEnabled(true);
        }
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
                            Ticket t = SessionData.currentSession(mContext).getTickets().get(position);
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
        Session currSession = SessionData.currentSession(mContext);
        currSession.newTicket();
        switchTicket(currSession.getCurrentTicket());
        try {
            SessionData.saveSession(mContext);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to save session", e);
        }
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
                Session currSession = SessionData.currentSession(mContext);
                Ticket current = currSession.getCurrentTicket();
                for (Ticket t : currSession.getTickets()) {
                    if (t.getLabel().equals(current.getLabel())) {
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
                try {
                    SessionData.saveSession(mContext);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Unable to save session", e);
                }
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
}
