package fr.pasteque.client.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.GridView;

import fr.pasteque.client.R;
import fr.pasteque.client.data.CatalogData;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.Category;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.widgets.CategoriesAdapter;
import fr.pasteque.client.widgets.CategoryItem;
import fr.pasteque.client.widgets.ProductBtnItem;
import fr.pasteque.client.widgets.ProductsBtnAdapter;

public class CatalogFragment extends ViewPageFragment {

    public interface Listener {
        void onCfProductClicked(Product product, Catalog catalogData);

        boolean onCfProductLongClicked(Product product);
    }

    //General
    private Listener mListener;
    //Data
    private Catalog mCatalogData;
    private Category mCurrentCategory;
    private ProductsBtnAdapter mViewProductsAdp;
    //View
    private Gallery mViewCategories;
    private GridView mViewProducts;

    @SuppressWarnings("unused") // Used via class reflection
    public static CatalogFragment newInstance(int pageNumber) {
        CatalogFragment frag = new CatalogFragment();
        ViewPageFragment.initPageNumber(pageNumber, frag);
        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ProductsCatalogListener!");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reuseData(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.catalog_block, container, false);
        mViewCategories = (Gallery) layout.findViewById(R.id.categoriesGrid);
        mViewCategories.setAdapter(new CategoriesAdapter(mCatalogData.getAllCategories()));
        mViewCategories.setOnItemSelectedListener(new CategoryItemSelectedListener());
        mViewCategories.setSelection(0, false);

        mViewProductsAdp = new ProductsBtnAdapter(mCatalogData.getProducts(mCurrentCategory));

        mViewProducts = (GridView) layout.findViewById(R.id.productsGrid);
        mViewProducts.setOnItemClickListener(new ProductItemClickListener());
        mViewProducts.setOnItemLongClickListener(new ProductItemLongClickListener());
        mViewProducts.setAdapter(mViewProductsAdp);
        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("catalog", mCatalogData);
    }

    public final Catalog getCatalogData() {
        return mCatalogData;
    }

    /*
     * PRIVATES
     */

    private void reuseData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            // From scratch
            mCatalogData = CatalogData.catalog(mContext);
        } else {
            // From state
            mCatalogData = (Catalog) savedInstanceState.getSerializable("catalog");
        }
        mCurrentCategory = mCatalogData.getAllCategories().get(0);
    }

    private void updateProductsView() {
        mViewProductsAdp.updateView(mCatalogData.getProducts(mCurrentCategory));
    }

    /*
     * LISTENERS
     */

    private class CategoryItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mCurrentCategory = ((CategoryItem) view).getCategory();
            CatalogFragment.this.updateProductsView();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private class ProductItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mListener.onCfProductClicked(((ProductBtnItem) view).getProduct(), getCatalogData());
        }
    }

    private class ProductItemLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            return mListener.onCfProductLongClicked(((ProductBtnItem) view).getProduct());
        }
    }
}
