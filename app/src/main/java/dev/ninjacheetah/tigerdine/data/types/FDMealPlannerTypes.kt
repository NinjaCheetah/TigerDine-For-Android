package dev.ninjacheetah.tigerdine.data.types

import kotlinx.serialization.Serializable

// Struct to parse the response from the FDMP search API. This API returns all the dining
// locations that are have menus available and the required IDs needed to get those menus.
@Serializable
data class FDSearchResponseParser(
    val success: Boolean,
    val errorMessage: String?,
    val data: Data
) {
    @Serializable
    data class Data(
        val result : List<Result>,
        val totalCount: Int
    ) {
        @Serializable
        data class Result(
            val locationId: Int,
            val accountId: Int,
            val tenantId: Int,
            val locationName: String,
            val locationCode: String,
            val locationDisplayName: String,
            val accountName: String
        )
    }
}

// Struct to parse the response from the FDMP meal periods API. This API returns all potential
// meal periods for a location based on its ID. This meal period ID is required to get the menu for
// that meal period from the meals API.
@Serializable
data class FDMealPeriodsParser(
    val success: Boolean,
    val errorMessage: String?,
    val data: Data
) {
    @Serializable
    data class Data(
        val id: Int,
        val mealPeriodName: String
    )
}

// Struct to parse the response from the FDMP meals API. This API contains the actual menu
// information for the specified location during the specified meal period. It doesn't contain
// every menu item, but it's the best source of menu information that I can access.
@Serializable
data class FDMealsParser(
    val responseStatus: String?,
    val result: List<Result>
) {
    @Serializable
    data class Result(
        val menuId: Int,
        val menuForDate: String,
        val menuToDate: String,
        val accountId: Int,
        val accountName: String,
        val menuTypeName: String,
        val mealPeriodId: Int,
        val allMenuRecipes: List<MenuRecipe>?
    ) {
        @Serializable
        data class MenuRecipe(
            val componentName: String,
            val componentId: Int,
            val componentTypeId: Int,
            val englishAlternateName: String,
            val category: String,
            val allergenName: String,
            val calories: String,
            val carbohydrates: String,
            val carbohydratesUOM: String,
            val dietaryFiber: String,
            val dietaryFiberUOM: String,
            val fat: String,
            val fatUOM: String,
            val protein: String,
            val proteinUOM: String,
            val saturatedFat: String,
            val saturatedFatUOM: String,
            val transFattyAcid: String,
            val transFattyAcidUOM: String,
            val calcium: String,
            val calciumUOM: String,
            val cholesterol: String,
            val cholesterolUOM: String,
            val iron: String,
            val ironUOM: String,
            val sodium: String,
            val sodiumUOM: String,
            val vitaminA: String,
            val vitaminAUOM: String,
            val vitaminC: String,
            val vitaminCUOM: String,
            val totalSugars: String,
            val totalSugarsUOM: String,
            val recipeProductDietaryName: String,
            val ingredientStatement: String,
            val sellingPrice: Double,
            val productMeasuringSize: Int,
            val productMeasuringSizeUnit: String,
            val itemsToOrder: Int
        )
    }
}

// A single nutritional entry, including the amount and the unit.
data class FDNutritionalEntry(
    val type: String,
    val amount: Double,
    val unit: String
)

// A single menu item, stripped down and reorganized to a format that actually makes sense for me
// to use in the rest of the app.
data class FDMenuItem(
    val id: Int,
    val name: String,
    val exactName: String,
    val category: String,
    val allergens: List<String>,
    val calories: Int,
    val nutritionalEntries: List<FDNutritionalEntry>,
    val dietaryMarkers: List<String>,
    val ingredients: String,
    val price: Double,
    val servingSize: Int,
    val servingSizeUnit: String
)
