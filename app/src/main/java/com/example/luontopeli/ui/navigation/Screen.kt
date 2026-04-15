package com.example.luontopeli.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    // Karttanäkymä: GPS-reitti ja havaintojen sijainnit
    object Map : Screen("map", "Kartta", Icons.Filled.Map)
    // Kameranäkymä: CameraX-esikatselu + kuvaaminen
    object Camera : Screen("camera", "Kamera", Icons.Filled.CameraAlt)
    // Löydöt: muiden käyttäjien havainnot Firebasesta
    object Discover : Screen("discover", "Löydöt", Icons.Filled.Explore)
    // Tilastot: askeleet, matka, omat havainnot
    object Stats : Screen("stats", "Tilastot", Icons.Filled.BarChart)
    // Profiili
    object Profile : Screen("profile", "Profiili", Icons.Filled.AccountCircle)

    companion object {
        val bottomNavScreens = listOf(Map, Camera, Discover, Stats, Profile)
    }
}