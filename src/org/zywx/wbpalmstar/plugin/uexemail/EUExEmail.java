package org.zywx.wbpalmstar.plugin.uexemail;

import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.ResoureFinder;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class EUExEmail extends EUExBase {
	public static final String tag = "uexEmail_";
	private ResoureFinder finder;
	public static final String EMAIL_SCHEMA = "mailto:";

	public EUExEmail(Context context, EBrowserView view) {
		super(context, view);
		finder = ResoureFinder.getInstance(context);
	}

	/**
	 * 打开系统发送邮件界面
	 * 
	 * @param inReceiverEmail
	 *            接受者邮箱地址
	 * @param inSubject
	 *            邮件主题
	 * @param inContent
	 *            邮件正文
	 * @param inAttachmentPath
	 *            邮件附件路径,附件路径 只支持wgt://和wgts://协议的路径
	 */

	public void open(String[] params) {
		if (params.length != 4) {
			return;
		}
		try {
			final String receiverEmail = params[0];
			final String subject = params[1];
			final String content = params[2];
			final String attachment = params[3];
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					String[] emails = receiverEmail.split(",");
					if (emails == null) {
						emails = new String[] { receiverEmail };
					}
					
					String[] attchments = null;
					if(attachment!=null){
						attchments = attachment.split(",");
					}
					if (attchments == null) {
						attchments = new String[] { attachment };
					}
					Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
					intent.putExtra(Intent.EXTRA_EMAIL, emails);
					intent.putExtra(Intent.EXTRA_TEXT, content);
					intent.putExtra(Intent.EXTRA_SUBJECT, subject);
					ArrayList<Uri> imageUris = new ArrayList<Uri>();
					for (String attchment : attchments) {
						String fullPath = getFullPath(attchment);
						if (fullPath != null) {
							imageUris.add(Uri.parse(fullPath));
						}
					}
					intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
					intent.setType("message/rfc882");
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(Intent.createChooser(intent, "选择发送邮件程序"));
				}
			});
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
		} catch (ActivityNotFoundException e) {
			Toast.makeText(mContext, finder.getString("can_not_find_suitable_app_perform_this_operation"),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 获取完整路径
	 * 
	 * @param inPath
	 * @return
	 */
	private String getFullPath(String inPath) {
		if(inPath==null||inPath.equals(""))
			return null;
		String fullPath = BUtility.getFullPath(mBrwView.getCurrentUrl(), inPath);
		if (fullPath.startsWith(BUtility.F_APP_SCHEMA) || fullPath.startsWith(BUtility.F_WIDGET_SCHEMA)
				|| fullPath.startsWith(BUtility.F_FILE_SCHEMA) || fullPath.startsWith("/")) {
			fullPath = BUtility.getSDRealPath(fullPath, mBrwView.getCurrentWidget().m_widgetPath,
					mBrwView.getCurrentWidget().m_wgtType);
		} else {
			fullPath = null;
		}
		return fullPath;
	}

	@Override
	protected boolean clean() {

		return false;
	}

}