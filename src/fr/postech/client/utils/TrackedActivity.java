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
package fr.postech.client.utils;

import fr.postech.client.Error;

import android.app.Activity;
import android.os.Bundle;

public class TrackedActivity extends Activity {

    protected boolean inFront;
    protected Integer pendingError;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if (state != null) {
            this.pendingError = (Integer) state.getSerializable("TrackedActivity.pendingError");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putSerializable("TrackedActivity.pendingError",
                this.pendingError);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.inFront = true;
        if (this.pendingError != null) {
            Error.showError(this.pendingError, this);
            this.pendingError = null;
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
}