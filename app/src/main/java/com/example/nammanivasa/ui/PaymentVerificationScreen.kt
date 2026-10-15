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
import com.example.nammanivasa.data.model.PaymentItem
import com.example.nammanivasa.ui.components.NnInlineEmptyCard
import com.example.nammanivasa.ui.components.NnSubScreenHeader
import com.example.nammanivasa.ui.components.NnSubmitErrorCard

@Composable
fun PaymentVerificationScreen(
    modifier: Modifier = Modifier,
    payments: List<PaymentItem>,
    isSubmitting: Boolean,
    submitError: String?,
    onVerify: (String, String?) -> Unit,
    onReject: (String, String?) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NnSubScreenHeader(
            title = "Payment Verification",
            subtitle = "Review and verify pending payments",
            onClose = onCancel
        )

        submitError?.let { error ->
            NnSubmitErrorCard(message = error)
        }

        val pendingPayments = payments.filter { it.status == "PENDING" }

        if (pendingPayments.isEmpty()) {
            NnInlineEmptyCard(message = "No pending payments to verify")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingPayments) { payment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Payment: ${payment.id}", fontWeight = FontWeight.Bold)
                                    Text("Bill: ${payment.billId ?: "N/A"}")
                                    Text("Payer: ${payment.payerUserId}")
                                    Text("Amount: ${payment.currency} ${payment.amount}")
                                }
                                Text(
                                    text = payment.status,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onVerify(payment.id, "Verified via app") },
                                    enabled = !isSubmitting
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                                        Spacer(modifier = Modifier.size(8.dp))
                                    }
                                    Text("Verify")
                                }
                                Button(
                                    onClick = { onReject(payment.id, "Rejected via app") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    enabled = !isSubmitting
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
}