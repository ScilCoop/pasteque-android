/*
    POS-Tech Android
    Copyright (C) 2012 SARL SCOP Scil (contact@scil.coop)

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
package fr.postech.client.models;

import java.util.List;
import java.util.ArrayList;

public class Session {

    public static Session currentSession = new Session();

    private User user;
    private List<Ticket> runningTickets;
    private Ticket currentTicket;

    /** Create an empty session */
    public Session() {
        this.runningTickets = new ArrayList<Ticket>();
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
        Ticket t = new Ticket();
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

    public void setCurrentTicket(Ticket t) {
        this.currentTicket = t;
    }

    public Ticket getCurrentTicket() {
        return this.currentTicket;
    }
}
