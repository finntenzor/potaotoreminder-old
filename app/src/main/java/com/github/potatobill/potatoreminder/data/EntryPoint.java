package com.github.potatobill.potatoreminder.data;

public class EntryPoint
{
	public final static int STATE_CREATE = 0;
	public final static int STATE_UPDATE = 1;
	public final static int STATE_RLEVEL_REMEMBER = 2;
	public final static int STATE_RLEVEL_LOW = 3;
	public final static int STATE_RLEVEL_FORGOT = 4;
	public final static int STATE_HINT = 5;
	public final static int STATE_SHOW = 6;
	
	public int id;
	public String hint;
	public String point;
	public int state;
	public EntryPoint(int id, String hint,String point,int state)
	{
		this.id = id;
		this.hint = hint;
		this.point = point;
		this.state = state;
	}
}
