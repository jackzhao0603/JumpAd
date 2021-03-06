package com.jackzhao.adjump.utils;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.jackzhao.adjump.R;

public class ToastUtils {

    private ToastUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    private static Toast toast;
    private static int messageColor = R.color.white;

    /**
     * 发送Toast,默认LENGTH_SHORT
     *
     * @param resId
     */
    public static void show(Context context, int resId) {
        showToast(context, context.getString(resId), Toast.LENGTH_SHORT);
    }

    public static void showText(Context context, String text) {
        showToast(context, text, Toast.LENGTH_SHORT);
    }

    private static void showToast(Context context, String massage, int duration) {
        // 设置显示文字的颜色
        SpannableString spannableString = new SpannableString(massage);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(ContextCompat.getColor(context, messageColor));
        spannableString.setSpan(colorSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (toast == null) {
            toast = Toast.makeText(context, spannableString, duration);
        } else {
            toast.setText(spannableString);
            toast.setDuration(duration);
        }
        // 设置显示的背景
        View view = toast.getView();
        view.setBackgroundResource(R.drawable.toast_frame_style);
        // 设置Toast要显示的位置，水平居中并在底部，X轴偏移0个单位，Y轴偏移200个单位，
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 200);
        toast.show();
    }

    /**
     * 在UI界面隐藏或者销毁前取消Toast显示
     */
    public static void cancel() {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
    }
}