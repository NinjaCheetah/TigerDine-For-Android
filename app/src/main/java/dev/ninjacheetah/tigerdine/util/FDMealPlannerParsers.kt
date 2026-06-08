package dev.ninjacheetah.tigerdine.util

import dev.ninjacheetah.tigerdine.data.types.FDMealsParser
import dev.ninjacheetah.tigerdine.data.types.FDMenuItem
import dev.ninjacheetah.tigerdine.data.types.FDNutritionalEntry

fun parseFDMealPlannerMenu(menu: FDMealsParser): List<FDMenuItem> {
    var menuItems: List<FDMenuItem> = emptyList()
    if (menu.result.isEmpty()) {
        return menuItems
    }

    // We only need to operate on the first index, because the request code is designed to only get
    // the menu for a single day, so there will only be a single index to operate on.
    val allMenuRecipes = menu.result.first().allMenuRecipes
    if (allMenuRecipes != null) {
        for (recipe in allMenuRecipes) {
            // Prevent duplicate items from being added, because for some reason the exact same
            // item with the exact same information might be included in FD MealPlanner more than
            // once.
            if (menuItems.find { it.id == recipe.componentId } != null) {
                continue
            }

            // englishAlternateName holds the proper name of the item, but it's blank for some
            // items for some reason. If that's the case, then we should fall back on componentName,
            // which is less user-friendly but works as a backup.
            val realName = if (recipe.englishAlternateName != "") {
                recipe.englishAlternateName.filter { !it.isWhitespace() }
            } else {
                recipe.componentName.filter { !it.isWhitespace() }
            }
            val allergens: List<String> = if (recipe.allergenName != "") recipe.allergenName.split(",") else emptyList()

            // Get the list of dietary markers (Vegan, Vegetarian, Pork, Beef), and drop
            // "Vegetarian" if "Vegan" is also included since that's kinda redundant.
            var dietaryMarkers = if (recipe.recipeProductDietaryName != "") {
                recipe.recipeProductDietaryName
                    .split(",")
                    .map { marker ->
                        marker.filter { !it.isWhitespace() }
                    }
            } else {
                emptyList()
            }
            if (dietaryMarkers.contains("Vegan")) {
                dietaryMarkers = dietaryMarkers.minus("Vegetarian")
            }
            val calories = recipe.calories.toDouble().toInt()

            // Collect and organize all the nutritional entries. I ordered them based off how they
            // were ordered in the nutritional facts panel on the side of the bag of goldfish that
            // lives on my desk, so presumably they're ordered correctly.
            val nutritionalEntries = listOf(
                FDNutritionalEntry(
                    type = "Total Fat",
                    amount = recipe.fat.toDouble(),
                    unit = recipe.fatUOM
                ),
                FDNutritionalEntry(
                    type = "Saturated Fat",
                    amount = recipe.saturatedFat.toDouble(),
                    unit = recipe.saturatedFatUOM
                ),
                FDNutritionalEntry(
                    type = "Trans Fat",
                    amount = recipe.transFattyAcid.toDouble(),
                    unit = recipe.transFattyAcidUOM
                ),
                FDNutritionalEntry(
                    type = "Cholesterol",
                    amount = recipe.cholesterol.toDouble(),
                    unit = recipe.cholesterolUOM
                ),
                FDNutritionalEntry(
                    type = "Sodium",
                    amount = recipe.sodium.toDouble(),
                    unit = recipe.sodiumUOM
                ),
                FDNutritionalEntry(
                    type = "Total Carbohydrates",
                    amount = recipe.carbohydrates.toDouble(),
                    unit = recipe.carbohydratesUOM
                ),
                FDNutritionalEntry(
                    type = "Dietary Fiber",
                    amount = recipe.dietaryFiber.toDouble(),
                    unit = recipe.dietaryFiberUOM
                ),
                FDNutritionalEntry(
                    type = "Total Sugars",
                    amount = recipe.totalSugars.toDouble(),
                    unit = recipe.totalSugarsUOM
                ),
                FDNutritionalEntry(
                    type = "Protein",
                    amount = recipe.protein.toDouble(),
                    unit = recipe.proteinUOM
                ),
                FDNutritionalEntry(
                    type = "Calcium",
                    amount = recipe.calcium.toDouble(),
                    unit = recipe.calciumUOM
                ),
                FDNutritionalEntry(
                    type = "Iron",
                    amount = recipe.iron.toDouble(),
                    unit = recipe.ironUOM
                ),
                FDNutritionalEntry(
                    type = "Vitamin A",
                    amount = recipe.vitaminA.toDouble(),
                    unit = recipe.vitaminAUOM
                ),
                FDNutritionalEntry(
                    type = "Vitamin C",
                    amount = recipe.vitaminC.toDouble(),
                    unit = recipe.vitaminCUOM
                )
            )

            val newItem = FDMenuItem(
                id = recipe.componentId,
                name = realName,
                exactName = recipe.componentName,
                category = recipe.category,
                allergens = allergens,
                calories = calories,
                nutritionalEntries = nutritionalEntries,
                dietaryMarkers = dietaryMarkers,
                ingredients = recipe.ingredientStatement,
                price = recipe.sellingPrice,
                servingSize = recipe.productMeasuringSize,
                servingSizeUnit = recipe.productMeasuringSizeUnit
            )
            menuItems = menuItems.plus(newItem)
        }
    }
    return menuItems
}
