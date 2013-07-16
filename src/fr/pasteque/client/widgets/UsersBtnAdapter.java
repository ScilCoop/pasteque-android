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
package fr.pasteque.client.widgets;

import fr.pasteque.client.R;
import fr.pasteque.client.models.User;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;

public class UsersBtnAdapter extends BaseAdapter {

    private List<User> users;

    public UsersBtnAdapter(List<User> users) {
        super();
        this.users = users;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return this.users.get(position);
    }

    @Override
    public int getCount() {
        return this.users.size();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User u = this.users.get(position);
        if (convertView != null && convertView instanceof UserBtnItem) {
            // Reuse the view
            UserBtnItem item = (UserBtnItem) convertView;
            item.reuse(u);
            return item;
        } else {
            // Create the view
            Context ctx = parent.getContext();
            UserBtnItem item = new UserBtnItem(ctx, u);
            return item;
        }
    }
}
