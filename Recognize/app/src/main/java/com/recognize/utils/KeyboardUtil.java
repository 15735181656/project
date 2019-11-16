package com.recognize.utils;


import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;

public class KeyboardUtil {
    public static void hideSoftKeyboard(@NonNull Context context,
                                        @NonNull EditText mEditText)
    {
        InputMethodManager inputmanger = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputmanger.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }
}
