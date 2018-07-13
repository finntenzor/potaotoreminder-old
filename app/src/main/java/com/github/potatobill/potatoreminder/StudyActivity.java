package com.github.potatobill.potatoreminder;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.github.potatobill.potatoreminder.data.EntryData;
import com.github.potatobill.potatoreminder.data.EntryPoint;
import com.github.potatobill.potatoreminder.data.Machine;
import com.github.potatobill.potatoreminder.data.PointAdapter;
import java.util.ArrayList;
import android.widget.Toast;
import android.util.Log;
import android.widget.ImageView;
import android.graphics.Bitmap;

public class StudyActivity extends Activity implements OnClickListener, OnItemClickListener
{
	App app;
	Machine machine;
	
	TextView tvPosition;
	TextView tvTitle;
	TextView tvContext;
	ProgressBar pbStudy;
	ListView lvContext;
	ImageView ivContext;
	Button btRemember;
	Button btLow;
	Button btForgot;
	Button btConfirm;
	
	private int showType;
	//private int reviewedNum;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_study);
		app = (App) getApplicationContext();
		if (app.dm!=null)
		{
			machine = app.dm.getMachine();
			//machine.LogAll();
			setView();
		} else
		{
			finish();
		}
	}
	private void setView()
	{
		tvPosition = (TextView) findViewById(R.id.studyTextViewPosition);
		tvTitle = (TextView) findViewById(R.id.studyTextViewTitle);
		tvContext = (TextView) findViewById(R.id.studyTextViewContext);
		pbStudy = (ProgressBar) findViewById(R.id.studyProgressBar);
		lvContext = (ListView) findViewById(R.id.studyListViewContext);
		btRemember = (Button) findViewById(R.id.studyButtonRemember);
		btLow = (Button) findViewById(R.id.studyButtonLow);
		btForgot = (Button) findViewById(R.id.studyButtonForgot);
		btConfirm = (Button) findViewById(R.id.studyButtonConfirm);
		ivContext = (ImageView) findViewById(R.id.studyImageViewContext);
		
		tvContext.setOnClickListener(this);
		btRemember.setOnClickListener(buttonListener);
		btLow.setOnClickListener(buttonListener);
		btForgot.setOnClickListener(buttonListener);
		btConfirm.setOnClickListener(this);
		lvContext.setOnItemClickListener(this);
		pbStudy.setOnClickListener(this);
	}
	
	private void refreshView()
	{
		if (showType != app.tempEntry.getInt("type"))
		{
			if (app.tempEntry.getInt("type") == EntryData.TYPE_LIST)
			{
				tvContext.setVisibility(View.GONE);
				btRemember.setVisibility(View.GONE);
				btLow.setVisibility(View.GONE);
				btForgot.setVisibility(View.GONE);
				lvContext.setVisibility(View.VISIBLE);
				btConfirm.setVisibility(View.VISIBLE);
				ivContext.setVisibility(View.GONE);
			} else
			{
				tvContext.setVisibility(View.VISIBLE);
				btRemember.setVisibility(View.VISIBLE);
				btLow.setVisibility(View.VISIBLE);
				btForgot.setVisibility(View.VISIBLE);
				lvContext.setVisibility(View.GONE);
				btConfirm.setVisibility(View.GONE);
				ivContext.setVisibility(View.GONE);
			}
		}
		pbStudy.setProgress(machine.getProgress());
	}
	
	private OnClickListener buttonListener = new OnClickListener(){
		@Override
		public void onClick(View v)
		{
			double d = 0;
			switch (v.getId())
			{
				case R.id.studyButtonRemember:
					d = machine.confirmPoint(EntryPoint.STATE_RLEVEL_REMEMBER);
					break;
				case R.id.studyButtonLow:
					d = machine.confirmPoint(EntryPoint.STATE_RLEVEL_LOW);
					break;
				case R.id.studyButtonForgot:
					d = machine.confirmPoint(EntryPoint.STATE_RLEVEL_FORGOT);
					break;
			}
			int hd = (int) (d * 100);
			String sd = String.valueOf(hd / 100.0);
			if (d > 0) {
				showToast("熟悉度+"+sd);
			} else if (d < 0) {
				showToast("熟悉度"+sd);
			} else {
				showToast("熟悉度不变");
			}
			if (app.tempEntry.getInt("type") == EntryData.TYPE_IMAGE) {
				app.tempBitmap.recycle();
			}
			toTempAndNext();
			loadFromTemp();
		}
	};
	
	@Override
	public void onClick(View v)
	{
		// TODO: Implement this method
		switch (v.getId())
		{
			case R.id.studyTextViewContext:
				if (app.tempEntry.getInt("type") == EntryData.TYPE_POINT) {
					tvContext.setText(app.tempEntry.getString("point"));
					tvContext.setGravity(Gravity.LEFT+Gravity.TOP);
					tvContext.setTextColor(android.graphics.Color.BLACK);
				} else {
					tvContext.setVisibility(View.GONE);
					ivContext.setVisibility(View.VISIBLE);
					ivContext.setImageBitmap(app.tempBitmap);
				}
				break;
			case R.id.studyProgressBar:
				showToast("算上这一次还有"+machine.getNum()+"个知识点要复习");
				machine.LogAll();
				break;
			case R.id.studyButtonConfirm:
				confirmList();
				break;
		}
	}

	public void confirmList()
	{
		boolean check = false;
		int num = app.tempList.size();
		int[] id = new int[num];
		int[] state = new int[num];
		for (int i = 0; i < num; i++)
		{
			id[i] = app.tempList.get(i).id;
			state[i] = app.tempList.get(i).state;
			check = state[i]==EntryPoint.STATE_SHOW || state[i]==EntryPoint.STATE_HINT;
			if (check) break;
		}
		if (check) showToast("还有知识点没有选好记忆情况哦！");
		else {
			showToast(buildToast(machine.confirmList(id,state)));
			toTempAndNext();
			loadFromTemp();
		}
	}
	
	private String buildToast(double[] d)
	{
		StringBuilder sb = new StringBuilder("熟悉度:");
		int ti;
		double td;
		for (double b : d)
		{
			ti = (int) (b * 100);
			td = ti / 100.0;
			if (td > 0)
			{
				sb.append("+");
				sb.append(String.valueOf(td));
			} else if (td < 0) {
				sb.append(String.valueOf(td));
			} else {
				sb.append("不变");
			}
			sb.append("   ");
		}
		return sb.toString();
	}
	
	private void showToast(String text)
	{
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		//machin.prepare的时候已经自动读取了第1项
		if (app.firstIn) {
			toTempAndNext();
			app.firstIn = false;
		}
		loadFromTemp();
	}
	
	private PointAdapter pa;
	private void loadFromTemp()
	{
		//Log.d("potatoreminder","study-load");
		int id = app.tempEntry.getInt("id");
		tvPosition.setText(app.dm.getPositionString(id)+">");
		String h;
		switch (app.tempEntry.getInt("type"))
		{
			case EntryData.TYPE_BACK:
				showToast("恭喜你，学习完成~");
				app.tempEntry = null;
				app.tempList = null;
				app.tempBitmap = null;
				ivContext.setImageResource(R.drawable.ic_entry_image);
				//设置图片防止退出时崩溃
				//清空引用保证GC可以回收内存
				finish();
				break;
			case EntryData.TYPE_IMAGE:
				tvTitle.setText(app.tempEntry.getString("title"));
				h = app.tempEntry.getString("hint");
				if (h.length()==0) 
				{
					tvContext.setText("(点击查看知识点内容)");
				} else
				{
					tvContext.setText(app.tempEntry.getString("hint"));
				}
				tvContext.setTextColor(0xFF939393);
				tvContext.setGravity(Gravity.CENTER);
				refreshView();
				break;
			case EntryData.TYPE_POINT:
				tvTitle.setText(app.tempEntry.getString("title"));
				h = app.tempEntry.getString("hint");
				if (h.length()==0) 
				{
					tvContext.setText("(点击查看知识点内容)");
				} else
				{
					tvContext.setText(app.tempEntry.getString("hint"));
				}
				tvContext.setTextColor(0xFF939393);
				tvContext.setGravity(Gravity.CENTER);
				refreshView();
				break;
			case EntryData.TYPE_LIST:
				tvTitle.setText(app.tempEntry.getString("title"));
				pa = new PointAdapter(this,app.tempList);
				lvContext.setAdapter(pa);
				refreshView();
				break;
		}
	}
	
	private void toTempAndNext() {
		app.tempEntry = machine.getEntry();
		app.tempList = machine.getList();
		app.tempBitmap = machine.getBitmap();
		//读取machine中的temo不能在子线程完成，否则会因不同步导致null引用
		new Thread()
		{
			@Override
			public void run()
			{
				//long a = System.currentTimeMillis();
				
				machine.next();
				//machine.LogAll();
				//long b = System.currentTimeMillis();
				//Log.e("potatoreminder","The next method cost " + String.valueOf(b-a));
			}
		}.start();
	}
	

	@Override
	public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
	{
		EntryPoint p = app.tempList.get(p3);
		switch (p.state)
		{
			case EntryPoint.STATE_HINT:
				int n = app.tempList.size();
				for (int i = 0;i<n;i++)
				{
					p = app.tempList.get(i);
					p.state = EntryPoint.STATE_SHOW;
				}
				break;
			case EntryPoint.STATE_RLEVEL_FORGOT:
			case EntryPoint.STATE_SHOW:
				p.state = EntryPoint.STATE_RLEVEL_REMEMBER;
				break;
			case EntryPoint.STATE_RLEVEL_REMEMBER:
				p.state = EntryPoint.STATE_RLEVEL_LOW;
				break;
			case EntryPoint.STATE_RLEVEL_LOW:
				p.state = EntryPoint.STATE_RLEVEL_FORGOT;
				break;
		}
		pa.notifyDataSetChanged();
	}
}
