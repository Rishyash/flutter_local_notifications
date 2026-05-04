import 'package:flutter_local_notifications_platform_interface/flutter_local_notifications_platform_interface.dart';

/// Launch details returned by
/// [AndroidFlutterLocalNotificationsPlugin.getFullScreenNotificationLaunchDetails]
/// when the app was opened via [AndroidFullScreenNotificationController.openMainApp].
///
/// Contains the original notification data plus the [actionType] and [actionData]
/// map passed to [openMainApp].
class FullScreenNotificationLaunchDetails {
  const FullScreenNotificationLaunchDetails({
    this.notificationResponse,
    this.actionType,
    this.actionData,
  });

  /// The notification that triggered the full-screen activity.
  final NotificationResponse? notificationResponse;

  /// The action key passed to [AndroidFullScreenNotificationController.openMainApp].
  final String? actionType;

  /// The action data passed to [AndroidFullScreenNotificationController.openMainApp].
  final Map<String, dynamic>? actionData;
}
