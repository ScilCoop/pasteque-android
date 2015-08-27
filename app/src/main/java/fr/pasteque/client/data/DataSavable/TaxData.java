package fr.pasteque.client.data.DataSavable;

import fr.pasteque.client.models.Tax;
import fr.pasteque.client.utils.exception.DataCorruptedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nsvir on 27/08/15.
 * n.svirchevsky@gmail.com
 */
public class TaxData extends AbstractDataSavable {

    private final static String FILENAME = "taxes.data";

    public ArrayList<Tax> taxes = new ArrayList<>();

    @Override
    protected String getFileName() {
        return FILENAME;
    }

    @Override
    protected List<Object> getObjectList() {
        List<Object> result = new ArrayList<>();
        result.add(taxes);
        return result;
    }

    @Override
    protected int getNumberOfObjects() {
        return 1;
    }

    @Override
    protected void recoverObjects(List<Object> objs) throws DataCorruptedException {
        this.taxes = (ArrayList<Tax>) objs.get(0);
    }

    public void setTaxes(HashMap<String, Double> taxes) {
        Map<String, Double> list = taxes;
        this.taxes.clear();
        for (Map.Entry<String, Double> entry: list.entrySet()) {
            this.taxes.add(new Tax(entry.getKey(), entry.getValue()));
        }
    }

    public ArrayList<Tax> getTaxes() {
        return this.taxes;
    }
}
