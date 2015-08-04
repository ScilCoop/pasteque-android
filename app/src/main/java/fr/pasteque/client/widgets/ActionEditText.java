package fr.pasteque.client.widgets;

/**
 * Created by nsvir on 04/08/15.
 * n.svirchevsky@gmail.com
 */

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

/**
 * This widget exists because the EditText.inputType="Multiline" erase the imeOption="actionDone"
 * http://stackoverflow.com/questions/5014219/multiline-edittext-with-done-softinput-action-label-on-2-3
 */
public class ActionEditText extends EditText {
    public ActionEditText(Context context) {
        super(context);
    }

    public ActionEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ActionEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection conn = super.onCreateInputConnection(outAttrs);
        outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        return conn;
    }


}
