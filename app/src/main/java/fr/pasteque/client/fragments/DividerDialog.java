package fr.pasteque.client.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.R;
import fr.pasteque.client.models.LocalTicket;
import fr.pasteque.client.models.Ticket;

/**
 * Created by svirch_n on 25/05/16
 * Last edited at 17:33.
 */
public class DividerDialog extends DialogFragment {

    public static final String TAG = "DIVIDER_DIALOG";
    private static final String TICKET_TAG = "TICKET_TAG";

    private ResultListener resultListener;
    private Ticket ticketToDivide;

    public interface ResultListener {
        void onDividerDialogResult(LocalTicket createdTicket);
    }

    public interface RequestResultListener {
        ResultListener onDividerDialogRequestResultListener();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View result = inflater.inflate(R.layout.divider_dialog_fragment, container, false);
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        ticketToDivide = (Ticket) getArguments().getSerializable(TICKET_TAG);
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
}
