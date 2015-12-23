package fr.pasteque.client.drivers.mpop;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import com.starmicronics.starioextension.commandbuilder.Bitmap.SCBBitmapConverter;
import com.starmicronics.starioextension.commandbuilder.ISCBBuilder;
import com.starmicronics.starioextension.commandbuilder.SCBFactory;
import fr.pasteque.client.utils.BitmapManipulation;

import java.util.List;

/**
 * Common function used by mPop
 * Based on StarIO Android SDK
 * Created by svirch_n on 22/12/15.
 */
class MPopFunction {

    private static final int WIDTH = 384;

    static byte[] createCommandsOpenCashDrawer()  {
        byte[] commands = new byte[1];

        commands[0] = 0x07;                             // BEL

        return commands;
    }

    static class Printer {

        static byte[] data(String data) {
            return data.getBytes();
        }

        static byte[] image(Bitmap bitmap) {
            MPopCommandDataList result = new MPopCommandDataList();
            ISCBBuilder builder = SCBFactory.createBuilder(SCBFactory.Emulation.Star);
            builder.appendBitmap(bitmap, false, MPopFunction.WIDTH, SCBBitmapConverter.Rotation.Normal);
            @SuppressWarnings("unchecked")
            List<byte[]> listBuf = builder.getBuffer();
            for(byte[] buf:listBuf) {
                result.add(buf);
            }
            return result.getByteArray();
        }

        static byte[] cut() {
            return new byte[]{0x1b, 0x64, 0x03};
        }

    }
}