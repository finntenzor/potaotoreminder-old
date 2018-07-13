/**
 * @author potatobill
 * 作者 potatobill
 * qq : 1535058704
 * 陕西省汉中中学 高三（19）班 董江彬
 * 遵循 Apache Licene 2.0 协议
 * 注：由于开源性质，无法提供更多个人信息，请验证个人信息的工作人员谅解
 */
package com.github.potatobill.potatoreminder;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.github.potatobill.potatoreminder.data.DataManager;
import com.github.potatobill.potatoreminder.data.EntryData;
import com.github.potatobill.potatoreminder.data.EntryPoint;
import java.lang.ref.WeakReference;
import android.os.Environment;
import java.io.File;
import android.widget.Toast;

public class WelcomeActivity extends Activity
{
	private LoadHandler mLoadHandler;
	private Thread mLoadThread;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		TextView tvS = (TextView) findViewById(R.id.welcomeTextViewState);
		ProgressBar pbP = (ProgressBar) findViewById(R.id.welcomeProgressBarProgress);
		if (mLoadHandler == null) mLoadHandler = new LoadHandler(this, tvS, pbP);
		if (mLoadThread == null)
		{
			mLoadThread = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						App app = (App) getApplicationContext();
						app.dm = new DataManager(new OpenHelper(app));
						String p = Environment.getExternalStorageDirectory().getPath();
						app.imagePath = new File(p,"PotatoReminder");
						boolean check;
						if (app.imagePath.exists())
						{
							if (app.imagePath.isDirectory())
							{
								if (app.imagePath.canRead()&&app.imagePath.canWrite())
								{
									check = true;
								} else
								{
									sendToast("图片存储目录无法读写 请检查目录"+app.imagePath.getPath()+"是否正常");
									check = false;
								}
							} else
							{
								sendToast("图片存储目录被占用 请检查目录"+app.imagePath.getPath()+"是否正常");
								check = false;
							}
						} else
						{
							if (app.imagePath.mkdirs())
							{
								if (app.imagePath.canRead()&&app.imagePath.canWrite())
								{
									check = true;
								} else
								{
									sendToast("图片存储目录无法读写 请检查目录"+app.imagePath.getPath()+"是否正常");
									check = false;
								}
							} else
							{
								sendToast("图片存储目录无法创建 请检查目录"+app.imagePath.getPath()+"是否正常");
								check = false;
							}
						}
						try
						{
							Thread.currentThread().sleep(1000);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
						//TODO 加入长时监测 超过500ms未完成 则显示进度条，并实时监测进度
						if (check)
						{
							jump();
						} else
						{
							over();
						}
					}
			});
			mLoadThread.start();
		}
		//TODO 每次启动显示不同的tips
	}
	
	private static class LoadHandler extends Handler
	{
		public final static int NONE = 0;
		public final static int SHOW = 1;
		public final static int HIDE = 2;
		public final static int SET_STATE = 3;
		public final static int SET_BOTH = 4;
		public final static int SHOW_TOAST = 5;
		private final WeakReference<TextView> tvS;
		private final WeakReference<ProgressBar> pbP;
		private final WeakReference<Context> ct;
		LoadHandler(Context context, TextView State, ProgressBar Progress)
		{
			tvS = new WeakReference<TextView>(State);
			pbP = new WeakReference<ProgressBar>(Progress);
			ct = new WeakReference<Context>(context);
		}
		@Override
		public void handleMessage(Message msg)
		{
			//handleMessage
			Bundle b;
			switch (msg.what)
			{
				case NONE:
					Log.w("potatoreminder","Welcome-LoadHandler-get a none message");
					break;
				case SHOW:
					tvS.get().setVisibility(View.VISIBLE);
					pbP.get().setVisibility(View.VISIBLE);
					break;
				case HIDE:
					tvS.get().setVisibility(View.GONE);
					pbP.get().setVisibility(View.GONE);
					break;
				case SET_STATE:
					tvS.get().setText((String) msg.obj);
					break;
				case SET_BOTH:
					b = (Bundle) msg.obj;
					tvS.get().setText(b.getString("state"));
					pbP.get().setProgress(b.getInt("progress"));
					break;
				case SHOW_TOAST:
					showToast(ct.get(), (String) msg.obj);
					break;
				default:
					Log.e("potatoreminder","Welcome-LoadHandler-get an unreadable message");
			}
		}
	}
	
	void showBoth()
	{
		Message msg = Message.obtain();
		msg.what = LoadHandler.SHOW;
		mLoadHandler.sendMessage(msg);
	}
	
	void hideBoth()
	{
		Message msg = Message.obtain();
		msg.what = LoadHandler.HIDE;
		mLoadHandler.sendMessage(msg);
	}
	
	void setState(String state)
	{
		Message msg = Message.obtain();
		msg.what = LoadHandler.SET_STATE;
		msg.obj = state;
		mLoadHandler.sendMessage(msg);
	}
	
	void setBoth(int progress, String state)
	{
		Message msg = Message.obtain();
		msg.what = LoadHandler.SET_BOTH;
		Bundle b = new Bundle();
		b.putString("state",state);
		b.putInt("progress",progress);
		msg.obj = b;
		mLoadHandler.sendMessage(msg);
	}
	
	void jump()
	{
		mLoadHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
					finish();
				}
			});
	}
	
	void over()
	{
		mLoadHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					App app = (App) getApplicationContext();
					app.dm.close();
					app.position = 0;
					finish();
					System.exit(0);
				}
			});
	}
	
	void sendToast(String text)
	{
		Message msg = Message.obtain();
		msg.what = LoadHandler.SHOW_TOAST;
		msg.obj = text;
		mLoadHandler.sendMessage(msg);
	}
	
	private static void showToast(Context context, String text)
	{
		Toast.makeText(context ,text,Toast.LENGTH_SHORT).show();
	}
	
	private class OpenHelper extends SQLiteOpenHelper
	{
		public final static String DBNAME = "point";
		public final static int VERSION = 2;
		public final static String[] COLUMNS_1 = {"id","parent","title","hint_text","point_text","last_time","level"};
		public final static String COLUMNS_2_S = "(id integer primary key,pid integer,title nvarchar(20),type integer,hint text,point text,create_time long,last_time long,last_state integer,key integer,level real)";
		
		//TODO 将来改为PotatoReminder 或者软件更名以后再改
		OpenHelper(Context context)
		{
			super(context, DBNAME, null,VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL("create table point" + COLUMNS_2_S);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			if (oldVersion == 1)
			{
				if (newVersion == 2)
				{
					//TODO 将字符串放置在其他地方
					showBoth();
					setBoth(0,"开始更新");
					db.execSQL("alter table 'point' rename to 'point_old'");
					setBoth(2, "更新数据结构 - 1/2");
					db.execSQL("create table point" + COLUMNS_2_S);
					setBoth(4, "更新数据结构 - 2/2");
					Cursor cursor = db.query("point_old", COLUMNS_1, null, null, null, null, null);
					setBoth(6, "获取数据 ");
					int num = cursor.getCount();
					ContentValues cv = new ContentValues();
					cursor.moveToFirst();
					int p;
					for (int i = 0; i < num; i++)
					{
						transLine(db, cursor, cv);
						p = 6 + (i*91) / num;
						db.insert("point", null, cv);
						setBoth(p,"更新数据 - " + i + "/" + num);
						cursor.moveToNext();
					}
					cursor.close();
					setBoth(97, "数据更新完毕");
					db.execSQL("drop table point_old");
					setBoth(100,"升级完毕");
					hideBoth();
				} else
				{
					throw new RuntimeException("Welcome-OpenHelper-newVersion超出范围");
				}
			} else
			{
				throw new RuntimeException("Welcome-OpenHelper-oldVersion超出范围");
			}
		}
	}
	
	private static void transLine(SQLiteDatabase db, Cursor cursor, ContentValues cv)
	{
		cv.put("id", cursor.getInt(0));
		cv.put("pid", cursor.getInt(1));
		cv.put("title", cursor.getString(2));
		cv.put("type", haveChild(db, cursor.getInt(0)));
		cv.put("hint", cursor.getString(3));
		cv.put("point", cursor.getString(4));
		cv.put("create_time", System.currentTimeMillis());
		cv.put("last_time", cursor.getString(5));
		cv.put("last_state", EntryPoint.STATE_CREATE);
		cv.put("key", 0);
		cv.put("level", cursor.getDouble(6));
	}
	
	private static int haveChild(SQLiteDatabase db, int id)
	{
		String[] cols = {String.valueOf(id)};
		Cursor cursor = db.query("point_old", null, "parent=?", cols, null, null, null);
		int num = cursor.getCount();
		cursor.close();
		if (num == 0) return EntryData.TYPE_NODE;
		else return EntryData.TYPE_POINT;
	}
}
