package com.example.proyectofinalmovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectofinalmovil.ui.theme.ProyectoFinalMovilTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProyectoFinalMovilTheme {
                // Pantalla principal con Scaffold
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        // Ajuste de posición para el FloatingActionButton hacia la izquierda y arriba
                        Box(modifier = Modifier.padding(bottom = 50.dp, end = 30.dp)) {
                            FloatingActionButton(
                                onClick = { /* No hace nada aún */ },
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text("+")
                            }
                        }
                    }
                ) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    // Definimos un layout vertical con el fondo blanco
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ajuste del texto "Notas"
        Text(
            text = "Notas",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, bottom = 16.dp),  // Se baja un poco más el texto
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ProyectoFinalMovilTheme {
        MainScreen()
    }
}
