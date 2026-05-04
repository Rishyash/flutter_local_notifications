package com.dexterous.flutterlocalnotifications;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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
                  handleOpenMainApp(call.arguments(), result);
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
   * Opens the main app carrying {@code actionType} and {@code actionData} so the main app
   * can retrieve them via {@code getFullScreenNotificationLaunchDetails()}.
   *
   * <p>On a secure lock screen the system unlock prompt is shown first. The main
   * app only opens after a successful unlock. If the user cancels, the activity stays visible.
   *
   * @param args map from Dart: {@code {"actionType": "...", "actionData": {...}}}
   */
  @SuppressWarnings("unchecked")
  private void handleOpenMainApp(Object args, MethodChannel.Result result) {
    Map<String, Object> argsMap = (args instanceof Map) ? (Map<String, Object>) args : new HashMap<>();

    KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
    boolean isSecureLocked = km != null && km.isKeyguardLocked() && km.isKeyguardSecure();

    if (isSecureLocked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      km.requestDismissKeyguard(
          this,
          new KeyguardManager.KeyguardDismissCallback() {
            @Override
            public void onDismissSucceeded() {
              launchMainActivity(argsMap);
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
      launchMainActivity(argsMap);
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
  /**
   * Starts the main app with a {@link FlutterLocalNotificationsPlugin#SELECT_NOTIFICATION_FULL_SCREEN}
   * action so {@code getNotificationAppLaunchDetails()} returns null while
   * {@code getFullScreenNotificationLaunchDetails()} returns the full data.
   */
  @SuppressWarnings("unchecked")
  private void launchMainActivity(Map<String, Object> argsMap) {
    Intent source = getIntent();
    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
    if (launchIntent == null) return;

    // Use a distinct action so the two launch-detail APIs don't overlap.
    launchIntent.setAction(FlutterLocalNotificationsPlugin.SELECT_NOTIFICATION_FULL_SCREEN);
    launchIntent.putExtra(
        FlutterLocalNotificationsPlugin.NOTIFICATION_ID,
        source.getIntExtra(FlutterLocalNotificationsPlugin.NOTIFICATION_ID, -1));
    launchIntent.putExtra(
        FlutterLocalNotificationsPlugin.PAYLOAD,
        source.getStringExtra(FlutterLocalNotificationsPlugin.PAYLOAD));

    // Carry actionType as a plain string.
    launchIntent.putExtra(
        FlutterLocalNotificationsPlugin.FULL_SCREEN_ACTION_TYPE,
        (String) argsMap.get("actionType"));

    // Serialise actionData map to JSON so it survives the Intent boundary.
    Object actionData = argsMap.get("actionData");
    if (actionData instanceof Map) {
      try {
        launchIntent.putExtra(
            FlutterLocalNotificationsPlugin.FULL_SCREEN_ACTION_DATA,
            new JSONObject((Map<String, Object>) actionData).toString());
      } catch (Exception ignored) {}
    }

    // FLAG_ACTIVITY_SINGLE_TOP reuses a running Flutter activity (triggers onNewIntent).
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

    startActivity(launchIntent);
  }
}
