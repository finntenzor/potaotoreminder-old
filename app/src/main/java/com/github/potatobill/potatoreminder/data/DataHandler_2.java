package com.github.potatobill.potatoreminder.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.io.File;
import android.graphics.Bitmap;

public class DataHandler_2
{
	public final static int VERSION = 2;
	public final static String TABLE = "point";
	public final static long ONE_DAY = 24 * 3600 * 1000L;

	public final static void loadByPosition(SQLiteDatabase db,
			ArrayList<EntryData> data, int position, int type)
	{
		final String[] COLUMNS = {"id","title","hint","point"};
		final String SELECTION = "pid=? and type=?";
		final String[] ARGS = {String.valueOf(position),String.valueOf(type)};
		Cursor cursor = db.query(TABLE,COLUMNS,SELECTION,ARGS,null,null,null);
		String t;
		String h;
		if (type == EntryData.TYPE_IMAGE) {
			for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
			{
				t = cursor.getString(1);
				h = cursor.getString(2) + "[图片]";
				data.add(new EntryData(cursor.getInt(0),t,h,type));
			}
		} else {
			for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
			{
				t = cursor.getString(1);
				h = anHint(cursor.getString(2),cursor.getString(3));
				data.add(new EntryData(cursor.getInt(0),t,h,type));
			}
		}
		cursor.close();
	}
	
	private final static String anHint(String hint, String point)
	{
		String[] t = point.split(System.getProperty("line.separator"));
		if (hint.length()==0)
		{
			if (t[0].length()==0)
			{
				return "";
			} else
			{
				return t[0];
			}
		} else
		{
			return hint;
		}
	}
	
	public final static int getParentId(SQLiteDatabase db,int id)
	{
		final String[] COLUMNS = {"pid"};
		final String[] ARGS = {String.valueOf(id)};
		int pid;
		if (id==0)
		{
			pid = 0;
		} else
		{
			Cursor cursor = db.query(TABLE,COLUMNS,"id=?",ARGS,null,null,null,null);
			cursor.moveToFirst();
			pid = cursor.getInt(0);
			cursor.close();
		}
		return pid;
	}
	
	public final static String getPositionString(SQLiteDatabase db,int id)
	{
		String p;
		if (id==0)
		{
			p = "学习";
		} else
		{
			final String[] COLUMNS = {"pid","title"};
			final String[] ARGS = {String.valueOf(id)};
			Cursor cursor = db.query(TABLE,COLUMNS,"id=?",ARGS,null,null,null,null);
			cursor.moveToFirst();
			int pid = cursor.getInt(0);
			String title = cursor.getString(1);
			cursor.close();
			p = getPositionString(db,pid) + "/" + title;
		}
		return p;
	}
	
	public final static int getMaxId(SQLiteDatabase db)
	{
		final String[] COLUMNS = {"id"};//desc
		Cursor cursor = db.query(TABLE,COLUMNS,null,null,null,null,"id desc");
		int id;
		if (cursor.moveToFirst())
		{
			id = cursor.getInt(0);
		} else
		{
			id = 0;
		}
		cursor.close();
		return id;
	}
	
	public final static int insertEntry(SQLiteDatabase db, int pid,String title,int type,String hint,String point)
	{
		ContentValues cv = new ContentValues();
		int id = getMaxId(db) + 1;
		cv.put("id",id);
		cv.put("pid",pid);
		//,,key integer,level real)";
		cv.put("title",title);
		cv.put("type",type);
		cv.put("hint",hint);
		cv.put("point",point);
		cv.put("create_time",System.currentTimeMillis());
		cv.put("last_time",System.currentTimeMillis());
		cv.put("last_state",EntryPoint.STATE_CREATE);
		cv.put("key",0);
		cv.put("level",0);
		if (db.insert(TABLE,null,cv) >= 0)
		{
			return id;
		} else
		{
			return 0;
		}
	}
	
	public final static int insertImage(SQLiteDatabase db, int pid,String title,int type,String hint,byte[] image)
	{
		ContentValues cv = new ContentValues();
		int id = getMaxId(db) + 1;
		cv.put("id",id);
		cv.put("pid",pid);
		//,,key integer,level real)";
		cv.put("title",title);
		cv.put("type",type);
		cv.put("hint",hint);
		cv.put("point",image);
		cv.put("create_time",System.currentTimeMillis());
		cv.put("last_time",System.currentTimeMillis());
		cv.put("last_state",EntryPoint.STATE_CREATE);
		cv.put("key",0);
		cv.put("level",0);
		if (db.insert(TABLE,null,cv) >= 0)
		{
			return id;
		} else
		{
			return 0;
		}
	}
	
