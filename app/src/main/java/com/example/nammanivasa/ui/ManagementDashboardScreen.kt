package com.example.nammanivasa.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammanivasa.data.model.MaintenanceRequestItem
import com.example.nammanivasa.data.model.NoticeItem
import com.example.nammanivasa.data.model.PaymentItem
import com.example.nammanivasa.data.model.VisitorRequest
import com.example.nammanivasa.ui.components.NnInlineEmptyCard
import com.example.nammanivasa.ui.components.NnSubScreenHeader

@Composable
fun ManagementDashboardScreen(
    modifier: Modifier = Modifier,
    visitors: List<VisitorRequest>,
    payments: List<PaymentItem>,
    notices: List<NoticeItem>,
    maintenanceRequests: List<MaintenanceRequestItem>,
    isLoading: Boolean,
    onApproveVisitor: (String) -> Unit,
    onRejectVisitor: (String) -> Unit,
    onVerifyPayment: (String, String?) -> Unit,
    onRejectPayment: (String, String?) -> Unit,
    onCancel: () -> Unit
) {
    val pendingVisitors = visitors.filter { it.status == "Pending" }
    val pendingPayments = payments.filter { it.status == "PENDING" }
    val draftNotices = notices.filter { it.status == "DRAFT" }
    val openMaintenance = maintenanceRequests.filter { it.status == "OPEN" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NnSubScreenHeader(
            title = "Management Dashboard",
            subtitle = "Overview of pending actions requiring attention",
            onClose = onCancel
        )

        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading...", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Visitor Approvals Section
                item {
                    Text(
                        text = "Visitor Approvals (${pendingVisitors.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    if (pendingVisitors.isEmpty()) {
                        NnInlineEmptyCard(message = "No pending visitor approvals")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pendingVisitors) { visitor ->
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
                                            Text(visitor.name, fontWeight = FontWeight.Bold)
                                            Text("Purpose: ${visitor.purpose} | Apt: ${visitor.apartment}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { onApproveVisitor(visitor.passCode) },
                                                enabled = !isLoading
                                            ) {
                                                Text("Approve")
                                            }
                                            Button(
                                                onClick = { onRejectVisitor(visitor.passCode) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                                ),
                                                enabled = !isLoading
                                            ) {
                                                Text("Reject")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Payment Verification Section
                item {
                    Text(
                        text = "Payment Verification (${pendingPayments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    if (pendingPayments.isEmpty()) {
                        NnInlineEmptyCard(message = "No pending payments")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pendingPayments) { payment ->
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
                                            Text("Payment: ${payment.id}", fontWeight = FontWeight.Bold)
                                            Text("Amount: ${payment.currency} ${payment.amount} | Payer: ${payment.payerUserId}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { onVerifyPayment(payment.id, "Verified via dashboard") },
                                                enabled = !isLoading
                                            ) {
                                                Text("Verify")
                                            }
                                            Button(
                                                onClick = { onRejectPayment(payment.id, "Rejected via dashboard") },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                                ),
                                                enabled = !isLoading
                                            ) {
                                                Text("Reject")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Notice Publishing Section
                item {
                    Text(
                        text = "Draft Notices (${draftNotices.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    if (draftNotices.isEmpty()) {
                        NnInlineEmptyCard(message = "No draft notices")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(draftNotices) { notice ->
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
                                            Text(notice.message, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                        }
                                        Button(
                                            onClick = { /* TODO: navigate to notice management */ },
                                            enabled = !isLoading
                                        ) {
                                            Text("Publish")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Maintenance Requests Section
                item {
                    Text(
                        text = "Open Maintenance (${openMaintenance.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    if (openMaintenance.isEmpty()) {
                        NnInlineEmptyCard(message = "No open maintenance requests")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(openMaintenance) { request ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(request.title, fontWeight = FontWeight.Bold)
                                        Text(request.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Category: ${request.category} | Priority: ${request.priority}", style = MaterialTheme.typography.bodySmall)
                                            Text(request.status, style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}