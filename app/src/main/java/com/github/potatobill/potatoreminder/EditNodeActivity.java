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

public class EditNodeActivity extends Activity
{
	App app;
	int id;
	TextView tvPosition;
	EditText etTitle;
	EditText etHint;
	EditText etPoint;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		app = (App) getApplicationContext();
		id = getIntent().getExtras().getInt("id");
		setContentView(R.layout.activity_edit_node);
		tvPosition = (TextView) findViewById(R.id.editTextViewPosition);
		etTitle = (EditText) findViewById(R.id.editEditTextTitle);
		etHint = (EditText) findViewById(R.id.editEditTextHint);
		etPoint = (EditText) findViewById(R.id.editEditTextPoint);
		if (savedInstanceState!=null)
		{
			etTitle.setText(savedInstanceState.getString("title"));
			etHint.setText(savedInstanceState.getString("hint"));
			etPoint.setText(savedInstanceState.getString("point"));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		// TODO: Implement this method
		super.onSaveInstanceState(outState);
		String title = etTitle.getText().toString();
		String hint = etHint.getText().toString();
		String point = point = etPoint.getText().toString();
		outState.putString("title",title);
		outState.putString("hint",hint);
		outState.putString("point",point);
	}

	@Override
	protected void onResume()
	{
		// TODO: Implement this method
		super.onResume();
		tvPosition.setText(app.dm.getPositionString(id) + ">");
		if (app.firstIn)
		{
			Bundle b = app.dm.readEntry(id);
			etTitle.setText(b.getString("title"));
			etHint.setText(b.getString("hint"));
			etPoint.setText(b.getString("point"));
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
		// TODO: Implement this method
		String t = etTitle.getText().toString();
		String h = etHint.getText().toString();
		String p = etPoint.getText().toString();
		switch (item.getItemId())
		{
			case R.id.editMenuConfirm:
				confirm(t,h,p,false);
				break;
			case R.id.editMenuDelete:
				if (app.dm.haveChild(id))
				{
					String tips = "该文件夹下还有子项，是否继续删除？";
					ResponseBuilder.create(this,tips,dConfirm).show();
				} else 
				{
					app.dm.deleteEntry(id);
					showToast("删除成功");
					finish();
				}
				break;
			case R.id.editMenuToNode:
				showToast("已经是文件夹");
				break;
			case R.id.editMenuCopy:
			case R.id.editMenuToPoint:
			case R.id.editMenuToList:
				showToast("功能未实现");
				break;
			case R.id.editMenuMove:
				Intent intent = new Intent(this,MoveActivity.class);
				intent.putExtra("type", EntryData.TYPE_NODE);
				intent.putExtra("id", id);
				startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	public void confirm(String t,String h,String p,boolean skip)
	{
		int state;
		state = app.dm.updateNode(id,t,h,p,skip);
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
			case DataManager.POINT_LONG:
				String tips = "知识点内容超过100字 可能不利于记忆"+System.getProperty("line.separator")+"是否继续编辑？";
				ResponseBuilder.create(this,tips,cConfirm).show();
				break;
		}
	}

	private DialogInterface.OnClickListener dConfirm = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface p1, int p2)
		{
			switch (p2)
			{
				case DialogInterface.BUTTON_POSITIVE:
					app.dm.deleteAll(id);
					finish();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					showToast("删除取消");
					break;
			}
		}
	};

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
					confirm(t,h,p,true);
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					showToast("编辑取消");
					break;
			}
		}
	};
}
