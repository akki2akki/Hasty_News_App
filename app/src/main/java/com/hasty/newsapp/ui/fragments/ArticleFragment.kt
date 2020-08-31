package com.hasty.newsapp.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.hasty.newsapp.R
import kotlinx.android.synthetic.main.fragment_article.*

class ArticleFragment : Fragment(R.layout.fragment_article) {

    val args : ArticleFragmentArgs by navArgs()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val article = args.article

        (activity as AppCompatActivity).setSupportActionBar(articleToolbar)
        val title  = article.source?.name
        if(title!!.isNotEmpty()){
            articleToolbar.title = title
        }


        articleToolbar?.setNavigationOnClickListener {
            if(articleWebView.canGoBack()){
                articleWebView?.goBack()
                return@setNavigationOnClickListener
            }
            (activity as AppCompatActivity).onBackPressed()
        }

        articleWebView?.apply {
            webViewClient = object : WebViewClient(){
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    view?.loadUrl(article.url!!)
                    return super.shouldOverrideUrlLoading(view, request)
                }
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    showProgress(true)
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    showProgress(false)
                    super.onPageFinished(view, url)
                }
            }
            loadUrl(article.url!!)

        }
    }

    private fun showProgress(isShow: Boolean) {
        if(isShow){
            articleProgressBar?.visibility = View.VISIBLE
        }else{
            articleProgressBar?.visibility = View.GONE
        }
    }
}