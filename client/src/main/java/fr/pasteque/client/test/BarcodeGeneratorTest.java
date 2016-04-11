/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.pasteque.client.test;


import android.annotation.SuppressLint;
import fr.pasteque.client.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import fr.pasteque.client.models.Barcode;
import fr.pasteque.client.utils.BarcodeGenerator;
import fr.pasteque.client.utils.BitmapManipulation;


@SuppressLint("Registered")
public class BarcodeGeneratorTest extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_generator_test);
        ImageView image =  (ImageView) findViewById(R.id.image_view);
        
        Bitmap bitmap = BarcodeGenerator.generate("7501054530107", Barcode.QR);
        image.setImageBitmap(BitmapManipulation.centeredBitmap(bitmap, 572));
    }
    
}
