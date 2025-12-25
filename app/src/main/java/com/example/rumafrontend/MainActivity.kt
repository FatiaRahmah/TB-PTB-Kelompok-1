package com.example.rumafrontend

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rumafrontend.ui.theme.rumaFrontendTheme
import com.example.rumafrontend.ui.theme.screen.*
import com.example.rumafrontend.ui.theme.screen.Beranda.BerandaScreen
import com.example.rumafrontend.ui.theme.notification.NotifikasiHolder
import com.example.rumafrontend.utils.TokenManager
import com.example.rumafrontend.ViewModel.AuthViewModel
import com.example.rumafrontend.ViewModel.AuthViewModelFactory
import com.example.rumafrontend.ViewModel.AgendaViewModel.AgendaViewModel
import com.example.rumafrontend.ui.theme.screen.TagihanViewModel
import com.example.rumafrontend.ui.theme.screen.TagihanViewModelFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.rumafrontend.ui.theme.components.RumaBottomBar
import com.example.rumafrontend.ui.theme.screen.profile.EditProfilScreen
import com.example.rumafrontend.ui.theme.screen.profile.ProfileScreen
import com.example.rumafrontend.ui.theme.screen.daftarBelanja.ShoppingListScreen
import com.example.rumafrontend.ui.theme.screen.daftarBelanja.ShoppingDetailScreen
import com.example.rumafrontend.ui.theme.screen.daftarBelanja.MapScreen
import com.example.rumafrontend.ui.theme.screen.daftarBelanja.ShoppingListViewModel
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.AndroidEntryPoint

