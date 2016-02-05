package fr.pasteque.client.drivers.mpop;

import android.content.Context;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import fr.pasteque.client.Pasteque;

/**
 * This class is the common use of mPop communication described in the StarIO Android SDK
 * Should only be used in the mpop package for a better flow control
 * Created by svirch_n on 22/12/15.
 */
class MPopCommunication {

    private final static int timeout = 10000; // 10000ms

    public enum Result {
        Success("Success"),
        ErrorUnknown("ErrorUnknown"),
        ErrorOpenPort("ErrorOpenPort"),
        ErrorBeginCheckedBlock("ErrorBeginCheckedBlock"),
        ErrorEndCheckedBlock("ErrorEndCheckedBlock"),
        ErrorWritePort("ErrorWritePort"),
        ErrorReadPort("ErrorReadPort");

        private final String mType;

        Result(String type) {
            mType = type;
        }

        public String getAsText() {
            return mType;
        }
    }

    public static Result sendCommands(byte[] commands, String portName, String portSettings, int timeout) {
        Result result = Result.ErrorUnknown;

        StarIOPort port = null;

        try {
            result = Result.ErrorOpenPort;

            port = StarIOPort.getPort(portName, portSettings, timeout, Pasteque.getAppContext());

//          // When using an USB interface, you may need to send the following data.
//          byte[] dummy = {0x00};
//          port.writePort(dummy, 0, dummy.length);

            StarPrinterStatus status;

            result = Result.ErrorBeginCheckedBlock;

            status = port.beginCheckedBlock();

            if (status.offline) {
                throw new StarIOPortException("A printer is offline");
            }

            result = Result.ErrorWritePort;

            port.writePort(commands, 0, commands.length);

            result = Result.ErrorEndCheckedBlock;

            port.setEndCheckedBlockTimeoutMillis(30000);     // 30000mS!!!

            status = port.endCheckedBlock();

            if (status.coverOpen) {
                throw new StarIOPortException("Printer cover is open");
            }
            else if (status.receiptPaperEmpty) {
                throw new StarIOPortException("Receipt paper is empty");
            }
            else if (status.offline) {
                throw new StarIOPortException("Printer is offline");
            }

            result = Result.Success;
        }
        catch (StarIOPortException e) {
            // Nothing
        }
        finally {
            if (port != null) {
                try {
                    StarIOPort.releasePort(port);

                    port = null;
                }
                catch (StarIOPortException e) {
                    // Nothing
                }
            }
        }

        return result;
    }

    static Result sendCommands(byte[] commands, StarIOPort port) {
        Result result = Result.ErrorUnknown;

        try {
            if (port == null) {
                result = Result.ErrorOpenPort;
                return result;
            }

//          // When using an USB interface, you may need to send the following data.
//          byte[] dummy = {0x00};
//          port.writePort(dummy, 0, dummy.length);

            StarPrinterStatus status;

            result = Result.ErrorWritePort;
            status = port.retreiveStatus();

            if (status.rawLength == 0) {
                throw new StarIOPortException("A printer is offline");
            }

            result = Result.ErrorWritePort;
            port.writePort(commands, 0, commands.length);

            result = Result.ErrorWritePort;
            status = port.retreiveStatus();

            if (status.rawLength == 0) {
                throw new StarIOPortException("A printer is offline");
            }

            result = Result.Success;
        }
        catch (StarIOPortException e) {
            // Nothing
        }

        return result;
    }
}