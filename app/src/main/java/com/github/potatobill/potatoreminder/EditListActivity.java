package com.github.potatobill.potatoreminder;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.EditText;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.github.potatobill.potatoreminder.data.DataManager;
import android.content.DialogInterface;
import android.app.Dialog;
import android.widget.ListView;
import java.util.ArrayList;
import com.github.potatobill.potatoreminder.data.EntryData;
import com.github.potatobill.potatoreminder.data.EntryAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.content.Intent;
import android.view.View;

public class EditListActivity extends Activity
{
	App app;
	int id;
	TextView tvPosition;
	EditText etTitle;
	EditText etHint;
	ListView lvContext;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		app = (App) getApplicationContext();
		id = getIntent().getExtras().getInt("id");
		setContentView(R.layout.activity_edit_list);
		tvPosition = (TextView) findViewById(R.id.editTextViewPosition);
		etTitle = (EditText) findViewById(R.id.editEditTextTitle);
		etHint = (EditText) findViewById(R.id.editEditTextHint);
		lvContext = (ListView) findViewById(R.id.editListViewContext);
		lvContext.setOnItemClickListener(new LvContextClick());
		if (savedInstanceState!=null)
		{
			etTitle.setText(savedInstanceState.getString("title"));
			etHint.setText(savedInstanceState.getString("hint"));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		// TODO: Implement this method
		super.onSaveInstanceState(outState);
		String title = etTitle.getText().toString();
		String hint = etHint.getText().toString();
		outState.putString("title",title);
		outState.putString("hint",hint);
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
		}
		load();
	}
	
	private void load()
	{
		ArrayList<EntryData> data = new ArrayList<EntryData>();
		app.dm.loadPointsByPosition(data, id);
		EntryAdapter ea = new EntryAdapter(this, data);
		lvContext.setAdapter(ea);
	}

	class LvContextClick implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
		{
			EntryData entry = (EntryData) lvContext.getItemAtPosition(p3);
			Intent t;
			Bundle b = new Bundle();
			switch (entry.type)
			{
				case EntryData.TYPE_POINT:
					t = new Intent(EditListActivity.this,EditPointActivity.class);
					app.firstIn = true;
					b.putInt("id",entry.id);
					b.putInt("position",id);
					t.putExtras(b);
					startActivity(t);
					break;
				case EntryData.TYPE_NEW_POINT:
					b.putInt("type",entry.type);
					b.putInt("position",id);
					t = new Intent(EditListActivity.this,NewActivity.class);
					t.putExtras(b);
					startActivity(t);
					break;
			}
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
		switch (item.getItemId())
		{
			case R.id.editMenuConfirm:
				confirm(t,h);
				break;
			case R.id.editMenuDelete:
				if (app.dm.haveChild(id))
				{
					String tips = "该列表下还有子项，是否继续删除？";
					ResponseBuilder.create(this,tips,dConfirm).show();
				} else
				{
					app.dm.deleteEntry(id);
					showToast("删除成功");
					finish();
				}
				break;
			case R.id.editMenuToList:
				showToast("已经是列表");
				break;
			case R.id.editMenuCopy:
			case R.id.editMenuToNode:
			case R.id.editMenuToPoint:
				showToast("功能未实现");
				break;
			case R.id.editMenuMove:
				Intent intent = new Intent(this,MoveActivity.class);
				intent.putExtra("type", EntryData.TYPE_LIST);
				intent.putExtra("id", id);
				startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	private DialogInterface.OnClickListener dConfirm = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface p1, int p2)
		{
			switch (p2)
			{
				case DialogInterface.BUTTON_POSITIVE:
					app.dm.deleteList(id);
					finish();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					showToast("删除取消");
					break;
			}
		}
	};

	public void confirm(String t,String h)
	{
		int state;
		state = app.dm.updateList(id,t,h);
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
		}
	}
	
}
