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

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class Ticket implements Serializable {

    private String label;
    private int articles;
    private List<TicketLine> lines;

    public Ticket() {
        this.lines = new ArrayList<TicketLine>();
    }

    public List<TicketLine> getLines() {
        return this.lines;
    }

    public TicketLine getLineAt(int index) {
        return this.lines.get(index);
    }

    public void addLine(Product p, int qty) {
        this.lines.add(new TicketLine(p, qty));
    }

    public void addProduct(Product p) {
        for (TicketLine l : this.lines) {
            if (l.getProduct().equals(p)) {
                l.addOne();
                this.articles++;
                return;
            }
        }
        this.addLine(p, 1);
        this.articles++;
    }

    public int getArticlesCount() {
        return this.articles;
    }

    public double getTotalPrice() {
        double total = 0.0;
        for (TicketLine l : this.lines) {
            total += l.getTotalPrice();
        }
        return total;
    }

    public double getSubTotalPrice() {
        double total = 0.0;
        for (TicketLine l : this.lines) {
            total += l.getSubtotalPrice();
        }
        return total;
    }

    public double getTaxPrice() {
        double total = 0.0;
        for (TicketLine l : this.lines) {
            total += l.getTaxPrice();
        }
        return total;
    }
}
