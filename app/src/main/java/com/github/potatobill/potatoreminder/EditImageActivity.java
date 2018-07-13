package com.github.potatobill.potatoreminder;
import android.app.Activity;
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
import com.github.potatobill.potatoreminder.data.EntryData;
import java.io.File;
import android.database.Cursor;

public class EditImageActivity extends Activity implements OnClickListener
{
	public final static int CAPTURE = 1;
	public final static int PICTURE = 2;
	private Handler mHandler;
	/*
	App app;
	int id;
	Uri uriImage;
	Bitmap tempBitmap;
	String tempPath;
	*/
	int id;
	
	TextView tvPosition;
	EditText etTitle;
	EditText etHint;
	EditText etLevel;
	ImageView ivImage;
	Button btCapture;
	Button btPicture;
	
	ImageHolder imageHolder;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		id = getIntent().getExtras().getInt("id");
		setContentView(R.layout.activity_edit_image);
		setView();
		mHandler = new MHandler();
		imageHolder = new ImageHolder((App) getApplicationContext(), mHandler, Constant.EditImage.MHANDLE_SHOW_TOAST, Constant.EditImage.MHANDLE_SHOW_IMAGE, Constant.EditImage.MHANDLE_FINISH);
		if (savedInstanceState!=null) {
			etTitle.setText(savedInstanceState.getString("title"));
			etHint.setText(savedInstanceState.getString("hint"));
			etLevel.setText(savedInstanceState.getString("level"));
			Uri uri = savedInstanceState.getParcelable("uri");
			if (uri != null) {
				imageHolder.setUri(uri);
			}
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.editButtonCapture:
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

			case R.id.editButtonPicture:
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

			case R.id.editImageViewImage:
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
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		String title = etTitle.getText().toString();
		String hint = etHint.getText().toString();
		String level = etLevel.getText().toString();
		outState.putString("title",title);
		outState.putString("hint",hint);
		outState.putString("level",level);
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
		tvPosition.setText(app.dm.getPositionString(app.position) + ">");
		Bundle b = app.dm.readEntry(id);
		etTitle.setText(b.getString("title"));
		etHint.setText(b.getString("hint"));
		etLevel.setText(String.valueOf(b.getDouble("level")));
		if(imageHolder.getUri() == null) {
			autoSetSize();
			imageHolder.decodeByte(b.getByteArray("point"));
		} else {
			autoSetSize();
			imageHolder.decodeImage();
		}
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate main_menu.xml 
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		String t = etTitle.getText().toString();
		String h = etHint.getText().toString();
		String rl = etLevel.getText().toString();
		double l;
		switch (item.getItemId())
		{
			case R.id.editMenuConfirm:
				try
				{
					l = Double.parseDouble(rl);
					imageHolder.updateImage(id,t,h,l);
				}
				catch (NumberFormatException e)
				{
					showToast("熟练度不是数字");
				}
				break;
			case R.id.editMenuDelete:
				App app = (App) getApplicationContext();
				app.dm.deleteEntry(id);
				showToast("删除成功");
				finish();
				break;
			case R.id.editMenuCopy:
			case R.id.editMenuToList:
			case R.id.editMenuToNode:
				showToast("功能未实现");
				break;
			case R.id.editMenuToPoint:
				showToast("已经是图片知识点");
				break;
			case R.id.editMenuMove:
				Intent intent = new Intent(this,MoveActivity.class);
				intent.putExtra("type", EntryData.TYPE_IMAGE);
				intent.putExtra("id", id);
				startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	
	
	private void showToast(String text)
	{
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	private void showImage() {
		Bitmap image = imageHolder.getImage();
		if (image != null) {
			ivImage.setImageBitmap(image);
		}
	}
	
	private void autoSetSize() {
		
	}
	
	private void setView()
	{
		tvPosition = (TextView) findViewById(R.id.editTextViewPosition);
		etTitle = (EditText) findViewById(R.id.editEditTextTitle);
		etHint = (EditText) findViewById(R.id.editEditTextHint);
		etLevel = (EditText) findViewById(R.id.editEditTextLevel);
		ivImage = (ImageView) findViewById(R.id.editImageViewImage);
		btCapture = (Button) findViewById(R.id.editButtonCapture);
		btPicture = (Button) findViewById(R.id.editButtonPicture);
		btCapture.setOnClickListener(this);
		btPicture.setOnClickListener(this);
	}
	
	private class MHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case Constant.EditImage.MHANDLE_SHOW_TOAST:
					showToast((String) msg.obj);
					break;
				case Constant.EditImage.MHANDLE_SHOW_IMAGE:
					showImage();
					break;
				case Constant.EditImage.MHANDLE_FINISH:
					finish();
					break;
			}
		}
	}
}
