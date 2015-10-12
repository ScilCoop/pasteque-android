package fr.pasteque.client.data.DataSavable;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import fr.pasteque.client.models.Cash;
import fr.pasteque.client.models.Discount;
import fr.pasteque.client.models.User;
import fr.pasteque.client.utils.File;
import fr.pasteque.client.utils.exception.DataCorruptedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOError;
import java.lang.reflect.Type;
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
        List<Type> classes = getClassList();
        List<Object> result = new ArrayList<>();
        Gson gson = getGson();
        String stringFile = null;
        try {
            stringFile = file.read();
            JSONObject obj = new JSONObject(stringFile);
            for (int i = 0; i < objectsNumber; i++) {
                Object objectToAdd = gson.fromJson((String) obj.get(String.valueOf(i)), classes.get(i));
                result.add(i, objectToAdd);
            }
        } catch (JSONException e) {
            throw new IOError(e);
        } catch (JsonSyntaxException | FileNotFoundException e) {
            throw newException(e, stringFile);
        }
        if (result.size() != getObjectList().size()) {
            throw newException(null, stringFile);
        }
        this.recoverObjects(result);
    }

    private DataCorruptedException newException(Throwable e, String stringFile) {
        return new DataCorruptedException(e, DataCorruptedException.Action.LOADING)
                .addFileName(getFileName())
                .addObjectList(getObjectList())
                .addFileContent(stringFile);
    }


    protected DataCorruptedException newException(Throwable e) {
        return new DataCorruptedException(e, DataCorruptedException.Action.LOADING)
                .addFileName(getFileName())
                .addObjectList(getObjectList());
    }

    /**
     * This is an ugly hack because generics in java is complicated.
     * GSON needs to know the Class type of the JSON object when creating a new object
     * Feel free to find a better solution
     * @return this list of class in the order of getObjectList
     */
    protected abstract List<Type> getClassList();

    @Override
    public boolean onLoadingFailed(DataCorruptedException e) {
        return false;
    }

    @Override
    public boolean onLoadingError(IOError e) {
        return false;
    }

    private void save(Context ctx, List<Object> objs) {
        JSONObject jsonObject = new JSONObject();
        int i = 0;
        for (Object obj : objs) {
            try {
                String object = getGson().toJson(obj);
                jsonObject.put(String.valueOf(i), object);
            } catch (JSONException | UnsupportedOperationException e) {
                throw new IOError(e);
            }
            i++;
        }
        file.write(jsonObject.toString());
    }

    protected Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        return builder.create();
    }
}
