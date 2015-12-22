package fr.pasteque.client.drivers.mpop;

/**
 * Common function used by mPop
 * Based on StarIO Android SDK
 * Created by svirch_n on 22/12/15.
 */
class MPopFunction {

    static byte[] createCommandsOpenCashDrawer()  {
        byte[] commands = new byte[1];

        commands[0] = 0x07;                             // BEL

        return commands;
    }

}