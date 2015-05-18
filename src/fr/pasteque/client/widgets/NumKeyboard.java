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

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

import fr.pasteque.client.R;

public class NumKeyboard extends TableLayout {

    public static final int KEY_0 = 0;
    public static final int KEY_1 = 1;
    public static final int KEY_2 = 2;
    public static final int KEY_3 = 3;
    public static final int KEY_4 = 4;
    public static final int KEY_5 = 5;
    public static final int KEY_6 = 6;
    public static final int KEY_7 = 7;
    public static final int KEY_8 = 8;
    public static final int KEY_9 = 9;
    public static final int KEY_00 = 10;
    public static final int KEY_DOT = 11;
    public static final int KEY_ENTER = 12;
    public static final int KEY_ERASE = 13;

    private String innerValue;
    private Handler keyHandler;
    private int decimals;

    public NumKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.innerValue = "";
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.numkeyboard, this);
        this.setKeyListener(R.id.key0, KEY_0);
        this.setKeyListener(R.id.key1, KEY_1);
        this.setKeyListener(R.id.key2, KEY_2);
        this.setKeyListener(R.id.key3, KEY_3);
        this.setKeyListener(R.id.key4, KEY_4);
        this.setKeyListener(R.id.key5, KEY_5);
        this.setKeyListener(R.id.key6, KEY_6);
        this.setKeyListener(R.id.key7, KEY_7);
        this.setKeyListener(R.id.key8, KEY_8);
        this.setKeyListener(R.id.key9, KEY_9);
        this.setKeyListener(R.id.key00, KEY_00);
        this.setKeyListener(R.id.keyDot, KEY_DOT);
        this.setKeyListener(R.id.keyEnter, KEY_ENTER);
        this.setKeyListener(R.id.keyErase, KEY_ERASE);
        this.decimals = 2;
    }

    public void setKeyHandler(Handler h) {
        this.keyHandler = h;
    }

    private void keyClicked(int key, Button btn) {
        if (key == KEY_00) {
            int dot = this.innerValue.indexOf('.');
            if (dot == -1 || this.innerValue.length() - dot + 1 <= this.decimals) {
                this.innerValue += btn.getText();
            } else if (this.innerValue.length() - dot <= this.decimals) {
                this.innerValue += '0';
            }
        } else if (key >= KEY_0 && key <= KEY_9) {
            int dot = this.innerValue.indexOf('.');
            if (dot == -1 || this.innerValue.length() - dot <= this.decimals) {
                this.innerValue += btn.getText();
            }
        } else if (key == KEY_DOT) {
            if (this.innerValue.indexOf('.') == -1) {
                if (this.innerValue.length() == 0) {
                    this.innerValue = "0.";
                } else {
                    this.innerValue += '.';
                }
            }
        } else if (key == KEY_ERASE) {
            if (this.innerValue.length() > 0) {
                this.innerValue = this.innerValue.substring(0, this.innerValue.length() - 1);
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
        if (this.innerValue.length() > 0) {
            this.innerValue = this.innerValue.substring(0,
                    this.innerValue.length() - 1);
        }
    }

    public String getRawValue() {
        return this.innerValue;
    }

    public double getValue() {
        if (this.innerValue.equals("")) {
            return 0.0;
        } else {
            return Double.parseDouble(this.innerValue);
        }
    }

    @Override
    public void setOnTouchListener(View.OnTouchListener listener) {
        super.setOnTouchListener(listener);
        this.findViewById(R.id.key0).setOnTouchListener(listener);
        this.findViewById(R.id.key1).setOnTouchListener(listener);
        this.findViewById(R.id.key2).setOnTouchListener(listener);
        this.findViewById(R.id.key3).setOnTouchListener(listener);
        this.findViewById(R.id.key4).setOnTouchListener(listener);
        this.findViewById(R.id.key5).setOnTouchListener(listener);
        this.findViewById(R.id.key6).setOnTouchListener(listener);
        this.findViewById(R.id.key7).setOnTouchListener(listener);
        this.findViewById(R.id.key8).setOnTouchListener(listener);
        this.findViewById(R.id.key9).setOnTouchListener(listener);
        this.findViewById(R.id.key00).setOnTouchListener(listener);
        this.findViewById(R.id.keyEnter).setOnTouchListener(listener);
        this.findViewById(R.id.keyDot).setOnTouchListener(listener);
        this.findViewById(R.id.keyErase).setOnTouchListener(listener);
    }

    private void setKeyListener(int id, final int key) {
        View v = this.findViewById(id);
        v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                keyClicked(key, (Button) v);
            }
        });
    }

}