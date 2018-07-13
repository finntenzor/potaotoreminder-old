package com.github.potatobill.potatoreminder;
import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.github.potatobill.potatoreminder.data.DataManager;
import android.widget.Toast;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.github.potatobill.potatoreminder.data.EntryData;
import android.widget.TextView;

public class NewActivity extends Activity
{
	App app;
	int position;
	TextView tvPosition;
	EditText etTitle;
	EditText etHint;
	EditText etPoint;
	int type;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new);
		app = (App) getApplicationContext();
		position = getIntent().getExtras().getInt("position");
		tvPosition = (TextView) findViewById(R.id.newTextViewPosition);
		etTitle = (EditText) findViewById(R.id.newEditTextTitle);
		etHint = (EditText) findViewById(R.id.newEditTextHint);
		etPoint = (EditText) findViewById(R.id.newEditTextPoint);
		type = getIntent().getExtras().getInt("type");
		if (savedInstanceState!=null)
		{
			String title = savedInstanceState.getString("title");
			String hint = savedInstanceState.getString("hint");
			String point = savedInstanceState.getString("point");
			etTitle.setText(title);
			etHint.setHint(hint);
			etPoint.setText(point);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		// TODO: Implement this method
		super.onSaveInstanceState(outState);
		outState.putString("title",etTitle.getText().toString());
		outState.putString("hint",etHint.getText().toString());
		outState.putString("point",etPoint.getText().toString());
	}

	@Override
	protected void onResume()
	{
		// TODO: Implement this method
		super.onResume();
		String t = app.dm.getPositionString(position);
		switch (type)
		{
			case EntryData.TYPE_NEW_LIST:
				t = t + "/>新建列表";
				break;
			case EntryData.TYPE_NEW_NODE:
				t = t + "/>新建文件夹";
				break;
			case EntryData.TYPE_NEW_POINT:
				t = t + "/>新建知识点";
				break;
		}
		tvPosition.setText(t);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate main_menu.xml 
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu._new, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// TODO: Implement this method
		switch (item.getItemId())
		{
			case R.id.mainMenuNew:
				//Toast.makeText(this,item.getTitle(),Toast.LENGTH_SHORT).show();
				String t = etTitle.getText().toString();
				String h = etHint.getText().toString();
				String p = etPoint.getText().toString();
				int state = app.dm.insertEntry(position,t,type,h,p,false,false);
				switch (state)
				{
					case DataManager.SUCCEED:
						showToast("新建成功");
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
						showPointLong();
						break;
					case DataManager.POINT_LIST:
						showPointList();
						break;
				}
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	private void showPointLong()
	{
		final String sep = System.getProperty("line.separator");
		final Dialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle("提示");
		builder.setMessage("知识点内容超过100字 可能不利于记忆"+sep+"是否继续创建？");
		DialogInterface.OnClickListener ocl =  new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// TODO: Implement this method
				switch (which)
				{
					case Dialog.BUTTON_POSITIVE:
						String t = etTitle.getText().toString();
						String h = etHint.getText().toString();
						String p = etPoint.getText().toString();
						int state = app.dm.insertEntry(position,t,type,h,p,true,false);
						switch (state)
						{
							case DataManager.SUCCEED:
								showToast("新建成功");
								finish();
								break;
							case DataManager.POINT_LIST:
								showPointList();
								break;
							default:
								showToast("发生未知错误 新建失败");
								break;
						}
						break;
					case Dialog.BUTTON_NEGATIVE:
						showToast("新建取消");
						break;
				}
			}
		};
		builder.setPositiveButton("确认",ocl);
		builder.setNegativeButton("取消",ocl);
		dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();
	}
	private void showPointList()
	{
		final Dialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle("提示");
		builder.setMessage("知识点内容含有回车 是否转为创建列表？");
		DialogInterface.OnClickListener ocl =  new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// TODO: Implement this method
				String t = etTitle.getText().toString();
				String h = etHint.getText().toString();
				String p = etPoint.getText().toString();
				int state;
				switch (which)
				{
					case Dialog.BUTTON_POSITIVE:
						state = app.dm.insertEntry(position,t,EntryData.TYPE_NEW_LIST,h,p,true,true);
						if (state == DataManager.SUCCEED)
						{
							showToast("新建成功");
							finish();
						} else
						{
							showToast("发生未知错误 新建失败");
						}
						break;
					case Dialog.BUTTON_NEUTRAL:
						state = app.dm.insertEntry(position,t,EntryData.TYPE_NEW_POINT,h,p,true,true);
						if (state == DataManager.SUCCEED)
						{
							showToast("新建成功");
							finish();
						} else
						{
							showToast("发生未知错误 新建失败");
						}
						break;
					case Dialog.BUTTON_NEGATIVE:
						showToast("新建取消");
						break;
				}
			}
		};
		builder.setPositiveButton("创建列表",ocl);
		builder.setNeutralButton("创建知识点",ocl);
		builder.setNegativeButton("取消",ocl);
		dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();
	}
	private void showToast(String text)
	{
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
}
