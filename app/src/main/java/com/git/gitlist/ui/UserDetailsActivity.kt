package com.git.gitlist.ui

import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.room.Room
import com.bumptech.glide.Glide
import com.git.gitlist.Database.AppDatabase
import com.git.gitlist.Interface.GitEndPoints
import com.git.gitlist.Modal.GitUser
import com.git.gitlist.R
import com.git.gitlist.Receivers.ConnectivityReceiver
import com.git.gitlist.Server.ServiceBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.schedule


class UserDetailsActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    private lateinit var imgUser: ImageView
    private lateinit var login : String
    private  var id : Int = 0
    private lateinit var txtName : TextView
    private lateinit var txtFollowers : TextView
    private lateinit var txtFollowing : TextView
    private lateinit var txtUserName : TextView
    private lateinit var txtCompany : TextView
    private lateinit var txtBlog : TextView
    private lateinit var imgBack : ImageView
    private lateinit var editNotes : EditText
    private lateinit var btnSave : Button
    private lateinit var  gitData : GitUser
    private lateinit var db: AppDatabase
    private lateinit var rlProgressbar : RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // call the broadcast receiver for check internet connectivity
        registerReceiver(ConnectivityReceiver(),
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
        // hide actin bar
        supportActionBar?.hide()
        // set layout to activity
        setContentView(R.layout.user_details_activity)
        //Initialize the Database
        db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "git-list.db"
        ).build()

        // receive data from previous activity
        login = intent.getStringExtra("login").toString()
        id = intent.getIntExtra("id", 0)

        // Initialize the UI component
        txtName = findViewById(R.id.txtName)
        txtFollowers = findViewById(R.id.txtFollowers)
        txtFollowing = findViewById(R.id.txtFollowing)
        txtUserName = findViewById(R.id.txtUserName)
        txtCompany = findViewById(R.id.txtCompany)
        txtBlog = findViewById(R.id.txtBlog)
        imgUser = findViewById(R.id.imgUser)
        imgBack = findViewById(R.id.imgBack)
        editNotes = findViewById(R.id.editNotes)
        btnSave = findViewById(R.id.btnSave)
        rlProgressbar = findViewById(R.id.rlProgressbar)

        // back button listener
        imgBack.setOnClickListener {
            val intent = Intent()
            setResult(2, intent)
            finish()
        }
        // show progress bar
        showVisibility()
        // save button listener.
        btnSave.setOnClickListener {
            if(editNotes.text.toString().trim().length === 0){
                ShowAlert("Please write note for that user before pressing save button")
            }else {
                showVisibility()
                GlobalScope.launch {
                    var data = GitUser(gitData?.id, gitData?.login, gitData?.avatar_url,gitData?.avatar_url_bitmap ,gitData?.type, gitData?.name, gitData?.company, gitData?.followers, gitData?.following, gitData?.blog, editNotes.text?.toString())
                    db.GitUserDao().UpdateSingleData(data)
                    Timer("SettingUp", false).schedule(2000) {

                        runOnUiThread(Runnable { HideVisibility()
                            ShowAlert("Data Inserted in database sucessfully")
                        })
                    }
                }

            }

        }

        // get data from database for particular user
        GlobalScope.launch {
            gitData =  db.GitUserDao().getSingleData(id)


        }


    }

    private fun setData(response: GitUser) {
        // set all the data in UI componenet
        txtName.text = response.login
        txtFollowers.text = response?.followers
        txtFollowing.text = response?.following
        txtUserName.text = response?.login
        txtCompany.text = response?.company
        txtBlog.text = response?.blog
        editNotes.setText(response?.notes)
        if(response.avatar_url_bitmap != null){
            var imageBytes = Base64.decode(response.avatar_url_bitmap, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imgUser.setImageBitmap(decodedImage)
        }else{
            Glide.with(this).load(response.avatar_url).into(imgUser)
        }
//        Glide.with(this@UserDetailsActivity).load(response.avatar_url).into(imgUser)
        // hide progress bar
        HideVisibility()
    }

    private fun AddData(response: GitUser){
        setData(response)
        GlobalScope.launch {
            var data = GitUser(response?.id, response?.login, response?.avatar_url, response?.avatar_url_bitmap,response?.type, response?.name, response?.company, response?.followers, response?.following, response?.blog, response?.notes)
            db.GitUserDao().UpdateSingleData(data)
        }
    }

    private fun GetUserDetail(){
        // Initialize the api call client
        val apiService = ServiceBuilder.getClient()!!.create(GitEndPoints::class.java)
        val call = apiService.getUserDetails(login)

        // send request for get data to api
        call.enqueue(object : Callback<GitUser> {
            override fun onResponse(call: Call<GitUser>, response: Response<GitUser>) {
                // response sucessfully received
                HideVisibility()
                when {
                    response.code() == 200 -> AddData(response.body()!!)
                    else -> Log.e("TARUN", "fail")
                }

            }

            override fun onFailure(call: Call<GitUser>, t: Throwable) {
                // responce failure
                HideVisibility()
                Toast.makeText(this@UserDetailsActivity, "Fail", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (!isConnected) {
            // when internet is not connected
            val messageToUser = "You are offline now."
            Toast.makeText(this@UserDetailsActivity, messageToUser, Toast.LENGTH_SHORT).show()
            if(!gitData.name.equals("")){
                // set data to ui componenet after get the data from databsae
                setData(gitData)
            }else{
                imgUser.setBackgroundResource(R.drawable.internet)
            }

        } else {
            // when internet is  connected
            val messageToUser = "You are online now."
            Toast.makeText(this@UserDetailsActivity, messageToUser, Toast.LENGTH_SHORT).show()

            if(gitData.name.equals("")){
                // get data from api
                GetUserDetail()
            }else{
                // set data to ui componenet after get the data from databsae
                setData(gitData)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent()
        setResult(2, intent)
        finish()
    }

    fun ShowAlert(message : String){
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(message)
                // if the dialog is cancelable
                .setCancelable(false)
                // positive button text and action
                .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->

                })

        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle("Sucess")
        // show alert dialog
        alert.show()
    }

    fun showVisibility(){
        // sset progressbar visibility to VISIBLE
        rlProgressbar.visibility = View.VISIBLE

    }

    fun HideVisibility(){
        // sset progressbar visibility to GONE
        rlProgressbar.visibility = View.GONE

    }
}