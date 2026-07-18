package com.example.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The backend stores category icons as Lucide icon names (matching the web frontend, which uses
 * lucide-react directly). There is no Lucide icon set for Compose, so this maps each allowed name
 * to the closest Material icon for display — the string sent/received over the API is untouched.
 */
fun iconForCategory(name: String): ImageVector = when (name) {
    "TrendingUp" -> Icons.Default.TrendingUp
    "TrendingDown" -> Icons.Default.TrendingDown
    "Coins" -> Icons.Default.MonetizationOn
    "Wallet" -> Icons.Default.AccountBalanceWallet
    "Utensils" -> Icons.Default.Restaurant
    "Car" -> Icons.Default.DirectionsCar
    "Home" -> Icons.Default.Home
    "Clapperboard" -> Icons.Default.Movie
    "Zap" -> Icons.Default.Bolt
    "HeartPulse" -> Icons.Default.Favorite
    "ShoppingBag" -> Icons.Default.ShoppingBag
    "Gift" -> Icons.Default.CardGiftcard
    "Plane" -> Icons.Default.Flight
    "GraduationCap" -> Icons.Default.School
    "Briefcase" -> Icons.Default.Work
    "Smartphone" -> Icons.Default.Smartphone
    "Dumbbell" -> Icons.Default.FitnessCenter
    "PawPrint" -> Icons.Default.Pets
    "CreditCard" -> Icons.Default.CreditCard
    "Tag" -> Icons.Default.LocalOffer
    "Music" -> Icons.Default.MusicNote
    "Shirt" -> Icons.Default.Style
    "ArrowLeftRight" -> Icons.Default.SwapHoriz
    "BookOpen" -> Icons.Default.Book
    "HandCoins" -> Icons.Default.MonetizationOn
    "CheckCircle" -> Icons.Default.CheckCircle
    else -> Icons.Default.Circle
}
