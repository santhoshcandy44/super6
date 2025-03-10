package com.lts360.compose.ui.news.qr.navigation.navhosts

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lts360.compose.ui.news.qr.navigation.routes.CreateQRCodeRoutes
import com.lts360.compose.ui.news.qr.screens.ContactVcardCreateQRScreen
import com.lts360.compose.ui.news.qr.screens.EmailCreateQRScreen
import com.lts360.compose.ui.news.qr.screens.EventCreateQRScreen
import com.lts360.compose.ui.news.qr.screens.LocationCreateQRScreen
import com.lts360.compose.ui.news.qr.screens.MessageCreateQRScreen
import com.lts360.compose.ui.news.qr.screens.PhoneCreateQRScreen
import com.lts360.compose.ui.news.qr.screens.TextCreateQRScreen
import com.lts360.compose.ui.news.qr.screens.UrlCreateQRScreen
import com.lts360.compose.ui.news.qr.screens.WifiCreateQRScreen

@Composable
fun QRCodeCreateNavHost() {
    // Set up navigation
    val navController = rememberNavController()

    // Navigation Host
    NavHost(navController = navController, startDestination = CreateQRCodeRoutes.Text) {
        composable<CreateQRCodeRoutes.Text>{
            TextCreateQRScreen(navController)
        }
        composable<CreateQRCodeRoutes.Url> {
            UrlCreateQRScreen(navController)
        }
        composable<CreateQRCodeRoutes.Wifi> {
            WifiCreateQRScreen(navController)
        }
        composable<CreateQRCodeRoutes.ContactvCard> {
            ContactVcardCreateQRScreen(navController)
        }
        composable<CreateQRCodeRoutes.Email> {
            EmailCreateQRScreen(navController)
        }
        composable<CreateQRCodeRoutes.Event> {
            EventCreateQRScreen(navController)
        }
        composable<CreateQRCodeRoutes.Location> {
            LocationCreateQRScreen(navController)
        }
        composable<CreateQRCodeRoutes.Phone> {
            PhoneCreateQRScreen(navController)
        }
        composable<CreateQRCodeRoutes.Message> {
            MessageCreateQRScreen(navController)
        }
    }
}
