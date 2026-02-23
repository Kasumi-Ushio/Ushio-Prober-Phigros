package org.kasumi321.ushio.phitracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import org.kasumi321.ushio.phitracker.ui.PhiTrackerNavHost
import org.kasumi321.ushio.phitracker.ui.theme.PhiTrackerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhiTrackerTheme {
                PhiTrackerNavHost()
            }
        }
    }
}
