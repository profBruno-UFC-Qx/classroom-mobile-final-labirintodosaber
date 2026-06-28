package com.labirintodosaber

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.labirintodosaber.data.local.UserPreferencesStore
import com.labirintodosaber.ui.navigation.AppNavGraph
import com.labirintodosaber.ui.theme.LabirintodoSaberTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPreferencesStore: UserPreferencesStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by userPreferencesStore.isDarkTheme
                .collectAsStateWithLifecycle(initialValue = false)

            LabirintodoSaberTheme(darkTheme = isDarkTheme) {
                AppNavGraph()
            }
        }
    }
}
