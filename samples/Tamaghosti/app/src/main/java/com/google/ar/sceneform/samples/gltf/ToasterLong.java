package com.google.ar.sceneform.samples.gltf;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.widget.Toast;

public class ToasterLong {
    private static final int SHORT_TOAST_DURATION = 2000;
    public  static Context c;
    public  ToasterLong(Context context) {
        this.c = context;

    }

    public static void makeLongToast(String text, long durationInMillis) {
        final Toast t = Toast.makeText(c, text, Toast.LENGTH_SHORT);

        t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        new CountDownTimer(Math.max(durationInMillis - SHORT_TOAST_DURATION, 1000), 1000) {
            @Override
            public void onFinish() {
                t.show();
            }

            @Override
            public void onTick(long millisUntilFinished) {
                t.show();
            }
        }.start();
    }
}
