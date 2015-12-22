package fr.pasteque.client.drivers.mpop;

import android.os.AsyncTask;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.starioextension.starioextmanager.StarIoExtManager;
import fr.pasteque.client.Pasteque;

/**
 * MPopManager is the public class which centralize all mPop behaviors
 * Created by svirch_n on 22/12/15.
 */
public class MPopManager {

    private final StarIOPort port;

    /**
     * Take the saved port in configuration
     */
    public MPopManager() throws StarIOPortException {
        this.port = MPopPort.getPort(Pasteque.getConfiguration().getPrinterModel());
    }

    public void openDrawer() {
        byte[] commands = MPopFunction.createCommandsOpenCashDrawer();
        new SendingCommand().execute(commands);
    }

    public class SendingCommand extends AsyncTask<byte[], Void, MPopCommunication.Result> {

        @Override
        protected MPopCommunication.Result doInBackground(byte[]... bytes) {
            MPopCommunication.Result result = null;
            if (bytes.length > 0)
                try {
                    synchronized (MPopManager.this.port) {
                        result = MPopCommunication.sendCommands(bytes[0], MPopManager.this.port);
                    }
                } catch (StarIOPortException e) {
                    e.printStackTrace();
                }
            return result;
        }
    }
}
