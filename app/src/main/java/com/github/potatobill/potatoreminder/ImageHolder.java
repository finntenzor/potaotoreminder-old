package com.github.potatobill.potatoreminder;
import android.net.Uri;
import android.graphics.Bitmap;
import android.content.Context;
import com.github.potatobill.potatoreminder.data.ImageUtils;
import java.io.IOException;
import android.os.Message;
import com.github.potatobill.potatoreminder.data.DataManager;
import android.os.Handler;
import android.database.Cursor;

public class ImageHolder
{
	Uri uri;
	Bitmap image;
	App app;
	Handler handler;

	DecodeImage decode;
	DecodeByte decodeByte;
	SaveImage save;
	UpdateImage update;
	
	int SHOW_TOAST;
	int SHOW_IMAGE;
	int FINISH;

	public ImageHolder(App app, Handler handler, int showToastFlag, int showImageFlag, int finishFlag) {
		this.app = app;
		this.handler = handler;
		this.SHOW_TOAST = showToastFlag;
		this.SHOW_IMAGE = showImageFlag;
		this.FINISH = finishFlag;
		decode = new DecodeImage();
		decodeByte = new DecodeByte();
		save = new SaveImage();
		update = new UpdateImage();
	}

	public void decodeImage () {
		if (decode.state()) {
			sendToast("正在解码图片");
		} else {
			new Thread(decode).start();
		}
	}

	public void setDecodeSize(int width, int height) {
		decode.setSize(width, height);
		decodeByte.setSize(width, height);
	}
	
	public void decodeByte(byte[] data) {
		if (decode.state()) {
			sendToast("正在解码图片");
		} else {
			decodeByte.setData(data);
			new Thread(decodeByte).start();
		}
	}

	public void saveImage(String title, String hint) {
		if (save.state()) {
			sendToast("正在保存图片");
		} else {
			save.setContent(title, hint);
			new Thread(save).start();
		}
	}
	
	public void updateImage(int id, String title, String hint, double level) {
		if (update.state()) {
			sendToast("正在保存图片");
		} else {
			update.setContent(id, title, hint, level);
			new Thread(update).start();
		}
	}

	private class DecodeImage implements Runnable
	{
		//表明线程是否已经运行
		boolean flag = false;
		int width;
		int height;

		@Override
		public void run()
		{
			//照相机或图库返回图片时调用此方法
			//需要先设置好大小
			//从uri读取图片
			flag = true;
			try {
				image = ImageUtils.uriToBitmap(app, uri, width, height);
				handler.sendEmptyMessage(SHOW_IMAGE);
			} catch (IOException e) {
				sendToast("无法打开图片或图片格式不正确");
			}
			flag = false;
		}

		public boolean state() {
			return flag;
		}

		public void setSize(int width, int height) {
			this.width = width;
			this.height = height;
		}
	}
	
	private class DecodeByte implements Runnable
	{
		//表明线程是否已经运行
		boolean flag = false;
		byte[] data;
		int width;
		int height;

		@Override
		public void run()
		{
			//照相机或图库返回图片时调用此方法
			//需要先设置好大小
			//从uri读取图片
			flag = true;
			try {
				image = ImageUtils.bytesToBitmap(data, width, height);
				handler.sendEmptyMessage(SHOW_IMAGE);
			} catch (Exception e) {
				sendToast("无法打开图片或图片格式不正确");
			}
			flag = false;
		}

		public boolean state() {
			return flag;
		}

		public void setData(byte[] data) {
			this.data = data;
		}
		
		public void setSize(int width, int height) {
			this.width = width;
			this.height = height;
		}
	}

	private class SaveImage implements Runnable
	{

		//表明线程是否运行
		boolean flag = false;
		String title;
		String hint;
		//表明保存是否成功

