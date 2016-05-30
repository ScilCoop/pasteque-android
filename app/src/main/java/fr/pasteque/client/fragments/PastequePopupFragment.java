package fr.pasteque.client.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import fr.pasteque.client.R;

/**
 * Created by svirch_n on 26/05/16
 * Last edited at 10:45.
 */
public abstract class PastequePopupFragment extends DialogFragment {


    protected TextView title;
    protected TextView positiveTitle;
    protected TextView negativeTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.popup_window);
        setCancelable(false);
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View result = inflater.inflate(R.layout.popup_model, container, false);
        FrameLayout frameContainer = (FrameLayout) result.findViewById(R.id.container);
        title = (TextView) result.findViewById(R.id.title);
        positiveTitle = (TextView) result.findViewById(R.id.btn_positive);
        positiveTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PastequePopupFragment.this.onPositiveClickListener();
            }
        });
        negativeTitle = (TextView) result.findViewById(R.id.btn_negative);
        negativeTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PastequePopupFragment.this.onNegativeClickListener();
            }
        });
        View child = onCreateFrameView(inflater, frameContainer, savedInstanceState);
        if (child != null) {
            frameContainer.addView(child);
        }
        return result;
    }

    protected abstract void onNegativeClickListener();

    protected abstract void onPositiveClickListener();

    public abstract View onCreateFrameView(LayoutInflater inflater, FrameLayout frameContainer, Bundle savedInstanceState);

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setPositiveTitle(String title) {
        this.positiveTitle.setText(title);
    }

    public void setNegativeTitle(String title) {
        this.negativeTitle.setText(title);
    }
}
