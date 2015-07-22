/*
    Pasteque Android client
    Copyright (C) Pasteque contributors, see the COPYRIGHT file

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package fr.pasteque.client.data;

import android.content.Context;
import fr.pasteque.client.Constant;
import fr.pasteque.client.models.Discount;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.text.ParseException;
import java.util.ArrayList;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;


/**
 * 
 * @author nsvir
 */
@RunWith(PowerMockRunner.class)
public class DiscountDataTest {

    private static String FILENAME = Constant.BUILD_FOLDER + "discount.data";

    @Mock
    private Context fakeContext;
    
    @Before
    public void setup() throws IOException {
        PipedInputStream pipeInput = new PipedInputStream();
    }
    
    @Test
    public void setCollectionTest() throws ParseException {
        Discount d1 = new Discount("d1", 0.20, "2015-06-30", "2015-06-30", "", 2);
        Discount d2 = new Discount("d2", 0.20, "2015-06-30", "2015-06-30", "", 2);
        ArrayList<Discount> collection = new ArrayList<Discount>();
        collection.add(null);
        collection.add(d1);
        collection.add(d2);
        DiscountData.setCollection(collection);
        assertEquals(2, DiscountData.getDiscounts().size());
        DiscountData.setCollection(null);
        collection = DiscountData.getDiscounts();
        assertNotNull(collection);
        assertTrue(collection.isEmpty());
    }
    
    @Test
    public void saveTest() throws Exception {
        FileOutputStream output = new FileOutputStream(FILENAME);
        expect(fakeContext.openFileOutput("discount.data", Context.MODE_PRIVATE)).andStubReturn(output);
        DiscountData.save(fakeContext);
    }

}