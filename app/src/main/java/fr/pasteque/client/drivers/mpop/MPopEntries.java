package fr.pasteque.client.drivers.mpop;

import java.util.ArrayList;

/**
 * Created by svirch_n on 21/12/15.
 */
public class MPopEntries extends ArrayList<MPopEntry> {

    public CharSequence[] getEntries() {
        CharSequence[] result = new CharSequence[size()];
        for (int i = 0; i < size(); i++) {
            result[i] = this.get(i).name;
        }
        return result;
    }

    public CharSequence[] getValues() {
        CharSequence[] result = new CharSequence[size()];
        for (int i = 0; i < size(); i++) {
            result[i] = this.get(i).value;
        }
        return result;
    }
}
