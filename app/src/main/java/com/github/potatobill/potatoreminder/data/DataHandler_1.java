package com.github.potatobill.potatoreminder.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DataHandler_1
{
	public final static int VERSION = 1;
	public final static String CREATE_SQL = "(id integer primary key,parent integer,title nvarchar(20),hint_text text,point_text text,last_time long,level real,memo text)";
	
	/** 
	* upgrade 检查一个记录是否有子项. 
	* @param db 数据库引用，用来进行操作
	* @param lst 显示进度的线程，用来显示升级进度 
	* @return 无
	* @exception SQLException 数据库读写操作失败时抛出 
	*/ 
	public final static boolean haveChild(SQLiteDatabase db,String table, int pid)
	{
		String[] cols = {String.valueOf(pid)};
		Cursor cursor = db.query(table, null, "parent=?", cols, null, null, null);
		boolean check = cursor.getCount()!=0;
		cursor.close();
		return check;
	}
	
}
