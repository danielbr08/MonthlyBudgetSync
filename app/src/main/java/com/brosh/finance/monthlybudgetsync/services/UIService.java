package com.brosh.finance.monthlybudgetsync.services;

import android.graphics.*;
import android.text.util.Linkify;
import android.view.View;
import android.widget.*;

public final class UIService {

    public void setHeaderProperties(TextView tv)
    {
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(15);
        tv.setClickable(true);
        Linkify.addLinks(tv,Linkify.ALL);
    }

    public void reverseLinearLayout(LinearLayout linearLayout)
    {
        for(int i = linearLayout.getChildCount()-1 ; i >= 0 ; i--)
        {
            View item = linearLayout.getChildAt(i);
            linearLayout.removeViewAt(i);
            linearLayout.addView(item);
        }
    }

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

    public  void strikeThroughText(TextView tv)
    {
        tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }
}
