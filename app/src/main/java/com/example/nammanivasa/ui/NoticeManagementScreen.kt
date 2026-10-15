package com.example.nammanivasa.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammanivasa.data.model.NoticeItem
import com.example.nammanivasa.data.repository.RepoCreateNoticeRequest
import com.example.nammanivasa.ui.components.NnInlineEmptyCard
import com.example.nammanivasa.ui.components.NnSubScreenHeader
import com.example.nammanivasa.ui.components.NnSubmitErrorCard

@Composable
fun NoticeManagementScreen(
    modifier: Modifier = Modifier,
    notices: List<NoticeItem>,
    isSubmitting: Boolean,
    submitError: String?,
    onCreateNotice: (RepoCreateNoticeRequest) -> Unit,
    onPublishNotice: (String) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isPublished by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NnSubScreenHeader(
            title = "Notice Management",
            subtitle = "Create and publish community notices",
            onClose = onCancel
        )

        submitError?.let { error ->
            NnSubmitErrorCard(message = error)
        }

        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            placeholder = { Text("Notice title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSubmitting
        )

        TextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            placeholder = { Text("Notice message") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            enabled = !isSubmitting
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = isPublished,
                onCheckedChange = { isPublished = it },
                enabled = !isSubmitting
            )
            Text("Publish immediately", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                enabled = !isSubmitting
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        onCreateNotice(RepoCreateNoticeRequest(
                            title = title,
                            message = message,
                            published = isPublished
                        ))
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isSubmitting && title.isNotBlank() && message.isNotBlank()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text("Create Notice")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Existing Notices",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (notices.isEmpty()) {
            NnInlineEmptyCard(message = "No notices found")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notices) { notice ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(notice.title, fontWeight = FontWeight.Bold)
                                Text(notice.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Status: ${notice.status}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (notice.status != "PUBLISHED") {
                                Button(
                                    onClick = { onPublishNotice(notice.id) },
                                    enabled = !isSubmitting
                                ) {
                                    Text("Publish")
                                }
                            } else {
                                Text("Published", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}