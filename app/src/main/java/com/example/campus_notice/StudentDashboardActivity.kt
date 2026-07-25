// Campus Pulse — customized for Aman
package com.example.campus_notice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campus_notice.databinding.ActivityStudentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentDashboardBinding
    private val noticeList = mutableListOf<Notice>()
    private lateinit var noticeAdapter: NoticeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val sharedPrefs = getSharedPreferences("NoticeBoardPrefs", Context.MODE_PRIVATE)
        val email = sharedPrefs.getString("email", "Unknown Email")
        
        binding.tvWelcomeInfo.text = getString(R.string.welcome_student)
        binding.tvWelcomeEmail.text = email

        setupRecyclerView()
        
        if (Utils.isNetworkAvailable(this)) {
            fetchNotices()
        } else {
            Toast.makeText(this, "No internet. Showing cached or empty notices.", Toast.LENGTH_SHORT).show()
        }

        checkAndRequestNotificationPermission()
        
        val serviceIntent = Intent(this, NoticeUpdateService::class.java)
        startService(serviceIntent)
        
        FirebaseMessaging.getInstance().subscribeToTopic("notices")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, 1, 0, getString(R.string.logout))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            val sharedPrefs = getSharedPreferences("NoticeBoardPrefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun setupRecyclerView() {
        noticeAdapter = NoticeAdapter(noticeList)
        binding.rvNotices.layoutManager = LinearLayoutManager(this)
        binding.rvNotices.adapter = noticeAdapter
        updateNoticeState()
    }

    private fun fetchNotices() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("notices")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                noticeList.clear()
                for (noticeSnapshot in snapshot.children) {
                    val notice = noticeSnapshot.getValue(Notice::class.java)
                    if (notice != null) {
                        noticeList.add(notice)
                    }
                }
                noticeList.sortByDescending { it.timestamp }
                noticeAdapter.notifyDataSetChanged()
                updateNoticeState()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@StudentDashboardActivity, "Failed to load notices: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateNoticeState() {
        val count = noticeList.size
        binding.tvNoticeCount.text = if (count == 1) {
            getString(R.string.notice_count_one)
        } else {
            getString(R.string.notice_count_many, count)
        }
        binding.emptyState.visibility = if (count == 0) View.VISIBLE else View.GONE
        binding.rvNotices.visibility = if (count == 0) View.INVISIBLE else View.VISIBLE
    }
}
