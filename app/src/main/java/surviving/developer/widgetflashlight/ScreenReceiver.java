package surviving.developer.widgetflashlight;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenReceiver extends BroadcastReceiver {

    public static boolean wasScreenOn = true;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

            //Log.d(WidgetProvider.TAG, "Screen OFF");
            wasScreenOn = false;
            context.startService(new Intent(context, WidgetProvider.UpdateService.class));

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {

            //Log.d(WidgetProvider.TAG, "Screen On");
            wasScreenOn = true;
        }
    }

}