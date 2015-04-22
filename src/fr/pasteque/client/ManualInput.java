package fr.pasteque.client;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

/*
** Maybe do a general class based on this one ? Might be useful
*/
public class ManualInput extends DialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
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
        tabpage1.setIndicator(getString(R.string.menu_manual_input));

        TabHost.TabSpec tabpage2 = tabs.newTabSpec("tab2");
        tabpage2.setContent(R.id.input_barcode);
        tabpage2.setIndicator(getString(R.string.menu_barcode));

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

        return layout;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setCanceledOnTouchOutside(true);
//        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        dialog.getWindow().setLayout(mWidth, mHeight);
        return dialog;
    }
}
