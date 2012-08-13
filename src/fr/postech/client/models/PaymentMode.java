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

import java.util.ArrayList;
import java.util.List;

public class PaymentMode {

    static {
        PaymentMode.MODES = new ArrayList<PaymentMode>();
        PaymentMode.MODES.add(new PaymentMode("cash", ""));
        PaymentMode.MODES.add(new PaymentMode("cheque", ""));
        PaymentMode.MODES.add(new PaymentMode("magcard", ""));
        PaymentMode.MODES.add(new PaymentMode("paperin", ""));
    }

    public static List<PaymentMode> MODES;

    private String code;
    private String label;

    public PaymentMode(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    public String getCode() {
        return this.code;
    }
}