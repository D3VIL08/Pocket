package dev.pocket.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector
import dev.pocket.core.model.CategoryIcon

/**
 * Maps the stored [CategoryIcon] enum onto a drawable. The mapping lives in the UI layer so the
 * database never stores a resource id that a later Compose version could invalidate.
 */
fun CategoryIcon.asImageVector(): ImageVector = when (this) {
    CategoryIcon.FOOD -> Icons.Filled.Restaurant
    CategoryIcon.TRANSPORT -> Icons.Filled.DirectionsBus
    CategoryIcon.BILLS -> Icons.Filled.Receipt
    CategoryIcon.SHOPPING -> Icons.Filled.ShoppingBag
    CategoryIcon.ENTERTAINMENT -> Icons.Filled.Movie
    CategoryIcon.HEALTH -> Icons.Filled.LocalHospital
    CategoryIcon.HOME -> Icons.Filled.Home
    CategoryIcon.EDUCATION -> Icons.Filled.School
    CategoryIcon.TRAVEL -> Icons.Filled.Flight
    CategoryIcon.SAVINGS -> Icons.Filled.Savings
    CategoryIcon.OTHER -> Icons.Filled.MoreHoriz
}