@HiltAndroidApp
class App : android.app.Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            
        } else {
            
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            enableEdgeToEdge()

            
            NotifikasiHolder.initialize(this)

            
            com.example.rumafrontend.network.ApiClient.init(this)

            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            setContent {
                rumaFrontendTheme {
                    var showSplash by remember { mutableStateOf(true) }

                    if (showSplash) {
                        SplashScreen(onNavigateToNext = { showSplash = false })
                    } else {
                        
                        val tagihanViewModel: TagihanViewModel = viewModel(
                            factory = TagihanViewModelFactory(this@MainActivity)
                        )
                        val agendaViewModel: AgendaViewModel = viewModel()

                        AppNavigation(
                            openNotifikasi = intent.getBooleanExtra("openNotifikasi", false),
                            tagihanId = intent.getIntExtra("tagihanId", -1),
                            tagihanViewModel = tagihanViewModel,
                            agendaViewModel = agendaViewModel
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            
            android.widget.Toast.makeText(this, "Fatal Startup Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        
        val tokenManager = TokenManager(this)
        tokenManager.clearToken()
    }
}

@Composable
fun AppNavigation(
    openNotifikasi: Boolean = false,
    tagihanId: Int = -1,
    tagihanViewModel: TagihanViewModel,
    agendaViewModel: AgendaViewModel
) {
    val navController = rememberNavController()

    
    LaunchedEffect(Unit) {
        tagihanViewModel.scheduleAllReminders()
    }

    
    LaunchedEffect(openNotifikasi) {
        if (openNotifikasi) {
            navController.navigate("notifikasi") {
                popUpTo("beranda") { inclusive = false }
            }
        }
    }

    
    LaunchedEffect(tagihanId) {
        if (tagihanId > 0) {
            navController.navigate("editTagihan/$tagihanId") {
                popUpTo("beranda") { inclusive = false }
            }
        }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val routesWithBottomBar = setOf(
        "beranda",
        "tagihan",
        "resep_favorit",
        "resep_keluarga",
        "profile"
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in routesWithBottomBar) {
                RumaBottomBar(navController = navController)
            }
        }
    )
    { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(paddingValues).consumeWindowInsets(paddingValues)
        ) {
            
            composable("splash") {
                SplashScreen(
                    onNavigateToNext = {
                        
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            
            composable("login") {
                val context = LocalContext.current
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(context)
                )
                val tagihanViewModel: TagihanViewModel = viewModel(
                    factory = TagihanViewModelFactory(context)
                )
                
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        
                        tagihanViewModel.syncAfterLogin()
                        navController.navigate("beranda") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate("register")
                    }
                )
            }

            composable("register") {
                val context = LocalContext.current
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(context)
                )
                
                RegistrasiScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                )
            }

            composable("beranda") {
                BerandaScreen(
                    onNavigateTo = { route ->
                        navController.navigate(route)
                    }
                )
            }

            composable("notifikasi") {
                NotifikasiScreen(navController = navController)
            }

            composable("tagihan") {
                TagihanScreen(
                    navController = navController,
                    viewModel = tagihanViewModel
                )
            }

            composable("addTagihan") {
                AddTagihanScreen(
                    navController = navController,
                    viewModel = tagihanViewModel
                )
            }

            composable(
                route = "editTagihan/{billId}",
                arguments = listOf(navArgument("billId") { type = NavType.IntType })
            ) { backStackEntry ->
                val billId = backStackEntry.arguments?.getInt("billId")
                AddTagihanScreen(
                    navController = navController,
                    viewModel = tagihanViewModel,
                    billId = billId
                )
            }

            composable("riwayat") {
                RiwayatScreen(
                    navController = navController,
                    viewModel = tagihanViewModel
                )
            }

            composable("calendar") {
                CalendarScreen(
                    navController = navController,
                    viewModel = tagihanViewModel
                )
            }

            composable("overdue") {
                OverdueScreen(
                    navController = navController,
                    viewModel = tagihanViewModel
                )
            }

            
            composable("agenda_list") {
                AgendaScreen(
                    navController = navController,
                    viewModel = agendaViewModel
                )
            }

            composable("add_agenda") {
                AddAgendaScreen(
                    navController = navController,
                    viewModel = agendaViewModel
                )
            }

            composable(
                route = "detail_agenda/{agendaId}",
                arguments = listOf(navArgument("agendaId") { type = NavType.LongType })
            ) { backStackEntry ->
                val agendaId = backStackEntry.arguments?.getLong("agendaId") ?: -1L
                DetailAgendaScreen(
                    navController = navController,
                    agendaId = agendaId,
                    viewModel = agendaViewModel
                )
            }

            composable("calendar_agenda") {
                CalendarAgendaScreen(
                    navController = navController,
                    viewModel = agendaViewModel
                )
            }

            
            composable("resep_keluarga") {
                com.example.rumafrontend.ui.theme.screen.resep.ResepScreen(
                    onAddResep = { navController.navigate("tambah_resep") },
                    onResepClick = { resepId -> navController.navigate("detail_resep/$resepId") }
                )
            }

            composable("tambah_resep") {
                com.example.rumafrontend.ui.theme.screen.resep.TambahResepScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = {
                        navController.navigate("resep_keluarga") {
                            popUpTo("tambah_resep") { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "detail_resep/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                com.example.rumafrontend.ui.theme.screen.resep.DetailResepScreen(
                    id = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { editId -> navController.navigate("edit_resep/$editId") },
                    onDeleted = { navController.popBackStack() }
                )
            }

            composable("resep_favorit") {
                com.example.rumafrontend.ui.theme.screen.resep.FavoritResepScreen(
                    onBack = { navController.popBackStack() },
                    onResepClick = { id -> navController.navigate("detail_resep/$id") }
                )
            }

            composable(
                route = "edit_resep/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                com.example.rumafrontend.ui.theme.screen.resep.TambahResepScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() },
                    resepId = id
                )
            }

            composable("profile") {
                ProfileScreen(
                    onEditClick = {
                        navController.navigate("editProfile")
                    },
                    onLogoutNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("beranda") { inclusive = true }
                        }
                    }
                )
            }

            composable("editProfile") {
                EditProfilScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            
            composable("shoppingList") {
                
                val shoppingViewModel: ShoppingListViewModel = viewModel()

                ShoppingListScreen(
                    viewModel = shoppingViewModel,
                    onNavigateToDetail = { id, title, date ->
                        
                        
                        val encodedTitle = java.net.URLEncoder.encode(
                            title,
                            java.nio.charset.StandardCharsets.UTF_8.toString()
                        )
                        val encodedDate = java.net.URLEncoder.encode(
                            date,
                            java.nio.charset.StandardCharsets.UTF_8.toString()
                        )

                        val route = "shoppingDetail/$id/$encodedTitle/$encodedDate"
                        
                        
                        navController.navigate(route)
                    },
                    onNavigateToMap = {
                        navController.navigate("map")
                    }
                )
            }

            
            composable(
                route = "shoppingDetail/{id}/{title}/{date}",
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType },
                    navArgument("title") { type = NavType.StringType },
                    navArgument("date") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("shoppingList")
                }

                val shoppingViewModel = viewModel<ShoppingListViewModel>(parentEntry)

                val shoppingItemId = backStackEntry.arguments?.getInt("id") ?: -1

                val title = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("title") ?: "",
                    java.nio.charset.StandardCharsets.UTF_8.toString()
                )

                val date = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("date") ?: "",
                    java.nio.charset.StandardCharsets.UTF_8.toString()
                )

                

                ShoppingDetailScreen(
                    shoppingItemId = shoppingItemId,
                    title = title,
                    date = date,
                    onBack = { navController.popBackStack() },
                    listViewModel = shoppingViewModel
                )
            }

            
            composable("map") {
                MapScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}