package dev.ninjacheetah.tigerdine.ui.navigation

object Routes {
    const val HOME = "home"

    const val VISITING_CHEFS = "visitingChefs"

    const val DETAIL = "detail/{locationId}"
    fun detail(locationId: Int) = "detail/$locationId"

    const val MENU = "menu/{locationId}"
    fun menu(locationId: Int) = "menu/$locationId"

    const val MENU_ITEM = "menuItem/{locationId}/{itemId}"
    fun menuItem(locationId: Int, itemId: Int) = "menuItem/$locationId/$itemId"
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
