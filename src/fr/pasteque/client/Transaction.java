package fr.pasteque.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.pasteque.client.data.CompositionData;
import fr.pasteque.client.fragments.PaymentFragment;
import fr.pasteque.client.fragments.ProductScaleDialog;
import fr.pasteque.client.fragments.CatalogFragment;
import fr.pasteque.client.fragments.TicketFragment;
import fr.pasteque.client.fragments.ViewPageFragment;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.CompositionInstance;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.utils.TrackedActivity;
import fr.pasteque.client.widgets.ProductBtnItem;

public class Transaction extends TrackedActivity
        implements CatalogFragment.ProductsCatalogListener,
        ProductScaleDialog.Listener {

    //List of codes. Java enums sucks...
    public static final int COMPOSITION = 1;

    private final String LOG_TAG = "Pasteque/Transaction";
    private final int PRODUCTS_CAT_FRAG = 0;
    private final int TICKET_FRAG = 1;
    private final int PAYMENT_FRAG = 2;
    private final TransPage[] PAGES = new TransPage[]{
            new TransPage(0.65f, CatalogFragment.class),
            new TransPage(0.35f, TicketFragment.class),
            new TransPage(0.65f, PaymentFragment.class)};

    private Context mContext;
    private ViewPager mPager;
    private TransactionPagerAdapter mPagerAdapter;

    private class TransPage {
        // Between 0.0 - 1.0
        private float mWidth;
        private String mTag;
        private Class<? extends ViewPageFragment> mPageFragment;

        public TransPage(float width, @NonNull Class<? extends ViewPageFragment> pageFragment) {
            mWidth = width;
            mPageFragment = pageFragment;
        }

        public float getWidth() {
            return mWidth;
        }

        public Class<? extends ViewPageFragment> getPageFragment() {
            return mPageFragment;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();
        mPagerAdapter = new TransactionPagerAdapter(getFragmentManager());
        mPager = new ViewPager(mContext);
        // There is View.generateViewId() but min_api < 17
        mPager.setId(R.id.transaction_view_pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setBackgroundResource(R.color.main_bg);
        setContentView(mPager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case COMPOSITION:
                if (resultCode == Activity.RESULT_OK) {
                    CompositionInstance compo = (CompositionInstance)
                            data.getSerializableExtra("composition");
                    TicketFragment ticket = (TicketFragment) mPagerAdapter.getFragment(TICKET_FRAG);
                    ticket.addProduct(compo);
                    ticket.updateView();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onProductClicked(ProductBtnItem item, Catalog catalogData) {
        Product p = item.getProduct();
        if (CompositionData.isComposition(p)) {
            Intent i = new Intent(this, CompositionInput.class);
            CompositionInput.setup(catalogData, CompositionData.getComposition(p.getId()));
            this.startActivityForResult(i, COMPOSITION);
        } else if (p.isScaled()) {
            // If the product is scaled, asks the weight
            ProductScaleDialog dial = ProductScaleDialog.newInstance(p);
            dial.show(getFragmentManager(), dial.getTag());
        } else {
            TicketFragment ticket = (TicketFragment) mPagerAdapter.getFragment(TICKET_FRAG);
            ticket.addProduct(p);
            ticket.updateView();
        }
    }

    @Override
    public boolean onProductLongClicked(ProductBtnItem item) {
        Product p = item.getProduct();
        TicketFragment ticket = (TicketFragment) mPagerAdapter.getFragment(TICKET_FRAG);
        String message = getString(R.string.prd_info_price,
                p.getTaxedPrice(ticket.getTariffArea()));

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(p.getLabel());
        b.setMessage(message);
        b.setNeutralButton(android.R.string.ok, null);
        b.show();
        return true;
    }

    @Override
    public void onProductScaleDialogPositiveClick(Product p, Double weight) {
        TicketFragment ticket = (TicketFragment) mPagerAdapter.getFragment(TICKET_FRAG);
        ticket.addScaledProduct(p, weight);
        ticket.updateView();
    }

    /**
     * ADAPTERS
     */

    private class TransactionPagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<Fragment> mFragmentReferences;

        public TransactionPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentReferences = new SparseArray<Fragment>();
        }

        @Override
        public Fragment getItem(int position) {
            Class<? extends ViewPageFragment> cls = PAGES[position].getPageFragment();
            ViewPageFragment result;
            try {
                Method newInstance = cls.getMethod("newInstance", int.class);
                result = (ViewPageFragment) newInstance.invoke(null, position);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                throw new RuntimeException(cls.getName() +
                        " must implement static newInstance function");
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage());
            }
            return result;
        }

        @Override
        public int getCount() {
            return PAGES.length;
        }

        @Override
        public float getPageWidth(int position) {
            return PAGES[position].getWidth();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            mFragmentReferences.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mFragmentReferences.remove(position);
            super.destroyItem(container, position, object);
        }

        // TODO: Handle when offscreen (+ page limit)
        // i.e, Force instantiate then delete.
        public Fragment getFragment(int position) {
            return mFragmentReferences.get(position);
        }
    }
}
