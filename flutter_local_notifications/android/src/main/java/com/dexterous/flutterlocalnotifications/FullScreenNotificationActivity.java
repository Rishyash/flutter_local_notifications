package com.dexterous.flutterlocalnotifications;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

/**
 * A full-screen notification activity that renders real Flutter UI over the lock screen.
 *
 * <p>Set {@code fullScreenIntent: true} in {@code AndroidNotificationDetails} to use it.
 * The activity always opens at the app's initial route ("/") directly on the lock screen —
 * no unlock required to see the UI.
 *
 * <p>Use {@code AndroidFullScreenNotificationController} (Dart) to:
 * <ul>
 *   <li>Dismiss the activity.
 *   <li>Open the main app (prompts unlock on secure lock screens).
 * </ul>
 */
public class FullScreenNotificationActivity extends FlutterActivity {

  /** Method channel name — must match the constant in AndroidFullScreenNotificationController.dart */
  private static final String CHANNEL =
      "dexterous.com/flutter/local_notifications/full_screen";

  // -------------------------------------------------------------------------
  // Lifecycle
  // -------------------------------------------------------------------------

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    // Window flags must be applied before super.onCreate() so Flutter's
    // first frame is already allowed to render over the lock screen.
    enableOverLockScreen();
    super.onCreate(savedInstanceState);
  }

  // -------------------------------------------------------------------------
  // FlutterActivity overrides
  // -------------------------------------------------------------------------

  /**
   * Registers the full-screen method channel alongside the app's own plugins.
   * {@code super.configureFlutterEngine()} handles GeneratedPluginRegistrant so
   * all other plugins (including FlutterLocalNotificationsPlugin) are available too.
   */
  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    super.configureFlutterEngine(flutterEngine);

    new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
        .setMethodCallHandler(
            (call, result) -> {
              switch (call.method) {
                case "dismiss":
                  finish();
                  result.success(null);
                  break;
                case "openMainApp":
                  handleOpenMainApp(result);
                  break;
                default:
                  result.notImplemented();
              }
            });
  }

  // -------------------------------------------------------------------------
  // Method channel handlers
  // -------------------------------------------------------------------------

  /**
   * Opens the main app with the notification response.
   *
   * <p>On a secure lock screen the system unlock prompt is shown first. The main
   * app only opens after a successful unlock, keeping the lock screen secure. If
   * the user cancels the prompt this activity stays visible so they can try again.
   */
  private void handleOpenMainApp(MethodChannel.Result result) {
    KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

    boolean isSecureLocked =
        km != null && km.isKeyguardLocked() && km.isKeyguardSecure();

    if (isSecureLocked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      km.requestDismissKeyguard(
          this,
          new KeyguardManager.KeyguardDismissCallback() {
            @Override
            public void onDismissSucceeded() {
              launchMainActivity();
              finish();
            }

            @Override
            public void onDismissCancelled() {
              // User cancelled — stay on the full-screen activity.
            }

            @Override
            public void onDismissError() {
              finish();
            }
          });
    } else {
      launchMainActivity();
      finish();
    }

    result.success(null);
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  /** Applies the flags that allow this activity to render over the lock screen. */
  private void enableOverLockScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      setShowWhenLocked(true);
      setTurnScreenOn(true);
    } else {
      getWindow()
          .addFlags(
              WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                  | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }
  }

  /**
   * Starts the app's main launch activity and carries the notification extras so
   * the plugin's existing {@code onNewIntent} / {@code onAttachedToActivity} handlers
   * fire {@code onNotificationResponse} as usual.
   */
  private void launchMainActivity() {
    Intent source = getIntent();
    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
    if (launchIntent == null) return;

    launchIntent.setAction(FlutterLocalNotificationsPlugin.SELECT_NOTIFICATION);
    launchIntent.putExtra(
        FlutterLocalNotificationsPlugin.NOTIFICATION_ID,
        source.getIntExtra(FlutterLocalNotificationsPlugin.NOTIFICATION_ID, -1));
    launchIntent.putExtra(
        FlutterLocalNotificationsPlugin.PAYLOAD,
        source.getStringExtra(FlutterLocalNotificationsPlugin.PAYLOAD));

    // FLAG_ACTIVITY_SINGLE_TOP reuses a running Flutter activity (triggers onNewIntent).
    // getLaunchIntentForPackage already adds FLAG_ACTIVITY_NEW_TASK.
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

    startActivity(launchIntent);
  }
}
