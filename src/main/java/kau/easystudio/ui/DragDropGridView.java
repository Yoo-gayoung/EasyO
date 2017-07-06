package kau.easystudio.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import kau.easystudio.R;

public class DragDropGridView extends GridView
{
	private Context _context;
	private Point _touchPosition = new Point(0, 0);
	private Point _touchPositionOffset = new Point(0, 0);
	private OnDropListener _onDropListener;
	private OnClickListener _onClickListener;


	private int _selectItemIndex = -1;
	private ImageView _dragView;
	private WindowManager _windowManager;
	private WindowManager.LayoutParams _windowParams;




	public DragDropGridView(Context $context, AttributeSet $attrs, int $defStyle)
	{
		super($context, $attrs, $defStyle);
		init($context);
	}
	
	
	public DragDropGridView(Context $context, AttributeSet $attrs)
	{
		super($context, $attrs);
		init($context);
	}
	
	
	public DragDropGridView(Context $context)
	{
		super($context);
		init($context);
	}
	
	
	private void init(Context $context)
	{
		_context = $context;
		setOnItemLongClickListener(onItemLongClickListener);
		setOnItemClickListener(onItemClickListener);
		
		_windowManager = (WindowManager) $context.getSystemService(Context.WINDOW_SERVICE);
		
		_windowParams = new WindowManager.LayoutParams();
		_windowParams.gravity = Gravity.LEFT | Gravity.TOP;
		//_windowParams.height = 80;
		//_windowParams.width = 80;
		_windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		_windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		_windowParams.format = PixelFormat.TRANSLUCENT;

	}
	
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent $e)
	{
		int x = (int) $e.getX();
		int y = (int) $e.getY();
		
		switch ($e.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				_touchPosition.set(x, y);
				break;
		}
		return super.onInterceptTouchEvent($e);
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent $e)
	{
		int x = (int) $e.getX();
		int y = (int) $e.getY();
		
		switch ($e.getAction())
		{
			case MotionEvent.ACTION_MOVE:
				doDragView(x, y);
				break;
			
			case MotionEvent.ACTION_UP:
				doDropView(x, y);
				break;
			
			case MotionEvent.ACTION_CANCEL:
				setNullDragView();
				break;
			
			default:
				break;
		}
		return super.onTouchEvent($e);
	}
	
	
	// 드래그 하는 동안 보일 view
	private void doMakeDragview()
	{
		View item = (View) getChildAt(_selectItemIndex - getFirstVisiblePosition()); //  getFirstVisiblePosition() 이 없으면 스크롤 됐을 때 문제 생김
		item.setDrawingCacheEnabled(true);
		//Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());
		
		ImageView image = new ImageView(_context);
		//image.setImageBitmap(bitmap);
		
		_touchPositionOffset.x = (int) (item.getWidth() * 0.5);
		_touchPositionOffset.y = (int) (item.getHeight() * 0.5);
		
		_windowParams.x = _touchPosition.x - _touchPositionOffset.x;
		_windowParams.y = _touchPosition.y - _touchPositionOffset.y;
		_windowManager.addView(image, _windowParams);
		_dragView = image;
	}
	
	
	// 드래그하기
	private void doDragView(int $x, int $y)
	{
		if (_dragView == null)
			return;
		
		_windowParams.x = $x - _touchPositionOffset.x;
		_windowParams.y = $y - _touchPositionOffset.y;
		_windowManager.updateViewLayout(_dragView, _windowParams);
	}
	
	
	// 드랍
	private void doDropView(int $x, int $y)
	{
		if (_dragView == null)
			return;
		
		int toIndex = pointToPosition($x, $y);
		if (toIndex <= INVALID_POSITION)
		{
			setNullDragView();
			return;
		}
		
		_onDropListener.drop(_selectItemIndex, toIndex);
		setNullDragView();
	}
	
	
	// 드래그 뷰 지우기
	private void setNullDragView()
	{
		if (_dragView != null)
		{
			_windowManager.removeView(_dragView);
			_dragView = null;
		}
	}

	private void doVideoview(){

		_onClickListener.Click(_selectItemIndex);
	}


	/**
	 * 드랍 후에 실행 할 리스터
	 * @param $listener
	 */
	public void setOnDropListener(OnDropListener $listener)
	{
		_onDropListener = $listener;
	}


	public void setOnClickListener(OnClickListener $listener)
	{
		_onClickListener = $listener;
	}



	/****************
	 * Listener
	 ***************/

	OnItemLongClickListener onItemLongClickListener = new OnItemLongClickListener()
	{
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int $position, long arg3)
		{


			if ($position <= INVALID_POSITION)
				return true;
			_selectItemIndex = $position;
			doMakeDragview();
			return true;
		}
	};

	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			_selectItemIndex = position;

			doVideoview();
		}
	};


	
	/************
	 * 인터페이스
	 ************/
	public interface OnDragListener
	{
		void drag(int from, int to);
	}
	
	public interface OnDropListener
	{
		void drop(int from, int to);
	}

	public interface OnClickListener
	{
		void Click(int position);
	}


}
