package fr.pasteque.client.unit.models;

import fr.pasteque.client.models.Barcode;
import fr.pasteque.client.models.Discount;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONParser;

public class DiscountTest {
    
    private final static String FILENAME_MODEL = "json/Discount.json";
    private final Discount discount_model;
    private final JSONObject discount_json_model;
    private final String id = "1";
    private final double rate = 0.25;
    private final String label = "test";
    private final String endDate = "2015-07-29";
    private final int barcodeType = Barcode.QR;
    private final String barcode = "DISC_0928349";
    private final int dispOrder = 0;
    private final String startDate = "2015-06-30";
    
    public DiscountTest() throws ParseException, IOException, JSONException {
        this.discount_model = new Discount(id, rate, startDate, endDate, barcode, barcodeType);
        
        String string = readFileAsString(FILENAME_MODEL);
        this.discount_json_model = (JSONObject) JSONParser.parseJSON(string);
    }
    
    private String readFileAsString(String filePath) throws IOException {
        StringBuilder fileData = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }
    
    @Test
    public void getterTest() throws ParseException {
        Assert.assertEquals(this.id, this.discount_model.getId());
        Assert.assertEquals(this.rate, this.discount_model.getRate(), 0.001);
        Assert.assertEquals(this.startDate, Discount.convertDateToString(this.discount_model.getStartDate()));
        Assert.assertEquals(this.endDate, Discount.convertDateToString(this.discount_model.getEndDate()));
        Assert.assertEquals(this.barcode, this.discount_model.getBarcode());
        Assert.assertEquals(this.barcodeType, this.discount_model.getBarcodeType());
    }
    
    private void compare(Discount d1, Discount d2) throws ParseException {
        Assert.assertEquals(d1.getId(), this.discount_model.getId());
        Assert.assertEquals(d1.getRate(), this.discount_model.getRate(), 0.001);
        Assert.assertEquals(Discount.convertDateToString(d1.getStartDate()), Discount.convertDateToString(this.discount_model.getStartDate()));
        Assert.assertEquals(Discount.convertDateToString(d1.getEndDate()), Discount.convertDateToString(this.discount_model.getEndDate()));
        Assert.assertEquals(d1.getBarcode(), this.discount_model.getBarcode());
        Assert.assertEquals(d1.getBarcodeType(), this.discount_model.getBarcodeType());
    }
    
    @Test
    public void setterTest() throws ParseException {
        Discount disc = new Discount("oij", 0.1, "9823-09-09", "0001-09-09", "non", 54);
        disc.setId(this.id);
        disc.setBarcode(this.barcode);
        disc.setBarcodeType(this.barcodeType);
        disc.setEndDate(this.endDate);
        disc.setStartDate(this.startDate);
        disc.setRate(this.rate);
        compare(disc, this.discount_model);
    }
    
    @Test
    public void toJSONTest() throws Exception {
        
    }
    
    @Test
    public void fromJSONTest() {
        
    }
    
    // ---Pour permettre l'exécution des test----------------------                                                                                                                                                  
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }
    
    public static junit.framework.Test suite() {
	return new junit.framework.JUnit4TestAdapter(DiscountTest.class);
    }

}
