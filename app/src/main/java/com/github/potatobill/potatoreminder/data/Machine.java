package com.github.potatobill.potatoreminder.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

public class Machine
{
	public final static long t1 = 5*60*1000;
	public final static long t2 = 15*60*1000;
	public final static long t3 = 40*60*1000;
	public final static long t4 = 8*3600*1000;
	public final static long t5 = 16*3600*1000;
	public final static long t6 = 24*3600*1000;
	public final static long t7 = 48*3600*1000;
	public final static long t8 = 72*3600*1000;
	public final static long t9 = 7*24*3600*1000;
	public final static long t10 = 15*24*3600*1000;
	public final static long te = 30*24*3600*1000L;
	
	//private final static double tla = -2.0879382808;
	//private final static double tlb = 0.4785003684;
	
	private final static String TABLE = "point";
	
	private SQLiteDatabase db;
	private Entry[] entryArray;
	private int confirmNum;
	
	private Bundle entry;
	private ArrayList<EntryPoint> list;
	private Bitmap bitmap;
	
	Machine(SQLiteDatabase db)
	{
		this.db = db;
		confirmNum = 0;
		entry = null;
		list = null;
		bitmap = null;
	}

	public final int getProgress()
	{
		int m = getNum() + confirmNum;
		//Log.e("potatoreminder",String.valueOf( getNum()));
		//Log.e("potatoreminder",String.valueOf(m));
		return confirmNum * 100 / m;
	}
	
	public final int getNum()
	{
		//返回第一个不需要复习的知识点index
		//时间恰好相等定作不需要复习
		long now = System.currentTimeMillis();
		return binarySearch(entryArray,now);
	}
	
	public ArrayList<EntryPoint> getList()
	{
		return list;
	}
	
	public Bitmap getBitmap()
	{
		return bitmap;
	}

	public Bundle getEntry()
	{
		return entry;
	}
	
	private static class Entry
	{
		public int id;
		public long time;
		public Entry(int id, long time)
		{
			this.id = id;
			this.time = time;
		}
	}
	
	public interface Settable
	{
		public void setBoth(final int progress, final String state);
	}
	
	//****************************************
	
	public final void prepare(Settable pdL, int id)
	{
		long testStart = System.currentTimeMillis();
		final String[] cols = {"id","type","last_time","last_state","level"};
		Cursor cursor;
		String[] args = new String[1];
		ArrayList<Entry> entryList = new ArrayList<Entry>();
		Queue<Integer> nodes = new LinkedList<Integer>();
		nodes.offer(id);
		int i = 0;
		int n = 0;
		pdL.setBoth(0,"开始加载");
		while (nodes.peek()!=null)
		{
			args[0] = String.valueOf(nodes.poll());
			cursor = db.query(TABLE,cols,"pid=?",args,null,null,null);
			n+=cursor.getCount();
			for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
			{
				pdL.setBoth(i*100/n,"加载中 - "+i+"/"+n);
				switch (cursor.getInt(1))
				{
					case EntryData.TYPE_NODE:
						nodes.offer(cursor.getInt(0));
						break;
					case EntryData.TYPE_LIST:
						prepareList(db,entryList,cursor.getInt(0));
						break;
					case EntryData.TYPE_IMAGE:
					case EntryData.TYPE_POINT:
						preparePoint(cursor, entryList);
						break;
				}
				i++;
			}
			cursor.close();
		}
		pdL.setBoth(80,"处理数据中");
		Collections.sort(entryList, new Comparator<Entry>(){
				@Override
				public int compare(Machine.Entry p1, Machine.Entry p2)
				{
					if (p1.time > p2.time) return 1;//升序排列
					else if (p1.time < p2.time) return -1;
					else return 0;
				}
			});
		pdL.setBoth(90,"处理数据中");
		entryArray = new Entry[entryList.size()];
		entryList.toArray(entryArray);
		long testEnd = System.currentTimeMillis();
		Log.i("potatoreminder","prepare time cost :" + String.valueOf(testEnd - testStart));
		pdL.setBoth(97,"预加载中");
		read(0);
		//如果没有第0个，则写入返回信息
		pdL.setBoth(100,"加载完成");
	}

