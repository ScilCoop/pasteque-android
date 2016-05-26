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
import android.widget.FrameLayout;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.R;
import fr.pasteque.client.data.Data;
import fr.pasteque.client.models.LocalTicket;
import fr.pasteque.client.models.Ticket;

/**
 * Created by svirch_n on 25/05/16
 * Last edited at 17:33.
 */
public class DividerDialog extends PastequePopupFragment {

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
    protected void onNegativeClickListener() {
        this.dismiss();
    }

    @Override
    protected void onPositiveClickListener() {
        this.dismiss();
        this.resultListener.onDividerDialogResult(Data.Session.currentSession().newLocalTicket(null));
    }

    @Override
    public View onCreateFrameView(LayoutInflater inflater, FrameLayout frameContainer, Bundle savedInstanceState) {
        setTitle(Pasteque.getStringResource(R.string.menu_divider));
        setPositiveTitle(Pasteque.getStringResource(R.string.divider_button_positive));
        setNegativeTitle(Pasteque.getStringResource(R.string.divider_button_negative));
        return null;
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
