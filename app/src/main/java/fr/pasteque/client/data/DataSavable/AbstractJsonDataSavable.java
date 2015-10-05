package fr.pasteque.client.data.DataSavable;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import fr.pasteque.client.models.Cash;
import fr.pasteque.client.utils.File;
import fr.pasteque.client.utils.exception.DataCorruptedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOError;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nsvir on 05/10/15.
 * n.svirchevsky@gmail.com
 */
public abstract class AbstractJsonDataSavable extends AbstractDataSavable {

    protected File file = new File(getFileName());

    @Override
    public void save(Context ctx) throws IOError {
        this.save(ctx, getObjectList());
    }

    @Override
    public void load(Context ctx) throws DataCorruptedException, IOError {
        int objectsNumber = getNumberOfObjects();
        List<Class> classes = getClassList();
        List<Object> result = new ArrayList<>();
        Gson gson = getGson();
        try {
            JSONObject obj = new JSONObject(file.read());
            JSONArray jsonArray = obj.getJSONArray("");
            for (int i = 0; i < objectsNumber; i++) {
                result.add(i, gson.fromJson((JsonElement) jsonArray.get(i), classes.get(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is an ugly hack because generics in java is complicated.
     * GSON needs to know the Class type of the JSON object when creating a new object
     * Feel free to find a better solution
     * @return this list of class in the order of getObjectList
     */
    protected abstract List<Class> getClassList();

    @Override
    public boolean onLoadingFailed(DataCorruptedException e) {
        return false;
    }

    @Override
    public boolean onLoadingError(IOError e) {
        return false;
    }

    private void save(Context ctx, List<Object> objs) {
        JSONArray jsonArray = new JSONArray();
        for (Object obj : objs) {
            jsonArray.put(getGson().toJson(obj));
        }
        file.write(jsonArray.toString());
    }

    protected Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        return builder.create();
    }
}
