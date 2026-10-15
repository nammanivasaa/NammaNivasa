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
import com.example.nammanivasa.data.model.MaintenanceRequestItem
import com.example.nammanivasa.data.repository.RepoCreateMaintenanceRequestRequest
import com.example.nammanivasa.ui.components.NnInlineEmptyCard
import com.example.nammanivasa.ui.components.NnSubScreenHeader
import com.example.nammanivasa.ui.components.NnSubmitErrorCard

@Composable
fun MaintenanceRequestScreen(
    modifier: Modifier = Modifier,
    requests: List<MaintenanceRequestItem>,
    isSubmitting: Boolean,
    submitError: String?,
    onCreateRequest: (RepoCreateMaintenanceRequestRequest) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("PLUMBING") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var flatId by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NnSubScreenHeader(
            title = "Maintenance Requests",
            subtitle = "Create a new maintenance request or view existing ones",
            onClose = onCancel
        )

        submitError?.let { error ->
            NnSubmitErrorCard(message = error)
        }

        Text("Create New Request", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            placeholder = { Text("e.g., Kitchen sink leak") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSubmitting
        )

        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            placeholder = { Text("Describe the issue") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            enabled = !isSubmitting
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                placeholder = { Text("PLUMBING, ELECTRICAL, CARPENTRY, OTHER") },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting
            )
            TextField(
                value = priority,
                onValueChange = { priority = it },
                label = { Text("Priority") },
                placeholder = { Text("LOW, MEDIUM, HIGH, URGENT") },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting
            )
        }

        TextField(
            value = flatId,
            onValueChange = { flatId = it },
            label = { Text("Flat ID (Optional)") },
            placeholder = { Text("e.g., A-101") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSubmitting
        )

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
                    if (title.isNotBlank() && description.isNotBlank()) {
                        onCreateRequest(RepoCreateMaintenanceRequestRequest(
                            title = title,
                            description = description,
                            category = category,
                            flatId = if (flatId.isNotBlank()) flatId else null,
                            priority = priority
                        ))
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isSubmitting && title.isNotBlank() && description.isNotBlank()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text("Submit Request")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your Requests",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (requests.isEmpty()) {
            NnInlineEmptyCard(message = "No maintenance requests")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(requests) { request ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(request.title, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(request.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Category: ${request.category} | Priority: ${request.priority}", style = MaterialTheme.typography.bodySmall)
                                Text(request.status, style = MaterialTheme.typography.labelMedium,
                                    color = when (request.status) {
                                        "OPEN" -> MaterialTheme.colorScheme.primary
                                        "IN_PROGRESS" -> MaterialTheme.colorScheme.tertiary
                                        "RESOLVED" -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}