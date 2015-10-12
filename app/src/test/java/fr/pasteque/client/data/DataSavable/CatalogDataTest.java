package fr.pasteque.client.data.DataSavable;

import fr.pasteque.client.Constant;
import fr.pasteque.client.data.AbstractDataTest;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.utils.exception.DataCorruptedException;
import org.easymock.IAnswer;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by nsvir on 12/10/15.
 * n.svirchevsky@gmail.com
 */
public class CatalogDataTest extends AbstractDataTest {

    @Override
    public String getTmpFilename() {
        return "catalog.json";
    }

    @Test
    public void save() throws FileNotFoundException, DataCorruptedException {
        addFileInputExpected(new IAnswer<FileInputStream>() {
            @Override
            public FileInputStream answer() throws Throwable {
                return new FileInputStream(Constant.JSON_FOLDER + "catalog.json");
            }
        });
        replayContext();
        CatalogData catalogData = new CatalogData();
        catalogData.load(fakeContext);
    }

    @Test
    public void simpleCatalog() throws FileNotFoundException {
        addDefaultFileOutputExpected();
        replayContext();
        CatalogData catalogData = new CatalogData();
        catalogData.setCatalog(new Catalog());
        catalogData.save(fakeContext);
    }
}