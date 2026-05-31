package com.labirintodosaber

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.labirintodosaber.ui.navigation.AppNavGraph
import com.labirintodosaber.ui.theme.LabirintodoSaberTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LabirintodoSaberTheme {
                AppNavGraph()
            }
        }
    }
}
