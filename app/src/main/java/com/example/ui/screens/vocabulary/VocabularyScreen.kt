package com.example.ui.screens.vocabulary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.L10nStrings
import com.example.data.model.VocabularyItem
import com.example.data.repository.Resource
import com.example.ui.components.LinguaXHeader
import com.example.ui.components.ResourceContainer
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyScreen(
    l10n: L10nStrings,
    vocabularyResource: Resource<List<VocabularyItem>>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterSavedOnly by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
            .padding(16.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LinguaXHeader(
            title = l10n.vocabularyTab,
            subtitle = "Master essential words & expressions"
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(l10n.searchVocabulary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vocabulary_search_input")
        )

        // Filter Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = !filterSavedOnly,
                onClick = { filterSavedOnly = false },
                label = { Text(l10n.filterAll) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LinguaXPrimary,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            )

            FilterChip(
                selected = filterSavedOnly,
                onClick = { filterSavedOnly = true },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(l10n.filterBookmarked)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LinguaXPrimary,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }

        // Vocabulary List
        ResourceContainer(
            resource = vocabularyResource,
            loadingText = l10n.loading,
            emptyText = l10n.noDataAvailable
        ) { rawList ->
            var vocabList by remember(rawList) { mutableStateOf(rawList) }

            val filteredList = remember(vocabList, searchQuery, filterSavedOnly) {
                vocabList.filter { item ->
                    val matchesQuery = searchQuery.isBlank() ||
                            item.word.contains(searchQuery, ignoreCase = true) ||
                            item.translation.contains(searchQuery, ignoreCase = true)
                    val matchesBookmark = !filterSavedOnly || item.isBookmarked
                    matchesQuery && matchesBookmark
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = l10n.noDataAvailable,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList) { item ->
                        VocabularyCard(
                            item = item,
                            onToggleBookmark = {
                                vocabList = vocabList.map {
                                    if (it.id == item.id) it.copy(isBookmarked = !it.isBookmarked) else it
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VocabularyCard(
    item: VocabularyItem,
    onToggleBookmark: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.word,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LinguaXPrimary
                        )
                    )
                    if (!item.phonetic.isNullOrBlank()) {
                        Text(
                            text = item.phonetic,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* Play audio pronunciation */ }) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Listen",
                            tint = LinguaXPrimary
                        )
                    }
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (item.isBookmarked) LinguaXAccentGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = LinguaXSecondaryContainer
            ) {
                Text(
                    text = item.partOfSpeech,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = LinguaXOnSecondaryContainer,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }

            Text(
                text = item.translation,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!item.exampleSentence.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LinguaXBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💬 \"${item.exampleSentence}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}
