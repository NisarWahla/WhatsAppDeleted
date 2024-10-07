package dzm.wamr.recover.deleted.messages.photo.media.util;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;

import dzm.wamr.recover.deleted.messages.photo.media.R;


public class InputFilterMinMax implements InputFilter {
    Context context;
    private final int min;
    private final int max;

    public InputFilterMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }


    public InputFilterMinMax(String min, String max, Context context) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
        this.context = context;
    }

    public static InputFilter[] getFilterArray(InputFilterMinMax inputFilterMinMax) {
        return new InputFilter[]{inputFilterMinMax};
    }

    boolean isShowToast=true;
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            int input = Integer.parseInt(dest.toString() + source.toString());
            if (input > max)
            {
                if (isShowToast)
                {
                    isShowToast=false;
                    Toast.makeText(context, context.getString(R.string.repeat_limit), Toast.LENGTH_SHORT).show();
                }


                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isShowToast=true;
                    }
                },500);
            }

            if (isInRange(min, max, input))
                return null;

        } catch (Exception nfe) {
        }
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}