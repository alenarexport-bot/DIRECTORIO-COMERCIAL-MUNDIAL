package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.BusinessRepository
import com.example.ui.DirectoryApp
import com.example.ui.DirectoryViewModel
import com.example.ui.DirectoryViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Setup Room Database Instance
    val database = Room.databaseBuilder(
      applicationContext,
      AppDatabase::class.java,
      "comercial_directory_db"
    ).fallbackToDestructiveMigration()
     .build()

    // Instantiate repo & viewModel factory
    val repository = BusinessRepository(database)
    val factory = DirectoryViewModelFactory(repository)
    val viewModel: DirectoryViewModel by viewModels { factory }

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        DirectoryApp(viewModel)
      }
    }
  }
}

