package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.DatabaseRepository
import com.example.ui.FrenchCoachViewModel
import com.example.ui.FrenchCoachViewModelFactory
import com.example.ui.screens.AppNavigationContainer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize Room offline database and repository layer
    val database = AppDatabase.getDatabase(this)
    val coachDao = database.coachDao()
    val repository = DatabaseRepository(coachDao)
    
    // Build FrenchCoachViewModel using custom factory
    val factory = FrenchCoachViewModelFactory(repository, application)
    val viewModel = ViewModelProvider(this, factory)[FrenchCoachViewModel::class.java]

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          AppNavigationContainer(
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}

