/// Represents a mapping between a view ID and its associated data.
///
/// This class is used to configure custom notification views by mapping
/// Android view IDs to their corresponding text content or action IDs.
///
/// Example:
/// ```dart
/// CustomViewMapping(
///   viewId: 'notification_title',
///   text: 'Hello from Flutter',
/// )
/// ```
class CustomViewMapping {
  /// Creates a [CustomViewMapping] with the specified view ID and optional
  /// text and action ID.
  ///
  /// [viewId] is the Android resource ID name (e.g., 'button_action', 'text_title')
  /// [text] is the text content to set on the view (for TextViews)
  /// [actionId] is the action identifier for clickable views (for Buttons)
  /// [visible] toggles the view's visibility (false → View.GONE)
  /// [backgroundResource] is a drawable resource name applied as the background
  /// [textColor] is a hex color string (e.g. '#FFFFFF') applied to a TextView
  /// [maxLines] sets the maximum number of lines on a TextView
  /// [bold] renders a TextView's text in bold
  const CustomViewMapping({
    required this.viewId,
    this.text,
    this.actionId,
    this.visible,
    this.backgroundResource,
    this.textColor,
    this.maxLines,
    this.bold,
  });

  /// The Android resource ID name of the view.
  ///
  /// This should match the ID defined in your XML layout file.
  /// Example: 'button_action', 'text_title', 'image_icon'
  final String viewId;

  /// The text content to display in the view.
  ///
  /// This is typically used for TextView elements where you want to
  /// dynamically set the text from Flutter.
  final String? text;

  /// The action identifier for clickable views.
  ///
  /// When the view is clicked, this action ID will be sent back to Flutter
  /// through the notification callback, allowing you to handle the action.
  final String? actionId;

  /// Whether the view is visible. `false` hides it with `View.GONE`.
  final bool? visible;

  /// Name of a drawable resource to apply as the view's background.
  ///
  /// Example: 'app_bg_green_all_rounded'. Resolved on the native side via
  /// `getIdentifier(name, "drawable", packageName)`.
  final String? backgroundResource;

  /// Hex color string applied as a TextView's text color. Example: '#D9052C'.
  final String? textColor;

  /// Maximum number of lines for a TextView.
  final int? maxLines;

  /// Whether the TextView text should be rendered bold.
  final bool? bold;

  /// Converts this mapping to a map for platform channel communication.
  Map<String, dynamic> toMap() {
    return <String, dynamic>{
      'viewId': viewId,
      if (text != null) 'text': text,
      if (actionId != null) 'actionId': actionId,
      if (visible != null) 'visible': visible,
      if (backgroundResource != null) 'backgroundResource': backgroundResource,
      if (textColor != null) 'textColor': textColor,
      if (maxLines != null) 'maxLines': maxLines,
      if (bold != null) 'bold': bold,
    };
  }
}

/// Configuration for a custom notification view layout.
///
/// This class allows you to specify a custom Android XML layout for
/// notifications and configure the views within that layout.
///
/// Example:
/// ```dart
/// AndroidNotificationDetails(
///   'channel_id',
///   'channel_name',
///   customContentView: CustomNotificationView(
///     layoutName: 'custom_notification',
///     viewMappings: [
///       CustomViewMapping(viewId: 'title', text: 'Title'),
///       CustomViewMapping(viewId: 'button', text: 'Click', actionId: 'btn_action'),
///     ],
///   ),
///   customBigContentView: CustomNotificationView(
///     layoutName: 'custom_notification_expanded',
///     viewMappings: [...],
///   ),
///   customHeadsUpContentView: CustomNotificationView(
///     layoutName: 'custom_notification_headsup',
///     viewMappings: [...],
///   ),
/// )
/// ```
class CustomNotificationView {
  /// Creates a [CustomNotificationView] with the specified layout and mappings.
  ///
  /// [layoutName] is the name of the Android XML layout resource
  /// [viewMappings] is a list of view configurations for the layout
  const CustomNotificationView({
    required this.layoutName,
    this.viewMappings = const <CustomViewMapping>[],
  });

  /// The name of the Android XML layout resource.
  ///
  /// This should match the layout file name without the .xml extension.
  /// Example: 'custom_notification_layout'
  final String layoutName;

  /// List of view mappings that configure the views in the layout.
  ///
  /// Each mapping specifies how to configure a specific view in the layout,
  /// such as setting text content or attaching click actions.
  final List<CustomViewMapping> viewMappings;

  /// Converts this view configuration to a map for platform channel communication.
  Map<String, dynamic> toMap() {
    return <String, dynamic>{
      'layoutName': layoutName,
      'viewMappings': viewMappings.map((m) => m.toMap()).toList(),
    };
  }
}
