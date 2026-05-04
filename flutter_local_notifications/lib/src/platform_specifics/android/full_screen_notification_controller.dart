import 'package:flutter/services.dart';

/// Controls the full-screen notification activity from Flutter.
///
/// Use this class inside your app's initial route ("/"), which is what
/// [FullScreenNotificationActivity] always opens — directly over the lock screen
/// so the user sees your Flutter UI without unlocking first.
///
/// ## Getting notification data
///
/// Use the standard [FlutterLocalNotificationsPlugin.getNotificationAppLaunchDetails]
/// method — it works unchanged inside this activity because the plugin is fully
/// registered in the full-screen Flutter engine:
///
/// ```dart
/// @override
/// void initState() {
///   super.initState();
///   _loadData();
/// }
///
/// Future<void> _loadData() async {
///   final details = await flutterLocalNotificationsPlugin
///       .getNotificationAppLaunchDetails();
///   final payload = details?.notificationResponse?.payload;
/// }
/// ```
///
/// ## Controlling the activity
///
/// ```dart
/// // Close without opening the main app
/// await AndroidFullScreenNotificationController.dismiss();
///
/// // Open the main app (shows PIN prompt on secure lock screens)
/// await AndroidFullScreenNotificationController.openMainApp();
/// ```
class AndroidFullScreenNotificationController {
  AndroidFullScreenNotificationController._();

  static const MethodChannel _channel = MethodChannel(
    'dexterous.com/flutter/local_notifications/full_screen',
  );

  /// Closes the full-screen activity without opening the main app.
  static Future<void> dismiss() => _channel.invokeMethod<void>('dismiss');

  /// Opens the main app and fires [onDidReceiveNotificationResponse] as usual.
  ///
  /// On a secure lock screen (PIN / pattern / password), the system unlock
  /// prompt is shown first. The app opens only after a successful unlock.
  /// If the user cancels, the full-screen activity stays visible so they can
  /// try again or dismiss it.
  static Future<void> openMainApp() => _channel.invokeMethod<void>('openMainApp');
}
