package fr.pasteque.client.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import fr.pasteque.client.R;
import fr.pasteque.client.data.Data;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.Category;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.interfaces.Item;
import fr.pasteque.client.widgets.BtnItem;
import fr.pasteque.client.widgets.ItemAdapter;

import java.util.ArrayList;

public class CatalogFragment extends ViewPageFragment {

    private static final String LABEL_CURRENT_CATEGORY = "currentCategory";

    public interface Listener {
        void onCfProductClicked(Product product, Catalog catalogData);

        boolean onCfProductLongClicked(Product product);

        void OnCfCatalogViewChanged(boolean visible, Category category);
    }

    //General
    private Listener mListener;
    //Data
    private Catalog mCatalogData;
    private Category mCurrentCategory;
    private ItemAdapter mViewProductsAdp;
    //View
    private GridView mViewCategories;
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
        mListener.OnCfCatalogViewChanged(mCurrentCategory != null, mCurrentCategory);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.catalog_block, container, false);
        mViewCategories = (GridView) layout.findViewById(R.id.categoriesGrid);
        mViewCategories.setAdapter(new ItemAdapter(mCatalogData.getAllCategories()));
        mViewCategories.setOnItemClickListener(new CategoryItemClickListener());

        mViewProductsAdp = new ItemAdapter(new ArrayList<Item>());

        mViewProducts = (GridView) layout.findViewById(R.id.productsGrid);
        mViewProducts.setOnItemClickListener(new ProductItemClickListener());
        mViewProducts.setOnItemLongClickListener(new ProductItemLongClickListener());
        mViewProducts.setAdapter(mViewProductsAdp);

        updateGridView();
        updateProductsView();
        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("catalog", mCatalogData);
        outState.putSerializable(LABEL_CURRENT_CATEGORY, mCurrentCategory);
    }

    public final Catalog getCatalogData() {
        return mCatalogData;
    }

    public boolean displayProducts() {
        return mViewProducts.getVisibility() == View.VISIBLE;
    }

    /*
     * PRIVATES
     */

    private void updateGridView() {
        if (mCurrentCategory == null) {
            setCategoriesVisible();
        } else {
            setProductsVisible();
        }
    }

    private void reuseData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            // From scratch
            mCatalogData = Data.Catalog.catalog(mContext);
            mCurrentCategory = null;
        } else {
            // From state
            mCatalogData = (Catalog) savedInstanceState.getSerializable("catalog");
            mCurrentCategory = (Category) savedInstanceState.getSerializable(LABEL_CURRENT_CATEGORY);
        }
    }

    public void setCategoriesVisible() {
        mListener.OnCfCatalogViewChanged(true, null);
        mViewCategories.setVisibility(View.VISIBLE);
        mViewProducts.setVisibility(View.INVISIBLE);
    }

    public void setProductsVisible() {
        mListener.OnCfCatalogViewChanged(false, mCurrentCategory);
        mViewCategories.setVisibility(View.INVISIBLE);
        mViewProducts.setVisibility(View.VISIBLE);
    }

    public Category getCurrentCategory() {
        return mCurrentCategory;
    }

    private void updateProductsView() {
        if (mCurrentCategory != null) {
            mViewProductsAdp.updateView(mCatalogData.getProducts(mCurrentCategory));
        }
    }

    /*
     * LISTENERS
     */

    private class CategoryItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mCurrentCategory = (Category) ((BtnItem) view).getItem();
            CatalogFragment.this.updateProductsView();
            CatalogFragment.this.setProductsVisible();
        }
    }

    private class ProductItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mListener.onCfProductClicked((Product) ((BtnItem) view).getItem(), getCatalogData());
            CatalogFragment.this.setCategoriesVisible();
            mCurrentCategory = null;
        }
    }

    private class ProductItemLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            return mListener.onCfProductLongClicked((Product) ((BtnItem) view).getItem());
        }
    }
}
