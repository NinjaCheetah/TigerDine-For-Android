package dev.ninjacheetah.tigerdine.ui.menus

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.ninjacheetah.tigerdine.data.constant.Allergens
import dev.ninjacheetah.tigerdine.data.state.DiningModel

@ExperimentalMaterial3Api
@ExperimentalMaterial3ExpressiveApi
@Composable
fun MenuDietaryRestrictionsFilterSheet(
    viewModel: DiningModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
    ) {
        Column {
            Text(
                "Diet",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )

            val noBeef by viewModel.noBeef.collectAsState()
            val noBeefInteractionSource = remember { MutableInteractionSource() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .toggleable(
                    value = noBeef,
                    onValueChange = viewModel::setNoBeef,
                    role = Role.Checkbox,
                    indication = null,
                    interactionSource = noBeefInteractionSource,
                )
            ) {
                Checkbox(
                    checked = noBeef,
                    interactionSource = noBeefInteractionSource,
                    onCheckedChange = viewModel::setNoBeef,
                )
                Text("No Beef")
            }

            val noPork by viewModel.noPork.collectAsState()
            val noPorkInteractionSource = remember { MutableInteractionSource() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .toggleable(
                        value = noPork,
                        onValueChange = viewModel::setNoPork,
                        role = Role.Checkbox,
                        indication = null,
                        interactionSource = noPorkInteractionSource,
                    )
            ) {
                Checkbox(
                    checked = noPork,
                    interactionSource = noPorkInteractionSource,
                    onCheckedChange = viewModel::setNoPork,
                )
                Text("No Pork")
            }

            val vegetarian by viewModel.vegetarian.collectAsState()
            val vegetarianInteractionSource = remember { MutableInteractionSource() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .toggleable(
                        value = vegetarian,
                        onValueChange = viewModel::setVegetarian,
                        role = Role.Checkbox,
                        indication = null,
                        interactionSource = vegetarianInteractionSource,
                    )
            ) {
                Checkbox(
                    checked = vegetarian,
                    interactionSource = vegetarianInteractionSource,
                    onCheckedChange = viewModel::setVegetarian,
                )
                Text("Vegetarian")
            }

            val vegan by viewModel.vegan.collectAsState()
            val veganInteractionSource = remember { MutableInteractionSource() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .toggleable(
                        value = vegan,
                        onValueChange = viewModel::setVegan,
                        role = Role.Checkbox,
                        indication = null,
                        interactionSource = veganInteractionSource,
                    )
            ) {
                Checkbox(
                    checked = vegan,
                    interactionSource = veganInteractionSource,
                    onCheckedChange = viewModel::setVegan,
                )
                Text("Vegan")
            }
        }

        Column {
            Text(
                "Allergens",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )

            val activeAllergens by viewModel.activeAllergens.collectAsState()
            for (allergen in Allergens.entries) {
                val allergenEnabled = activeAllergens.contains(allergen.toString())
                val allergenInteractionSource = remember { MutableInteractionSource() }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .toggleable(
                            value = allergenEnabled,
                            onValueChange = { viewModel.toggleActiveAllergen(allergen.toString()) },
                            role = Role.Checkbox,
                            indication = null,
                            interactionSource = allergenInteractionSource,
                        )
                ) {
                    Checkbox(
                        checked = allergenEnabled,
                        interactionSource = allergenInteractionSource,
                        onCheckedChange = { viewModel.toggleActiveAllergen(allergen.toString()) },
                    )
                    Text(allergen.toString())
                }
            }
        }
    }
}
