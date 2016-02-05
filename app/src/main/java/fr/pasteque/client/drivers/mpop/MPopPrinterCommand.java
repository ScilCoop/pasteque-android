package fr.pasteque.client.drivers.mpop;

/**
 * Created by svirch_n on 05/02/16.
 */
public interface MPopPrinterCommand {
    MPopCommunication.Result sendCommand(byte[] data);
}