	public final static int insertEntry(SQLiteDatabase db, int id, int pid,String title,int type,String hint,String point)
	{
		ContentValues cv = new ContentValues();
		cv.put("id",id);
		cv.put("pid",pid);
		cv.put("title",title);
		cv.put("type",type);
		cv.put("hint",hint);
		cv.put("point",point);
		cv.put("create_time",System.currentTimeMillis());
		cv.put("last_time",System.currentTimeMillis());
		cv.put("last_state",EntryPoint.STATE_CREATE);
		cv.put("key",0);
		cv.put("level",0);
		if (db.insert(TABLE,null,cv) >= 0) {
			return id;
		} else {
			return 0;
		}
	}
	
	public final static int updateEntry(SQLiteDatabase db, int id,ContentValues cv)
	{
		String[] args = {String.valueOf(id)};
		cv.put("last_time",System.currentTimeMillis());
		cv.put("last_state",EntryPoint.STATE_UPDATE);
		return db.update(TABLE,cv,"id=?",args);
	}

	public final static int moveEntry(SQLiteDatabase db, int id, int pid)
	{
		String[] args = {String.valueOf(id)};
		ContentValues cv = new ContentValues();
		cv.put("pid",pid);
		return db.update(TABLE,cv,"id=?",args);
	}
	
	public final static int deleteEntry(SQLiteDatabase db, int id)
	{
		String[] args = {String.valueOf(id)};
		return db.delete(TABLE,"id=?",args);
	}
	
	public final static Bundle readEntry(SQLiteDatabase db, int id)
	{
		final Bundle b = new Bundle();
		final String[] COLUMNS = {"id","pid","title","type","hint","point"
			,"create_time","last_time","last_state","key","level"};
		String[] args = {String.valueOf(id)};
		Cursor cursor = db.query(TABLE,COLUMNS,"id=?",args,null,null,null,null);
		if (cursor.moveToFirst())
		{
			b.putInt("id",cursor.getInt(0));
			b.putInt("pid",cursor.getInt(1));
			b.putString("title",cursor.getString(2));
			b.putInt("type",cursor.getInt(3));
			b.putString("hint",cursor.getString(4));
			if (cursor.getInt(3) == EntryData.TYPE_IMAGE) {
				b.putByteArray("point",cursor.getBlob(5));
			} else {
				b.putString("point",cursor.getString(5));
			}
			b.putLong("create_time",cursor.getLong(6));
			b.putLong("last_time",cursor.getLong(7));
			b.putInt("last_state",cursor.getInt(8));
			b.putInt("key",cursor.getInt(9));
			b.putDouble("level",cursor.getDouble(10));
			cursor.close();
			return b;
		} else
		{
			cursor.close();
			return null;
		}
	}
	
	public final static boolean haveChild(SQLiteDatabase db, int pid)
	{
		String[] cols = {String.valueOf(pid)};
		Cursor cursor = db.query(TABLE, null, "pid=?", cols, null, null, null);
		boolean check = cursor.getCount()!=0;
		cursor.close();
		return check;
	}
	
	public final static double study(SQLiteDatabase db,int id,int state)
	{
		String[] args = {String.valueOf(id)};
		ContentValues cv = new ContentValues();
		cv.put("last_time",System.currentTimeMillis());
		cv.put("last_state",state);
		Bundle b = readEntry(db,id);
		double d;
		switch (state)
		{
			case EntryPoint.STATE_RLEVEL_REMEMBER:
				d = Machine.theoryLevel(b);
				break;
			case EntryPoint.STATE_RLEVEL_FORGOT:
				d = -1;
				break;
			default:
				d = 0;
		}
		double newl = b.getDouble("level") + d;
		if (newl < 0) {
			newl = 0;
			d = newl - b.getDouble("level");
		} else if (newl > 10) {
			newl = 10;
			d = newl - b.getDouble("level");
		}
		cv.put("level",newl);
		if (db.update(TABLE,cv,"id=?",args)>=0)
		{
			return d;
		} else
		{
			return 0;
		}
	}

