package dev.ninjacheetah.tigerdine.ui.navigation

object Routes {
    const val HOME = "home"

    const val VISITING_CHEFS = "visitingChefs"

    const val ABOUT = "about"

    const val DONATE = "donate"

    const val DETAIL = "detail"

    const val MENU = "menu"

    const val MENU_ITEM = "menuItem/{itemId}"
    fun menuItem(itemId: Int) = "menuItem/$itemId"
}

//fun routeTitle(route: String?): String =
//    when {
//        route == Routes.HOME -> "TigerDine For Android Beta"
//        route == Routes.VISITING_CHEFS -> "Visiting Chefs"
//        route?.startsWith("detail/") == true -> "Details"
//        route?.startsWith("menu/") == true -> "Menu"
//        route?.startsWith("menuItem/") == true -> "Details"
//        else -> "TigerDine"
//    }
