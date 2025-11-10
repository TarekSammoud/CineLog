package fr.eseo.ld.ts.cinelog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import fr.eseo.ld.ts.cinelog.R
import fr.eseo.ld.ts.cinelog.model.Media
import fr.eseo.ld.ts.cinelog.viewmodel.ImdbViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: ImdbViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    // Live data from the viewmodel
    val mediaList by viewModel.mediaList.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()

    // Query states
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var debouncedQuery by remember { mutableStateOf("") }

    LaunchedEffect(query.text) {
        delay(250)
        debouncedQuery = query.text
    }

    LaunchedEffect(Unit) {
        viewModel.fetchAllMedia()
    }

    fun onQueryChange(newValue: TextFieldValue) {
        query = newValue
    }

    val filtered = remember(mediaList, debouncedQuery) {
        if (debouncedQuery.isBlank()) mediaList
        else mediaList.filter { it.primaryTitle.contains(debouncedQuery, ignoreCase = true) }
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Scaffold(
            topBar = {

                TopAppBar(
                    title = { Text(text = "Recherche",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )},
                    navigationIcon = {}
                )
            }
        ) { padding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { onQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    placeholder = { Text("Rechercher un film...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (query.text.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange(TextFieldValue("") ) }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    errorMessage != null -> Text("Erreur : $errorMessage", color = MaterialTheme.colorScheme.error)
                    else -> {
                        if (filtered.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Aucun résultat")
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 4.dp)
                            ) {
                                items(filtered, key = { it.id }) { media ->
                                    SearchMediaCard(media = media, onClick = {
                                        navController.navigate("DETAILS_SCREEN/${media.id}")
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchMediaCard(
    media: Media,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(8.dp)),
        onClick = onClick
    ) {
        AsyncImage(
            model = media.primaryImage?.url,
            contentDescription = media.primaryTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            placeholder = painterResource(R.drawable.ic_launcher_foreground),
            error = painterResource(R.drawable.ic_launcher_foreground)
        )
    }
}
