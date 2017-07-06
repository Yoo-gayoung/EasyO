package kau.easystudio.multiselectview;


import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class Utils {


	public static int convertDpToPx(Context ctx, float dp) {
		float d = ctx.getResources().getDisplayMetrics().density;
		return (int) (d * dp);
	}

	public static ArrayList<AlbumsModel> getAllDirectoriesWithImages(
			Cursor cursor) {
		if (cursor == null) {
			return null;
		}
		cursor.moveToFirst();
		int size = cursor.getCount();

		TreeSet<String> folderPathList = new TreeSet<String>();
		ArrayList<AlbumsModel> albumsModels = new ArrayList<AlbumsModel>();
		HashMap<String, AlbumsModel> map = new HashMap<String, AlbumsModel>();

		String imgPath, folderPath;
		AlbumsModel tempAlbumsModel;
		for (int i = 0; i < size; i++) {
			imgPath = cursor.getString(0).trim();
			folderPath = imgPath.substring(0, imgPath.lastIndexOf("/"));
			if (folderPathList.add(folderPath)) {
				AlbumsModel gm = new AlbumsModel ();
                String folderName = gm.getFolderName ();
                String folderImagePath = gm.getFolderName ();

                gm.folderName = folderPath.substring(
						folderPath.lastIndexOf("/") + 1, folderPath.length());
				gm.folderImages.add(imgPath);
				gm.folderImagePath = imgPath;
				albumsModels.add(gm);
				map.put(folderPath, gm);
			} else if (folderPathList.contains(folderPath)) {
				tempAlbumsModel = map.get(folderPath);
				tempAlbumsModel.folderImages.add(imgPath);
			}
			cursor.moveToNext();
		}
		return albumsModels;
	}

}
