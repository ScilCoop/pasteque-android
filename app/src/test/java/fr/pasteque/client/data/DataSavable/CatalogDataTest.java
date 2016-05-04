package fr.pasteque.client.data.DataSavable;

import fr.pasteque.client.Constant;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.utils.exception.DataCorruptedException;
import org.junit.Test;

import java.io.FileNotFoundException;

import static junit.framework.Assert.assertEquals;

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
        replayContext();
        CatalogData catalogData = new CatalogData();
        catalogData.setFile(createCustomFile(Constant.JSON_FOLDER + "catalog.json"));
        catalogData.load(fakeContext);
    }

    @Test
    public void simpleCatalog() throws FileNotFoundException {
        replayContext();
        CatalogData catalogData = new CatalogData();
        catalogData.setCatalog(new Catalog());
        catalogData.setFile(createDefaultTmpFile());
        catalogData.save(fakeContext);
    }

    @Test
    public void readCatalog() throws Throwable {
        replayContext();
        CatalogData catalogData = new CatalogData();
        catalogData.setFile(createDefaultTmpFile());
        try {
            catalogData.load(fakeContext);
        } catch (DataCorruptedException e) {
            throw e.Exception;
        }
        System.out.println(catalogData.toString());
    }
}