package surviving.developer.widgetflashlight;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.*;
import android.hardware.Camera;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import surviving.developer.widgetflashlight.R;


public class WidgetProvider extends AppWidgetProvider {
    // log tag
    public static final String TAG = "FlashLiteProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, UpdateService.class).putExtra("flash_mode_on", false));
    }

    public static class UpdateService extends Service {
        private static BroadcastReceiver mReceiver;
        private static IntentFilter filter;
        public boolean flash_mode_on;
        public Camera cam;

        @Override
        public void onCreate() {

            filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            mReceiver = new ScreenReceiver();
            registerReceiver(mReceiver, filter);
        }

        protected void cameraOn() {
            Log.v(TAG, "Camera ON");
            Camera.Parameters p = cam.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            cam.setParameters(p);
            cam.startPreview();
        }

        protected void cameraOff() {
            Log.v(TAG, "Camera OFF");
            if (cam != null) {
                cam.stopPreview();
                cam.release();
                cam = null;
            }
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            flash_mode_on = intent.getBooleanExtra("flash_mode_on", false);

            Log.v(TAG, "flash_mode_on=" + flash_mode_on);

            if (cam != null && flash_mode_on) {
                flash_mode_on = false;
            }

            if (ScreenReceiver.wasScreenOn == false && cam != null) {
                // turn on camera again when screen was turned off
                cameraOff();
                cam = Camera.open();

                cameraOn();
                return START_STICKY;
            }

            if (flash_mode_on) {
                if (cam == null) {
                    cam = Camera.open();
                }
                cameraOn();
            } else {
                cameraOff();
            }

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);
            views.setImageViewResource(R.id.imageViewWidget, flash_mode_on ? R.drawable.on : R.drawable.off);

            Intent pIntent;
            pIntent = new Intent(UpdateService.this, UpdateService.class)
                    .putExtra("flash_mode_on", !flash_mode_on);
            views.setOnClickPendingIntent(R.id.widgetRelativeLayout, PendingIntent.getService(this, 0, pIntent, 0));

            ComponentName thisWidget = new ComponentName(this, WidgetProvider.class);
            AppWidgetManager.getInstance(this).updateAppWidget(thisWidget, views);

            return START_STICKY;
        }

        @Override
        public void onDestroy() {
            cameraOff();
            super.onDestroy();
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}