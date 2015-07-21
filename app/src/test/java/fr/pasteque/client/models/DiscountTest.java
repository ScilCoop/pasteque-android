package fr.pasteque.client.models;

import fr.pasteque.client.models.Barcode;
import fr.pasteque.client.models.Discount;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONParser;

public class DiscountTest {

    private final static String FILENAME_MODEL = "json/Discount.json";
    private Discount discount_model;
    private JSONObject discount_json_model;
    private String id;
    private double rate;
    private String label;
    private String endDate;
    private int barcodeType;
    private String barcode;
    private int dispOrder;
    private String startDate;

    @Before
    public void setUp() throws ParseException, IOException, JSONException {
        this.startDate = "2015-06-30";
        this.dispOrder = 0;
        this.barcode = "DISC_0928349";
        this.barcodeType = Barcode.QR;
        this.endDate = "2015-07-29";
        this.label = "test";
        this.rate = 0.25;
        this.id = "1";
        
        this.discount_model = new Discount(id, rate, startDate, endDate, "",  barcodeType);

        String string = readFileAsString(FILENAME_MODEL);
        this.discount_json_model = (JSONObject) JSONParser.parseJSON(string);
    }

    private String readFileAsString(String filePath) throws IOException {
        StringBuilder fileData = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void constructorTest() throws ParseException {
        try {
            new Discount(null, 0, null, null, null, 5);
            throw new AssertionError("barcode null must show Log.w");
        } catch (RuntimeException e) {
            //Stub expected because of null barcode
        }
        new Discount(null, 0, null, null, "", 5);
    }
    
    @Test
    public void getterTest() throws ParseException {
        Assert.assertEquals(this.id, this.discount_model.getId());
        Assert.assertEquals(this.rate, this.discount_model.getRate(), 0.001);
        Assert.assertEquals(this.startDate, Discount.convertDateToString(this.discount_model.getStartDate()));
        Assert.assertEquals(this.endDate, Discount.convertDateToString(this.discount_model.getEndDate()));
        Assert.assertEquals(this.barcode, this.discount_model.getBarcode());
        Assert.assertEquals(this.barcodeType, this.discount_model.getBarcode().getType());
    }

    private void compare(Discount d1, Discount d2) throws ParseException {
        Assert.assertEquals(d1.getId(), this.discount_model.getId());
        Assert.assertEquals(d1.getRate(), this.discount_model.getRate(), 0.001);
        Assert.assertEquals(Discount.convertDateToString(d1.getStartDate()), Discount.convertDateToString(this.discount_model.getStartDate()));
        Assert.assertEquals(Discount.convertDateToString(d1.getEndDate()), Discount.convertDateToString(this.discount_model.getEndDate()));
        Assert.assertEquals(d1.getBarcode().getCode(), this.discount_model.getBarcode().getCode());
        Assert.assertEquals(d1.getBarcode().getType(), this.discount_model.getBarcode().getType());
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
        JSONObject obj = this.discount_model.toJSON();
        org.skyscreamer.jsonassert.JSONAssert.assertEquals(obj, this.discount_json_model, false);
    }

    @Test
    public void fromJSONTest() throws JSONException, ParseException {
        Discount discount = Discount.fromJSON(this.discount_json_model);
        this.compare(discount, this.discount_model);
    }

    @Test
    public void isValideTest() throws ParseException {

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.HOUR, 1);
        Date more1 = calendar.getTime();
        calendar.add(Calendar.HOUR, 1);
        Date more2 = calendar.getTime();
        calendar.add(Calendar.HOUR, -3);
        Date less1 = calendar.getTime();
        calendar.add(Calendar.HOUR, -1);
        Date less2 = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH");
        System.out.println("now: " + format.format(now) + "h");
        System.out.println("more1: " + format.format(more1) + "h");
        System.out.println("more2: " + format.format(more2) + "h");
        System.out.println("less1: " + format.format(less1) + "h");
        System.out.println("less2: " + format.format(less2) + "h");
        
        Discount discount = new Discount(id, rate, startDate, endDate, barcode, barcodeType);

        //Event not started
        discount.setStartDate(less2);
        discount.setEndDate(less1);
        assertFalse(discount.isValid());
        
        
        //Event ended
        discount.setStartDate(more1);
        discount.setEndDate(more2);
        assertFalse(discount.isValid());

        //Event corrupted
        discount.setStartDate(more1);
        discount.setEndDate(less1);
        try {
            discount.isValid();
            throw new AssertionError("Descending dates must throw error when compared");
        } catch (RuntimeException e) {
        }
        
        //Valide events
        discount.setStartDate(less1);
        discount.setEndDate(more1);
        assertTrue(discount.isValid());
    }

    // ---Pour permettre l'exécution des test----------------------                                                                                                                                                  
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(DiscountTest.class);
    }

}
