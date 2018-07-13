package com.github.potatobill.potatoreminder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.github.potatobill.potatoreminder.data.DataHandler_2;
import java.io.File;
import java.util.ArrayList;
import android.app.ProgressDialog;
import android.os.Handler;
import android.app.Dialog;
import android.os.Message;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class BrowseActivity extends Activity implements OnItemClickListener {
	TextView tvPosition;
	ListView lvBrowse;
	File filePosition;
	private AlertDialog inputDialog;
	private AlertDialog confirmDialog;
	private ProgressDialog pdProgress;
	private Handler mHandler;
	private Exporter mExporter;
	private Importer mImporter;
	ArrayList<BrowseEntry> data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse);
		// 添加控件引用
		tvPosition = (TextView) findViewById(R.id.browseTextViewPosition);
		lvBrowse = (ListView) findViewById(R.id.browseListView);
		lvBrowse.setOnItemClickListener(this);
		if (savedInstanceState == null) {
			filePosition = Environment.getExternalStorageDirectory();
		} else {
			filePosition = (File) savedInstanceState.getSerializable("filePosition");
		}
		// 创建对话框
		createInputDialog();
		createProgressDialog();
		createConfirmDialog();
		mHandler = new MHandler();
		mExporter = new Exporter(mHandler);
		mExporter.setApp((App) getApplicationContext());
		mImporter = new Importer(mHandler);
		mImporter.setApp((App) getApplicationContext());
	}

	@Override
	protected void onResume() {
		super.onResume();
		showDir();
	}

	@Override
	public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
		// Toast.makeText(this, String.valueOf(p3), Toast.LENGTH_SHORT).show();
		BrowseEntry be = data.get(p3);
		switch (be.getType()) {
		case BrowseEntry.TYPE_BACK:
			filePosition = filePosition.getParentFile();
			showDir();
			break;
		case BrowseEntry.TYPE_DIRECTORY:
			filePosition = new File(filePosition, be.getName());
			showDir();
			break;
		case BrowseEntry.TYPE_FILE:
			App app = (App) getApplicationContext();
			SQLiteDatabase importDb = null;
			try {
				importDb = app.dm.openImportDb(new File(filePosition, be.getName()));
			} catch (RuntimeException e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
			if (importDb != null) {
				mImporter.setImportDb(importDb);
				confirmDialog.show();
			}

		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("filePosition", filePosition);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.browse, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.browseMenuExport:
			if (filePosition.canWrite()) {
				inputDialog.show();
			} else {
				showExcetion("真可惜，权限不足 这里不能保存");
			}
			break;
		case R.id.browseMenuRefresh:
			showDir();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void createConfirmDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					dialog.dismiss();
					pdProgress.show();
					new Thread(mImporter).start();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					dialog.dismiss();
				}
			}
		};
		App app = (App) getApplicationContext();
		confirmDialog = builder.setTitle("确认导入").setIcon(R.drawable.ic_launcher).setPositiveButton("确定", listener)
				.setNegativeButton("取消", listener)
				.setMessage("即将导入到以下位置:" + app.dm.getPositionString(app.position)).create();
	}

	private void createInputDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final EditText etInput = new EditText(this);
		etInput.setFocusable(true);
		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					String intendName = etInput.getText().toString() + ".db";
					File intendFile = new File(filePosition, intendName);
					dialog.dismiss();
					if (intendFile.exists()) {
						showExcetion("这个名字已经被用过了 换一个吧");
					} else {
						pdProgress.show();
						mExporter.setFile(new File(filePosition, etInput.getText().toString() + ".db"));
						new Thread(mExporter).start();
					}
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					dialog.dismiss();
				}
			}
		};
		App app = (App) getApplicationContext();
		inputDialog = builder.setTitle("请输入导出文件名").setIcon(R.drawable.ic_launcher)
				.setPositiveButton("确定", listener).setNegativeButton("取消", listener)
				.setMessage("即将导出:" + app.dm.getPositionString(app.position)).setView(etInput).create();
	}

	private void createProgressDialog() {
		pdProgress = new ProgressDialog(this);
		pdProgress.setIcon(R.drawable.ic_launcher);
		pdProgress.setTitle("加载中");
		pdProgress.setMessage("正在加载");
		pdProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pdProgress.setMax(100);
		pdProgress.setProgress(0);
		pdProgress.setCancelable(false);
	}

	private final void showDir() {
		data = new ArrayList<BrowseEntry>();
		// 通过分类的方法排序
		ArrayList<BrowseEntry> tempHideDir = new ArrayList<BrowseEntry>();
		ArrayList<BrowseEntry> tempHideFile = new ArrayList<BrowseEntry>();
		ArrayList<BrowseEntry> tempDir = new ArrayList<BrowseEntry>();
		ArrayList<BrowseEntry> tempFile = new ArrayList<BrowseEntry>();
		// 添加返回
		File fileParent = filePosition.getParentFile();
		if (fileParent != null && fileParent.canRead()) {
			data.add(new BrowseEntry(BrowseEntry.TYPE_BACK, "返回上一层"));
		}

		// 获取子目录和文件
		File[] files = filePosition.listFiles();

		// 添加数据
		for (File f : files) {
			if (f.canRead()) {
				if (f.isDirectory()) {
					if (f.isHidden()) {
						tempHideDir.add(new BrowseEntry(BrowseEntry.TYPE_DIRECTORY, f.getName()));
					} else {
						tempDir.add(new BrowseEntry(BrowseEntry.TYPE_DIRECTORY, f.getName()));
					}
				} else if (f.isFile()) {
					if (f.isHidden()) {
						tempHideFile.add(new BrowseEntry(BrowseEntry.TYPE_FILE, f.getName()));
					} else {
						tempFile.add(new BrowseEntry(BrowseEntry.TYPE_FILE, f.getName()));
					}
				}
				// 不是文件夹不是文件则忽略
			}
			// 不可读则忽略
		}
		data.addAll(tempHideDir);
		data.addAll(tempDir);
		data.addAll(tempHideFile);
		data.addAll(tempFile);
		// 向ListView添加数据
		lvBrowse.setAdapter(new BrowseAdapter(this, data));
		// 显示路径
		tvPosition.setText(filePosition.getPath());
	}

	private void showExcetion(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_launcher).setTitle("发生错误").setMessage("错误信息:" + message)
				.setPositiveButton("确定", null).show();
	}

	private class Importer implements Runnable, DataHandler_2.Settable {
		private SQLiteDatabase importDb;
		private App app;
		private Handler mHandler;
		private int progress;
		private String state;

		@Override
		public void run() {
			try {
				app.dm.importData(importDb, app.position, this);
				mHandler.sendEmptyMessage(Constant.Browse.MHANDLE_HIDE_PROGRESS_DIALOG);
				mHandler.sendEmptyMessage(Constant.Browse.MHANDLE_FINSH);
			} catch (Exception e) {
				e.printStackTrace();
				Message exceptionMessage = Message.obtain();
				exceptionMessage.what = Constant.Browse.MHANDLE_SHOW_EXCEPTION;
				exceptionMessage.obj = e.getMessage();
				mHandler.sendMessage(exceptionMessage);
			}
		}

		@Override
		public void setBoth(int progress, String state) {
			this.progress = progress;
			this.state = state;
			mHandler.sendEmptyMessage(Constant.Browse.MHANDLE_SHOW_IMPORT_PROGRESS);
		}

		public int getProgress() {
			return progress;
		}

		public String getState() {
			return state;
		}

		public void setImportDb(SQLiteDatabase importDb) {
			this.importDb = importDb;
		}

		public void setApp(App app) {
			this.app = app;
		}

		public Importer(Handler handler) {
			this.mHandler = handler;
		}
	}

	private class Exporter implements Runnable, DataHandler_2.Settable {
		private File file;
		private App app;
		private Handler mHandler;
		private int progress;
		private String state;

		@Override
		public void run() {
			try {
				app.dm.exportData(file, this, app.position);
				mHandler.sendEmptyMessage(Constant.Browse.MHANDLE_HIDE_PROGRESS_DIALOG);
				mHandler.sendEmptyMessage(Constant.Browse.MHANDLE_REFRESH);
			} catch (Exception e) {
				e.printStackTrace();
				Message exceptionMessage = Message.obtain();
				exceptionMessage.what = Constant.Browse.MHANDLE_SHOW_EXCEPTION;
				exceptionMessage.obj = e.getMessage();
				mHandler.sendMessage(exceptionMessage);
			}
		}

		@Override
		public void setBoth(int progress, String state) {
			this.progress = progress;
			this.state = state;
			mHandler.sendEmptyMessage(Constant.Browse.MHANDLE_SHOW_EXPORT_PROGRESS);
		}

		public int getProgress() {
			return progress;
		}

		public String getState() {
			return state;
		}

		public void setFile(File file) {
			this.file = file;
		}

		public void setApp(App app) {
			this.app = app;
		}

		public Exporter(Handler handler) {
			this.mHandler = handler;
		}
	}

	private class MHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constant.Browse.MHANDLE_SHOW_EXCEPTION:
				showExcetion((String) msg.obj);
				pdProgress.dismiss();
				break;
			case Constant.Browse.MHANDLE_SHOW_EXPORT_PROGRESS:
				pdProgress.setProgress(mExporter.getProgress());
				pdProgress.setMessage(mExporter.getState());
				break;
			case Constant.Browse.MHANDLE_SHOW_IMPORT_PROGRESS:
				pdProgress.setProgress(mImporter.getProgress());
				pdProgress.setMessage(mImporter.getState());
				break;
			case Constant.Browse.MHANDLE_HIDE_PROGRESS_DIALOG:
				pdProgress.dismiss();
				break;
			case Constant.Browse.MHANDLE_REFRESH:
				showDir();
				break;
			case Constant.Browse.MHANDLE_FINSH:
				finish();
				break;
			default:
				showExcetion("意外的处理信息");
				break;
			}
		}

	}

	private class BrowseEntry {
		public final static int TYPE_BACK = 0;
		public final static int TYPE_FILE = 1;
		public final static int TYPE_DIRECTORY = 2;

		private int type;
		private String name;

		public BrowseEntry(int type, String name) {
			this.type = type;
			this.name = name;
		}

		public void setType(int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

	private class BrowseAdapter extends ArrayAdapter<BrowseEntry> {
		public BrowseAdapter(Context context, ArrayList<BrowseEntry> data) {
			super(context, R.layout.point, data);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// 获取viewholder
			ViewHolder vh;
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(getContext());
				convertView = inflater.inflate(R.layout.file, parent, false);

				vh = new ViewHolder();
				vh.ivType = (ImageView) convertView.findViewById(R.id.fileImageView);
				vh.tvName = (TextView) convertView.findViewById(R.id.fileTextView);
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}
			/*
			 * if (vh != null) { Log.e("potatoreminder",vh.toString()); } else {
			 * Log.e("potatoreminder","null"); }
			 */
			// 处理项目
			BrowseEntry be = getItem(position);
			switch (be.getType()) {
			case BrowseEntry.TYPE_BACK:
				vh.ivType.setImageResource(R.drawable.ic_entry_back);
				break;
			case BrowseEntry.TYPE_FILE:
				vh.ivType.setImageResource(R.drawable.ic_entry_list);
				break;
			case BrowseEntry.TYPE_DIRECTORY:
				vh.ivType.setImageResource(R.drawable.ic_entry_node);
				break;
			default:
				vh.ivType.setImageResource(android.R.drawable.ic_delete);
				break;
			}
			vh.tvName.setText(be.getName());
			return convertView;
		}

		// ViewHolder
		private class ViewHolder {
			public ImageView ivType;
			public TextView tvName;

			@Override
			public String toString() {
				String temp;
				if (ivType == null) {
					temp = "null;";
				} else {
					temp = ivType.toString() + ";";
				}
				if (tvName == null) {
					temp = temp + "null";
				} else {
					temp = temp + tvName.toString();
				}
				return temp;
			}

		}

	}
}
