/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.pasteque.client.test;


import fr.pasteque.client.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.zxing.common.BitMatrix;
import fr.pasteque.client.models.Barcode;
import fr.pasteque.client.utils.BarcodeGenerator;


/**
 *
 * @author svirch_n
 */
public class BarcodeGeneratorTest extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_generator_test);
        ImageView image =  (ImageView) findViewById(R.id.image_view);
        
        Bitmap bitmap = BarcodeGenerator.generate("7501054530107", Barcode.QR);
        image.setImageBitmap(bitmap);
    }
    
}