    public final static boolean deleteChildren(SQLiteDatabase db, int id)
    {
        final String[] cols = {"id"};
        final String[] args = {String.valueOf(id)};
        boolean check = true;
        Cursor cursor = db.query(TABLE,cols,"pid=?",args,null,null,null);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            check = check && deleteEntry(db,cursor.getInt(0)) >= 0;
        }
        cursor.close();
        return check;
    }

    public final static void deleteAll(SQLiteDatabase db, int id)
    {
        final String[] cols = {"id","type"};
        Cursor cursor;
        String[] args = new String[1];
        Queue<Integer> nodes = new LinkedList<>();
        nodes.offer(id);
		db.beginTransaction();
        while (nodes.peek()!=null)
        {
            id = nodes.poll();
            args[0] = String.valueOf(id);
            deleteEntry(db, id);
            cursor = db.query(TABLE,cols,"pid=?",args,null,null,null);
            for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
            {
                switch (cursor.getInt(1))
                {
                    case EntryData.TYPE_NODE:
                    case EntryData.TYPE_LIST:
                        nodes.offer(cursor.getInt(0));
                        break;
                    case EntryData.TYPE_POINT:
                        deleteEntry(db,cursor.getInt(0));
                        break;
                }
            }
            cursor.close();
        }
		db.setTransactionSuccessful();
		db.endTransaction();
    }
	
	public interface Settable
	{
		public void setBoth(final int progress, final String state);
	}
	
	public final static void exportData(SQLiteDatabase db, File exportFile, Settable pdL, int id)
	{
		//初始化导出数据库
		SQLiteDatabase exportDb = SQLiteDatabase.openOrCreateDatabase(exportFile, null);
		final String COLUMNS = "(id integer primary key,pid integer,title nvarchar(20),type integer,hint text,point text)";
		exportDb.execSQL("create table point" + COLUMNS);
		//准备列表
		ArrayList<Bundle> entryList = new ArrayList<Bundle>();
		//读取所有项目(排序)
		readDataForExport(db, id, pdL, entryList);
		//处理数据(重置id和pid)
		dealDataForExport(entryList, id, pdL);
		//写入数据
		writeDataForExport(entryList, pdL, exportDb);
		//关闭数据库
		exportDb.close();
	}

	private final static void writeDataForExport(ArrayList<Bundle> entryList, Settable pdL, SQLiteDatabase exportDb)
	{
		int i = 0;
		int n = entryList.size();
		ContentValues cv = new ContentValues();
		exportDb.beginTransaction();
		try {
			for (Bundle b : entryList)
			{
				pdL.setBoth(60 + i * 35 / n, "导出 - " + i + "/" + n);
				try {
					Thread.sleep(2);
					//随时可以提速
				} catch (InterruptedException e) {}
				i++;
				cv.put("id", b.getInt("nid"));
				cv.put("pid", b.getInt("npid"));
				cv.put("title", b.getString("title"));
				cv.put("type", b.getInt("type"));
				cv.put("hint", b.getString("hint"));
				if (b.getInt("type") == EntryData.TYPE_IMAGE) {
					cv.put("point", b.getByteArray("point"));
				} else {
					cv.put("point", b.getString("point"));
				}
				exportDb.insert("point", null, cv);
			}
			exportDb.setTransactionSuccessful();
		} finally {
			pdL.setBoth(95, "保存数据");
			exportDb.endTransaction();
			pdL.setBoth(100, "完成");
		}
		
	}

	private final static void dealDataForExport(ArrayList<Bundle> entryList, int rootId, Settable pdL)
	{
		int i = 0;
		int n = entryList.size();
		int pid;
		for (Bundle b : entryList) {
			
			//赋予新的id
			i++;
			b.putInt("nid", i);
			
			//赋予新的pid
			pid = b.getInt("pid");
			if (pid == rootId) {
				b.putInt("npid", 0);
				//0表示根目录 即导出前的位置
			} else {
				//根据原id和pid的关系填充pid
				for (Bundle bj : entryList) {
					if (pid == bj.getInt("id")) {
						//如果按照从上往下的顺序 理论上一定能找到npid
						b.putInt("npid", bj.getInt("nid"));
						break;
					}
				}
			}
			
			//输出状态
			pdL.setBoth(20 + i * 40 / n, "加载中 - " + i + "/" + n);
		}
	}

	private final static void readDataForExport(SQLiteDatabase db, int id, Settable pdL, ArrayList<Bundle> entryList)
	{
		int i = 0;
		int n = 0;
		final String[] cols = {"id","type"};
		Cursor cursor;
		String[] args = new String[2];
		Queue<Integer> nodes = new LinkedList<Integer>();
		nodes.offer(id);
		pdL.setBoth(0, "加载");
		while (nodes.peek() != null)
		{
			args[0] = String.valueOf(nodes.poll());
			//读取文件夹
			args[1] = String.valueOf(EntryData.TYPE_NODE);
			cursor = db.query(TABLE, cols, "pid=? and type=?", args, null, null, "title asc");
			n += cursor.getCount();
			for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
			{
				pdL.setBoth(i * 10 / n, "加载中 - " + i + "/" + n);
				nodes.offer(cursor.getInt(0));
				entryList.add(readEntry(db, cursor.getInt(0)));
				i++;
			}
			cursor.close();
			//读取列表
			args[1] = String.valueOf(EntryData.TYPE_LIST);
			cursor = db.query(TABLE, cols, "pid=? and type=?", args, null, null, "title asc");
			n += cursor.getCount();
			for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
			{
				pdL.setBoth(i * 10 / n, "加载中 - " + i + "/" + n);
				nodes.offer(cursor.getInt(0));
				entryList.add(readEntry(db, cursor.getInt(0)));
				i++;
			}
			cursor.close();
			//读取知识点
			args[1] = String.valueOf(EntryData.TYPE_POINT);
			cursor = db.query(TABLE, cols, "pid=? and type=?", args, null, null, "title asc");
			n += cursor.getCount();
			for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
			{
				pdL.setBoth(i * 10 / n, "加载中 - " + i + "/" + n);
				entryList.add(readEntry(db, cursor.getInt(0)));
				i++;
			}
			cursor.close();
			//读取图片
			args[1] = String.valueOf(EntryData.TYPE_IMAGE);
			cursor = db.query(TABLE, cols, "pid=? and type=?", args, null, null, "title asc");
			n += cursor.getCount();
			for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
			{
				pdL.setBoth(i * 10 / n, "加载中 - " + i + "/" + n);
				entryList.add(readEntry(db, cursor.getInt(0)));
				i++;
			}
			cursor.close();
		}
	}
	
	//导入功能
	public final static SQLiteDatabase openImportDb(File importFile) throws RuntimeException {
		final String[] cols = {"id","pid","title","type","hint","point"};
		SQLiteDatabase importDb;
		Cursor cursor;
		
		//检查文件是否存在
		if (importFile==null || !importFile.exists() || !importFile.canRead()) {
			throw new RuntimeException("文件不存在或不可读");
		}
		
		//检查数据库能否打开
		try {
			importDb = SQLiteDatabase.openOrCreateDatabase(importFile, null);
		} catch (Exception e) {
			throw new RuntimeException("该文件无法作为数据库打开，详情:" + e.getMessage());
		}
		
		//检查表是否存在
		try {
			cursor = importDb.query("point", cols, null, null, null, null, null);
		} catch (Exception e) {
			throw new RuntimeException("数据格式不正确，详情:" + e.getMessage());
		}
		
		//检查相关列是否存在
		try {
			cursor.getColumnIndexOrThrow("id");
			cursor.getColumnIndexOrThrow("pid");
			cursor.getColumnIndexOrThrow("title");
			cursor.getColumnIndexOrThrow("type");
			cursor.getColumnIndexOrThrow("hint");
			cursor.getColumnIndexOrThrow("point");
		} catch (Exception e) {
			cursor.close();
			throw new RuntimeException("数据格式不正确，详情:" + e.getMessage());
		}
		return importDb;
	}
	
	public final static void importData(SQLiteDatabase db, SQLiteDatabase importDb, int importPosition, Settable pdL) {
		Bundle[] data;
		//读取数据 添加id
		data = readDataForImport(db, importDb, pdL);
		//添加pid
		dealDataForImport(data, importPosition, pdL);
		//写入本地数据库
		writeDataForImport(db, data, pdL);
	}
	
	public final static Bundle[] readDataForImport(SQLiteDatabase db, SQLiteDatabase importDb, Settable pdL)
	{
		final String[] COLUMNS = {"id","pid","title","type","hint","point"};
		Cursor cursor = importDb.query(TABLE,COLUMNS,null,null,null,null, "title asc");
		int maxId = getMaxId(db);
		int i = 0;
		int n = cursor.getCount();
		Bundle[] data = new Bundle[n];
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			data[i] = new Bundle();
			data[i].putInt("id",cursor.getInt(0));
			data[i].putInt("pid",cursor.getInt(1));
			data[i].putString("title",cursor.getString(2));
			data[i].putInt("type",cursor.getInt(3));
			data[i].putString("hint",cursor.getString(4));
			if (cursor.getInt(3) == EntryData.TYPE_IMAGE) {
				data[i].putByteArray("point",cursor.getBlob(5));
			} else {
				data[i].putString("point",cursor.getString(5));
			}
			data[i].putInt("nid",maxId + i + 1);
			pdL.setBoth(i * 20 / n, "加载中 - " + i + "/" + n);
			i++;
		}
		cursor.close();
		return data;
	}
	
	public final static void dealDataForImport(Bundle[] data, int importPosition, Settable pdL) {
		int i = 0;
		int n = data.length;
		int pid;
		for (Bundle b : data) {
			//获取导入表中的pid
			pid = b.getInt("pid");
			if (pid == 0) {
				b.putInt("npid", importPosition);
			} else {
			//根据原id和pid的关系填充pid
				for (Bundle bj : data) {
					if (pid == bj.getInt("id")) {
						//如果按照从上往下的顺序 理论上一定能找到npid
						b.putInt("npid", bj.getInt("nid"));
						break;
					}
				}
			}
			
			//输出状态
			pdL.setBoth(20 + i * 40 / n, "加载中 - " + i + "/" + n);
			i++;
		}
	}
	
	public final static void writeDataForImport(SQLiteDatabase db, Bundle[] data, Settable pdL) {
		ContentValues cv = new ContentValues();
		int i = 0;
		int n = data.length;
		db.beginTransaction();
		try {
			for (;i<n;i++) {
				pdL.setBoth(60 + i * 35 / n, "导出 - " + i + "/" + n);
				try {
					Thread.sleep(2);
					//随时可以提速
				} catch (InterruptedException e) {}
				cv.put("id",data[i].getInt("nid"));
				cv.put("pid",data[i].getInt("npid"));
				cv.put("title",data[i].getString("title"));
				cv.put("type",data[i].getInt("type"));
				cv.put("hint",data[i].getString("hint"));
				if (data[i].getInt("type") == EntryData.TYPE_IMAGE) {
					cv.put("point",data[i].getByteArray("point"));
				} else {
					cv.put("point",data[i].getString("point"));
				}
				cv.put("create_time",System.currentTimeMillis());
				cv.put("last_time",System.currentTimeMillis());
				cv.put("last_state",EntryPoint.STATE_CREATE);
				cv.put("key",0);
				cv.put("level",0);
				db.insert("point", null, cv);
			}
			db.setTransactionSuccessful();
		} finally {
			pdL.setBoth(95, "保存数据");
			db.endTransaction();
			pdL.setBoth(100, "完成");
		}
	}
	
	//计划功能
	public final static void setPlan(SQLiteDatabase db, int position, int planNum, Settable pdL) {
		final String[] cols = {"id","type"};
		Cursor cursor;
		String[] args = new String[1];
		Queue<Integer> nodes = new LinkedList<Integer>();
		nodes.offer(position);
		int i = 0;
		int n = 0;
		int setNum = 0;
		boolean flag = true;
		int days = 0;
		pdL.setBoth(0,"正在调整计划");
		db.beginTransaction();
		try {
			while (nodes.peek()!=null && flag)
			{
				args[0] = String.valueOf(nodes.poll());
				cursor = db.query(TABLE,cols,"pid=?",args,null,null,"title asc");
				n += cursor.getCount();
				for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
				{
					pdL.setBoth(i * 100 / n, "调整中 - " + i + "/" + n);
					i++;
					//计算延迟天数
					if (planNum > 0) {
						days = setNum / planNum;
					} else {
						days = 0;
					}
					//调整项目计划
					switch (cursor.getInt(1))
					{
						case EntryData.TYPE_NODE:
							nodes.offer(cursor.getInt(0));
							break;
						case EntryData.TYPE_LIST:
							flag = setListPlan(db, cursor.getInt(0), days);
							setNum++;
							break;
						case EntryData.TYPE_IMAGE:
						case EntryData.TYPE_POINT:
							//TODO
							flag = updateForPlan(db, cursor.getInt(0), days);
							setNum++;
							break;
					}
					//万一失败
					if (!flag) break;
				}
				cursor.close();
			}
			if (flag) {
				db.setTransactionSuccessful();
			}
		} finally {
			if (flag) {
				pdL.setBoth(100, "调整完毕");
			} else {
				pdL.setBoth(100, "调整失败");
			}
			db.endTransaction();
		}
	}
	
	public final static boolean setListPlan(SQLiteDatabase db, int id, int days) {
		final String[] cols = {"id"};
		String[] args = {String.valueOf(id)};
		Cursor cursor = db.query(TABLE,cols,"pid=?",args,null,null,"title asc");
		boolean flag = true;
		for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
		{
			flag = updateForPlan(db, cursor.getInt(0), days);
			if (!flag) break;
		}
		cursor.close();
		return flag;
	}
	
	public final static boolean updateForPlan(SQLiteDatabase db, int id, int days)
	{
		String[] args = {String.valueOf(id)};
		ContentValues cv = new ContentValues();
		cv.put("last_time", System.currentTimeMillis() + ONE_DAY * days);
		cv.put("last_state", EntryPoint.STATE_UPDATE);
		return db.update(TABLE,cv,"id=?",args)>=0;
	}
}
