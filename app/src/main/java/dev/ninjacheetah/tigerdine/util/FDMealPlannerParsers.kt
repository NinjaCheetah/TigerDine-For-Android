package dev.ninjacheetah.tigerdine.util

import dev.ninjacheetah.tigerdine.data.types.FDMealsParser
import dev.ninjacheetah.tigerdine.data.types.FDMenuItem
import dev.ninjacheetah.tigerdine.data.types.FDNutritionalEntry

fun parseFDMealPlannerMenu(menu: FDMealsParser): Pair<List<FDMenuItem>, Map<String, List<Int>>> {
    var menuItems: List<FDMenuItem> = emptyList()
    if (menu.result.isEmpty()) {
        return Pair(menuItems, emptyMap())
    }

    val firstResult = menu.result.first()
    val conceptToItemsMap: MutableMap<String, List<Int>> = mutableMapOf()

    // I hate the FD MealPlanner API so much. Why is this entirely based on a grid system?? I feel
    // like there is no logical reason to have your menu items sorted into boxes based on where
    // they appear in a grid and not just through like ID matching or something. Holy shit.
    // And then also even if we're going to do category association by row, why does an API
    // request for one day's menu provide me with 26 weeks worth of concept data that I have to
    // match up with the main menu data?? Just show me the data for the current week! You know what
    // it is already because I had to provide it in my API request. Gah.
    val rowToConceptMap: MutableMap<Int, String> = mutableMapOf()
    if (firstResult.conceptData != null) {
        for (concept in firstResult.conceptData.filter { it.strWeekStartDate == firstResult.strWeekStartDate }) {
            val cleanConceptName = concept.conceptName.decodingHTMLEntities()
            rowToConceptMap[concept.rowId] = cleanConceptName

            if (conceptToItemsMap[cleanConceptName] == null) {
                conceptToItemsMap[cleanConceptName] = listOf()
            }
        }
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
                recipe.englishAlternateName.trim()
            } else {
                recipe.componentName.trim()
            }
            val allergens: List<String> = if (recipe.allergenName != "") recipe.allergenName.split(",") else emptyList()

            // Get the list of dietary markers (Vegan, Vegetarian, Pork, Beef), and drop
            // "Vegetarian" if "Vegan" is also included since that's kinda redundant.
            var dietaryMarkers = if (recipe.recipeProductDietaryName != "") {
                recipe.recipeProductDietaryName
                    .split(",")
                    .map { marker ->
                        marker.trim()
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

            val conceptName = rowToConceptMap.getValue(recipe.rowId)
            var target = conceptToItemsMap.getValue(conceptName)
            target = target.plus(recipe.componentId)
            conceptToItemsMap[conceptName] = target
        }
    }

    return Pair(menuItems, conceptToItemsMap)
}
