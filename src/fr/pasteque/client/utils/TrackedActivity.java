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
package fr.pasteque.client.utils;

import fr.pasteque.client.R;
import fr.pasteque.client.Error;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class TrackedActivity extends ActionBarActivity {

    protected boolean inFront;
    protected Integer pendingError;
    protected String pendingStrError;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        // Quick hack as icon and logo doesn't work from manifest
        this.getSupportActionBar().setLogo(R.drawable.logo_worldline);
        if (state != null) {
            this.pendingError = (Integer) state.getSerializable("TrackedActivity.pendingError");
            this.pendingStrError = (String) state.getSerializable("TrackedActivity.pendingStrError");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putSerializable("TrackedActivity.pendingError",
                this.pendingError);
        state.putSerializable("TrackedActivity.pendingStrError",
                this.pendingStrError);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.inFront = true;
        if (this.pendingError != null) {
            Error.showError(this.pendingError, this);
            this.pendingError = null;
        }
        if (this.pendingStrError != null) {
            Error.showError(this.pendingStrError, this);
            this.pendingStrError = null;
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        this.inFront = false;
    }

    public boolean isFront() {
        return this.inFront;
    }

    public Integer getPendingError() {
        return this.pendingError;
    }

    public void setPendingError(int error) {
        this.pendingError = error;
    }
    public void setPendingError(String error) {
        this.pendingStrError = error;
    }
}