package com.git.gitlist.adapter

import android.app.Activity
import android.graphics.*
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.git.gitlist.Modal.GitUser
import com.git.gitlist.R
import java.io.ByteArrayOutputStream

class getListAdapter(val activity: Activity,val listener:onClassRedirect):RecyclerView.Adapter<getListHolder>() {

    private lateinit var parent : ViewGroup
    private  var gitList: ArrayList<GitUser> = ArrayList<GitUser>()
    private lateinit var listeners:onClassRedirect

    // interface for click item
    interface onClassRedirect{
        fun onItemClick(data: GitUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): getListHolder {
        this.parent = parent
        this.listeners = listener
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gitlist_adapter, parent, false)
        return getListHolder(view)
    }

    override fun getItemCount(): Int {
        return gitList.size
    }

    override fun onBindViewHolder(holder: getListHolder, position: Int) {

        return holder.bind(gitList[position],listener,position)
    }

    fun submitList(tempList: List<GitUser>) {
        gitList.addAll( tempList)
        notifyDataSetChanged()
    }

    fun clearData(){
        gitList.clear()
    }




}
    class getListHolder(itemView : View): RecyclerView.ViewHolder(itemView){
        // Initialize the UI component
        private val img_profile_image: ImageView = itemView.findViewById(R.id.img_profile_image)
        private val imgNotes: ImageView = itemView.findViewById(R.id.imgNotes)
        private val txtName: TextView = itemView.findViewById(R.id.txtName)
        private val rlRoot: ConstraintLayout = itemView.findViewById(R.id.rlRoot)

        // bind the data to UI component
        fun bind(data: GitUser,listener:getListAdapter.onClassRedirect,position: Int) {
//            Glide.with(itemView.context).load(data.avatar_url).into(img_profile_image)

            if(data.avatar_url_bitmap != null){
                var imageBytes = Base64.decode(data.avatar_url_bitmap, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)


                if((position+1)%4 === 0){
                    decodedImage?.apply {
                        // set inverted colors bitmap to second image view
                        invertColors()?.apply {
                            img_profile_image.setImageBitmap(this)
                        }
                    }
                    Log.e("TARUN",""+position)
                }else{
                    img_profile_image.setImageBitmap(decodedImage)
                }
            }else{
                Glide.with(itemView.context).load(data.avatar_url).into(img_profile_image)
            }


            txtName.text = "Name: "+data.login
            if(data.notes === null || data.notes.equals("")){
                imgNotes.visibility = View.GONE
            }else{
                imgNotes.visibility = View.VISIBLE
            }

            rlRoot.setOnClickListener {
                listener?.onItemClick(data)

            }
        }

    }


// extension function to invert bitmap colors
fun Bitmap.invertColors(): Bitmap? {
    val bitmap = Bitmap.createBitmap(
        width,
        height,
        Bitmap.Config.ARGB_8888
    )

    val matrixInvert = ColorMatrix().apply {
        set(
            floatArrayOf(
                -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            )
        )
    }

    val paint = Paint()
    ColorMatrixColorFilter(matrixInvert).apply {
        paint.colorFilter = this
    }

    Canvas(bitmap).drawBitmap(this, 0f, 0f, paint)
    return bitmap
}






