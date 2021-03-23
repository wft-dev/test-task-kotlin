package com.git.gitlist.ui

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.git.gitlist.Database.AppDatabase
import com.git.gitlist.ImgaeCache.MyCache
import com.git.gitlist.Interface.GitEndPoints
import com.git.gitlist.Modal.GitUser
import com.git.gitlist.R
import com.git.gitlist.Receivers.ConnectivityReceiver
import com.git.gitlist.Server.ServiceBuilder
import com.git.gitlist.adapter.getListAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


class MainActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener,getListAdapter.onClassRedirect {
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var imgNoInternet: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var search_text: EditText
    private lateinit var adapter: getListAdapter
    private   var gitUserList : ArrayList<GitUser> = ArrayList()
    private lateinit var progress_bar : ProgressBar
    private var isLoading: Boolean = false
    private var handler: Handler = Handler()
    private var requestCode: Int = 0
    private  var  gitlist :List<GitUser> = emptyList()
    private lateinit var db: AppDatabase
    private lateinit var listener : ConnectivityReceiver.ConnectivityReceiverListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // call the broadcast receiver for check internet connectivity
        registerReceiver(
            ConnectivityReceiver(),
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
        // Hide Action bar
        supportActionBar?.hide()
        // set layout to activity
        setContentView(R.layout.activity_main)

        this.listener = this

        //Initialize the Database
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "git-list.db"
        ).build()




