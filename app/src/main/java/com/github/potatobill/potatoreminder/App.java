package com.github.potatobill.potatoreminder;

import android.app.Application;

import com.github.potatobill.potatoreminder.data.DataManager;
import java.io.File;
import android.os.Bundle;
import java.util.ArrayList;
import com.github.potatobill.potatoreminder.data.EntryPoint;
import android.graphics.Bitmap;

public class App extends Application {
	public DataManager dm;
	public boolean firstIn;
	public int position;
	public File imagePath;

	public Bundle tempEntry;
	public ArrayList<EntryPoint> tempList;
	public Bitmap tempBitmap;

	@Override
	public void onCreate() {
		super.onCreate();
		position = 0;
		firstIn = false;
	}
}
