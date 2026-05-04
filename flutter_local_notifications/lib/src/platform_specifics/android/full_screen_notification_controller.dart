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

  /// Closes the full-screen activity.
  static Future<void> dismiss() => _channel.invokeMethod<void>('dismiss');

  /// Opens the main app, carrying [actionType] and [actionData] so the main
  /// app can retrieve them via
  /// [AndroidFlutterLocalNotificationsPlugin.getFullScreenNotificationLaunchDetails].
  ///
  /// On a secure lock screen the system unlock prompt is shown first — the app
  /// opens only after a successful unlock. If the user cancels, the activity
  /// stays visible.
  ///
  /// [getNotificationAppLaunchDetails] returns `null` for this launch so the
  /// two flows don't conflict.
  ///
  /// ```dart
  /// await AndroidFullScreenNotificationController.openMainApp(
  ///   actionType: 'FALSE_THEFT_ALERT_BOTTOM',
  ///   actionData: {'vehicleNum': 'HR55AD8556', 'vehicleId': '123'},
  /// );
  /// ```
  static Future<void> openMainApp({
    required String actionType,
    Map<String, dynamic> actionData = const {},
  }) =>
      _channel.invokeMethod<void>('openMainApp', {
        'actionType': actionType,
        'actionData': actionData,
      });
}
