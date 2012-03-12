package com.view;

import com.adventure.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class AdventureTextView extends TextView {
    public AdventureTextView(Context context) {
        super(context);
    }

    public AdventureTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public AdventureTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.AdventureText);
        String font = a.getString(R.styleable.AdventureText_font);
        setTypeface(Typeface.createFromAsset(getResources().getAssets(), font));
        
        a.recycle();
    }
}