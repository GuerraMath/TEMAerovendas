// Caminho: app/src/main/java/com/temaerovendas/MainActivity.kt
package com.temaerovendas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.temaerovendas.navigation.AppNavGraph
import com.temaerovendas.ui.theme.TEMAerovendasTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TEMAerovendasTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}
