package com.github.potatobill.potatoreminder.data;
import android.widget.ArrayAdapter;
import android.content.Context;
import java.util.ArrayList;
import com.github.potatobill.potatoreminder.R;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

public class PointAdapter extends ArrayAdapter<EntryPoint> {
	public PointAdapter(Context context,ArrayList<EntryPoint> data)
	{
		super(context,R.layout.point,data);
	}

	private class ViewHolder
	{
		TextView tvLeft;
		TextView tvRight;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder;
		if (convertView == null)
		{
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(R.layout.point, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.tvLeft = (TextView) convertView.findViewById(R.id.pointTextViewLeft);
			viewHolder.tvRight = (TextView) convertView.findViewById(R.id.pointTextViewRight);
			convertView.setTag(viewHolder);
		} else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		EntryPoint p = getItem(position);
		String h;
		if (p.hint.length() == 0)
		{
			h = "(点击查看知识点内容)";
		} else
		{
			h = p.hint;
		}
		switch (p.state)
		{
			case EntryPoint.STATE_HINT:
				viewHolder.tvRight.setVisibility(View.GONE);
				viewHolder.tvLeft.setTextColor(0xFF939393);
				viewHolder.tvLeft.setText(h);
				break;
			case EntryPoint.STATE_SHOW:
				viewHolder.tvRight.setVisibility(View.GONE);
				viewHolder.tvLeft.setTextColor(android.graphics.Color.BLACK);
				viewHolder.tvLeft.setText(p.point);
				break;
			case EntryPoint.STATE_RLEVEL_FORGOT:
				viewHolder.tvRight.setText("不记得");
				viewHolder.tvRight.setVisibility(View.VISIBLE);
				viewHolder.tvLeft.setTextColor(android.graphics.Color.BLACK);
				viewHolder.tvLeft.setText(p.point);
				break;
			case EntryPoint.STATE_RLEVEL_LOW:
				viewHolder.tvRight.setText("记不清");
				viewHolder.tvRight.setVisibility(View.VISIBLE);
				viewHolder.tvLeft.setTextColor(android.graphics.Color.BLACK);
				viewHolder.tvLeft.setText(p.point);
				break;
			case EntryPoint.STATE_RLEVEL_REMEMBER:
				viewHolder.tvRight.setText("记得");
				viewHolder.tvRight.setVisibility(View.VISIBLE);
				viewHolder.tvLeft.setTextColor(android.graphics.Color.BLACK);
				viewHolder.tvLeft.setText(p.point);
				break;
		}
		
		return convertView;
	}
}
