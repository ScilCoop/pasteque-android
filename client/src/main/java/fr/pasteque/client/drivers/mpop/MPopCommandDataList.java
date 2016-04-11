package fr.pasteque.client.drivers.mpop;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * CommandData buffer and for convenient appending
 * Based on StarIO Android SDK
 * Created by svirch_n on 23/12/15.
 */
public class MPopCommandDataList extends ArrayList<Byte> {

    MPopCommandDataList add(int... arg) {
        for(int value:arg) {
            add((byte) value);
        }
        return this;
    }

    MPopCommandDataList add(byte[] arg) {
        for(byte value:arg) {
            add( value );
        }
        return this;
    }

    MPopCommandDataList add(String arg) {
        byte[] argByte = arg.getBytes();

        for(byte value:argByte) {
            add( value );
        }
        return this;
    }

    byte[] getByteArray() {
        ByteBuffer output;

        output = ByteBuffer.allocate(this.size());

        for(Byte value:this) {
            output.put(value);
        }

        return output.array();
    }
}
