package com.dexterous.flutterlocalnotifications.models;

import androidx.annotation.Nullable;

/**
 * Represents a mapping between a view ID and its associated data.
 * Used to configure custom notification views by mapping Android view IDs
 * to their corresponding text content or action IDs.
 */
public class CustomViewMapping {
    /**
     * The Android resource ID name of the view.
     */
    public String viewId;

    /**
     * The text content to display in the view (for TextViews).
     */
    @Nullable
    public String text;

    /**
     * The action identifier for clickable views (for Buttons).
     */
    @Nullable
    public String actionId;

    /**
     * Whether the view is visible. {@code false} hides it with {@code View.GONE}.
     */
    @Nullable
    public Boolean visible;

    /**
     * Name of a drawable resource to apply as the view's background.
     */
    @Nullable
    public String backgroundResource;

    /**
     * Hex color string applied as a TextView's text color (e.g. "#D9052C").
     */
    @Nullable
    public String textColor;

    /**
     * Maximum number of lines for a TextView.
     */
    @Nullable
    public Integer maxLines;

    /**
     * Whether the TextView text should be rendered bold.
     */
    @Nullable
    public Boolean bold;

    public CustomViewMapping(String viewId, @Nullable String text, @Nullable String actionId) {
        this.viewId = viewId;
        this.text = text;
        this.actionId = actionId;
    }

    public CustomViewMapping(
            String viewId,
            @Nullable String text,
            @Nullable String actionId,
            @Nullable Boolean visible,
            @Nullable String backgroundResource,
            @Nullable String textColor,
            @Nullable Integer maxLines,
            @Nullable Boolean bold) {
        this.viewId = viewId;
        this.text = text;
        this.actionId = actionId;
        this.visible = visible;
        this.backgroundResource = backgroundResource;
        this.textColor = textColor;
        this.maxLines = maxLines;
        this.bold = bold;
    }
}
