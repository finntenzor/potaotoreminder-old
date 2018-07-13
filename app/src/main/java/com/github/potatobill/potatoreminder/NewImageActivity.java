package com.github.potatobill.potatoreminder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.potatobill.potatoreminder.data.DataManager;
import com.github.potatobill.potatoreminder.data.ImageUtils;
import java.io.File;
import java.io.IOException;
import android.text.Layout;
import android.widget.LinearLayout;

public class NewImageActivity extends Activity implements OnClickListener
{
	public final static int CAPTURE = 1;
	public final static int PICTURE = 2;
	
	TextView tvPosition;
	EditText etTitle;
	EditText etHint;
	ImageView ivImage;
	Button btCapture;
	Button btPicture;
	MHandler mHandler;
	ImageHolder imageHolder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_image);
		setView();
		mHandler = new MHandler();
		imageHolder = new ImageHolder((App) getApplicationContext(), mHandler, Constant.NewImage.MHANDLE_SHOW_TOAST, Constant.NewImage.MHANDLE_SHOW_IMAGE, Constant.NewImage.MHANDLE_FINISH);
		if (savedInstanceState!=null)
		{
			String title = savedInstanceState.getString("title");
			String hint = savedInstanceState.getString("hint");
			Uri uri = savedInstanceState.getParcelable("uri");
			etTitle.setText(title);
			etHint.setHint(hint);
			if (uri != null) {
				imageHolder.setUri(uri);
			}
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString("title",etTitle.getText().toString());
		outState.putString("hint",etHint.getText().toString());
		Uri uri = imageHolder.getUri();
		if (uri != null) {
			outState.putParcelable("uri",uri);
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		App app = (App) getApplicationContext();
		String t = app.dm.getPositionString(app.position);
		t = t + "/>新建图片知识点";
		tvPosition.setText(t);
		if(imageHolder.getUri() != null) {
			autoSetSize();
			imageHolder.decodeImage();
		}
	}
	
	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.newImageButtonCapture:
				Intent t = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				
				//生成应该存储图片的uri
				App app = (App) getApplicationContext();
				String imageName = "image_" + String.valueOf(System.currentTimeMillis()) + ".png";
				Uri uri = Uri.fromFile(new File(app.imagePath,imageName));
				
				//保存uri
				imageHolder.setUri(uri);
				t.putExtra(MediaStore.EXTRA_OUTPUT, uri);
				startActivityForResult(t,CAPTURE);
				break;
				
			case R.id.newImageButtonPicture:
				Intent t2;
				if (Build.VERSION.SDK_INT >= 19) { //19代表KITKAT
					t2 = new Intent(Intent.ACTION_OPEN_DOCUMENT);
				} else {
					t2 = new Intent(Intent.ACTION_GET_CONTENT);
				}
				
				t2.setType("image/*");
				t2.putExtra("crop", true);
				t2.putExtra("return-data", true);
				startActivityForResult(t2, PICTURE);
				break;
				
			case R.id.newImageImageViewImage:
				showImage();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK)
		{
			switch (requestCode)
			{
				case CAPTURE:
					//调用读取图片
					autoSetSize();
					imageHolder.decodeImage();
					break;
				case PICTURE:
					if (data.getData()==null) {
						showToast("图片地址有误 您选择图片了吗？");
						break;
					}
					
					//先setUri再读取图片
					autoSetSize();
					imageHolder.setUri(data.getData());
					imageHolder.decodeImage();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu._new, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.mainMenuNew:
				String t = etTitle.getText().toString();
				String h = etHint.getText().toString();
				imageHolder.saveImage(t,h);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setView()
	{
		tvPosition = (TextView) findViewById(R.id.newImageTextViewPosition);
		etTitle = (EditText) findViewById(R.id.newImageEditTextTitle);
		etHint = (EditText) findViewById(R.id.newImageEditTextHint);
		ivImage = (ImageView) findViewById(R.id.newImageImageViewImage);
		btCapture = (Button) findViewById(R.id.newImageButtonCapture);
		btPicture = (Button) findViewById(R.id.newImageButtonPicture);
		btCapture.setOnClickListener(this);
		btPicture.setOnClickListener(this);
	}
	
	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	private void showImage() {
		Bitmap image = imageHolder.getImage();
		if (image != null) {
			ivImage.setImageBitmap(image);
		}
	}
	
	private void autoSetSize() {
		//LinearLayout frame = (LinearLayout) findViewById(R.id.newImageLinearLayoutFrame);
		//imageHolder.setDecodeSize(ivImage.getMaxWidth(), ivImage.getMaxHeight());
	}
	
	private class MHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case Constant.NewImage.MHANDLE_SHOW_TOAST:
					showToast((String) msg.obj);
					break;
				case Constant.NewImage.MHANDLE_SHOW_IMAGE:
					showImage();
					break;
				case Constant.NewImage.MHANDLE_FINISH:
					finish();
					break;
			}
		}
	}
}
