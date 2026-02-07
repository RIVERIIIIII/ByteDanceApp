package com.example.firsttry.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.firsttry.model.City
import kotlinx.coroutines.launch

@Composable
fun CityPickerDialog(
    cities: Map<Char, List<City>>,
    onDismiss: () -> Unit,
    onCitySelected: (City) -> Unit,
    onLocateClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                    Text("选择城市", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onLocateClick) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("定位")
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    val listState = rememberLazyListState()
                    val scope = rememberCoroutineScope()
                    val sortedKeys = cities.keys.toList()

                    val indexMap = remember(cities) {
                        val map = mutableMapOf<Char, Int>()
                        var currentIndex = 0
                        sortedKeys.forEach { key ->
                            map[key] = currentIndex
                            currentIndex += (cities[key]?.size ?: 0) + 1 
                        }
                        map
                    }

                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        sortedKeys.forEach { key ->
                            item {
                                Text(
                                    text = key.toString(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.LightGray.copy(alpha = 0.3f))
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(cities[key] ?: emptyList()) { city ->
                                Text(
                                    text = city.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onCitySelected(city) }
                                        .padding(16.dp)
                                )
                                HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        sortedKeys.forEach { key ->
                            Text(
                                text = key.toString(),
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .clickable {
                                        scope.launch {
                                            indexMap[key]?.let { listState.scrollToItem(it) }
                                        }
                                    },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
