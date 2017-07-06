package kau.easystudio.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class AppListItem
{
	long _id;
	CharSequence _auid;
	Bitmap _thumb;
	String _path;

	public AppListItem(int $id, CharSequence $auid, Bitmap $thumb, String $path)
	{
		_auid = $auid;
		_thumb = $thumb;
		_path = $path;
	}

}
