// Campus Pulse — customized for Aman
package com.example.campus_notice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campus_notice.databinding.ItemNoticeBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoticeAdapter(private val noticeList: List<Notice>) : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    class NoticeViewHolder(val binding: ItemNoticeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val binding = ItemNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoticeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        val notice = noticeList[position]
        holder.binding.tvTitle.text = notice.title
        holder.binding.tvDescription.text = notice.description
        holder.binding.tvCreatedBy.text = holder.itemView.context.getString(R.string.posted_by, notice.createdBy)
        
        holder.binding.tvTimestamp.text = if (notice.timestamp > 0L) {
            val sdf = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault())
            sdf.format(Date(notice.timestamp))
        } else {
            holder.itemView.context.getString(R.string.recently)
        }
    }

    override fun getItemCount(): Int {
        return noticeList.size
    }
}
