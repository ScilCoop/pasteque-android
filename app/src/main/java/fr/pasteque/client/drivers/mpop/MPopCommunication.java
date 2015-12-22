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
        Success,
        ErrorUnknown,
        ErrorOpenPort,
        ErrorBeginCheckedBlock,
        ErrorEndCheckedBlock,
        ErrorWritePort,
        ErrorReadPort,
    }

    public static Result sendCommands(byte[] commands, String deviceName) throws StarIOPortException {
        return sendCommands(commands, deviceName, null);
    }

    public static Result sendCommandsDoNotCheckCondition(byte[] commands, String deviceName) throws StarIOPortException {
        return sendCommandsDoNotCheckCondition(commands, deviceName, null);
    }

    public static Result sendCommands(byte[] commands, String deviceName, String portSettings) throws StarIOPortException {
        return sendCommands(commands, MPopPort.getPort(deviceName, portSettings, timeout, Pasteque.getAppContext()));
    }

    public static Result sendCommandsDoNotCheckCondition(byte[] commands, String deviceName, String portSettings) throws StarIOPortException {
        return sendCommandsDoNotCheckCondition(commands, MPopPort.getPort(deviceName, portSettings, timeout, Pasteque.getAppContext()));
    }

    public static Result sendCommands(byte[] commands, StarIOPort port) throws StarIOPortException{
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

        return result;
    }

    public static Result sendCommandsDoNotCheckCondition(byte[] commands, StarIOPort port) {
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
