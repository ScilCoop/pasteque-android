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
package fr.pasteque.client.data.DataSavable;

import fr.pasteque.client.models.Customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomerData extends AbstractObjectDataSavable {

    private static final String FILENAME = "customers.data";

    public List<Customer> customers = new ArrayList<Customer>();
    public List<Customer> createdCustomers = new ArrayList<>();
    // Map containing which local id to replace with server id
    public HashMap<String, String> resolvedIds = new HashMap<>();

    public void setCustomers(List<Customer> c) {
        customers = c;
    }

    public void addCreatedCustomer(Customer c) {
        this.customers.add(c);
        this.createdCustomers.add(c);
    }

    @Override
    protected String getFileName() {
        return CustomerData.FILENAME;
    }

    @Override
    protected List<Object> getObjectList() {
        List<Object> result = new ArrayList<>();
        result.add(customers);
        result.add(createdCustomers);
        result.add(resolvedIds);
        return result;
    }

    @Override
    protected int getNumberOfObjects() {
        return 3;
    }

    @Override
    protected void recoverObjects(List<Object> objs) {
        customers = (List<Customer>) objs.get(0);
        createdCustomers = (List<Customer>) objs.get(1);
        resolvedIds = (HashMap<String, String>) objs.get(2);
    }
}
