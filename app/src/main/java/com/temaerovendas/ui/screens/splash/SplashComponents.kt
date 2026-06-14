package com.temaerovendas.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.temaerovendas.R
import com.temaerovendas.ui.theme.SilverAccent

@Composable
fun TEMLogoPlaceholder() {
    Image(
        painter = painterResource(id = R.drawable.logo_tem),
        contentDescription = "TEM Aerovendas",
        modifier = Modifier
            .width(200.dp)
            .wrapContentHeight()
    )
}

@Composable
fun SplashTagline() {
    Text(
        text = "Plataforma Premium de Compra e Venda de Aeronaves",
        style = MaterialTheme.typography.bodyMedium,
        color = SilverAccent,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 32.dp)
    )
}
