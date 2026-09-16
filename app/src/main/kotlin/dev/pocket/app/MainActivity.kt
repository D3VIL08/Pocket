package dev.pocket.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import dev.pocket.app.navigation.PocketNavHost
import dev.pocket.core.designsystem.theme.PocketTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Draw behind the system bars; the Scaffolds inside handle their own insets.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            PocketTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PocketNavHost()
                }
            }
        }
    }
}
