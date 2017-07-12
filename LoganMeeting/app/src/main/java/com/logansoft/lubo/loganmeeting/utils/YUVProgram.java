package com.logansoft.lubo.loganmeeting.utils;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class YUVProgram {

	private final static String TAG = "YUVProgram";

	private boolean mBProgBuilt = false;

	private int mPositionHandle;
	private int mProgram;
	private int mCoordHandle;
	private int mVideoWidth;
	private int mVideoHeight;

	private int[] mTextureIds = new int[3];

	private ByteBuffer mCoordBuffer; // buffer holding the texture coordinates

	private ByteBuffer mVerticeBuffer; // buffer holding the vertices

	// 顶点数组(物体表面坐标取值范围是-1到1,数组坐标：左下，右下，左上，右上)
	private float[] mVertexVertices = { -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f,
			1.0f, 1.0f, };

	// 像素，纹理数组(纹理坐标取值范围是0-1，坐标原点位于左下角,数组坐标：左上，右上，左下，右下,如果先左下，图像会倒过来)
	private float[] mTextureVertices = { 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f,
			1.0f, 0.0f, };

	private static final String VERTEX_SHADER = "attribute vec4 aPosition;\n"
			+ "attribute vec2 aTextureCoord;\n"
			+ "varying vec2 vTextureCoord;\n" + "void main() {\n"
			+ "  gl_Position = aPosition;\n"
			+ "  vTextureCoord = aTextureCoord;\n" + "}\n";

	// The fragment shader.
	// Do YUV to RGB565 conversion.
	private static final String FRAGMENT_SHADER = "precision mediump float;\n"
			+ "uniform sampler2D Ytex;\n" + "uniform sampler2D Utex,Vtex;\n"
			+ "varying vec2 vTextureCoord;\n" + "void main(void) {\n"
			+ "  float r,g,b,y,u,v;\n" + "  float nx=vTextureCoord[0];\n"
			+ "  float ny=vTextureCoord[1];\n"
			+ "  y=texture2D(Ytex,vec2(nx,ny)).r;\n"
			+ "  u=texture2D(Utex,vec2(nx,ny)).r;\n"
			+ "  v=texture2D(Vtex,vec2(nx,ny)).r;\n"
			+ "  y=1.1643*(y-0.0625);\n" + "  u=u-0.5;\n" + "  v=v-0.5;\n"
			+ "  r=y+1.5958*v;\n" + "  g=y-0.39173*u-0.81290*v;\n"
			+ "  b=y+2.017*u;\n" + "  gl_FragColor=vec4(r,g,b,1.0);\n" + "}\n";

	public YUVProgram() {

	}

	public boolean isProgramBuilt() {
		return mBProgBuilt;
	}

	public void buildProgram() {
		if (mProgram <= 0) {
			mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
		}
		Log.d(TAG, "buildProgram _program = " + mProgram);
 
		/*
		 * get handle for "vPosition" and "a_texCoord"
		 */
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
		Log.d(TAG, "buildProgram _positionHandle = " + mPositionHandle);
		checkGlError("glGetAttribLocation aPosition");
		if (mPositionHandle == -1) {
			throw new RuntimeException(
					"Could not get attribute location for aPosition");
		}
		mCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
		Log.d(TAG, "buildProgram _coordHandle = " + mCoordHandle);
		checkGlError("glGetAttribLocation aTextureCoord");
		if (mCoordHandle == -1) {
			throw new RuntimeException(
					"Could not get attribute location for aTextureCoord");
		}

		/*
		 * get uniform location for y/u/v, we pass data through these uniforms
		 */
		int yHandle = GLES20.glGetUniformLocation(mProgram, "Ytex");
		Log.d(TAG, "buildProgram yhandle = " + yHandle);
		checkGlError("glGetUniformLocation Ytex");
		if (yHandle == -1) {
			throw new RuntimeException(
					"Could not get uniform location for Ytex");
		}
		int uHandle = GLES20.glGetUniformLocation(mProgram, "Utex");
		Log.d(TAG, "buildProgram uhandle = " + uHandle);
		checkGlError("glGetUniformLocation Utex");
		if (uHandle == -1) {
			throw new RuntimeException(
					"Could not get uniform location for Utex");
		}
		int vHandle = GLES20.glGetUniformLocation(mProgram, "Vtex");
		Log.d(TAG, "buildProgram vhandle = " + vHandle);
		checkGlError("glGetUniformLocation Vtex");
		if (vHandle == -1) {
			throw new RuntimeException(
					"Could not get uniform location for Vtex");
		}

		mBProgBuilt = true;
	}

	/**
	 * build a set of textures, one for Y, one for U, and one for V.
	 */
	public void buildTextures(Buffer y, Buffer u, Buffer v, int width,
			int height) {
		if (width != mVideoWidth || height != mVideoHeight) {
			if (mVideoWidth > 0 && mVideoHeight > 0) {
				GLES20.glDeleteTextures(3, mTextureIds, 0);
			}
			mVideoWidth = width;
			mVideoHeight = height;
			Log.d(TAG, "buildTextures videoSizeChanged: w=" + mVideoWidth
					+ " h=" + mVideoHeight);
			if (mVideoWidth > 0 && mVideoHeight > 0) {
				GLES20.glGenTextures(3, mTextureIds, 0);
				Log.d(TAG, "glGenTextures Y = " + mTextureIds[0] + " U = "
						+ mTextureIds[1] + "  V = " + mTextureIds[2]);
			} else {
				return;
			}
		}

		// building texture for Y data
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		buildTextures(mTextureIds[0], y, mVideoWidth, mVideoHeight);

		// building texture for U data
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		buildTextures(mTextureIds[1], u, mVideoWidth / 2, mVideoHeight / 2);

		// building texture for V data
		GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
		buildTextures(mTextureIds[2], v, mVideoWidth / 2, mVideoHeight / 2);
	}

	public void buildTextures(int texture, Buffer planer, int width, int height) {
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
		checkGlError("glBindTexture");
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
				width, height, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE,
				planer);
		checkGlError("glTexImage2D");
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_CLAMP_TO_EDGE);
	}

	/**
	 * render the frame the YUV data will be converted to RGB by shader.
	 */
	public void drawFrame() {
		updateBuffers();

		GLES20.glUseProgram(mProgram);
		checkGlError("glUseProgram");

		GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT,
				false, 8, mVerticeBuffer);
		checkGlError("glVertexAttribPointer mPositionHandle");
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		GLES20.glVertexAttribPointer(mCoordHandle, 2, GLES20.GL_FLOAT, false,
				8, mCoordBuffer);
		checkGlError("glVertexAttribPointer maTextureHandle");
		GLES20.glEnableVertexAttribArray(mCoordHandle);

		// bind textures
		int yHandle = GLES20.glGetUniformLocation(mProgram, "Ytex");
		checkGlError("glGetUniformLocation Ytex");
		GLES20.glUniform1i(yHandle, 0);

		int uHandle = GLES20.glGetUniformLocation(mProgram, "Utex");
		checkGlError("glGetUniformLocation Utex");
		GLES20.glUniform1i(uHandle, 1);

		int vHandle = GLES20.glGetUniformLocation(mProgram, "Vtex");
		checkGlError("glGetUniformLocation Vtex");
		GLES20.glUniform1i(vHandle, 2);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mCoordHandle);
	}

	/**
	 * create program and load shaders, fragment shader is very important.
	 */
	public int createProgram(String vertexSource, String fragmentSource) {
		// create shaders
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
		int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
		// just check
		Log.d(TAG, "createProgram vertexShader = " + vertexShader);
		Log.d(TAG, "createProgram pixelShader = " + pixelShader);

		int program = GLES20.glCreateProgram();
		if (program != 0) {
			GLES20.glAttachShader(program, vertexShader);
			checkGlError("glAttachShader");
			GLES20.glAttachShader(program, pixelShader);
			checkGlError("glAttachShader");
			GLES20.glLinkProgram(program);
			int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
			if (linkStatus[0] != GLES20.GL_TRUE) {
				Log.e(TAG, "Could not link program: ", null);
				Log.e(TAG, GLES20.glGetProgramInfoLog(program), null);
				GLES20.glDeleteProgram(program);
				program = 0;
			}
		}
		return program;
	}

	/**
	 * create shader with given source.
	 */
	private int loadShader(int shaderType, String source) {
		Log.d(TAG, "loadShader");
		int shader = GLES20.glCreateShader(shaderType);
		if (shader != 0) {
			GLES20.glShaderSource(shader, source);
			GLES20.glCompileShader(shader);
			int[] compiled = new int[1];
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {
				Log.e(TAG, "Could not compile shader " + shaderType + ":", null);
				Log.e(TAG, GLES20.glGetShaderInfoLog(shader), null);
				GLES20.glDeleteShader(shader);
				shader = 0;
			}
		}
		return shader;
	}

	/**
	 * these two buffers are used for holding vertices, screen vertices and
	 * texture vertices.
	 */
	public void updateBuffers() {
		float[] vert = mVertexVertices;
		float[] coord = mTextureVertices;
		if (mVerticeBuffer == null) {
			mVerticeBuffer = ByteBuffer.allocateDirect(vert.length * 4);
			mVerticeBuffer.order(ByteOrder.nativeOrder());
			mVerticeBuffer.asFloatBuffer().put(vert);
			mVerticeBuffer.position(0);
		}

		if (mCoordBuffer == null) {
			mCoordBuffer = ByteBuffer.allocateDirect(coord.length * 4);
			mCoordBuffer.order(ByteOrder.nativeOrder());
			mCoordBuffer.asFloatBuffer().put(coord);
			mCoordBuffer.position(0);
		}
	}

	// SetCoordinates
	// Sets the coordinates where the stream shall be rendered.
	// Values must be between 0 and 1.
	public void setCoordinates(float left, float top, float right, float bottom) {
		// if ((top > 0 || top < -1) || (right > 1 || right < 0)
		// || (bottom > 1 || bottom < 0) || (left > 0 || left < -1)) {
		// return;
		// }

		// Bottom Left
		mVertexVertices[0] = left;
		mVertexVertices[1] = top;

		// Bottom Right
		mVertexVertices[2] = right;
		mVertexVertices[3] = top;

		// Top Right
		mVertexVertices[4] = left;
		mVertexVertices[5] = bottom;

		// Top Left
		mVertexVertices[6] = right;
		mVertexVertices[7] = bottom;

		mVerticeBuffer = null;

		updateBuffers();
	}

	private void checkGlError(String op) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, "checkGlError option:" + op + ": glError " + error, null);
			throw new RuntimeException(op + ": glError " + error);
		}
	}

}
