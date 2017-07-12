package com.logansoft.lubo.loganmeeting.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.logansoft.lubo.loganmeeting.R;


@SuppressLint("InflateParams")
public class UITool {

	private static final String TAG = "LiveControlTool";

	public interface ConfirmDialogCallback {
		void onOk();

		void onCancel();
	}

	private static class ConfirmDialog extends Dialog {

		public boolean ok = false;

		public ConfirmDialog(Context context, int theme) {
			super(context, theme);
			// TODO Auto-generated constructor stub
		}

	}

	public static Dialog showConfirmDialog(Context context, String message,
			ConfirmDialogCallback callback) {
		return showConfirmDialog(context, message, callback, true);
	}

	public static Dialog showConfirmDialog(Context context, String message,
			final ConfirmDialogCallback callback, boolean cancelable) {
		final ConfirmDialog confirmDialog = new ConfirmDialog(context,
				R.style.ConfirmDialog);
		confirmDialog.setCancelable(cancelable);
		View view = LayoutInflater.from(context).inflate(
				R.layout.layout_confirm_dailog, null);
		confirmDialog.setContentView(view);
		TextView msgTV = (TextView) view.findViewById(R.id.tv_message);
		msgTV.setText(message);
		view.findViewById(R.id.cancel).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						confirmDialog.dismiss();
					}
				});
		view.findViewById(R.id.ok).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				callback.onOk();
				confirmDialog.ok = true;
				confirmDialog.dismiss();
			}
		});
		confirmDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				if (!confirmDialog.ok) {
					callback.onCancel();
				}
			}
		});
		confirmDialog.show();
		return confirmDialog;
	}

	public static void showMessageDialog(Context context, String message,
			final ConfirmDialogCallback callback) {
		try {
			final Dialog dialog = new Dialog(context, R.style.ConfirmDialog);
			dialog.setCancelable(false);
			View view = LayoutInflater.from(context).inflate(
					R.layout.layout_confirm_dailog, null);
			dialog.setContentView(view);
			TextView msgTV = (TextView) view.findViewById(R.id.tv_message);
			msgTV.setText(message);
			view.findViewById(R.id.cancel).setVisibility(View.GONE);
			view.findViewById(R.id.ok).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					});
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					// TODO Auto-generated method stub
					callback.onOk();
				}
			});
			dialog.show();
		} catch (Exception e) {
			Log.d(TAG, "showMessageDialog fail");
		}
	}

	private static Dialog mProcessDialog = null;

	public static void showProcessDialog(Activity activity, String message) {
		hideProcessDialog(activity);
		try {
			mProcessDialog = new Dialog(activity, R.style.ConfirmDialog);
			mProcessDialog.setCancelable(false);
			mProcessDialog.setOwnerActivity(activity);
			View view = LayoutInflater.from(activity).inflate(
					R.layout.layout_confirm_dailog, null);
			mProcessDialog.setContentView(view);
			TextView msgTV = (TextView) view.findViewById(R.id.tv_message);
			msgTV.setText(message);
			view.findViewById(R.id.cancel).setVisibility(View.GONE);
			view.findViewById(R.id.ok).setVisibility(View.GONE);
			mProcessDialog.show();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "showProcessDialog fail");
			mProcessDialog = null;
		}
	}

	public static void hideProcessDialog(Activity activity) {
		try {
			if (mProcessDialog != null) {
				Activity at = mProcessDialog.getOwnerActivity();
				if (at != activity) {
					return;
				}
				mProcessDialog.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "hideProcessDialog fail");
		}
		mProcessDialog = null;
	}

}