        // Initialize the UI component
        progress_bar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recyclerView)
        search_text = findViewById(R.id.search_text)
        imgNoInternet = findViewById(R.id.imgNoInternet)
        search_text.addTextChangedListener(textWatcher)
        setupRecyclerView()


        // get data from database
        GlobalScope.launch {
            gitlist =  db.GitUserDao().getAll()
            runOnUiThread(Runnable {
                if(gitlist.size !== 0){
                    if(requestCode == 2){
                        gitUserList.clear()
                        adapter.clearData()
                    }
                    // set data in recyclerview from database
                    updateDatabaseList(gitlist)
                    imgNoInternet.visibility = View.GONE
                }
            })
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            Toast.makeText(this@MainActivity, "On Filter", Toast.LENGTH_SHORT).show()
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            Toast.makeText(this@MainActivity, "before Filter", Toast.LENGTH_SHORT).show()
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // filteration the array list
            if(s.toString().trim()!!.length === 0){
                isLoading = false
                updateFilterList(gitUserList)
            }else{
                var userList: List<GitUser> = gitUserList.filter { t -> t.login!!.contains(s.toString()) }
                isLoading = true
                updateFilterList(userList as List<GitUser>)
            }

        }
    }

    private fun setupRecyclerView() {
        // Initialize the adapter
        adapter = getListAdapter(this, this)
        // set recycler view with default settings
        linearLayoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter

        // recyclerview scrollview
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isLoading) {
                    Log.e("TARUN Scroll",gitUserList.size.toString())
                    if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == gitUserList.size - 1) {
                        loadData()
                        isLoading = true
                    }
                }
            }
        })

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                    positionStart,
                    0
                )
            }
        })
    }


    fun loadData(isInitLoad: Boolean = false) {
        progress_bar.visibility = View.VISIBLE
        val apiService = ServiceBuilder.getClient()!!.create(GitEndPoints::class.java)
        var call : Call<List<GitUser>>
        if (isInitLoad) {
            call = apiService.getGitList(0)
        }else{
            if(gitUserList.size === 0){
                call = apiService.getGitList(0)
            }else{
                call = gitUserList.get(gitUserList.size - 1).id?.let { apiService.getGitList(it) }!!
            }

        }

        // request send to api for data
        call.enqueue(object : Callback<List<GitUser>> {
            override fun onResponse(call: Call<List<GitUser>>, response: Response<List<GitUser>>) {
                // responce sucessfully received
                progress_bar.visibility = View.GONE
                when {
                    response.code() == 200 -> updateDataList(response.body()!!)
                    else -> Log.e("TARUN", "fail")
                }

            }

            override fun onFailure(call: Call<List<GitUser>>, t: Throwable) {
                // api failure
                progress_bar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Fail", Toast.LENGTH_SHORT).show()
            }
        })

    }

    fun updateDataList(newList: List<GitUser>){

        // call the data from database
        newList?.forEach{
            try {
                var mMyTask = DownloadTask(it,db).execute(stringToURL(it.avatar_url))

            } catch (e: IOException) {
                System.out.println(e)
            }

        }

        gitUserList.addAll(newList)
        gitlist = gitUserList
        adapter.submitList(newList)
        isLoading = false

    }

    fun updateDatabaseList(newList: List<GitUser>){
        gitUserList.clear()
        adapter.clearData()
        gitUserList.addAll(newList)
        adapter.submitList(newList)
        isLoading = false
    }

    fun updateFilterList(newList: List<GitUser>){
        adapter.clearData()
        adapter.submitList(newList)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (!isConnected) {
            // if internet is not connected
            val messageToUser = "You are offline now."
            Toast.makeText(this@MainActivity, messageToUser, Toast.LENGTH_SHORT).show()
            if(gitlist.size !== 0){
                if(requestCode == 2){
                    gitUserList.clear()
                    adapter.clearData()
                }
                // set data in recyclerview from database
                updateDatabaseList(gitlist)

                imgNoInternet.visibility = View.GONE
            }else{
                imgNoInternet.visibility = View.VISIBLE
            }

        } else {
            // if internet is  connected
            imgNoInternet.visibility = View.GONE
            val messageToUser = "You are online now."
            Toast.makeText(this@MainActivity, messageToUser, Toast.LENGTH_SHORT).show()

            if(requestCode == 2){
                gitUserList.clear()
                adapter.clearData()
            }

            // if data is exist in database then set the data otherwise get data from api's


                if(gitlist.size === 0){
                    // get data from api's
                    loadData(isInitLoad = true)
                }else{
                    /// setdata in recyclerview from database
                    updateDatabaseList(gitlist)
                }


        }
    }


    override fun onResume() {
        super.onResume()
        // check updated data when come back from next activity
        if(requestCode == 2){
            search_text.setText("")
            gitUserList.clear()
            adapter.clearData()
            updateDatabaseList(gitlist)
        }
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onItemClick(data: GitUser) {
        // click event on listview item
        // passed the data to next activity with request code
        val intent = Intent(this, UserDetailsActivity::class.java)
        intent.putExtra("login", data.login)
        intent.putExtra("id", data.id)
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==2){
            this.requestCode = requestCode
            GlobalScope.launch {
                gitlist =  db.GitUserDao().getAll()

            }
        }
    }


    // download image from url and save in cache
    private class DownloadTask(its : GitUser,dbs : AppDatabase) :
        AsyncTask<URL?, Void?, Bitmap?>() {
        var it : GitUser = its
        var db: AppDatabase = dbs



        override fun onPreExecute() {

        }

        override fun doInBackground(vararg urls: URL?): Bitmap? {
            val url = urls[0]
            var connection: HttpURLConnection? = null
            try {
                connection = url?.openConnection() as HttpURLConnection
                connection.connect()
                val inputStream = connection!!.inputStream
                val bufferedInputStream = BufferedInputStream(inputStream)
                val bitmap =BitmapFactory.decodeStream(bufferedInputStream)
                var data = GitUser(it.id, it.login, it.avatar_url, encodeImages(bitmap),"", "", "", "", "", "")
                try {
                    db.GitUserDao().insertSingleData(data)
                }catch (e: Exception){

                }

                return bitmap
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        private fun encodeImages(bm: Bitmap): String? {
            val baos = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val b = baos.toByteArray()
            return Base64.encodeToString(b, Base64.DEFAULT)
        }


        // When all async task done
        override fun onPostExecute(result: Bitmap?) {
            // Hide the progress dialog

            if (result != null) {
                MyCache.instance.saveBitmapToCahche(it.id.toString(),result)

            } else {
                // Notify user that an error occurred while downloading image
//                Toast.makeText(th, "Error", Toast.LENGTH_SHORT).show()
            }
        }


    }

    protected fun stringToURL(url:String?): URL? {
        try {
            var url = URL(url)
            return url
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }




}

