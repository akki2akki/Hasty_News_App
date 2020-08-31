package com.hasty.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.AbsListView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hasty.newsapp.R
import com.hasty.newsapp.adapters.NewsAdapter
import com.hasty.newsapp.ui.NewsActivity
import com.hasty.newsapp.ui.viewmodels.NewsViewModel
import com.hasty.newsapp.utils.Constants
import com.hasty.newsapp.utils.Constants.SEARCH_NEWS_TIME_DELAY
import com.hasty.newsapp.utils.Resource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.category_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class HomeFragment : Fragment(R.layout.fragment_home) {

    private val TAG = "HomeFragment"
    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var newsCatt : String

    private lateinit var bottomSheetBehavior : BottomSheetBehavior<RelativeLayout>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as NewsActivity).viewModel
        setUpRecyclerView()

        bottomSheetBehavior = BottomSheetBehavior.from(categoryBottomSheet)

        viewModel.newsCat.observe(viewLifecycleOwner, {
            newsCatt = it
            filterIcon.text = it
            Log.d(TAG, "filterListWithCategory: ${newsCatt}")
        })

        filterIcon.setOnClickListener {
            showHideBottomSheet()
        }

        newsAdapter.setOnItemClickListener {
            val isArticleEmpty = it.url.isNullOrEmpty()
            val isValidUrl = URLUtil.isValidUrl(it.url)
            Log.d(TAG, "onClick : isUrlEmpty : $isArticleEmpty  & isValidUrl : $isValidUrl")
            if(isArticleEmpty || !isValidUrl) {
                Toast.makeText(requireContext(), "Data Not Available!", Toast.LENGTH_SHORT).show()
                return@setOnItemClickListener
            }
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_homeFragment_to_articleFragment, bundle)
        }

        viewModel.searchNews.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        if (newsResponse.articles.isEmpty()) {
                            Toast.makeText(requireContext(), "Response Empty", Toast.LENGTH_SHORT).show()
                        }
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        if (newsAdapter.differ.currentList.isEmpty()) {
                            showAnimation(true)
                        } else {
                            showAnimation(false)
                        }

                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        Log.d("TAG", "Total Pages: $totalPages")
                        isLastPage = viewModel.breakingNewsPage == totalPages
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        Toast.makeText(context, "Error : $it", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

        //Handling category here
        chipGroup?.apply {
            setOnCheckedChangeListener { group, checkedId ->
                val chip = group.findViewById<Chip>(checkedId)
                chip?.text?.let{
                    filterListWithCategory(it.toString())
                }
                showHideBottomSheet()
            }
        }
        closeBottomSheetIV?.apply {
            setOnClickListener {
                showHideBottomSheet()
            }
        }

        //Search Functionality for News
        var job : Job? = null
        searchEditText.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if(editable.toString().isNotEmpty()){
                        filterListWithCategory(editable.toString())
                    }else{
                        filterListWithCategory(newsCatt)
                    }
                }
            }
        }

    }

    private fun filterListWithCategory(categoryOrQuery: String) {
        viewModel.apply {
            searchNewsResponse = null
            newsCat.postValue(categoryOrQuery)
            searchNewsPage = 1
            getSearchNews(categoryOrQuery)
        }
       
    }

    private fun showHideBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        else
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showProgressBar() {
        homeProgressBar?.let {
            it.visibility = View.VISIBLE
            isLoading = true
        }
    }

    private fun showAnimation(isShow : Boolean){
        if(isShow){
            homeAnimationView.visibility = View.VISIBLE
        }else{
            homeAnimationView.visibility = View.GONE
        }
    }

    private fun hideProgressBar() {
        homeProgressBar?.let {
            it.visibility = View.GONE
            isLoading = false
        }
    }
    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter(true)
        homeRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            val divider = DividerItemDecoration(context,LinearLayoutManager.VERTICAL)
            addItemDecoration(divider)
            adapter = newsAdapter
            addOnScrollListener(this@HomeFragment.scrollListener)
        }
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManger = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManger.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManger.childCount
            val totalItemCount = layoutManger.itemCount


            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE

            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning
                    && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.getSearchNews(newsCatt)
                isScrolling = false
            }

        }
    }
}