package com.bestway.technologies.todolist.ui.lists

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.BaseAdapter
import android.widget.HeaderViewListAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bestway.technologies.todolist.R
import com.bestway.technologies.todolist.databinding.ActivityListsDrawerBinding
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ListsDrawerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityListsDrawerBinding
    private lateinit var viewModel: ListViewModel
    private lateinit var navController: NavController
    private lateinit var menu: Menu
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListsDrawerBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_TodoList)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(ListViewModel::class.java)

        setSupportActionBar(binding.appBarListsDrawer.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        navView = binding.navView

        menu = navView.menu
        val submenu = menu.addSubMenu("Your Lists")
        lifecycleScope.launch {
            viewModel.list.collect { listItem ->
                submenu.clear()
                for (item in listItem) {
                    submenu.add(item.name)
                }
            }
        }

        var i = 0
        val count: Int = navView.childCount
        while (i < count) {
            val child: View = navView.getChildAt(i)
            if (child is ListView) {
                val menuView: ListView = child
                val adapter: HeaderViewListAdapter? =
                    menuView.adapter as HeaderViewListAdapter?
                (adapter?.wrappedAdapter as BaseAdapter?)?.notifyDataSetChanged()
            }
            i++
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_lists_drawer) as NavHostFragment
        navController = navHostFragment.findNavController()
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.addListItemDialogFragment), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.lists_drawer, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_lists_drawer)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDrawer()
        } else {
            super.onBackPressed()
        }
    }

    private fun isNavDrawerOpen(): Boolean {
        return binding.drawerLayout.isDrawerOpen(GravityCompat.START)
    }

    private fun closeNavDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }
}