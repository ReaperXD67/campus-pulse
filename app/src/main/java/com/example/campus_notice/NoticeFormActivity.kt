// Campus Pulse — customized for Aman
package com.example.campus_notice

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campus_notice.databinding.ActivityNoticeFormBinding
import com.google.firebase.database.FirebaseDatabase

class NoticeFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoticeFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnPublish.setOnClickListener {
            publishNotice()
        }
    }

    private fun publishNotice() {
        if (!Utils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Please connect to the internet to publish a notice", Toast.LENGTH_SHORT).show()
            return
        }

        val title = binding.etNoticeTitle.text.toString().trim()
        val description = binding.etNoticeDescription.text.toString().trim()

        binding.tilNoticeTitle.error = null
        binding.tilNoticeDescription.error = null

        if (title.isEmpty()) {
            binding.tilNoticeTitle.error = getString(R.string.field_required)
            return
        }

        if (description.isEmpty()) {
            binding.tilNoticeDescription.error = getString(R.string.field_required)
            return
        }

        val sharedPrefs = getSharedPreferences("NoticeBoardPrefs", Context.MODE_PRIVATE)
        val createdBy = sharedPrefs.getString("email", "Unknown Staff") ?: "Unknown Staff"

        val databaseRef = FirebaseDatabase.getInstance().getReference("notices")
        val noticeId = databaseRef.push().key ?: return

        val notice = Notice(
            id = noticeId,
            title = title,
            description = description,
            createdBy = createdBy,
            timestamp = System.currentTimeMillis()
        )

        setLoading(true)
        databaseRef.child(noticeId).setValue(notice)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Notice Published Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    setLoading(false)
                    Toast.makeText(this, "Failed to publish notice: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnPublish.isEnabled = !isLoading
        binding.btnPublish.text = getString(if (isLoading) R.string.publishing else R.string.publish)
        binding.etNoticeTitle.isEnabled = !isLoading
        binding.etNoticeDescription.isEnabled = !isLoading
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
