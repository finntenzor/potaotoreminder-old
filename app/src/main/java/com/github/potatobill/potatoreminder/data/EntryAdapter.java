package com.github.potatobill.potatoreminder.data;

import java.util.ArrayList;

import com.github.potatobill.potatoreminder.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EntryAdapter extends ArrayAdapter<EntryData> {
	public EntryAdapter(Context context,ArrayList<EntryData> data)
	{
		super(context,R.layout.entry,data);
	}

	private class ViewHolder
	{
		TextView tvT;
		TextView tvH;
		ImageView iv;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder;
		if (convertView == null)
		{
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(R.layout.entry, parent, false);
			
			viewHolder = new ViewHolder();
			viewHolder.tvT = (TextView) convertView.findViewById(R.id.entryTextViewTitle);
			viewHolder.tvH = (TextView) convertView.findViewById(R.id.entryTextViewHint);
			viewHolder.iv = (ImageView) convertView.findViewById(R.id.entryImageView);
			convertView.setTag(viewHolder);
		} else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		EntryData d = getItem(position);
		switch (d.type)
		{
			case EntryData.TYPE_BACK:
				viewHolder.iv.setImageResource(R.drawable.ic_entry_back);
				break;
			case EntryData.TYPE_POINT:
				viewHolder.iv.setImageResource(R.drawable.ic_entry_point);
				break;
			case EntryData.TYPE_NODE:
				viewHolder.iv.setImageResource(R.drawable.ic_entry_node);
				break;
			case EntryData.TYPE_LIST:
				viewHolder.iv.setImageResource(R.drawable.ic_entry_list);
				break;
			case EntryData.TYPE_IMAGE:
				viewHolder.iv.setImageResource(R.drawable.ic_entry_image);
				break;
			case EntryData.TYPE_NEW_POINT:
				viewHolder.iv.setImageResource(R.drawable.ic_entry_new_point);
				break;
			case EntryData.TYPE_NEW_NODE:
				viewHolder.iv.setImageResource(R.drawable.ic_entry_new_node);
				break;
			case EntryData.TYPE_NEW_LIST:
				viewHolder.iv.setImageResource(R.drawable.ic_entry_new_list);
				break;
			case EntryData.TYPE_NEW_IMAGE:
				viewHolder.iv.setImageResource(R.drawable.ic_entry_new_image);
				break;
			default:
				viewHolder.iv.setImageResource(android.R.drawable.btn_default);
				break;
		}
		viewHolder.tvT.setText(d.title);
		if (d.hint.length()==0)
		{
			viewHolder.tvH.setVisibility(View.GONE);
		} else
		{
			viewHolder.tvH.setVisibility(View.VISIBLE);
			viewHolder.tvH.setText(d.hint);
		}
		return convertView;
	}
}
