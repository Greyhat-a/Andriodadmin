package com.nexus.app.ui.navigation

import androidx.annotation.DrawableRes
import com.nexus.app.R

/** Top-level destinations shown in the bottom navigation bar. */
enum class Destination(
    val route: String,
    val label: String,
    @DrawableRes val icon: Int,
) {
    Devices("devices", "Devices", R.drawable.ic_nav_devices),
    Map("map", "Map", R.drawable.ic_nav_map),
    Files("files", "Files", R.drawable.ic_nav_files),
    Academy("academy", "Academy", R.drawable.ic_nav_academy),
    Settings("settings", "Settings", R.drawable.ic_nav_settings),
}
