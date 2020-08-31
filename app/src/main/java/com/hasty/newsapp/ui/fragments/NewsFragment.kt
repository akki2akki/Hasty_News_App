package com.hasty.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.hasty.newsapp.R
import com.hasty.newsapp.adapters.NewsAdapter
import com.hasty.newsapp.ui.NewsActivity
import com.hasty.newsapp.ui.viewmodels.NewsViewModel
import com.hasty.newsapp.utils.Constants.QUERY_PAGE_SIZE
import com.hasty.newsapp.utils.Resource
import kotlinx.android.synthetic.main.fragment_news.*

const val TAG = "NewsFragment"

class NewsFragment : Fragment(R.layout.fragment_news) {

    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as NewsActivity).viewModel
        setUpRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_newsFragment_to_articleFragment, bundle)
        }

        viewModel.breakingNews.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        if(newsAdapter.differ.currentList.isEmpty()){
                            showAnimation(true)
                        }else{
                            showAnimation(false)
                        }
                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                        Log.d(TAG, "Total Pages : $totalPages")
                        isLastPage = viewModel.breakingNewsPage == totalPages
                        Toast.makeText(
                            context,
                            "swipe to more",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is Resource.Error -> {
                    showAnimation(true)
                    hideProgressBar()
                    response.message?.let {
                        Toast.makeText(context, "Error Occured : $it", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }

        })
    }

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()
        newsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(this)
            adapter = newsAdapter
            addOnScrollListener(this@NewsFragment.scrollListener)
        }
    }

    private fun showProgressBar() {
        newsProgressbar?.let {
            it.visibility = View.VISIBLE
            isLoading = true
        }
    }

    private fun hideProgressBar() {
        newsProgressbar?.let {
            it.visibility = View.GONE
            isLoading = false
        }
    }

    private fun showAnimation(isShow : Boolean){
        if(isShow){
            newsAnimationView.visibility = View.VISIBLE
        }else{
            newsAnimationView.visibility = View.GONE
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

            Log.d(
                TAG, "onScrolled: firstVisibleItemPos = $firstVisibleItemPosition \n " +
                        "visibleItemCount = $visibleItemCount \n totalItemCount = $totalItemCount "
            )

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE

            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning
                    && isTotalMoreThanVisible && isScrolling

            Log.d(TAG, "Should Paginate = $shouldPaginate")

            if (shouldPaginate) {
                viewModel.getBreakingNews("in")
                isScrolling = false
            }

        }
    }


}