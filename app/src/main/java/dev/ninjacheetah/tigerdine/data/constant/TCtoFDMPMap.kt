package dev.ninjacheetah.tigerdine.data.constant

val tCtoFDMPMap: Map<Int, Pair<Int, Int>> = mapOf(
    // These are ordered based on the way that they're ordered in the FD MealPlanner
    // search API response.
    30 to (1 to 1),   // Artesano
    31 to (2 to 2),   // Beanz
    23 to (7 to 7),   // Crossroads
    25 to (8 to 8),   // Cantina
    34 to (6 to 6),   // Ctr-Alt-DELi
    21 to (10 to 10), // Gracie's
    22 to (4 to 4),   // Brick City Cafe
    441 to (11 to 11),// Loaded Latke
    38 to (12 to 12), // Midnight Oil
    26 to (14 to 4),  // RITZ
    35 to (18 to 17), // The College Grind
    24 to (15 to 14)  // The Commons
)
