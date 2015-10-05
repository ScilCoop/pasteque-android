package fr.pasteque.client.data.DataSavable;

import android.content.Context;
import android.util.Log;
import fr.pasteque.client.models.CashRegister;
import fr.pasteque.client.models.TicketId;
import fr.pasteque.client.utils.exception.DataCorruptedException;

import java.io.IOError;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nsvir on 18/08/15.
 * n.svirchevsky@gmail.com
 */
public class TicketIdData extends AbstractObjectDataSavable {

    private static final String LOG_TAG = "pasteque/ticketIdData";
    public static String FILENAME = "ticketid.data";

    private TicketId ticketId;
    private String cashId;

    @Override
    protected String getFileName() {
        return TicketIdData.FILENAME;
    }

    @Override
    protected List<Object> getObjectList() {
        List<Object> result = new ArrayList<>();
        result.add(ticketId);
        result.add(cashId);
        return result;
    }

    @Override
    protected int getNumberOfObjects() {
        return 2;
    }

    @Override
    protected void recoverObjects(List<Object> objs) throws DataCorruptedException {
        this.ticketId = (TicketId) objs.get(0);
        this.cashId = (String) objs.get(1);
    }

    @Override
    public boolean onLoadingFailed(DataCorruptedException e) {
        super.onLoadingFailed(e);
        return true;
    }

    @Override
    public boolean onLoadingError(IOError e) {
        return super.onLoadingError(e);
    }

    public void ticketClosed(Context ctx) {
        this.ticketId.ticketClosed();
        this.save(ctx);
    }

    public int newTicketId() {
        int result = this.ticketId.newTicketId();
        return result;
    }

    public void notifyDataJustSent() {
        this.ticketId.notifyDataJustSent();
    }

    /**
     * Check if the ticketId must be updated and if it is correct.
     * @param cashReg
     * @param newUser
     */
    public void updateTicketId(CashRegister cashReg, boolean newUser) {
        int cashTicketId = cashReg.getNextTicketId();
        if (this.ticketId == null || this.cashId == null ||
                newUser || !this.cashId.equals(cashReg.getMachineName())) {
            this.ticketId = new TicketId(cashTicketId);
            this.cashId = cashReg.getMachineName();
        } else {
            if (this.ticketId.hasNotCreatedTickets()) {
                if (this.ticketId.getId() != cashTicketId) {
                    Log.d(LOG_TAG, "Server & local ticketId aren't the same.");
                }
                this.ticketId = new TicketId(cashTicketId);
            } else if (this.ticketId.getId() < cashReg.getNextTicketId()) {
                Log.d(LOG_TAG, "local ticketId is corrupted, the server's value has been set");
                this.ticketId = new TicketId(cashTicketId);
            } else {
                //The ticketId looks correct it must not be updated
            }
        }
    }
}
