package fr.eseo.ld.ts.cinelog

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import fr.eseo.ld.ts.cinelog.ui.screens.MovieDetailsPreview
import fr.eseo.ld.ts.cinelog.ui.screens.SummaryScreenPreview
import fr.eseo.ld.ts.cinelog.ui.theme.AppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("lifecycle", "onCreate Started")
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MovieDetailsPreview()
            }
        }
    }


}
