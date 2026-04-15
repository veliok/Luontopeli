package com.example.luontopeli.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.luontopeli.viewmodel.StatsViewModel

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) { // Lisätty ViewModel
    val lastSession by viewModel.lastSession.collectAsState(initial = null)

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Jos edellinen retki löytyy, näytetään sen tiedot
            lastSession?.let { session ->
                Text("Edellinen retki", style = MaterialTheme.typography.headlineSmall)
                Text("Askeleet: ${session.stepCount}")
                Text("Matka: ${session.distanceMeters}m")
            } ?: run {
                Text("Ei aiempia retkiä")
            }
        }
    }
}