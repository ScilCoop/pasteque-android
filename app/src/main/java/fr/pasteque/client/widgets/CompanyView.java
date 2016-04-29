package fr.pasteque.client.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import fr.pasteque.client.R;
import fr.pasteque.client.models.Company;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by svirch_n on 26/04/16
 * Last edited at 12:06.
 */
public class CompanyView extends RelativeLayout {
    
    private static final String DEFAULT_TEXT = "N/A";
    private static final int PADDING = 0;
    private static int name = 1;
    private static int address = 2;
    private static int city = 3;
    private static int country = 4;
    private final Map<Integer, TextView> views = new HashMap<>();
    private ImageView logo;

    public CompanyView(Context context) {
        super(context);
        this.init();
    }

    public CompanyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public CompanyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CompanyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    private void init() {
        setPadding(PADDING, PADDING, PADDING, PADDING);
        int[] ids = {CompanyView.name, CompanyView.address, CompanyView.city, CompanyView.country};
        int above = 0;
        for (int each: ids) {
            TextView textView = (TextView) inflate(getContext(), R.layout.pdf_text_line, null);
            textView.setId(each);
            textView.setText(CompanyView.DEFAULT_TEXT);
            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (above != 0) {
                layoutParams.addRule(RelativeLayout.BELOW, above);
            }
            textView.setLayoutParams(layoutParams);
            addView(textView);
            views.put(each, textView);
            above = each;
        }
        logo = (ImageView) inflate(getContext(), R.layout.pdf_customer_logo, null);
        RelativeLayout.LayoutParams params = (LayoutParams) this.generateDefaultLayoutParams();
        params.addRule(CENTER_IN_PARENT, TRUE);
        logo.setLayoutParams(params);
        logo.setVisibility(INVISIBLE);
        addView(logo);
    }

    public void setCompany(Company company) {
        showText();
        setText(name, company.name);
        setText(address, company.address.address);
        setText(city, company.address.city + " " + company.address.postCode);
        setText(country, company.address.country);
    }

    private void setText(int id, String text) {
        views.get(id).setText(text);
    }

    public void noCompany() {
        showLogo();
    }

    private void showLogo() {
        for (TextView each: views.values()) {
            each.setVisibility(INVISIBLE);
        }
        logo.setVisibility(VISIBLE);
        setText(name, "A PADDING TEXT");
    }

    private void showText() {
        for (TextView each: views.values()) {
            each.setVisibility(VISIBLE);
        }
        logo.setVisibility(INVISIBLE);
    }
}
