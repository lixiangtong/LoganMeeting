package com.logansoft.lubo.loganmeeting.utils;

import android.content.Context;
import android.util.AttributeSet;

import com.cloudroom.cloudroomvideosdk.model.UsrVideoId;

public class YUVVideoView extends YUVView {

	private UsrVideoId mUsrVideoId = null;

	public YUVVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public UsrVideoId getUsrVideoId() {
		return mUsrVideoId;
	}

	public void setUsrVideoId(UsrVideoId usrVideoId) {
		this.mUsrVideoId = usrVideoId;
		if (usrVideoId == null) {
			getYUVRender().update(null, 0, 0);
		}
	}

}
