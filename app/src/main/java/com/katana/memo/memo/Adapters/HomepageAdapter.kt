package com.katana.memo.memo.Adapters

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import com.katana.memo.memo.Helper.DatabaseHelper
import com.katana.memo.memo.Models.MemoModel
import com.katana.memo.memo.R
import kotlinx.android.synthetic.main.recyclerview_row.view.*
import java.util.*


class HomepageAdapter(context: Context, list: List<MemoModel>) : RecyclerView.Adapter<HomepageAdapter.MainViewHolder>() {

    var inflater: LayoutInflater = LayoutInflater.from(context)
    var modelList: ArrayList<MemoModel> = ArrayList(list)
    var lastPosition: Int = -1

    companion object {
        lateinit var dbHelper: DatabaseHelper
        var itemPosition: Int = 0
        lateinit var c: Context
    }

    init {
        dbHelper = DatabaseHelper(context)
        c = context
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MainViewHolder {
        val view: View = inflater.inflate(R.layout.recyclerview_row, parent, false)
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        itemPosition = dbHelper.theAmountOfMemos - position
        setScaleAnimation(holder.itemView, position)
        holder.bindData(modelList[position])
    }

    override fun getItemCount(): Int {
        return modelList.size
    }



    fun reloadRecyclerView(data: ArrayList<MemoModel>) {
        modelList.clear()
        modelList.addAll(data)
        notifyDataSetChanged()
    }

    class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var memoTitle: AppCompatTextView = itemView.findViewById(R.id.memoTitleTextView) as AppCompatTextView
        var memoBody: AppCompatTextView = itemView.findViewById(R.id.memoBodyTextView) as AppCompatTextView
        var favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton) as ImageButton

        fun bindData(memoModel: MemoModel) {
            memoTitle.text = memoModel.memoTitle
            memoBody.text = memoModel.memoBody
            if (HomepageAdapter.dbHelper.checkIfFavorite(itemPosition)) {
                favoriteButton.setImageDrawable(HomepageAdapter.c.resources.getDrawable(R.drawable.ic_favorite_filled))
            } else {
                favoriteButton.setImageDrawable(HomepageAdapter.c.resources.getDrawable(R.drawable.ic_favorite_unfilled))
            }

            if (HomepageAdapter.dbHelper.checkMemoForPhoto(itemPosition)) {
                itemView.miniPhoto.setImageDrawable(HomepageAdapter.c.resources.getDrawable(R.drawable.ic_mini_photo_icon))
            } else {
                itemView.miniPhoto.setImageDrawable(null)
            }

            if (HomepageAdapter.dbHelper.checkMemoForDrawing(itemPosition)) {
                itemView.miniDrawing.setImageDrawable(c.resources.getDrawable(R.drawable.ic_mini_drawing_icon))

            } else {
                itemView.miniDrawing.setImageDrawable(null)
            }

            if (HomepageAdapter.dbHelper.checkMemoForAddedImages(itemPosition)) {
                itemView.miniImage.setImageDrawable(c.resources.getDrawable(R.drawable.ic_mini_image_icon))
            } else {
                itemView.miniImage.setImageDrawable(null)
            }

            if (HomepageAdapter.dbHelper.checkMemoForAudios(itemPosition)) {
                itemView.miniAudio.setImageDrawable(c.resources.getDrawable(R.drawable.ic_mini_audio_icon))
            } else {
                itemView.miniAudio.setImageDrawable(null)
            }

        }


    }

    private fun setScaleAnimation(view: View, position: Int) {
        if (position > lastPosition) {
//            val anim = ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, view.width / 2, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
//            anim.duration = Random().nextInt(501).toLong()

            val anim = AnimationUtils.loadAnimation(c, R.anim.slide_up_animation)

            view.startAnimation(anim)
            lastPosition = position
        }
    }

}



