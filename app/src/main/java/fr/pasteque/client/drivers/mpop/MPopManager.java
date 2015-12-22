package fr.pasteque.client.drivers.mpop;

import android.os.AsyncTask;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.starioextension.starioextmanager.StarIoExtManager;
import fr.pasteque.client.Pasteque;

/**
 * MPopManager is the public class which centralize all mPop behaviors
 * Thread safety Area
 * Created by svirch_n on 22/12/15.
 */
public class MPopManager {

    private static MPopManager mPopManager;
    private final StarIOPort port;

    /**
     * Take the saved port in configuration
     */
    MPopManager() throws StarIOPortException {
        this.port = MPopPort.getPort(Pasteque.getConfiguration().getPrinterModel());
    }

    /**
     * This is a singleton getter and is used by at least SendingCommand
     * @return MPopManager's singleton
     * @throws StarIOPortException
     */
    static synchronized MPopManager getMPOPManager() throws StarIOPortException {
        if (mPopManager == null) {
            mPopManager = new MPopManager();
        }
        return mPopManager;
    }

    public static void openDrawer() {
        byte[] commands = MPopFunction.createCommandsOpenCashDrawer();
        new SendingCommand().execute(commands);
    }

    public static class SendingCommand extends AsyncTask<byte[], Void, MPopCommunication.Result> {

        @Override
        protected MPopCommunication.Result doInBackground(byte[]... bytes) {
            MPopCommunication.Result result = null;
            if (bytes.length > 0)
                try {
                    MPopManager mPopManager = MPopManager.getMPOPManager();
                    synchronized (mPopManager.port) {
                        result = MPopCommunication.sendCommands(bytes[0], mPopManager.port);
                    }
                } catch (StarIOPortException e) {
                    e.printStackTrace();
                }
            return result;
        }
    }
}
