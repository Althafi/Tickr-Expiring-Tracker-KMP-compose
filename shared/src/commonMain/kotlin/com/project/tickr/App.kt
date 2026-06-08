package com.project.tickr

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.project.tickr.ui.navigation.TickrNavGraph
import com.project.tickr.ui.theme.TickrTheme

@Composable
@Preview
fun App() {
    TickrTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            TickrNavGraph(navController = navController)
        }
    }
}