package com.recognize.views;

import android.content.Context;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;

import android.graphics.Bitmap;

import com.recognize.views.ClipZoomImageView;
import com.recognize.views.CutImageBorderView;


public class ClipImageLayout extends RelativeLayout {
	private ClipZoomImageView mZoomImageView;
	private CutImageBorderView mClipImageView;
	private int mHorizontalPadding = 20;// 框左右的边距，这里左右边距为0，为�?��屏幕宽度的正方形�?

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	public ClipImageLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
 
		mZoomImageView = new ClipZoomImageView (context);
		mClipImageView = new CutImageBorderView(context);
 
		android.view.ViewGroup.LayoutParams lp = new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
 
		this.addView(mZoomImageView, lp);
		this.addView(mClipImageView, lp);
 
		// 计算padding的px
		mHorizontalPadding = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, mHorizontalPadding, getResources()
						.getDisplayMetrics());
		mZoomImageView.setHorizontalPadding(mHorizontalPadding);
		mClipImageView.setHorizontalPadding(mHorizontalPadding);
	}
 
	public void setImageDrawable(Drawable drawable) {
		mZoomImageView.setImageDrawable(drawable);
	}
 
	public void setImageBitmap(Bitmap bitmap) {
		mZoomImageView.setImageBitmap(bitmap);
	}
 
	/**
	 * 对外公布设置边距的方�?单位为dp
	 * 
	 * @param mHorizontalPadding
	 */
	public void setHorizontalPadding(int mHorizontalPadding) {
		this.mHorizontalPadding = mHorizontalPadding;
	}
 
	/**
	 * 裁切图片
	 * 
	 * @return
	 */
	public Bitmap clip() {
		return mZoomImageView.clip();
	}
}