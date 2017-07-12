package com.logansoft.lubo.loganmeeting.utils;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class YUVView extends GLSurfaceView {

	private YUVFrameRender mYUVRender = null;

	public YUVView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mYUVRender = new YUVFrameRender(this);
	}

	public YUVFrameRender getYUVRender() {
		return mYUVRender;
	}

	public void update(byte[] yuvData, int w, int h) {
		mYUVRender.update(yuvData, w, h);
	}
}
