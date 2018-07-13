package com.github.potatobill.potatoreminder;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;

public class ResponseBuilder
{
	public static Dialog create(Context context,String tips,DialogInterface.OnClickListener ocl)
	{
		final Dialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle("提示");
		builder.setMessage(tips);
		builder.setPositiveButton("确认",ocl);
		builder.setNegativeButton("取消",ocl);
		dialog = builder.create();
		dialog.setCancelable(false);
		return dialog;
	}
}
