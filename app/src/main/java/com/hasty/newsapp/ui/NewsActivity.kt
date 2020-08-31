package com.hasty.newsapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.hasty.newsapp.R
import com.hasty.newsapp.repositories.NewsRepository
import com.hasty.newsapp.ui.viewmodels.NewsViewModel
import com.hasty.newsapp.ui.viewmodels.NewsViewModelProviderFactory
import kotlinx.android.synthetic.main.activity_news.*

class NewsActivity : AppCompatActivity() {

    lateinit var viewModel : NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        bottomNavView.setupWithNavController(newsNavHostFragment.findNavController())
        val repository = NewsRepository()
        val viewModelProviderFactory = NewsViewModelProviderFactory(application,repository)
        viewModel = ViewModelProvider(this,viewModelProviderFactory).get(NewsViewModel::class.java)
    }
}