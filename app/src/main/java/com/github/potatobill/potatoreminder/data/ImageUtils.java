package com.github.potatobill.potatoreminder.data;
import android.media.ThumbnailUtils;
import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.content.Context;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.util.Log;

public class ImageUtils
{
	public final static int MAX_SIZE = 500;
	public final static Bitmap.CompressFormat IMAGE_FORMAT = Bitmap.CompressFormat.JPEG;
	//默认图片最大大小为500，以免数据库爆炸
	//ThumbnailUtils中的方法操作图片
	public final static byte[] uriToBytes(Context context, Uri uri, int quality) throws FileNotFoundException, IOException {
		Bitmap source = uriToBitmap(context,uri);
		if (source == null) {
			return null;
		}
		Bitmap tempBitmap;
		int sourceHeight = 1000; //source.getHeight();
		int sourceWidth = 1000; //source.getWidth();
		/*
		if (sourceHeight > sourceWidth) {
			tempBitmap = ThumbnailUtils.extractThumbnail(source, MAX_SIZE * sourceWidth / sourceHeight, MAX_SIZE);
		} else {
			tempBitmap = ThumbnailUtils.extractThumbnail(source, MAX_SIZE, MAX_SIZE * sourceHeight / sourceWidth);
		}
		*/
		tempBitmap = ThumbnailUtils.extractThumbnail(source, 500, 500);
		ByteArrayOutputStream baos =  new ByteArrayOutputStream();
		tempBitmap.compress(IMAGE_FORMAT, quality, baos);
		tempBitmap.recycle();
		return baos.toByteArray();
	}
	
	public final static Bitmap uriToBitmap(Context context, Uri uri) throws FileNotFoundException, IOException {
		if (context == null) {
			return null;
		}
		if (uri == null) {
			return null;
		}
		InputStream input = context.getContentResolver().openInputStream(uri);
		Bitmap source = null;
		try {
			source = BitmapFactory.decodeStream(input);
			return source;
		} finally {
			input.close();
		}
	}
	
	public final static Bitmap uriToBitmap(Context context, Uri uri, int width, int height) throws FileNotFoundException, IOException {
		if (context == null) {
			return null;
		}
		if (uri == null) {
			return null;
		}
		//Log.e("potato",uri.toString());
		Bitmap source = uriToBitmap(context, uri);
		Bitmap tempBitmap = null;
		if (source != null) {
			tempBitmap = ThumbnailUtils.extractThumbnail(source, 500, 500);
			source.recycle();
		} 
		return tempBitmap;
	}
	
	public final static Bitmap bytesToBitmap(byte[] source, int width, int height) {
		BitmapFactory.Options ops = new BitmapFactory.Options();
		ops.outWidth = width;
		ops.outHeight = height;
		ops.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(source, 0, source.length, ops);
	}
}
