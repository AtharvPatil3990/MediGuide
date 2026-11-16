package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalMediStrings
import com.example.data.local.entity.ReminderStatus
import com.example.domain.model.UserProfile
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.DeepTealDark
import com.example.ui.theme.MintAccent
import com.example.ui.theme.TerracottaCoral
import com.example.ui.theme.WarmAmber

@Composable
fun MediTopBar(
    activeProfile: UserProfile?,
    currentLanguage: AppLanguage,
    onCycleLanguage: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalMediStrings.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = strings.offlineBadge,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onCycleLanguage)
                    .testTag("top_language_chip")
            ) {
                Text(
                    text = currentLanguage.nativeName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onProfileClick)
                    .testTag("top_profile_chip")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = strings.activeProfileLabel,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = activeProfile?.name?.substringBefore(" ") ?: "User",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun AdherenceRingCanvas(
    percent: Int,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
    val arcColor = MaterialTheme.colorScheme.onPrimary
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(82.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 9.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = arcColor,
                startAngle = -90f,
                sweepAngle = (percent.coerceIn(0, 100) / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun HeroGradientCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(DeepTeal, DeepTealDark)
                )
            )
            .padding(20.dp)
    ) {
        content()
    }
}

@Composable
fun ReminderStatusBadge(
    status: ReminderStatus,
    responseType: ReminderStatus?,
    modifier: Modifier = Modifier
) {
    val strings = LocalMediStrings.current
    val (label, bgColor, fgColor, icon) = when {
        status == ReminderStatus.COMPLETED && responseType == ReminderStatus.TAKEN ->
            Quad(strings.statusTaken, MintAccent.copy(alpha = 0.16f), DeepTealDark, Icons.Filled.CheckCircle)
        status == ReminderStatus.COMPLETED && responseType == ReminderStatus.SKIPPED ->
            Quad(strings.statusSkipped, TerracottaCoral.copy(alpha = 0.16f), TerracottaCoral, Icons.Filled.SkipNext)
        status == ReminderStatus.REMINDER_SENT ->
            Quad(strings.statusReminderSent, WarmAmber.copy(alpha = 0.18f), WarmAmber, Icons.Filled.NotificationsActive)
        status == ReminderStatus.COMPLETED ->
            Quad(strings.statusCompleted, MintAccent.copy(alpha = 0.16f), DeepTealDark, Icons.Filled.CheckCircle)
        else ->
            Quad(strings.statusScheduled, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Filled.Schedule)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fgColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = fgColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

@Composable
fun ProfileAvatarBadge(
    name: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "M",
            style = MaterialTheme.typography.titleMedium,
            color = if (isActive) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