	private static void preparePoint(Cursor cursor, ArrayList<Entry> entryList)
	{
		long time;
		time = cursor.getLong(2) + theoryTime(cursor.getInt(3), cursor.getDouble(4));
		entryList.add(new Entry(cursor.getInt(0), time));
	}
	
	private static void prepareList(SQLiteDatabase db, ArrayList<Entry> entryList, int id)
	{
		final String[] cols = {"id","last_time","last_state","level"};
		String[] args = {String.valueOf(id)};
		Cursor cursor = db.query(TABLE,cols,"pid=?",args,null,null,null);
		long t;
		long d;
		long tmin = 0;
		//Log.e("potatoreminder",String.valueOf(System.currentTimeMillis()));
		for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
		{
			d = theoryTime(cursor.getInt(2),cursor.getDouble(3));
			t = cursor.getLong(1) + d;
			
			Log.e("potatoreminder",String.valueOf(d));
			
			if (tmin==0||t<tmin)
			{
				tmin = t;
			}
		}
		cursor.close();
		if (tmin!=0) entryList.add(new Entry(id,tmin));
	}
	
	//****************************************
	
	private final void read(int n)
	{
		if (entryArray.length>n)
		{
			long now = System.currentTimeMillis();
			Entry e = entryArray[n];
			entry = DataHandler_2.readEntry(db,e.id);
			//Log.e("potato","read:" + String.valueOf(n) + String.valueOf(entry == null));
			if (e.time < now)
			{
				if (bitmap!=null) {
					//bitmap.recycle();
					//回收应该交给studyactivity进行
					bitmap = null;
				}
				switch (entry.getInt("type"))
				{
					case EntryData.TYPE_POINT:
						list = null;
						break;
					case EntryData.TYPE_LIST:
						ArrayList<EntryPoint> data= new ArrayList<EntryPoint>();
						readList(data,e.id);
						list = data;
						break;
					case EntryData.TYPE_IMAGE:
						list = null;
						bitmap = ImageUtils.bytesToBitmap(entry.getByteArray("point"),500,500);
				}
			} else
			{
				entry.putInt("type",EntryData.TYPE_BACK);
				list = null;
				bitmap = null;
			}
		} else
		{
			//无法读取某一项时，表示应该返回
			if (entry == null) entry = new Bundle();
			entry.putInt("type",EntryData.TYPE_BACK);
			list = null;
			bitmap = null;
		}
	}
	
	private final void readList(ArrayList<EntryPoint> data, int id)
	{
		final String[] COLUMNS = {"id","hint","point"};
		final String SELECTION = "pid=? and type=?";
		final String[] ARGS = {String.valueOf(id),String.valueOf(EntryData.TYPE_POINT)};
		Cursor cursor = db.query(TABLE,COLUMNS,SELECTION,ARGS,null,null,null);
		String h;
		String p;
		for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
		{
			h = cursor.getString(1);
			p = cursor.getString(2);
			data.add(new EntryPoint(cursor.getInt(0),h,p,EntryPoint.STATE_HINT));
		}
		cursor.close();
	}
	
	//****************************************
	
	public final void next()
	{
		read(1);
	}
	
	//****************************************
	//确认只能确认当前第一个项目
	
	public final double confirmPoint(int state)
	{
		//Log.d("potatoreminder","machine-confirmPoint");
		int id = entryArray[0].id;
		double d = DataHandler_2.study(db,id,state);
		Bundle b = DataHandler_2.readEntry(db,id);
		long t = b.getLong("last_time") + theoryTime(state,b.getDouble("level"));
		changeAndSort(entryArray,t);
		confirmNum++;
		return d;
	}
	
