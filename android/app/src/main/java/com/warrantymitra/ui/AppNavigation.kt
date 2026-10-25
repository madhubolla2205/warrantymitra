package com.warrantymitra.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.warrantymitra.Incoming

/** Holds text or a photo shared into the app until the add screen picks it up. */
object ShareInbox {
    var text: String? = null
    var imageUri: android.net.Uri? = null
}

@Composable
fun AppNavigation(incoming: Incoming?, onIncomingHandled: () -> Unit) {
    val nav = rememberNavController()
    val vm: ItemsViewModel = viewModel()

    LaunchedEffect(incoming) {
        when (incoming) {
            is Incoming.OpenItem -> nav.navigate("detail/${incoming.id}")
            is Incoming.SharedText -> { ShareInbox.text = incoming.text; nav.navigate("edit") }
            is Incoming.SharedImage -> { ShareInbox.imageUri = incoming.uri; nav.navigate("edit") }
            null -> return@LaunchedEffect
        }
        onIncomingHandled()
    }

    NavHost(nav, startDestination = "list") {
        composable("list") {
            ListScreen(vm, onOpen = { nav.navigate("detail/$it") }, onAdd = { nav.navigate("edit") })
        }
        composable("edit?id={id}", arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = 0L })) { entry ->
            val id = entry.arguments?.getLong("id")?.takeIf { it > 0 }
            EditScreen(vm, id, onDone = { savedId ->
                nav.popBackStack()
                if (id == null) nav.navigate("detail/$savedId")
            }, onCancel = { nav.popBackStack() })
        }
        composable("detail/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            DetailScreen(vm, entry.arguments!!.getLong("id"), onBack = { nav.popBackStack() }, onEdit = { nav.navigate("edit?id=$it") })
        }
    }
}
