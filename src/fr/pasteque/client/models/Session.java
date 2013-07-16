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

    private User user;
    private List<Ticket> runningTickets;
    private Ticket currentTicket;
    private int ticketNumber;

    /** Create an empty session */
    public Session() {
        this.runningTickets = new ArrayList<Ticket>();
        this.ticketNumber = 1;
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
    public Ticket newTicket() {
        Ticket t = new Ticket(String.valueOf(this.ticketNumber++));
        this.runningTickets.add(t);
        this.currentTicket = t;
        return t;
    }

    /** Create a new ticket for a given table.
     * To match sharedticket table the id of the ticket is set to the id of
     * the table.
     */
    public Ticket newTicket(Place p) {
        Ticket t = new Ticket(p.getName());
        this.runningTickets.add(t);
        this.currentTicket = t;
        return t;
    }

    public void closeTicket(Ticket t) {
        this.runningTickets.remove(t);
    }
    
    public List<Ticket> getTickets() {
        return this.runningTickets;
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

}
