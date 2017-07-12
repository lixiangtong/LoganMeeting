package com.logansoft.lubo.loganmeeting.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class YUVFrameRender implements Renderer {

	@SuppressWarnings("unused")
	private static final String TAG = "YUVFrameRender";

	private GLSurfaceView mYUVSurface;
	private YUVProgram mYUVProgram = new YUVProgram();
	private int mVideoWidth = 0, mVideoHeight = 0;
	private int mShowWidth = 0, mShowHeight = 0;
	private ByteBuffer mYBuffer;
	private ByteBuffer mUBuffer;
	private ByteBuffer mVBuffer;
	private ReentrantReadWriteLock mBufferLock = new ReentrantReadWriteLock();

	public static boolean detectOpenGLES20(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ConfigurationInfo info = am.getDeviceConfigurationInfo();
		return (info.reqGlEsVersion >= 0x20000);
	}

	public YUVFrameRender(GLSurfaceView surface) {
		mYUVSurface = surface;

		mYUVSurface.setEGLContextClientVersion(2);
		mYUVSurface.setRenderer(this);
		mYUVSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		if (!mYUVProgram.isProgramBuilt()) {
			mYUVProgram.buildProgram();
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);

		updateShowSize();
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		mBufferLock.readLock().lock();
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		// Log.d(TAG, String.format(
		// "onDrawFrame mYBuffer:%s width:%d height:%d",
		// mYBuffer == null ? "null" : "notnull", mVideoWidth,
		// mVideoHeight));
		if (mYBuffer != null && this.mVideoWidth > 0 && this.mVideoHeight > 0) {
			// reset position, have to be done
			mYBuffer.position(0);
			mUBuffer.position(0);
			mVBuffer.position(0);
			mYUVProgram.buildTextures(mYBuffer, mUBuffer, mVBuffer,
					mVideoWidth, mVideoHeight);
			mYUVProgram.drawFrame();
		}
		GLES20.glFinish();
		mBufferLock.readLock().unlock();
	}

	public void update(byte[] yuvData, int w, int h) {
		mBufferLock.writeLock().lock();
		if (w > 0 && h > 0) {
			int yarraySize = w * h;
			int uvarraySize = yarraySize / 4;
			if (w != mVideoWidth || h != mVideoHeight) {
				this.mVideoWidth = w;
				this.mVideoHeight = h;
				mYBuffer = ByteBuffer.allocate(yarraySize);
				mUBuffer = ByteBuffer.allocate(uvarraySize);
				mVBuffer = ByteBuffer.allocate(uvarraySize);
				updateShowSize();
			}
			mYBuffer.clear();
			mUBuffer.clear();
			mVBuffer.clear();
			mYBuffer.put(yuvData, 0, yarraySize);
			mUBuffer.put(yuvData, yarraySize, uvarraySize);
			mVBuffer.put(yuvData, yarraySize + uvarraySize, uvarraySize);
		} else {
			this.mVideoWidth = w;
			this.mVideoHeight = h;
		}
		mBufferLock.writeLock().unlock();
		// request to render
		mYUVSurface.requestRender();
	}

	private void updateShowSize() {
		int sw = 0, sh = 0;
		if (mVideoWidth > 0 && mVideoHeight > 0) {
			int vWidth = mYUVSurface.getWidth();
			int vHeight = mYUVSurface.getHeight();
			float dw = (float) vWidth / mVideoWidth;
			int h = (int) (dw * mVideoHeight);
			if (h > mYUVSurface.getHeight()) {
				float dh = (float) vHeight / mVideoHeight;
				sw = (int) (dh * mVideoWidth);
				sh = mYUVSurface.getHeight();
			} else {
				sw = mYUVSurface.getWidth();
				sh = h;
			}
		}
		if (sw != mShowWidth || sh != mShowHeight) {
			mShowWidth = sw;
			mShowHeight = sh;
			setCoordinates(mShowWidth, mShowHeight);
		}
	}

	public void setCoordinates(int width, int height) {
		float dw = (float) width / mYUVSurface.getWidth();
		float dh = (float) height / mYUVSurface.getHeight();
		mYUVProgram.setCoordinates(0 - dw, 0 - dh, dw, dh);
	}
}
