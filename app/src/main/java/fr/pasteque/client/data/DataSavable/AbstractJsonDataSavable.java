package fr.pasteque.client.data.DataSavable;

import android.content.Context;
import android.util.Log;
import com.google.gson.*;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.utils.file.ExternalFile;
import fr.pasteque.client.utils.file.File;
import fr.pasteque.client.utils.exception.DataCorruptedException;
import fr.pasteque.client.utils.file.InternalFile;

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

    private static final String TAG_LOG = "pasteque/json_data";
    private static final String JSON_DIRECTORY = "json";
    protected File file = new InternalFile(JSON_DIRECTORY, getFileName());

    protected void setFile(File file) {
        this.file = file;
    }

    @Override
    public void save(Context ctx) throws IOError {
        this.save(ctx, getObjectList(), this.file);
    }

    public static String getDirectory() {
        return JSON_DIRECTORY;
    }

    @Override
    public void load(Context ctx) throws DataCorruptedException, IOError {
        int objectsNumber = getNumberOfObjects();
        List<Type> classes = getClassList();
        List<Object> result = new ArrayList<>();
        Gson gson = getGson();
        JsonParser parser = new JsonParser();
        String stringFile = null;
        try {
            stringFile = file.read();
            JsonElement tradeElement = parser.parse(stringFile);
            JsonArray array = tradeElement.getAsJsonArray();
            for (int i = 0; i < objectsNumber; i++) {
                Object objectToAdd = gson.fromJson(gson.toJson(array.get(i)), classes.get(i));
                result.add(i, objectToAdd);
            }
        } catch (JsonSyntaxException | FileNotFoundException e) {
            throw newException(e, stringFile);
        }
        if (result.size() != getObjectList().size()) {
            throw newException(null, stringFile);
        }
        this.recoverObjects(result);
    }

    @Override
    public void export(String dir) {
        this.save(Pasteque.getAppContext(), getObjectList(), new ExternalFile(dir, getFileName()));
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

    protected void save(Context ctx, List<Object> objs, File file) {
        JsonArray array = new JsonArray();
        JsonParser parser = new JsonParser();
        for (Object obj : objs) {
            try {
                JsonElement object = parser.parse(getGson().toJson(obj));
                array.add(object);
            } catch (UnsupportedOperationException e) {
                throw new IOError(e);
            }
        }
        try {
            file.write(array.toString());
        } catch (FileNotFoundException e) {
            Log.e(TAG_LOG, "JsonDataSavable::save error",e);
        }
    }

    protected Gson getGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .enableComplexMapKeySerialization()
                .create();
    }
}
