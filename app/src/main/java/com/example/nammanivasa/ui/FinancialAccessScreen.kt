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
import com.example.nammanivasa.data.model.FinancialAccessGrantItem
import com.example.nammanivasa.data.repository.RepoCreateFinancialAccessGrantRequest
import com.example.nammanivasa.ui.components.NnInlineEmptyCard
import com.example.nammanivasa.ui.components.NnSubScreenHeader
import com.example.nammanivasa.ui.components.NnSubmitErrorCard

@Composable
fun FinancialAccessScreen(
    modifier: Modifier = Modifier,
    grants: List<FinancialAccessGrantItem>,
    isSubmitting: Boolean,
    submitError: String?,
    ownerUserId: String?,
    onCreateGrant: (RepoCreateFinancialAccessGrantRequest) -> Unit,
    onRevokeGrant: (String) -> Unit,
    onCancel: () -> Unit
) {
    var granteeUserId by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NnSubScreenHeader(
            title = "Financial Access",
            subtitle = "Grant household members access to view financial records",
            onClose = onCancel
        )

        submitError?.let { error ->
            NnSubmitErrorCard(message = error)
        }

        Text("Grant New Access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        TextField(
            value = granteeUserId,
            onValueChange = { granteeUserId = it },
            label = { Text("Grantee User ID") },
            placeholder = { Text("Enter user ID to grant access") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSubmitting
        )

        TextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Reason (Optional)") },
            placeholder = { Text("e.g., Spouse access for bill payments") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSubmitting
        )

        TextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (Optional)") },
            placeholder = { Text("Additional notes") },
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
                    val owner = ownerUserId
                    if (owner != null && granteeUserId.isNotBlank()) {
                        onCreateGrant(RepoCreateFinancialAccessGrantRequest(
                            ownerUserId = owner,
                            granteeUserId = granteeUserId,
                            reason = if (reason.isNotBlank()) reason else null,
                            notes = if (notes.isNotBlank()) notes else null
                        ))
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isSubmitting && granteeUserId.isNotBlank() && ownerUserId != null
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text("Grant Access")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Existing Access Grants",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (grants.isEmpty()) {
            NnInlineEmptyCard(message = "No access grants")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grants) { grant ->
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
                                Text("Grant to: ${grant.granteeUserId}", fontWeight = FontWeight.Bold)
                                grant.reason?.let { Text("Reason: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                grant.notes?.let { Text("Notes: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                            Text(
                                text = grant.status,
                                style = MaterialTheme.typography.labelMedium,
                                color = when (grant.status) {
                                    "ACTIVE" -> MaterialTheme.colorScheme.primary
                                    "REVOKED" -> MaterialTheme.colorScheme.error
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