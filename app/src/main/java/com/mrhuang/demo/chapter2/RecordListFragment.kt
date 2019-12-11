package com.mrhuang.demo.chapter2

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mrhuang.demo.R
import java.io.File

class RecordListFragment : BottomSheetDialogFragment() {
    private var listView: ListView? = null
    lateinit var fileList: Array<File>
    var listener: ItemClickListener? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.fragment_record_list, container, true)
        getViews(contentView)
        viewCreated()
        return contentView
    }

    fun getViews(rootView: View) {
        listView = rootView.findViewById(R.id.listView)
    }

    fun viewCreated() {
        fileList = activity!!.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.listFiles() // 读取文件夹下文件
        val adapter = FileListAdapter()
        listView!!.adapter = adapter
    }

    fun setItemClickListener(listener: ItemClickListener?) {
        this.listener = listener
    }

    private inner class FileListAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return fileList.size
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            val viewHolder: ViewHolder = convertView.tag as ViewHolder
            viewHolder.textView.text = fileList[position].name
            convertView.setOnClickListener { listener!!.onRecordClick(fileList[position]) }
            return convertView
        }
    }

    internal inner class ViewHolder(itemView: View) {
        var textView: TextView = itemView.findViewById(android.R.id.text1)
    }

    interface ItemClickListener {
        fun onRecordClick(file: File?)
    }
}