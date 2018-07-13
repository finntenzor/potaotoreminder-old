package com.github.potatobill.potatoreminder.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import java.util.ArrayList;
import java.io.File;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class DataManager
{
	public final static int VERSION = DataHandler_2.VERSION;
	public final static String TABLE = DataHandler_2.TABLE;

	public final static int SUCCEED = 0;
	public final static int FAILED = 1;
	public final static int TITLE_SHORT = 2;
	public final static int TITLE_LONG = 3;
	public final static int HINT_LONG = 4;
	public final static int POINT_LONG = 5;
	public final static int POINT_TOO_LONG = 6;
	public final static int POINT_LIST = 7;
	public final static int LEVEL_LOW = 8;
	public final static int LEVEL_HIGH = 9;
	
	
	SQLiteDatabase db;
	Machine mMachine;
	
	public DataManager(SQLiteOpenHelper helper)
	{
		Log.i("potatoreminder","Datamanager create");
		db = helper.getWritableDatabase();
	}
	
	public DataManager(SQLiteDatabase db)
	{
		this.db = db;
	}
	
	public boolean isOpen()
	{
		return db.isOpen();
	}

	public void close()
	{
		db.close();
	}
	
	public final void loadByPosition(ArrayList<EntryData> data,int position)
	{
		
		if (position!=0) data.add(new EntryData(0,"返回上一层","",EntryData.TYPE_BACK));
		DataHandler_2.loadByPosition(db, data, position, EntryData.TYPE_NODE);
		DataHandler_2.loadByPosition(db, data, position, EntryData.TYPE_LIST);
		DataHandler_2.loadByPosition(db, data, position, EntryData.TYPE_POINT);
		DataHandler_2.loadByPosition(db, data, position, EntryData.TYPE_IMAGE);
		data.add(new EntryData(0,"新建文件夹...","",EntryData.TYPE_NEW_NODE));
		data.add(new EntryData(0,"新建列表...","",EntryData.TYPE_NEW_LIST));
		data.add(new EntryData(0,"新建知识点...","",EntryData.TYPE_NEW_POINT));
		data.add(new EntryData(0,"新建图片知识点...","",EntryData.TYPE_NEW_IMAGE));
	}

	public final void loadMoveByPosition(ArrayList<EntryData> data,int position)
	{
		if (position!=0) data.add(new EntryData(0,"返回上一层","",EntryData.TYPE_BACK));
		DataHandler_2.loadByPosition(db, data, position, EntryData.TYPE_NODE);
		DataHandler_2.loadByPosition(db, data, position, EntryData.TYPE_LIST);
		DataHandler_2.loadByPosition(db, data, position, EntryData.TYPE_POINT);
		DataHandler_2.loadByPosition(db, data, position, EntryData.TYPE_IMAGE);
	}
	
	public final void loadPointsByPosition(ArrayList<EntryData> data,int position)
	{
		DataHandler_2.loadByPosition(db, data, position, EntryData.TYPE_POINT);
		data.add(new EntryData(0,"新建知识点...","",EntryData.TYPE_NEW_POINT));
	}
	
	
	public void logAll()
	{
		final String[] cols = {"id","pid","title"};
		Cursor cursor = db.query("point",cols,null,null,null,null,null);
		for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
		{
			Log.i("potatoreminder","entry-"+cursor.getString(0)+";"+cursor.getString(1)+";"+cursor.getString(2));
		}
		cursor.close();
	}
	
	public final String getPositionString(int id)
	{
		return DataHandler_2.getPositionString(db,id);
	}
	
	public final int getParentId(int id)
	{
		return DataHandler_2.getParentId(db,id);
	}
	
	public final int getNewId()
	{
		return DataHandler_2.getMaxId(db) + 1;
	}
	
	public final int insertEntry(int pid,String title,int type,String hint,String point,boolean skipPointLong,boolean skipPointList)
	{
		final String sep = System.getProperty("line.separator");
		final String sep2 = ";";
		int back;
		String[] points;
		String[] entries;
		if (title.length()==0)
		{
			back =  TITLE_SHORT;
		} else if (title.length()>20)
		{
			back = TITLE_LONG;
		} else if (hint.length()>50)
		{
			back = HINT_LONG;
		} else if (point.length()>2000)
		{
			back = POINT_TOO_LONG;
		} else if ((!skipPointLong)&&point.length()>100)
		{
			back = POINT_LONG;
		} else
		{
			switch (type)
			{
				case EntryData.TYPE_NEW_NODE:
					if (DataHandler_2.insertEntry(db,pid,title,EntryData.TYPE_NODE,hint,point) > 0)
					{
						back = SUCCEED;
					} else 
					{
						back = FAILED;
					}
					break;
				case EntryData.TYPE_NEW_POINT:
					if ((!skipPointList)&&point.indexOf(sep)>0)
					{
						back = POINT_LIST;
					} else {
						if (DataHandler_2.insertEntry(db,pid,title,EntryData.TYPE_POINT,hint,point) > 0)
						{
							back = SUCCEED;
						} else 
						{
							back = FAILED;
						}
					}
					break;
				case EntryData.TYPE_NEW_LIST:
					points = point.split(sep);
					boolean check = true;
					int id = DataHandler_2.insertEntry(db,pid,title,EntryData.TYPE_LIST,hint,"");
					if (id > 0)
					{
						for (int i=0;i<points.length;i++)
						{
							if (points[i].isEmpty()) {
								continue;
							}
							entries = points[i].split(sep2);
							switch (entries.length) {
								case 0:
									break;
								case 1:
									check = DataHandler_2.insertEntry(db,id,title,EntryData.TYPE_POINT,hint,entries[0]) > 0;
									break;
								default:
								case 2:
									check = DataHandler_2.insertEntry(db,id,title,EntryData.TYPE_POINT,hint + entries[1],entries[0]) > 0;
									break;
							}
							if (!check) {
								break;
							}
						}
						if (check)
						{
							back = SUCCEED;
						} else
						{
							back = FAILED;
						}
					} else
					{
						back = FAILED;
					}
					break;
					/*
				case EntryData.TYPE_NEW_IMAGE:
					if (DataHandler_2.insertImage(db,pid,title,EntryData.TYPE_IMAGE,hint,BitmapFactory.decodeFile(point)) > 0)
					{
						back = SUCCEED;
					} else 
					{
						back = FAILED;
					}
					break;
					*/
				default:
					back = FAILED;
					break;
			}
		}
		return back;
	}
	
	public final int insertEntry(int pid,String title,String hint,byte[] image)
	{
		int flag;
		if (title.length()==0) {
			flag =  TITLE_SHORT;
		} else if (title.length()>20) {
			flag = TITLE_LONG;
		} else if (hint.length()>50) {
			flag = HINT_LONG;
		} else {
			if (DataHandler_2.insertImage(db,pid,title,EntryData.TYPE_IMAGE,hint,image) > 0) {
				flag = SUCCEED;
			} else {
				flag = FAILED;
			}
		}
		return flag;
	}
	
	public final int deleteEntry(int id)
	{
		if (DataHandler_2.deleteEntry(db,id)>=0)
		{
			return SUCCEED;
		} else
		{
			return FAILED;
		}
	}
	
	public final int updateNode(int id,String title,String hint,String point,boolean skip)
	{
		int back;
		if (title.length()==0)
		{
			back =  TITLE_SHORT;
		} else if (title.length()>20)
		{
			back = TITLE_LONG;
		} else if (hint.length()>50)
		{
			back = HINT_LONG;
		} else if (point.length()>2000)
		{
			back = POINT_TOO_LONG;
		} else if ((!skip)&&point.length()>100)
		{
			back = POINT_LONG;
		} else {
			ContentValues cv = new ContentValues();
			cv.put("title",title);
			cv.put("hint",hint);
			cv.put("point",point);
			if (DataHandler_2.updateEntry(db,id,cv)>=0)
			{
				back = SUCCEED;
			} else
			{
				back = FAILED;
			}
		}
		return back;
	}
	
	public final int updateList(int id,String title,String hint)
	{
		int back;
		if (title.length()==0)
		{
			back =  TITLE_SHORT;
		} else if (title.length()>20)
		{
			back = TITLE_LONG;
		} else if (hint.length()>50)
		{
			back = HINT_LONG;
		} else {
			ContentValues cv = new ContentValues();
			cv.put("title",title);
			cv.put("hint",hint);
			if (DataHandler_2.updateEntry(db,id,cv)>=0)
			{
				back = SUCCEED;
			} else
			{
				back = FAILED;
			}
		}
		return back;
	}
	
	public final int updatePoint(int id,String title,String hint,String point,double level,boolean skip)
	{
		int back;
		if (title.length()==0)
		{
			back =  TITLE_SHORT;
		} else if (title.length()>20)
		{
			back = TITLE_LONG;
		} else if (hint.length()>50)
		{
			back = HINT_LONG;
		} else if (point.length()>2000)
		{
			back = POINT_TOO_LONG;
		} else if (level<0)
		{
			back = LEVEL_LOW;
		} else if (level>10)
		{
			back = LEVEL_HIGH;
		} else if ((!skip)&&point.length()>100)
		{
			back = POINT_LONG;
		} else {
			ContentValues cv = new ContentValues();
			cv.put("title",title);
			cv.put("hint",hint);
			cv.put("point",point);
			cv.put("level",level);
			if (DataHandler_2.updateEntry(db,id,cv)>=0)
			{
				back = SUCCEED;
			} else
			{
				back = FAILED;
			}
		}
		return back;
	}
	
	public final int updateImage(int id,String title,String hint,byte[] image,double level)
	{
		int back;
		if (title.length()==0)
		{
			back =  TITLE_SHORT;
		} else if (title.length()>20)
		{
			back = TITLE_LONG;
		} else if (hint.length()>50)
		{
			back = HINT_LONG;
		} else if (level<0)
		{
			back = LEVEL_LOW;
		} else if (level>10)
		{
			back = LEVEL_HIGH;
		} else {
			ContentValues cv = new ContentValues();
			cv.put("title",title);
			cv.put("hint",hint);
			cv.put("point",image);
			cv.put("level",level);
			if (DataHandler_2.updateEntry(db,id,cv)>=0)
			{
				back = SUCCEED;
			} else
			{
				back = FAILED;
			}
		}
		return back;
	}
	
	public final int updateImage(int id,String title,String hint,double level)
	{
		int back;
		if (title.length()==0)
		{
			back =  TITLE_SHORT;
		} else if (title.length()>20)
		{
			back = TITLE_LONG;
		} else if (hint.length()>50)
		{
			back = HINT_LONG;
		} else if (level<0)
		{
			back = LEVEL_LOW;
		} else if (level>10)
		{
			back = LEVEL_HIGH;
		} else {
			ContentValues cv = new ContentValues();
			cv.put("title",title);
			cv.put("hint",hint);
			cv.put("level",level);
			if (DataHandler_2.updateEntry(db,id,cv)>=0)
			{
				back = SUCCEED;
			} else
			{
				back = FAILED;
			}
		}
		return back;
	}
	
	public final Bundle readEntry(int id)
	{
		return DataHandler_2.readEntry(db,id);
	}
	
	public final boolean haveChild(int id)
	{
		return DataHandler_2.haveChild(db,id);
	}
	
	public final void setMachine()
	{
		mMachine = new Machine(db);
	}
	
	public final Machine getMachine()
	{
		return mMachine;
	}
	
	public final int study(int id,int state)
	{
		if (DataHandler_2.study(db,id,state)>=0)
		{
			return SUCCEED;
		} else
		{
			return FAILED;
		}
	}

	public final int deleteList(int id)
	{
		if (DataHandler_2.deleteChildren(db,id) && DataHandler_2.deleteEntry(db,id)>=0)
		{
			return SUCCEED;
		} else
		{
			return FAILED;
		}
	}

	public final void deleteAll(int id)
	{
		DataHandler_2.deleteAll(db, id);
	}

	public final int moveEntry(int id, int position)
	{
		if (DataHandler_2.moveEntry(db,id,position)>=0)
		{
			return SUCCEED;
		} else
		{
			return FAILED;
		}
	}
	
	public final void exportData(File file, DataHandler_2.Settable pdL, int id) {
		DataHandler_2.exportData(db, file, pdL, id);
	}
	
	public final SQLiteDatabase openImportDb(File file) throws RuntimeException {
		return DataHandler_2.openImportDb(file);
	}
	
	public final void importData(SQLiteDatabase importDb, int importPosition, DataHandler_2.Settable pdL) {
		DataHandler_2.importData(db, importDb, importPosition, pdL);
		importDb.close();
	}
	
	public final void setPlan(int position, int planNum, DataHandler_2.Settable pdL) {
		DataHandler_2.setPlan(db, position, planNum, pdL);
	}
	
}
