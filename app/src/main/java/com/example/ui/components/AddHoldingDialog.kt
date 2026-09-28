package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetType

@Composable
fun AddHoldingDialog(
    onDismiss: () -> Unit,
    onConfirm: (symbol: String, name: String, shares: Double, price: Double, assetType: AssetType) -> Unit
) {
    var symbol by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var sharesText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AssetType.EQUITY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Portfolio Holding", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it.uppercase() },
                    label = { Text("Symbol (e.g. MSFT, ETH)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("holding_symbol_input")
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Asset Name (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sharesText,
                        onValueChange = { sharesText = it },
                        label = { Text("Shares/Units") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("holding_shares_input")
                    )

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Avg Price ($)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("holding_price_input")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssetType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val shares = sharesText.toDoubleOrNull() ?: 1.0
                    val price = priceText.toDoubleOrNull() ?: 100.0
                    if (symbol.isNotBlank()) {
                        onConfirm(symbol.trim(), name.trim(), shares, price, selectedType)
                    }
                },
                enabled = symbol.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_holding_button")
            ) {
                Text("Add Holding")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
