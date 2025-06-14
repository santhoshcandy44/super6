package com.lts360.compose.ui.news.qr.navigation.navhosts

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
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

    val backStacks = rememberNavBackStack(CreateQRCodeRoutes.Text)

    NavDisplay(
        backStack = backStacks,
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {

            entry<CreateQRCodeRoutes.Text> {
                TextCreateQRScreen{
                    backStacks.removeLastOrNull()
                }
            }
            entry<CreateQRCodeRoutes.Url> {
                UrlCreateQRScreen{
                    backStacks.removeLastOrNull()
                }
            }
            entry<CreateQRCodeRoutes.Wifi> {
                WifiCreateQRScreen{
                    backStacks.removeLastOrNull()
                }
            }
            entry<CreateQRCodeRoutes.ContactvCard> {
                ContactVcardCreateQRScreen{
                    backStacks.removeLastOrNull()
                }
            }
            entry<CreateQRCodeRoutes.Email> {
                EmailCreateQRScreen{
                    backStacks.removeLastOrNull()
                }
            }
            entry<CreateQRCodeRoutes.Event> {
                EventCreateQRScreen{
                    backStacks.removeLastOrNull()
                }
            }
            entry<CreateQRCodeRoutes.Location> {
                LocationCreateQRScreen{
                    backStacks.removeLastOrNull()
                }
            }
            entry<CreateQRCodeRoutes.Phone> {
                PhoneCreateQRScreen{
                    backStacks.removeLastOrNull()
                }
            }
            entry<CreateQRCodeRoutes.Message> {
                MessageCreateQRScreen{
                    backStacks.removeLastOrNull()
                }
            }
        }
    )

}