package com.github.potatobill.potatoreminder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.potatobill.potatoreminder.data.DataManager;
import com.github.potatobill.potatoreminder.data.EntryAdapter;
import com.github.potatobill.potatoreminder.data.EntryData;
import com.github.potatobill.potatoreminder.data.Machine;

import java.util.ArrayList;

public class MoveActivity extends Activity
{
	App app;
	ListView lvContext;
	TextView tvPosition;
	int position;
	int type;
	int id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (app==null) app = (App) getApplication();
		setContentView(R.layout.activity_move);
		lvContext = (ListView) findViewById(R.id.moveListViewEdit);
		tvPosition = (TextView) findViewById(R.id.moveTextViewPosition);
		LvContextClick lvccl = new LvContextClick();
		lvContext.setOnItemClickListener(lvccl);
		Bundle b= getIntent().getExtras();
		if (b!=null)
		{
			type = b.getInt("type");
			id = b.getInt("id");
		}
		if (savedInstanceState == null)
		{
			position = app.position;
		} else
		{
			position = savedInstanceState.getInt("position");
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("position",position);
	}

	@Override
	protected void onResume() {
		super.onResume();
		load();
	}

	private void load()
	{
		ArrayList<EntryData> data = new ArrayList<EntryData>();
		if (app.dm!=null&&app.dm.isOpen())
		{
			app.dm.loadMoveByPosition(data, position);
			EntryAdapter ea = new EntryAdapter(this, data);
			lvContext.setAdapter(ea);
			tvPosition.setText(app.dm.getPositionString(position)+"/");
		} else
		{
			finish();
		}
		
	}
	
	class LvContextClick implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
		{
			EntryData entry = (EntryData) lvContext.getItemAtPosition(p3);
			switch (type)
			{
				case EntryData.TYPE_IMAGE:
				case EntryData.TYPE_POINT:
					switch (entry.type)
					{
						case EntryData.TYPE_LIST:
						case EntryData.TYPE_NODE:
							position=entry.id;
							load();
							break;
						case EntryData.TYPE_BACK:
							position=app.dm.getParentId(position);
							load();
							break;
					}
					break;
				case EntryData.TYPE_LIST:
				case EntryData.TYPE_NODE:
					switch (entry.type)
					{
						case EntryData.TYPE_LIST:
							showToast("不可以移动到列表下");
							break;
						case EntryData.TYPE_NODE:
							if (entry.id == id)
							{
								showToast("不可以移动到自己的子目录");
							} else
							{
								position=entry.id;
								load();
							}
							break;
						case EntryData.TYPE_BACK:
							position=app.dm.getParentId(position);
							load();
							break;
					}
					break;
			}
		}
		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.move, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.moveMenuConfirm:
				if (position == app.dm.getParentId(id))
				{
					showToast("不能移动到和原来相同的位置");
				} else if (app.dm.moveEntry(id,position) == DataManager.SUCCEED)
				{
					showToast("移动成功");
					finish();
				} else
				{
					showToast("不知原因 移动失败");
				}

		}
		return super.onOptionsItemSelected(item);
	}
	
	private void showToast(String text)
	{
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

}
