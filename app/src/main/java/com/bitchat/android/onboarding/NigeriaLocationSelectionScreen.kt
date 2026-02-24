package com.bitchat.android.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.bitchat.android.location.NigeriaLocation
import com.bitchat.android.R

@Composable
fun NigeriaLocationSelectionScreen(
    modifier: Modifier = Modifier,
    onLocationSelected: (NigeriaLocation) -> Unit
) {
    val context = LocalContext.current
    val nigeriaData = remember { loadNigeriaData(context) }

    var selectedState by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("") }
    var selectedLga by remember { mutableStateOf("") }
    var selectedWard by remember { mutableStateOf("") }
    var selectedConstituency by remember { mutableStateOf("") }

    val states = nigeriaData.states.map { it.name }
    val regions = nigeriaData.states.find { it.name == selectedState }?.regions?.map { it.name } ?: emptyList()
    val lgas = nigeriaData.states.find { it.name == selectedState }?.regions?.find { it.name == selectedRegion }?.lgas?.map { it.name } ?: emptyList()
    val wards = nigeriaData.states.find { it.name == selectedState }?.regions?.find { it.name == selectedRegion }?.lgas?.find { it.name == selectedLga }?.wards?.map { it.name } ?: emptyList()
    val constituencies = nigeriaData.states.find { it.name == selectedState }?.regions?.find { it.name == selectedRegion }?.lgas?.find { it.name == selectedLga }?.wards?.find { it.name == selectedWard }?.constituencies ?: emptyList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "LOCATION SELECTION",
            fontSize = 24.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Select your administrative area in Nigeria to continue.",
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        DropdownSelection("State", selectedState, states) { selectedState = it; selectedRegion = ""; selectedLga = ""; selectedWard = ""; selectedConstituency = "" }
        DropdownSelection("Region", selectedRegion, regions) { selectedRegion = it; selectedLga = ""; selectedWard = ""; selectedConstituency = "" }
        DropdownSelection("LGA / District", selectedLga, lgas) { selectedLga = it; selectedWard = ""; selectedConstituency = "" }
        DropdownSelection("Ward", selectedWard, wards) { selectedWard = it; selectedConstituency = "" }
        DropdownSelection("Constituency", selectedConstituency, constituencies) { selectedConstituency = it }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                onLocationSelected(NigeriaLocation(selectedState, selectedRegion, selectedLga, selectedWard, selectedConstituency))
            },
            enabled = selectedState.isNotEmpty() && selectedRegion.isNotEmpty() && selectedLga.isNotEmpty() && selectedWard.isNotEmpty() && selectedConstituency.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("CONTINUE", fontFamily = FontFamily.Monospace)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelection(label: String, selectedValue: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontFamily = FontFamily.Monospace) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontFamily = FontFamily.Monospace) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun loadNigeriaData(context: android.content.Context): NigeriaData {
    val json = context.assets.open("ng.json").bufferedReader().use { it.readText() }
    return Gson().fromJson(json, NigeriaData::class.java)
}

data class NigeriaData(val states: List<StateData>)
data class StateData(val name: String, val regions: List<RegionData>)
data class RegionData(val name: String, val lgas: List<LgaData>)
data class LgaData(val name: String, val wards: List<WardData>)
data class WardData(val name: String, val constituencies: List<String>)
