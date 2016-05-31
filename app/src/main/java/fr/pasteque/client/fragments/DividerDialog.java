package fr.pasteque.client.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.R;
import fr.pasteque.client.data.Data;
import fr.pasteque.client.interfaces.TicketLineEditListener;
import fr.pasteque.client.models.LocalTicket;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.TicketLine;
import fr.pasteque.client.utils.Tuple;
import fr.pasteque.client.widgets.TicketLineItem;
import fr.pasteque.client.widgets.TicketLinesAdapter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by svirch_n on 25/05/16
 * Last edited at 17:33.
 */
public class DividerDialog extends PastequePopupFragment {

    public static final String TAG = "DIVIDER_DIALOG";
    private static final String TICKET_TAG = "TICKET_TAG";

    private ResultListener resultListener;
    private DividerAdapter newTicketAdapter;
    private Ticket ticketToDivide;

    public interface ResultListener {
        void onDividerDialogResult(LocalTicket createdTicket);
    }

    public interface RequestResultListener {
        ResultListener onDividerDialogRequestResultListener();
    }

    @Override
    protected void onNegativeClickListener() {
        this.dismiss();
    }

    @Override
    protected void onPositiveClickListener() {
        this.dismiss();
        LocalTicket result = this.createTicketResult(newTicketAdapter.getLines());
        this.resultListener.onDividerDialogResult(result);
    }

    private LocalTicket createTicketResult(List<TicketLine> lines) {
        LocalTicket result = Data.Session.currentSession().newLocalTicket(null);
        for (TicketLine each: lines){
            result.addTicketLine(each);
            this.ticketToDivide.removeTicketLine(each);
        }
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ticketToDivide = (Ticket) getArguments().getSerializable(TICKET_TAG);
    }

    @Override
    public View onCreateFrameView(LayoutInflater inflater, FrameLayout frameContainer, Bundle savedInstanceState) {
        setTitle(Pasteque.getStringResource(R.string.menu_divider));
        setPositiveTitle(Pasteque.getStringResource(R.string.divider_button_positive));
        setNegativeTitle(Pasteque.getStringResource(R.string.divider_button_negative));
        View result = inflater.inflate(R.layout.divider_dialog, frameContainer, false);
        ListView originalTicketListView = (ListView) result.findViewById(R.id.list1);
        DividerAdapter originalAdapter = new DividerAdapter(new LinkedList<>(this.ticketToDivide.getLines()));
        originalTicketListView.setAdapter(originalAdapter);
        ListView newTicketListView = (ListView) result.findViewById(R.id.list2);
        newTicketAdapter = new DividerAdapter(new LinkedList<TicketLine>());
        newTicketListView.setAdapter(newTicketAdapter);
        originalAdapter.setAdapter(newTicketAdapter);
        newTicketAdapter.setAdapter(originalAdapter);
        frameContainer.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return result;
    }

    /**
     * @deprecated not called since API 23 (Marshmallow)
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        onAttach((RequestResultListener) activity);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttach((RequestResultListener) context);
    }

    private void onAttach(RequestResultListener context) {
        resultListener = context.onDividerDialogRequestResultListener();
    }

    @NonNull
    public static DividerDialog newInstance(Ticket ticketToDivide) {
        DividerDialog result = new DividerDialog();
        Bundle args = new Bundle();
        args.putSerializable(DividerDialog.TICKET_TAG, ticketToDivide);
        result.setArguments(args);
        return result;
    }

    private class DividerAdapter extends TicketLinesAdapter {

        private final List<TicketLine> ticketLines;
        private DividerAdapter dividerAdapter;

        private View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TicketLineItem ticketLineItem = (TicketLineItem) view;
                TicketLine ticketLine = ticketLineItem.getLine();
                int index = DividerAdapter.this.ticketLines.indexOf(ticketLine);
                DividerAdapter.this.ticketLines.remove(ticketLine);
                try {
                    Tuple<TicketLine, TicketLine> ticketLineTicketLineTuple = ticketLine.splitTicketLineArticle();
                    DividerAdapter.this.dividerAdapter.addTicketLine(ticketLineTicketLineTuple.first());
                    TicketLine second = ticketLineTicketLineTuple.second();
                    if (second != null) {
                        DividerAdapter.this.ticketLines.add(index, second);
                    }
                } catch (TicketLine.CannotSplitScaledProductException e) {
                    DividerAdapter.this.dividerAdapter.addTicketLine(ticketLine);
                }
                notifyDataSetChanged();
                dividerAdapter.notifyDataSetChanged();
            }
        };

        DividerAdapter(List<TicketLine> ticketLines) {
            super(ticketLines, nullListener, false);
            this.ticketLines = ticketLines;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View result = super.getView(position, convertView, parent);
            result.findViewById(R.id.product_edit_group).setVisibility(View.GONE);
            result.setOnClickListener(onClickListener);
            return result;
        }

        public void setAdapter(DividerAdapter adapter) {
            dividerAdapter = adapter;
        }

        public void addTicketLine(TicketLine ticketLine) {
            for (TicketLine each: this.ticketLines) {
                if (each.canMerge(ticketLine)) {
                    each.merge(ticketLine);
                    return;
                }
            }
            this.ticketLines.add(ticketLine);
        }


        public List<TicketLine> getLines() {
            return this.ticketLines;
        }
    }

    private static final TicketLineEditListener nullListener = new TicketLineEditListener() {
        @Override
        public void addQty(TicketLine t) {

        }

        @Override
        public void remQty(TicketLine t) {

        }

        @Override
        public void mdfyQty(TicketLine t) {

        }

        @Override
        public void editProduct(TicketLine t) {

        }

        @Override
        public void delete(TicketLine t) {

        }
    };
}
