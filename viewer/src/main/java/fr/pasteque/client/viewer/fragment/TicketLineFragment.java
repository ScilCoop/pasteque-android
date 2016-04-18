package fr.pasteque.client.viewer.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import fr.pasteque.client.viewer.MainActivity;
import fr.pasteque.client.viewer.Pasteque;
import fr.pasteque.client.viewer.R;
import fr.pasteque.client.viewer.models.SharedTicketsHolder;
import fr.pasteque.client.viewer.models.Ticket;
import fr.pasteque.client.viewer.models.TicketLine;
import fr.pasteque.client.viewer.utils.Window;
import org.w3c.dom.Text;

import static android.view.View.MeasureSpec;

/**
 * Created by svirch_n on 14/04/16
 * Last edited at 17:50.
 */
public class TicketLineFragment extends Fragment {

    private SharedTicketsHolder sharedTicketsHolder;
    private GridLayout gridlLayout;
    private LayoutInflater inflater;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        View view = inflater.inflate(R.layout.grid_view, container);
        gridlLayout = (GridLayout) view.findViewById(R.id.grid);
        update();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Window.calcWindow(activity);
        this.sharedTicketsHolder = ((MainActivity) activity).getSharedTicketsHolder();
    }

    public void notifyDataSetInvalidated() {
        update();
    }

    private void update() {
        gridlLayout.removeAllViewsInLayout();
        int padding = 4;
        int row = 0;
        int column = 0;
        boolean[][] board = new boolean[Pasteque.getConf().getRowSize() + 1][Pasteque.getConf().getColumnSize()];
        for (Ticket ticket : this.sharedTicketsHolder) {
            ListView listView = (ListView) this.inflater.inflate(R.layout.product_holder, null);
            TextView header = getListVIewHeader(ticket);
            listView.addHeaderView(header);
            listView.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.list_item, ticket.lines));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = Window.getWidth() / Pasteque.getConf().getColumnSize() - padding * 2;
            params.setMargins(padding, padding, padding, padding);
            GridLayout.Spec rowSpec;
            GridLayout.Spec columnSpec = GridLayout.spec(column);
            int height = Window.getHeight();
            board[row][column] = true;
            if (getListViewHeight(listView, header) > (height / 2) && (row + 1) < Pasteque.getConf().getRowSize()) {
                rowSpec = GridLayout.spec(row, 2);
                params.height = height - padding * 2 - 20;
                board[row + 1][column] = true;
            } else {
                rowSpec = GridLayout.spec(row, 1);
                params.height = height / Pasteque.getConf().getRowSize() - padding * 2 - 8;
            }
            params.columnSpec = columnSpec;
            params.rowSpec = rowSpec;
            listView.setLayoutParams(params);

            gridlLayout.addView(listView);
            while (board[row][column]) {
                column++;
                if (column >= Pasteque.getConf().getColumnSize()) {
                    column = 0;
                    row++;
                    if (row >= Pasteque.getConf().getRowSize()) {
                        Log.d("Pasteque", "Max Cells");
                        break;
                    }
                }
            }
        }
    }

    private int getListViewHeight(ListView list, TextView header) {
        ListAdapter adapter = list.getAdapter();
        int listHeight;
        list.measure(MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        header.measure(MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        listHeight = list.getMeasuredHeight() * adapter.getCount() + (adapter.getCount() * list.getDividerHeight()) + header.getMeasuredHeight();
        return listHeight;
    }

    private TextView getListVIewHeader(Ticket ticket) {
        TextView textView = (TextView) this.inflater.inflate(R.layout.list_item, null);
        textView.setGravity(Gravity.CENTER);
        textView.setText(ticket.label);
        return textView;
    }


}
