package com.example.luontopeli.ui.health

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import com.example.luontopeli.data.remote.health.HealthConnectManager
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun HealthConnectPermissionScreen(
    healthConnectManager: HealthConnectManager
) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.containsAll(healthConnectManager.getRequiredPermissions())) {
            Toast.makeText(context, "Yhteys luotu!", Toast.LENGTH_SHORT).show()
        }
    }

    // Jos sovellus ei tue Health Connectia
    if (!healthConnectManager.isAvailable()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Health Connect ei ole saatavilla",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    "Varmista, että Health Connect on asennettu ja päivitetty laitteen asetuksista tai Play-kaupasta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Terveystiedot",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                "Synkronoi askeleet ja kalorit suoraan laitteestasi.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    android.util.Log.e("LUONTOPELI", "NAPPIA PAINETTU!")
                    try {
                        val luvat = healthConnectManager.getRequiredPermissions()
                        android.util.Log.e("LUONTOPELI", "Pyydetään luvat: $luvat")
                        permissionLauncher.launch(luvat)
                    } catch (e: Exception) {
                        android.util.Log.e("LUONTOPELI", "VIRHE: ${e.message}")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Yhdistä Health Connect")
            }
        }
    }
}