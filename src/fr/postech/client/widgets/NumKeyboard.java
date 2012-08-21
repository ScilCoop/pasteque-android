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

package fr.postech.client.widgets;
 
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;

import fr.postech.client.R;
 
public class NumKeyboard extends TableLayout {

    public static final int KEY_9 = 9;
    public static final int KEY_8 = 8;
    public static final int KEY_7 = 7;
    public static final int KEY_6 = 6;
    public static final int KEY_5 = 5;
    public static final int KEY_4 = 4;
    public static final int KEY_3 = 3;
    public static final int KEY_2 = 2;
    public static final int KEY_1 = 1;
    public static final int KEY_0 = 0;
    public static final int KEY_DOT = 10;
    public static final int KEY_ENTER = 11;

    private String innerValue;
    private Handler keyHandler;
    private int decimals;

    public NumKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.innerValue = "";
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.numkeyboard, this);
        this.setKeyListener(R.id.key9, KEY_9);
        this.setKeyListener(R.id.key8, KEY_8);
        this.setKeyListener(R.id.key7, KEY_7);
        this.setKeyListener(R.id.key6, KEY_6);
        this.setKeyListener(R.id.key5, KEY_5);
        this.setKeyListener(R.id.key4, KEY_4);
        this.setKeyListener(R.id.key3, KEY_3);
        this.setKeyListener(R.id.key2, KEY_2);
        this.setKeyListener(R.id.key1, KEY_1);
        this.setKeyListener(R.id.key0, KEY_0);
        this.setKeyListener(R.id.keydot, KEY_DOT);
        this.setKeyListener(R.id.keyenter, KEY_ENTER);
        this.decimals = 2;
    }

    public void setKeyHandler(Handler h) {
        this.keyHandler = h;
    }

    private void keyClicked(int key) {
        if (key >= KEY_0 && key <= KEY_9) {
            int dot = this.innerValue.indexOf('.');
            if (dot == -1 || this.innerValue.length() - dot <= this.decimals) {
                this.innerValue += String.valueOf(key);    
            }
        } else if (key == KEY_DOT) {
            if (this.innerValue.indexOf('.') == -1) {
                if (this.innerValue.equals("")) {
                    this.innerValue = "0.";
                } else {
                    this.innerValue += '.';
                }
            }
        }
        if (this.keyHandler != null) {
            Message m = this.keyHandler.obtainMessage(key);
            m.sendToTarget();
        }
    }

    public void clear() {
        this.innerValue = "";
    }

    public void correct() {
        this.innerValue = this.innerValue.substring(0,
                                                    this.innerValue.length() - 1);
    }

    public double getValue() {
        if (this.innerValue.equals("")) {
            return 0.0;
        } else {
            return Double.parseDouble(this.innerValue);
        }
    }

    private void setKeyListener(int id, final int key) {
        View v = this.findViewById(id);
        v.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    keyClicked(key);
                }
            });
    }

}