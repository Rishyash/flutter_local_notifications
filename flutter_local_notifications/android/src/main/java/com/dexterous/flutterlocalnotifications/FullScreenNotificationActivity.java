package com.dexterous.flutterlocalnotifications;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

/**
 * Transparent activity used as the target for full-screen notification intents.
 *
 * <p>Declared in the library manifest with {@code showWhenLocked} and {@code turnScreenOn}, so it
 * can appear over the lock screen without touching the host app's main activity. It immediately
 * forwards the notification tap to the app's main launch activity and finishes itself, keeping the
 * normal {@code onNotificationResponse} callback flow intact.
 */
public class FullScreenNotificationActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    enableOverLockScreen();
    forwardToMainActivity();
    finish();
  }

  private void enableOverLockScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      // API 27+: use the dedicated Activity methods (FLAG_* equivalents are deprecated there)
      setShowWhenLocked(true);
      setTurnScreenOn(true);
    } else {
      getWindow()
          .addFlags(
              WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                  | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }
  }

  private void forwardToMainActivity() {
    Intent source = getIntent();

    PackageManager pm = getPackageManager();
    Intent launchIntent = pm.getLaunchIntentForPackage(getPackageName());
    if (launchIntent == null) {
      return;
    }

    // Carry the notification action and data so the plugin's existing
    // onNewIntent / onAttachedToActivity handlers pick it up unchanged.
    launchIntent.setAction(FlutterLocalNotificationsPlugin.SELECT_NOTIFICATION);
    launchIntent.putExtra(
        FlutterLocalNotificationsPlugin.NOTIFICATION_ID,
        source.getIntExtra(FlutterLocalNotificationsPlugin.NOTIFICATION_ID, -1));
    launchIntent.putExtra(
        FlutterLocalNotificationsPlugin.PAYLOAD,
        source.getStringExtra(FlutterLocalNotificationsPlugin.PAYLOAD));

    // FLAG_ACTIVITY_SINGLE_TOP reuses the existing Flutter activity and triggers
    // onNewIntent, which the plugin already listens to. getLaunchIntentForPackage
    // already adds FLAG_ACTIVITY_NEW_TASK so the app can start from the lock screen.
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

    startActivity(launchIntent);
  }
}
