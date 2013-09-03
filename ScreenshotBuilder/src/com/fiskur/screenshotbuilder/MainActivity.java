package com.fiskur.screenshotbuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import com.fiskur.screenshotbuilder.ScreenshotAdapter.Screenshot;

public class MainActivity extends Activity {

	private static final int MODE_ROW = 1;
	private static final int MODE_X2 = 2;
	private static final int MODE_COL = 4;
	private static int mode = MODE_ROW;

	private static final int ACTION_GALLERY = 100;

	private GridView gridView;
	private ScreenshotAdapter screenshotAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		screenshotAdapter = new ScreenshotAdapter(this);
		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(screenshotAdapter);
		
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		
		if(Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null){
			if(type.startsWith("image/")){
				ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
				if(imageUris != null){
					for(Uri uri : imageUris){
						try {
							if (mode == MODE_ROW) {
								gridView.setNumColumns(gridView.getCount() + 1);
							}

							Drawable drawable = Drawable.createFromStream(getContentResolver().openInputStream(uri), "s");
							screenshotAdapter.addItem(uri, drawable, System.currentTimeMillis());

							gridView.invalidate();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}else{
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			startActivityForResult(photoPickerIntent, ACTION_GALLERY);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTION_GALLERY:
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				Log.d("X", "Uri path: " + uri.getPath() + " uri.toString(): " + uri.toString());
				try {
					if (mode == MODE_ROW) {
						gridView.setNumColumns(gridView.getCount() + 1);
					}

					Drawable drawable = Drawable.createFromStream(getContentResolver().openInputStream(uri), "s");
					screenshotAdapter.addItem(uri, drawable, System.currentTimeMillis());

					gridView.invalidate();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (R.id.action_view == item.getItemId()) {
			switch (mode) {
			case MODE_ROW:
				gridView.setNumColumns(2);
				item.setIcon(R.drawable.grid);
				mode = MODE_X2;
				break;
			case MODE_X2:
				gridView.setNumColumns(1);
				item.setIcon(R.drawable.column);
				mode = MODE_COL;
				break;
			case MODE_COL:
				gridView.setNumColumns(gridView.getCount());
				item.setIcon(R.drawable.row);
				mode = MODE_ROW;
				break;
			}
			
			//Failed attempt to fix redraw issue when layout mode is changed after scrolling to bottom
			/*
			gridView.postDelayed(new Runnable(){

				@Override
				public void run() {
					l("Delayed action");
					//TODO
				}}, 1000);
				*/
		}
		if (R.id.action_share == item.getItemId()) {
			int numItems = screenshotAdapter.getCount();
			if (numItems < 1) {
				return super.onOptionsItemSelected(item);
			}
			List<Screenshot> screenshots = screenshotAdapter.getScreenshots();

			int i = 0;
			int width = 0;
			boolean sizeError = false;
			for (Screenshot screenshot : screenshots) {
				int thisWidth = screenshot.drawable.getIntrinsicWidth();
				if (i > 0 && width != thisWidth) {
					sizeError = true;
					Toast.makeText(MainActivity.this, "Images are not all the same size", Toast.LENGTH_LONG).show();
				}
				width = thisWidth;
				i++;
			}
			if (!sizeError) {
				Bitmap stitched = buildBitmap();
				if (stitched != null) {
					saveShareBitmap(stitched);
				}
			}
		}
		if (R.id.action_get == item.getItemId()) {
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			startActivityForResult(photoPickerIntent, ACTION_GALLERY);
		}
		if (R.id.action_about == item.getItemId()) {
			Intent aboutIntent = new Intent(MainActivity.this.getApplication(), AboutActivity.class);
			startActivity(aboutIntent);
		}
		return super.onOptionsItemSelected(item);
	}

	private void showAlert(String error) {
		new AlertDialog.Builder(MainActivity.this).setMessage(error).setCancelable(true).create().show();
	}

	private void saveShareBitmap(Bitmap output) {
		String storageState = Environment.getExternalStorageState();
		if (storageState.equals(Environment.MEDIA_MOUNTED)) {
			if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState)) {
				showAlert("Storage is read only");
			}
			String imageFile = "" + System.currentTimeMillis() + ".jpg";
			File imgDir = getExternalCacheDir();
			imgDir.mkdirs();
			File cacheFile = new File(imgDir, imageFile);

			try {
				cacheFile.createNewFile();
				String photoPath = cacheFile.getAbsolutePath();
				try {
					FileOutputStream out = new FileOutputStream(photoPath);
					output.compress(Bitmap.CompressFormat.PNG, 90, out);
				} catch (Exception e) {
					e.printStackTrace();
				}

				Uri _fileUri = Uri.fromFile(cacheFile);

				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
				sharingIntent.setType("image/*");
				sharingIntent.putExtra(Intent.EXTRA_TEXT, "[screenshot tool by fiskur.eu]");
				sharingIntent.putExtra(Intent.EXTRA_STREAM, _fileUri);
				startActivity(Intent.createChooser(sharingIntent, "Share screenshots"));
			} catch (IOException e) {
				showAlert("Could not create file: " + cacheFile.getAbsolutePath() + " error: " + e.toString());
			}
		} else {
			showAlert("External Storage (SD Card) is required.\n\nCurrent state: " + storageState);
		}
	}

	private Bitmap buildBitmap() {
		int numCols = gridView.getNumColumns();
		int numPics = gridView.getCount();

		List<Screenshot> screenshots = screenshotAdapter.getScreenshots();
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;

		Bitmap outputBitmap = Bitmap.createBitmap(width * numPics, height, Config.ARGB_8888);
		int numRows;
		switch (mode) {
		case MODE_ROW:
			numRows = 1;
			outputBitmap = Bitmap.createBitmap(width * numPics, height, Config.ARGB_8888);
			break;
		case MODE_X2:
			numRows = numPics / 2 + numPics % 2;
			l("X2 calculations, numPics: " + numPics + " numCols: " + numCols + " numRows: " + numRows);
			outputBitmap = Bitmap.createBitmap(width * 2, height * numRows, Config.ARGB_8888);
			break;
		case MODE_COL:
			numRows = numPics;
			outputBitmap = Bitmap.createBitmap(width, height * numPics, Config.ARGB_8888);
			break;
		}

		Canvas outputCanvas = new Canvas(outputBitmap);
		Rect src, dest;

		int i = 1;
		int rowNum = -1;
		int colNum = 0;
		for (Screenshot screenshot : screenshots) {
			try {
				switch (mode) {
				case MODE_ROW:
					colNum = i - 1;
					rowNum = 0;
					break;
				case MODE_X2:
					if (i % 2 == 1) {
						rowNum++;
						colNum = 0;
					} else {
						colNum = 1;
					}
					//l("Processing image: " + i + " colNum: " + colNum + " rowNum: " + rowNum);
					break;
				case MODE_COL:
					colNum = 0;
					rowNum = i - 1;
					break;
				}

				Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(screenshot.getPath()), null, null);
				src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
				dest = new Rect(src);
				dest.offset(colNum * bitmap.getWidth(), rowNum * bitmap.getHeight());
				outputCanvas.drawBitmap(bitmap, src, dest, null);
				i++;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		return outputBitmap;
	}

	private void l(String message) {
		Log.d("Screens", message);
	}
}
