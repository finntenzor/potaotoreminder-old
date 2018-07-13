package com.github.potatobill.potatoreminder.data;

public class EntryData
{
	public final static int TYPE_BACK = 0;
	public final static int TYPE_POINT = 1;
	public final static int TYPE_NODE = 2;
	public final static int TYPE_LIST = 3;
	public final static int TYPE_IMAGE = 4;
	public final static int TYPE_NEW_POINT = 17;
	public final static int TYPE_NEW_NODE = 18;
	public final static int TYPE_NEW_LIST = 19;
	public final static int TYPE_NEW_IMAGE = 20;
	public int id;
	public String title;
	public String hint;
	public int type;
	public EntryData()
	{
	}
	public EntryData(int id, String title, String hint, int type)
	{
		this.id = id;
		this.title = title;
		this.hint = hint;
		this.type = type;
	}
}
