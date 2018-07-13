package com.github.potatobill.potatoreminder;

import android.app.Activity;
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
import com.github.potatobill.potatoreminder.data.EntryAdapter;
import com.github.potatobill.potatoreminder.data.EntryData;

import java.util.ArrayList;
import android.app.ProgressDialog;

import com.github.potatobill.potatoreminder.data.Machine;
import com.github.potatobill.potatoreminder.data.DataHandler_2;
import android.os.Message;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

public class MainActivity extends Activity implements Machine.Settable
{
	App app;
	ListView lvContext;
	TextView tvPosition;
	Button btStudy;
	Button btExport;
	
	private ProgressDialog pdLoad;
	private MHandler mHandler;
	private Thread mThread;
	private Planner mPlanner;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (App) getApplication();
		Log.i("potatoreminder","main-onCreate");
		setContentView(R.layout.activity_main);
		lvContext = (ListView) findViewById(R.id.mainListViewEdit);
		btStudy = (Button) findViewById(R.id.mainButtonStudy);
		btExport = (Button) findViewById(R.id.mainButtonExport);
		tvPosition = (TextView) findViewById(R.id.mainTextViewPosition);
		LvContextClick lvccl = new LvContextClick();
		lvContext.setOnItemClickListener(lvccl);
		lvContext.setOnItemLongClickListener(lvccl);
		Button btTest = (Button) findViewById(R.id.mainButtonTest);
		btTest.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					//app.dm.logAll();
					//app.dm.down(MainActivity.this);
					//app.dm.temp();
				}
		});
		if (mHandler == null) mHandler = new MHandler();
		mPlanner = new Planner(mHandler);
		mPlanner.setApp((App) getApplicationContext());
		
		createProgressDialog();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.i("potatoreminder","main-resume");
		load();
	}

	private void load()
	{
		ArrayList<EntryData> data = new ArrayList<EntryData>();
		if (app.dm != null && app.dm.isOpen())
		{
			app.dm.loadByPosition(data, app.position);
			EntryAdapter ea = new EntryAdapter(this, data);
			lvContext.setAdapter(ea);
			tvPosition.setText(app.dm.getPositionString(app.position)+"/");
		} else
		{
			finish();
		}
		
	}
	
	class LvContextClick implements OnItemClickListener,OnItemLongClickListener
	{
		@Override
		public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
		{
			EntryData entry = (EntryData) lvContext.getItemAtPosition(p3);
			Intent t;
			Bundle b = new Bundle();
			switch (entry.type)
			{
				case EntryData.TYPE_NODE:
					app.firstIn = true;
					t = new Intent(MainActivity.this,EditNodeActivity.class);
					b.putInt("id",entry.id);
					b.putInt("position",entry.id);
					t.putExtras(b);
					startActivity(t);
					break;
				case EntryData.TYPE_LIST:
					app.firstIn = true;
					t = new Intent(MainActivity.this,EditListActivity.class);
					b.putInt("id",entry.id);
					t.putExtras(b);
					startActivity(t);
					break;
				case EntryData.TYPE_POINT:
					app.firstIn = true;
					t = new Intent(MainActivity.this,EditPointActivity.class);
					b.putInt("id",entry.id);
					t.putExtras(b);
					startActivity(t);
					break;
				case EntryData.TYPE_IMAGE:
					app.firstIn = true;
					t = new Intent(MainActivity.this,EditImageActivity.class);
					t.putExtra("id",entry.id);
					startActivity(t);
					break;
				case EntryData.TYPE_BACK:
					app.position=app.dm.getParentId(app.position);
					load();
					break;
				case EntryData.TYPE_NEW_NODE:
				case EntryData.TYPE_NEW_LIST:
				case EntryData.TYPE_NEW_POINT:
					b.putInt("type",entry.type);
					b.putInt("position",app.position);
					t = new Intent(MainActivity.this,NewActivity.class);
					t.putExtras(b);
					startActivity(t);
					break;
				case EntryData.TYPE_NEW_IMAGE:
					app.firstIn = true;
					startActivity(new Intent(MainActivity.this,NewImageActivity.class));
					break;
			}
			return true;
		}


		@Override
		public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
		{
			EntryData entry = (EntryData) lvContext.getItemAtPosition(p3);
			Intent t;
			Bundle b = new Bundle();
			switch (entry.type)
			{
				case EntryData.TYPE_NODE:
					app.position=entry.id;
					load();
					break;
				case EntryData.TYPE_LIST:
					app.firstIn = true;
					t = new Intent(MainActivity.this,EditListActivity.class);
					b.putInt("id",entry.id);
					t.putExtras(b);
					startActivity(t);
					break;
				case EntryData.TYPE_POINT:
					app.firstIn = true;
					t = new Intent(MainActivity.this,EditPointActivity.class);
					b.putInt("id",entry.id);
					t.putExtras(b);
					startActivity(t);
					break;
				case EntryData.TYPE_IMAGE:
					app.firstIn = true;
					t = new Intent(MainActivity.this,EditImageActivity.class);
					t.putExtra("id",entry.id);
					startActivity(t);
					break;
				case EntryData.TYPE_BACK:
					app.position=app.dm.getParentId(app.position);
					load();
					break;
				case EntryData.TYPE_NEW_NODE:
				case EntryData.TYPE_NEW_LIST:
				case EntryData.TYPE_NEW_POINT:
					b.putInt("type",entry.type);
					b.putInt("position",app.position);
					t = new Intent(MainActivity.this,NewActivity.class);
					t.putExtras(b);
					startActivity(t);
					break;
				case EntryData.TYPE_NEW_IMAGE:
					app.firstIn = true;
					startActivity(new Intent(MainActivity.this,NewImageActivity.class));
					break;
			}
		}
		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate main_menu.xml 
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.mainMenuStudy:
				pdLoad.setTitle("加载中");
				pdLoad.setMessage("正在加载");
				pdLoad.show();
				if (mThread == null)
				{
					mThread = new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							App app = (App) getApplicationContext();
							app.dm.setMachine();
							app.dm.getMachine().prepare(MainActivity.this,app.position);
							//app.dm.getMachine().LogAll();
							if (app.dm.getMachine().getEntry().getInt("type") == EntryData.TYPE_BACK) unjump();
							else jump();
						}
					});
					mThread.start();
				}
				app.firstIn = true;
				break;
			case R.id.mainMenuExport:
				Intent toExport = new Intent(this, BrowseActivity.class);
				toExport.putExtra("flag", Constant.MAIN_TO_EXPORT);
				startActivity(toExport);
				break;
			case R.id.mainMenuImport:
				Intent toImport = new Intent(this, BrowseActivity.class);
				toImport.putExtra("flag", Constant.MAIN_TO_IMPORT);
				startActivity(toImport);
				break;
			case R.id.mainMenuSearch:
				showToast("功能暂未开发 敬请期待");
				break;
			case R.id.mainMenuPlan:
				//TODO 实现计划功能
				pdLoad.setTitle("调整计划");
				pdLoad.setMessage("正在调整");
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				final EditText etInput = new EditText(this);
				etInput.setFocusable(true);
				final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								String input = etInput.getText().toString();
								try {
									int planNum = Integer.parseInt(input);
									mPlanner.setPlanNum(planNum);
									pdLoad.show();
									new Thread(mPlanner).start();
								} catch (NumberFormatException e) {
									showExcetion("您输入的不是数字哦");
								}
								break;
							case DialogInterface.BUTTON_NEGATIVE:
								dialog.dismiss();
						}
					}
				};
				builder.setTitle("请输入计划").setIcon(R.drawable.ic_launcher)
					.setPositiveButton("确定",listener).setNegativeButton("取消",listener)
					.setMessage("我计划每天学习…个").setView(etInput).create().show();
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void createProgressDialog() {
		pdLoad = new ProgressDialog(this);
		pdLoad.setIcon(R.drawable.ic_launcher);
		pdLoad.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pdLoad.setMax(100);
		pdLoad.setProgress(0);
		pdLoad.setCancelable(false);
	}
	
	@Override
	public void setBoth(final int progress,final String state)
	{
		if (pdLoad != null)
		{
			mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					pdLoad.setProgress(progress);
					pdLoad.setMessage(state);
				}
			});
		}
	}
	
	private void jump()
	{
		mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					pdLoad.dismiss();
					mThread = null;
					Intent t = new Intent(MainActivity.this,StudyActivity.class);
					startActivity(t);
				}
			});
	}
	
	private void unjump()
	{
		mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					pdLoad.dismiss();
					mThread = null;
					showToast("这里暂时还没有要复习的知识哦");
				}
			});
	}
	
	private void showToast(String text)
	{
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
			if (app.position==0)
			{
				if((System.currentTimeMillis()-exitTime) > 2000){  
					Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
					exitTime = System.currentTimeMillis();   
				} else {
					app.dm.close();
					app.position = 0;
					finish();
					System.exit(0);
				}
				return true;   
			} else
			{
				app.position = app.dm.getParentId(app.position);
				load();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private class Planner implements Runnable, DataHandler_2.Settable
	{
		private App app;
		private Handler mHandler;
		private int planNum;
		private int progress;
		private String state;

		@Override
		public void run()
		{
			try {
				app.dm.setPlan(app.position, planNum, this);
				mHandler.sendEmptyMessage(Constant.Main.MHANDLE_HIDE_PROGRESS_DIALOG);

			} catch (Exception e) {
				e.printStackTrace();
				Message exceptionMessage = Message.obtain();
				exceptionMessage.what = Constant.Main.MHANDLE_SHOW_EXCEPTION;
				exceptionMessage.obj = e.getMessage();
				mHandler.sendMessage(exceptionMessage);
			}
		}

		@Override
		public void setBoth(int progress, String state)
		{
			this.progress = progress;
			this.state = state;
			mHandler.sendEmptyMessage(Constant.Main.MHANDLE_SHOW_PLAN_PROGRESS);
		}

		public int getProgress()
		{
			return progress;
		}

		public String getState()
		{
			return state;
		}
		public void setPlanNum(int planNum) {
			this.planNum = planNum;
		}

		public void setApp(App app) {
			this.app = app;
		}

		public Planner(Handler handler) {
			this.mHandler = handler;
		}
	}
	
	private void showExcetion(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_launcher).setTitle("发生错误")
			.setMessage("错误信息:" + message).setPositiveButton("确定", null).show();
	}
	
	private class MHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what) {
				case Constant.Main.MHANDLE_SHOW_EXCEPTION:
					showExcetion((String) msg.obj);
					pdLoad.dismiss();
					break;
				case Constant.Main.MHANDLE_SHOW_PLAN_PROGRESS:
					pdLoad.setProgress(mPlanner.getProgress());
					pdLoad.setMessage(mPlanner.getState());
					break;
				case Constant.Main.MHANDLE_HIDE_PROGRESS_DIALOG:
					pdLoad.dismiss();
					break;
				default:
					showExcetion("意外的处理信息");
					break;
			}
		}
	}
}
