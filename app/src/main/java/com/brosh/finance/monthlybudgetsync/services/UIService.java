package com.brosh.finance.monthlybudgetsync.services;

import android.graphics.*;
import android.os.Build;
import android.text.util.Linkify;
import android.view.View;
import android.widget.*;

import androidx.annotation.RequiresApi;

public final class UIService {

    public static void setHeaderProperties(TextView tv)
    {
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(15);
        tv.setClickable(true);
        Linkify.addLinks(tv,Linkify.ALL);
    }

    public static void reverseLinearLayout(LinearLayout linearLayout)
    {
        for(int i = linearLayout.getChildCount()-1 ; i >= 0 ; i--)
        {
            View item = linearLayout.getChildAt(i);
            linearLayout.removeViewAt(i);
            linearLayout.addView(item);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setLanguageConf(LinearLayout l)
    {
        for (int i = 0;i < l.getChildCount();i++)
        {
            View v = l.getChildAt(i);
            v.setTextDirection(View.TEXT_DIRECTION_LTR);
            v.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }
        reverseLinearLayout(l);
    }

    public static void strikeThroughText(TextView tv)
    {
        tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }
}
