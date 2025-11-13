package ph.edu.comteq.jokesapiclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ph.edu.comteq.jokesapiclient.ui.theme.JokesApiClientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JokesApiClientTheme {
                val jokesViewModel: JokesViewModel = viewModel()
                var showDialog by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { showDialog = true },
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Joke"
                            )
                        }
                    }
                ) { innerPadding ->
                    JokesScreen(
                        viewModel = jokesViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                if (showDialog) {
                    AddJokeDialog(
                        onDismiss = { showDialog = false },
                        onConfirm = { setup, punchline ->
                            showDialog = false
                            jokesViewModel.addJoke(setup, punchline)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun JokesScreen(viewModel: JokesViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var jokeToDelete by remember { mutableStateOf<Joke?>(null) }
    var jokeToEdit by remember { mutableStateOf<Joke?>(null) }

    // Fetch jokes when the screen first loads
    LaunchedEffect(Unit) {
        viewModel.getJokes()
    }

    Column(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is JokesUiState.Idle -> {
                // initial state (show nothing or a placeholder)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No jokes to show")
                }
            }
            is JokesUiState.Loading -> {
                // loading
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is JokesUiState.Success -> {
                // show a list of jokes
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(state.jokes) { joke ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // Joke content
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = joke.setup,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = joke.punchline,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                // Actions row - magkatabi na ang mga icon
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Edit button
                                    IconButton(
                                        onClick = { jokeToEdit = joke }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Joke",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Delete button
                                    IconButton(
                                        onClick = {
                                            jokeToDelete = joke
                                            showDeleteDialog = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Joke",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is JokesUiState.Error -> {
                // show an error message
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                jokeToDelete = null
            },
            title = { Text("Delete Joke?") },
            text = { Text("Are you sure you want to delete this joke?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        jokeToDelete?.id?.let { viewModel.deleteJoke(it) }
                        showDeleteDialog = false
                        jokeToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    jokeToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Joke Dialog
    jokeToEdit?.let { joke ->
        EditJokeDialog(
            joke = joke,
            onDismiss = { jokeToEdit = null },
            onConfirm = { setup, punchline ->
                viewModel.updateJoke(joke.id!!, setup, punchline)
                jokeToEdit = null
            }
        )
    }
}

@Composable
fun AddJokeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var setup by remember { mutableStateOf("") }
    var punchline by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Joke") },
        text = {
            Column {
                OutlinedTextField(
                    value = setup,
                    onValueChange = { setup = it },
                    label = { Text(text = "Setup") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = punchline,
                    onValueChange = { punchline = it },
                    label = { Text(text = "Punchline") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (setup.isNotBlank() && punchline.isNotBlank()) {
                        onConfirm(setup, punchline)
                        setup = ""
                        punchline = ""
                    }
                },
                enabled = setup.isNotBlank() && punchline.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
fun EditJokeDialog(
    joke: Joke,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var setup by remember { mutableStateOf(joke.setup) }
    var punchline by remember { mutableStateOf(joke.punchline) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Joke") },
        text = {
            Column {
                OutlinedTextField(
                    value = setup,
                    onValueChange = { setup = it },
                    label = { Text("Setup") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = punchline,
                    onValueChange = { punchline = it },
                    label = { Text("Punchline") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (setup.isNotBlank() && punchline.isNotBlank()) {
                        onConfirm(setup, punchline)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