	public final double[] confirmList(int[] id, int[] state)
	{
		int num;
		if (id.length >= state.length) num = id.length;
		else num = state.length;
		double[] d = new double[num];
		Bundle b;
		long t;
		long tmin = 0;
		for (int i = 0; i < num; i++)
		{
			d[i] = DataHandler_2.study(db,id[i],state[i]);
			b = DataHandler_2.readEntry(db,id[i]);
			t = b.getLong("last_time") + theoryTime(state[i],b.getDouble("level"));
			if (tmin==0||t<tmin)
			{
				tmin = t;
			}
		}
		changeAndSort(entryArray,tmin);
		confirmNum++;
		return d;
	}
	
	//****************************************
	
	private final static long theoryTime(int state,double level)
	{
		if (state == EntryPoint.STATE_RLEVEL_REMEMBER)
		{
			if (level < 1) return t1;
			else if (level < 2) return t2;
			else if (level < 3) return t3;
			else if (level < 4) return t4;
			else if (level < 5) return t5;
			else if (level < 6) return t6;
			else if (level < 7) return t7;
			else if (level < 8) return t8;
			else if (level < 9) return t9;
			else if (level < 10) return t10;
			else return te;
		} else
		{
			return t1;
		}
	}

	public final static double theoryLevel(Bundle lastData)
	{
		//y = 0.3 + e ^ (ax+b)
		long last = lastData.getLong("last_time");
		int state = lastData.getInt("last_state");
		double level = lastData.getDouble("level");
		
		double d;
		double ta = theoryTime(state,level);
		double tb = theoryTime(state,level+1);
		double t = System.currentTimeMillis() - last;
		if (t < 0.8 * ta) {
			d = 0;
			//Log.e("potatobill","a");
		} else if (t < ta) {
			d = (t - 0.8 * ta) / (0.2 * ta);
			//Log.e("potatobill","b");
		} else if (t < ta + 0.2 * tb) {
			d = 1;
			//Log.e("potatobill","c");
		} else if (t < ta + tb) {
			if (level < 3) {
				d = 1;
			} else {
				d = 1 - 0.65 * (t - (ta + 0.2 * tb)) / (0.8 * tb);
			}
			//Log.e("potatobill","d");
		} else {
			if (level < 3) {
				d = 1;
			} else {
				d = 0.35;
			}
			//Log.e("potatobill","e");
		}
		return d;
	}
	
	//****************************************
	
	public final static void changeAndSort(Entry[] data, long time)
	{
		if (data.length == 1)
		{
			data[0] = new Entry(data[0].id, time);
		} else if (data.length > 1)
		{
			Entry e = new Entry(data[0].id, time);
			int index = binarySearch(data, time)-1;
			if (index!=-1)
			{
				System.arraycopy(data,1,data,0,index);
				data[index] = e;
			} else
			{
				data[0] = e;
			}
		}
	}
	
	public final static int binarySearch(Entry[] data, long time)
	{
		//找到大于等于time的第一个位置
		//数组为空则返回0
		if (data.length==0) return 0;
        int lo = 0;
        int hi = data.length - 1;

        while (lo <= hi) {  
            int mid = (lo + hi) >>> 1;//无符号右移  
            Entry midVal = data[mid];  

            if (midVal.time < time) {  
                lo = mid + 1;  
            } else if (midVal.time > time) {  
                hi = mid - 1;  
            } else {  
                return mid;  // value found  
            }  
        }  
        return lo;  // value not present  
    }
	
	//****************************************
	
	public void LogAll()
	{
		/*
		Log.i("potatoreminder","machine-now-"+String.valueOf(System.currentTimeMillis()));
		for (Entry e : entryArray)
		{
			Log.i("potatoreminder","machine-logall-"+String.valueOf(e.id)+";"+String.valueOf(e.time));
		}
		*/
	}
}
