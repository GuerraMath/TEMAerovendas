// Caminho: app/src/main/java/com/temaerovendas/ui/screens/disclaimer/DisclaimerDialog.kt
package com.temaerovendas.ui.screens.disclaimer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.temaerovendas.domain.model.DisclaimerContent
import com.temaerovendas.ui.theme.*

/**
 * Dialog modal de Termo de Responsabilidade — Inspeção Pré-Compra.
 *
 * Exibido obrigatoriamente sempre que o usuário toca em
 * "Solicitar Proposta / Mais Informações". O botão de confirmação
 * permanece desabilitado até que o checkbox de aceite seja marcado.
 *
 * @param onAccept chamado quando o usuário confirma o aceite — deve
 *                  disparar a ação original (envio da solicitação de proposta).
 * @param onDismiss chamado ao cancelar/fechar sem aceitar.
 */
@Composable
fun DisclaimerDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    var accepted by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NavyMid,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Text(
                    text = DisclaimerContent.TITLE,
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldLight,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Corpo do termo — rolável
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 320.dp)
                ) {
                    Text(
                        text = DisclaimerContent.BODY,
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverAccent,
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .padding(end = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Checkbox de aceite obrigatório
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = accepted,
                        onCheckedChange = { accepted = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = GoldLight,
                            uncheckedColor = SilverAccent,
                            checkmarkColor = NavyDeep
                        )
                    )
                    Text(
                        text = DisclaimerContent.CHECKBOX_LABEL,
                        style = MaterialTheme.typography.bodySmall,
                        color = WhitePrimary,
                        modifier = Modifier
                            .padding(start = 4.dp, top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ações
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onAccept,
                        enabled = accepted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldLight,
                            contentColor = NavyDeep,
                            disabledContainerColor = NavyLight,
                            disabledContentColor = SilverAccent
                        )
                    ) {
                        Text(
                            text = DisclaimerContent.CONFIRM_BUTTON,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(DisclaimerContent.CANCEL_BUTTON, color = SilverAccent)
                    }
                }
            }
        }
    }
}
