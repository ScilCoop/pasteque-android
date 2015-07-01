package fr.pasteque.client.widgets;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import fr.pasteque.client.R;
import fr.pasteque.client.models.Ticket;

public class CustomerTicketHistoryAdapter extends BaseAdapter {

    public static final String TAG = CustomerTicketHistoryAdapter.class.getSimpleName();

    private Context mCtx;
    private List<Ticket> mListData;

    private static class HistoryViewHolder {
        TextView id;
        TextView date;
        TextView price;
    }

    public CustomerTicketHistoryAdapter(Context ctx, List<Ticket> listData) {
        mCtx = ctx;
        mListData = listData;
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int position) {
        return mListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HistoryViewHolder holder;
        Ticket t = mListData.get(position);
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mCtx).getLayoutInflater();
            convertView = inflater.inflate(R.layout.customer_info_history_line, parent, false);

            holder = new HistoryViewHolder();
            holder.id = (TextView) convertView.findViewById(R.id.ticket_id);
            holder.date = (TextView) convertView.findViewById(R.id.ticket_date);
            holder.price = (TextView) convertView.findViewById(R.id.ticket_price);
            convertView.setTag(holder);
        } else {
            holder = (HistoryViewHolder) convertView.getTag();
        }
        holder.id.setText(t.getTicketId());
        holder.date.setText("26-01-1994");
        holder.price.setText(Double.toString(t.getTicketPrice()));
        return convertView;
    }

    @Override
    public boolean isEmpty() {
        return mListData.isEmpty();
    }
}
