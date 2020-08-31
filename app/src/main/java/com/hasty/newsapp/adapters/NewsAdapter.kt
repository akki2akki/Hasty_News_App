package com.hasty.newsapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hasty.newsapp.R
import com.hasty.newsapp.models.Article
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_news.view.*
import kotlinx.android.synthetic.main.item_news.view.itemArticleImageView
import kotlinx.android.synthetic.main.item_news.view.itemArticleTitleTV
import kotlinx.android.synthetic.main.item_news.view.itemNewsSourceTV
import kotlinx.android.synthetic.main.item_news_short.view.*

class NewsAdapter(
    val isShort : Boolean = false
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    private val differCallBack = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val layoutToUse = if(isShort){
            R.layout.item_news_short
        }else{
            R.layout.item_news
        }
        return NewsViewHolder(
            LayoutInflater.from(parent.context).inflate(
               layoutToUse,
                parent,
                false
            )
        )
    }

    private var onItemClickListener : ((Article) -> Unit)? = null

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = differ.currentList[position]
        if(isShort){
            holder.itemView.apply {
                Glide.with(this).load(article.urlToImage)
                    .placeholder(R.drawable.ic_baseline_image_24)
                    .into(itemArticleImageViewS)
                itemArticleTitleTVS.text = article.title
                article.source?.name.let {
                    if(it!!.isNotEmpty()) itemNewsSourceTVS.text = "${article.source?.name}"
                }
               setOnClickListener {
                   onItemClickListener?.let {
                       it(article)
                   }
               }
            }
        }else{
            holder.itemView.apply {
                Glide.with(this).load(article.urlToImage)
                    .placeholder(R.drawable.ic_baseline_image_24)
                    .into(itemArticleImageView)
                itemArticleTitleTV.text = article.title
                itemArticleDescTV.text = article.description
                article.source?.name.let {
                    if(it!!.isNotEmpty()) itemNewsSourceTV.text = "Source: ${article.source?.name}"
                }
                itemActionButton.setOnClickListener {
                    onItemClickListener?.let {
                        it(article)
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun setOnItemClickListener(listener: (Article) -> Unit) {
        onItemClickListener = listener
    }

}