		@Override
		public void run()
		{
			// TODO: Implement this method
			flag = true;
			try{
				byte[] data = ImageUtils.uriToBytes(app, uri, Constant.NewImage.SAVE_QUALITY);
				int state;
				if (data != null) {
					state = app.dm.insertEntry(app.position, title, hint, data);
					//这个地方用这个方法可能会造成失败较为消耗时间
					//修正这个问题需要修改整个软件的框架
				} else {
					state = -1;
				}

				String message;
				switch (state)
				{
					case DataManager.SUCCEED:
						message = "成功";
						break;
					case DataManager.FAILED:
						message = "发生未知错误 新建失败";
						break;
					case DataManager.TITLE_SHORT:
						message = "标题不能为空 请输入标题";
						break;
					case DataManager.TITLE_LONG:
						message = "标题太长 请将标题控制在20字以内";
						break;
					case DataManager.HINT_LONG:
						message = "提示语太长 请将提示语控制在20字以内";
						break;
					case -1:
						message = "图片未能保存 请确认已经选好图片";
						break;
					default:
						message = "未知原因 操作失败";
						break;
				}
				sendToast(message);
				if (state == DataManager.SUCCEED) {
					handler.sendEmptyMessage(FINISH);
				}
			} catch (IOException e) {
				sendToast("无法打开图片或图片格式不正确");
			}
			flag = false;
		}

		public void setContent(String title, String hint) {
			this.title = title;
			this.hint = hint;
		}

		public boolean state() {
			return flag;
		}
	}
	
	private class UpdateImage implements Runnable
	{

		//表明线程是否运行
		boolean flag = false;
		int id;
		String title;
		String hint;
		double level;
		//表明保存是否成功

		@Override
		public void run()
		{
			// TODO: Implement this method
			flag = true;
			try{
				int state;
				if (uri == null) {
					state = app.dm.updateImage(id, title, hint, level);
				} else {
					byte[] data = ImageUtils.uriToBytes(app, uri, Constant.NewImage.SAVE_QUALITY);
					if (data != null) {
						state = app.dm.updateImage(id, title, hint, data, level);
						//这个地方用这个方法可能会造成失败较为消耗时间
						//修正这个问题需要修改整个软件的框架
					} else {
						state = -1;
					}
				}
				String message;
				switch (state)
				{
					case DataManager.SUCCEED:
						message = "成功";
						break;
					case DataManager.FAILED:
						message = "发生未知错误 新建失败";
						break;
					case DataManager.TITLE_SHORT:
						message = "标题不能为空 请输入标题";
						break;
					case DataManager.TITLE_LONG:
						message = "标题太长 请将标题控制在20字以内";
						break;
					case DataManager.HINT_LONG:
						message = "提示语太长 请将提示语控制在20字以内";
						break;
					case -1:
						message = "图片未能保存 请确认已经选好图片";
						break;
					default:
						message = "未知原因 操作失败";
						break;
				}
				sendToast(message);
				if (state == DataManager.SUCCEED) {
					handler.sendEmptyMessage(FINISH);
				}
			} catch (IOException e) {
				sendToast("无法打开图片或图片格式不正确");
			}
			flag = false;
		}

		public void setContent(int id, String title, String hint, double level) {
			this.id = id;
			this.title = title;
			this.hint = hint;
			this.level = level;
		}

		public boolean state() {
			return flag;
		}

		
	}
	
	private void sendToast(String text) {
		Message msg = Message.obtain();
		msg.what = SHOW_TOAST;
		msg.obj = text;
		handler.sendMessage(msg);
	}
	
	public void setFlags(int showImageFlag, int finishFlag) {
		this.SHOW_IMAGE = showImageFlag;
		this.FINISH = finishFlag;
	}

	public void setUri(Uri uri)
	{
		this.uri = uri;
	}

	public Uri getUri()
	{
		return uri;
	}

	public void setImage(Bitmap image)
	{
		this.image = image;
	}

	public Bitmap getImage()
	{
		return image;
	}
//
}
