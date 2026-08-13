package com.example.ui.screens.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.L10nStrings
import com.example.data.model.AchievementItem
import com.example.data.repository.Resource
import com.example.ui.components.LinguaXHeader
import com.example.ui.components.ResourceContainer
import com.example.ui.theme.*

@Composable
fun AchievementsScreen(
    l10n: L10nStrings,
    achievementsResource: Resource<List<AchievementItem>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
            .padding(16.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LinguaXHeader(
            title = l10n.achievementsTab,
            subtitle = l10n.unlockedAchievements
        )

        ResourceContainer(
            resource = achievementsResource,
            loadingText = l10n.loading,
            emptyText = l10n.noDataAvailable
        ) { achievements ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(achievements) { item ->
                    AchievementCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(item: AchievementItem) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isUnlocked) Color.White else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isUnlocked) 2.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (item.isUnlocked) LinguaXAccentGold.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (item.iconName) {
                            "fire" -> Icons.Default.LocalFireDepartment
                            "globe" -> Icons.Default.Language
                            "trophy" -> Icons.Default.EmojiEvents
                            "book" -> Icons.Default.MenuBook
                            else -> Icons.Default.Star
                        },
                        contentDescription = null,
                        tint = if (item.isUnlocked) LinguaXAccentGold else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (item.isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )
                    Surface(
                        shape = CircleShape,
                        color = LinguaXSecondaryContainer
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall.copy(color = LinguaXOnSecondaryContainer),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!item.isUnlocked) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { item.progress.toFloat() / item.maxProgress.toFloat().coerceAtLeast(1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = LinguaXPrimary,
                        trackColor = LinguaXPrimaryContainer
                    )
                } else if (!item.unlockedAt.isNullOrBlank()) {
                    Text(
                        text = "Unlocked on ${item.unlockedAt}",
                        style = MaterialTheme.typography.labelSmall,
                        color = LinguaXSuccessGreen,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
