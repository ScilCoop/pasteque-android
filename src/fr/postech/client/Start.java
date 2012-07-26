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
package fr.postech.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import java.util.ArrayList;
import java.util.List;

import fr.postech.client.models.User;
import fr.postech.client.models.Product;
import fr.postech.client.models.Session;
import fr.postech.client.models.Ticket;
import fr.postech.client.widgets.UserBtnItem;
import fr.postech.client.widgets.UsersBtnAdapter;

public class Start extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect);
        List<User> users = new ArrayList<User>();
        users.add(new User("Pierre"));
        users.add(new User("Paul"));
        users.add(new User("Jacques"));
        users.add(new User("Jean"));
        users.add(new User("Biloute"));
        UsersBtnAdapter adapt = new UsersBtnAdapter(users);
        GridView logins = (GridView) this.findViewById(R.id.loginGrid);
        logins.setAdapter(adapt);
        logins.setOnItemClickListener(new UserClickListener());
    }

    private class UserClickListener implements OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View v,
                                int position, long id) {
            UserBtnItem item = (UserBtnItem) v;
            User user = item.getUser();
            Session.currentSession.setUser(user);
            Product p1 = new Product("P1", 1.2, 0.196);
            Product p2 = new Product("P2", 4.3, 0.196);
            Product p3 = new Product("Produit 3", 2.0, 0.196);
            Product p4 = new Product("Produit 4", 2.5, 0.196);
            List<Product> prds = new ArrayList<Product>();
            prds.add(p1); prds.add(p2); prds.add(p3); prds.add(p4);
            if (Session.currentSession.getCurrentTicket() == null) {
                // Create a ticket if there isn't anyone
                Ticket t = Session.currentSession.newTicket();
            }
            TicketInput.setup(prds, Session.currentSession.getCurrentTicket());
            Intent i = new Intent(Start.this, TicketInput.class);
            Start.this.startActivity(i);
        }
    }
}
