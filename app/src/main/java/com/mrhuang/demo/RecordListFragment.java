package com.mrhuang.demo;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

public class RecordListFragment extends BottomSheetDialogFragment {

    ListView listView;

    File[] fileList;

    ItemClickListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_record_list, container, true);
        getViews(contentView);
        viewCreated();
        return contentView;
    }

    void getViews(View rootView) {
        listView = rootView.findViewById(R.id.listView);
    }

    void viewCreated() {

        fileList = getActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC).listFiles();// 读取文件夹下文件
        FileListAdapter adapter = new FileListAdapter();
        listView.setAdapter(adapter);
    }

    void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    private class FileListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return fileList.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.textView.setText(fileList[position].getName());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onRecordClick(fileList[position]);
                }
            });

            return convertView;
        }
    }

    class ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            textView = itemView.findViewById(android.R.id.text1);
        }
    }

    interface ItemClickListener {
        public void onRecordClick(File file);
    }
}
