package com.brosh.finance.monthlybudgetsync.utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Definitions;

public final class TextUtil {
    public static String getSentenceCapitalLetter(String sentence, char separator) {
        if (sentence.indexOf(separator) == -1)
            return getWordCapitalLetter(sentence);
        return getWordCapitalLetter(sentence.substring(0, sentence.indexOf(separator) + 1)) + getSentenceCapitalLetter(sentence.substring(sentence.indexOf(separator) + 1), separator);
    }

    public static String getWordCapitalLetter(String word) {
        char firstLetter = word.charAt(0);
        if (firstLetter >= 97 && firstLetter <= 122)
            firstLetter -= 32;
        return new String(firstLetter + word.substring(1));
    }

    public static String getSeperator() {
        return Definitions.ARROW_RIGHT;
    }

    public static void showMessage(String message, int duration, Context context) {
        new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, message, duration);
                View view = toast.getView();
                view.getBackground().setColorFilter(context.getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN);
                TextView text = view.findViewById(android.R.id.message);
                text.setTextColor(context.getResources().getColor(R.color.colorApp));
                toast.show();
            }
        }.run();
    }

    public static String getEmailComma(String email) {
        return email.trim().replace(Definitions.DOT, Definitions.COMMA);
    }

    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
