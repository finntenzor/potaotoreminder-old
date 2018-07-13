package com.github.potatobill.potatoreminder;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.EditText;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.github.potatobill.potatoreminder.data.DataManager;
import com.github.potatobill.potatoreminder.data.EntryData;

import android.content.DialogInterface;
import android.app.Dialog;
import android.util.Log;

public class EditPointActivity extends Activity
{
	App app;
	int id;
	TextView tvPosition;
	EditText etTitle;
	EditText etHint;
	EditText etPoint;
	EditText etLevel;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//long a = System.currentTimeMillis();
		app = (App) getApplicationContext();
		id = getIntent().getExtras().getInt("id");
		setContentView(R.layout.activity_edit_point);
		tvPosition = (TextView) findViewById(R.id.editTextViewPosition);
		etTitle = (EditText) findViewById(R.id.editEditTextTitle);
		etHint = (EditText) findViewById(R.id.editEditTextHint);
		etPoint = (EditText) findViewById(R.id.editEditTextPoint);
		etLevel = (EditText) findViewById(R.id.editEditTextLevel);
		//long b = System.currentTimeMillis();
		//Log.i("potatoreminder",String.valueOf(b-a));
		if (savedInstanceState!=null)
		{
			etTitle.setText(savedInstanceState.getString("title"));
			etHint.setText(savedInstanceState.getString("hint"));
			etPoint.setText(savedInstanceState.getString("point"));
			etLevel.setText(savedInstanceState.getString("level"));
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		String title = etTitle.getText().toString();
		String hint = etHint.getText().toString();
		String point = point = etPoint.getText().toString();
		String level = etLevel.getText().toString();
		outState.putString("title",title);
		outState.putString("hint",hint);
		outState.putString("point",point);
		outState.putString("level",level);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		tvPosition.setText(app.dm.getPositionString(id) + ">");
		if (app.firstIn)
		{
			Bundle b = app.dm.readEntry(id);
			etTitle.setText(b.getString("title"));
			etHint.setText(b.getString("hint"));
			etLevel.setText(String.valueOf(b.getDouble("level")));
			etPoint.setText(b.getString("point"));
			//Log.e("potatoreminder",String.valueOf(b.getLong("last_time")));
			app.firstIn = false;
		}
	}
	
	private void showToast(String text)
	{
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate main_menu.xml 
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		String t = etTitle.getText().toString();
		String h = etHint.getText().toString();
		String p = etPoint.getText().toString();
		String rl = etLevel.getText().toString();
		double l;
		switch (item.getItemId())
		{
			case R.id.editMenuConfirm:
				try
				{
					l = Double.parseDouble(rl);
					confirm(t,h,p,l,false);
				}
				catch (NumberFormatException e)
				{
					showToast("熟练度不是数字");
				}
				break;
			case R.id.editMenuDelete:
				app.dm.deleteEntry(id);
				showToast("删除成功");
				finish();
				break;
			case R.id.editMenuCopy:
			case R.id.editMenuToList:
			case R.id.editMenuToNode:
				showToast("功能未实现");
				break;
			case R.id.editMenuToPoint:
				showToast("已经是知识点");
				break;
			case R.id.editMenuMove:
				Intent intent = new Intent(this,MoveActivity.class);
				intent.putExtra("type", EntryData.TYPE_POINT);
				intent.putExtra("id", id);
				startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void confirm(String t,String h,String p,double l,boolean skip)
	{
		int state;
		state = app.dm.updatePoint(id,t,h,p,l,skip);
		switch (state)
		{
			case DataManager.SUCCEED:
				showToast("编辑成功");
				finish();
				break;
			case DataManager.FAILED:
				showToast("发生未知错误 新建失败");
				break;
			case DataManager.TITLE_SHORT:
				showToast("标题不能为空 请输入标题");
				break;
			case DataManager.TITLE_LONG:
				showToast("标题太长 请将标题控制在20字以内");
				break;
			case DataManager.HINT_LONG:
				showToast("提示语太长 请将提示语控制在20字以内");
				break;
			case DataManager.POINT_TOO_LONG:
				showToast("知识点内容太长 请将知识点内容控制在2000字以内");
				break;
			case DataManager.LEVEL_LOW:
			case DataManager.LEVEL_HIGH:
				showToast("熟悉度应该是一个0~10之间的数字");
				break;
			case DataManager.POINT_LONG:
				String tips = "知识点内容超过100字 可能不利于记忆"+System.getProperty("line.separator")+"是否继续编辑？";
				ResponseBuilder.create(this,tips,cConfirm).show();
				break;
		}
	}
	
	private DialogInterface.OnClickListener cConfirm = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface p1, int p2)
		{
			switch (p2)
			{
				case DialogInterface.BUTTON_POSITIVE:
					String t = etTitle.getText().toString();
					String h = etHint.getText().toString();
					String p = etPoint.getText().toString();
					String rl = etLevel.getText().toString();
					double l = Double.parseDouble(rl);
					confirm(t,h,p,l,true);
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					showToast("编辑取消");
					break;
			}
		}
	};
}
