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
package fr.pasteque.client.models;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class Session implements Serializable {

    private static final int EQUALS = 0;
    private static final int NOT_EQUALS = 1;
    private User user;
    private List<Ticket> runningTickets;
    private List<Ticket> localSharedTickets;
    private Ticket currentTicket;

    /** Create an empty session */
    public Session() {
        this.runningTickets = new ArrayList<>();
        this.localSharedTickets = new ArrayList<>();
    }

    public void setUser(User u) {
        this.user = u;
    }

    public User getUser() {
        return this.user;
    }

    /** Create a new ticket and register it as current.
     * @return The created ticket
     */
    public Ticket newCurrentTicket() {
        Ticket t = new Ticket();
        this.runningTickets.add(t);
        this.currentTicket = t;
        return t;
    }

    /** Create a new ticket for a given table.
     * To match sharedticket table the id of the ticket is set to the id of
     * the table.
     */
    public Ticket newCurrentTicket(Place p) {
        Ticket t = new Ticket(p.getId(), p.getName());
        this.runningTickets.add(t);
        this.currentTicket = t;
        return t;
    }

    public Ticket newLocalTicket(String label) {
        return new LocalTicket(label);
    }

    public void addTicketToRunningTickets(LocalTicket ticket) {
        ticket.switchShareable();
        this.runningTickets.add(ticket);
    }

    public void updateLocalTicket(Ticket t) {
        _updateOrCreate(t);
    }

    private void _updateOrCreate(Ticket newTicket) {
        boolean	exist = false;
        for (int i = 0; i < runningTickets.size(); ++i) {
            if (newTicket.getLabel().equals(runningTickets.get(i).getLabel())) {
                runningTickets.set(i, newTicket);
                exist = true;
                break;
            }
        }
        if (!exist) {
            runningTickets.add(newTicket);
        }
    }


    public void removeTicket(Ticket t) {
        t.close();
        this.runningTickets.remove(t);
    }

    public void closeTicket(Ticket t) {
        removeTicket(t);
    }

    public List<Ticket> getTickets() {
        return this.runningTickets;
    }

    public Ticket getTicket(String id) {
        for (Ticket t : runningTickets) {
            if (t.getId() != null && t.getId().equals(id))
                return t;
        }
        return null;
    }

    /** Check if there is a non empty ticket pending */
    public boolean hasRunningTickets() {
        for (Ticket t : this.runningTickets) {
            if (!t.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** Check if there is at least two tickets pending */
    public boolean hasWaitingTickets() {
	boolean one = false;
	for (Ticket t : this.runningTickets) {
	    if (one) {
		return true;
	    }
	    one = true;
	}
	return false;
    }

    public boolean hasTicket() {
        return this.runningTickets.size() > 0;
    }

    public void setCurrentTicket(Ticket t) {
        this.currentTicket = t;
    }

    public Ticket getCurrentTicket() {
        return this.currentTicket;
    }

    public void updateSharedTickets(List<Ticket> sharedtickets) {
        this._updateSharedTicket(sharedtickets);
        this._removeClosedSharedTickets(sharedtickets);
        this.localSharedTickets = sharedtickets;
    }

    private void _removeClosedSharedTickets(List<Ticket> sharedtickets) {
        for (Ticket oldTicket : this.localSharedTickets) {
            innerLoop:
            {
                for (Ticket newTicket : sharedtickets) {
                    if (newTicket.getLabel().equals(oldTicket.getLabel())) {
                        break innerLoop;
                    }
                }
                this._removeSharedTicket(oldTicket);
            }
        }
    }

    private void _removeSharedTicket(Ticket oldTicket) {
        this.runningTickets.remove(oldTicket);
    }

    private void _updateSharedTicket(List<Ticket> sharedtickets) {
        for (Ticket newTicket: sharedtickets) {
            this.updateLocalTicket(newTicket);
        }
    }
}
