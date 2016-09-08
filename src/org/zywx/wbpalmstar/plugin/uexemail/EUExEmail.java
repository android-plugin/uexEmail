/*
 * Copyright (c) 2016.  The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.zywx.wbpalmstar.plugin.uexemail;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

public class EUExEmail extends EUExBase {
	public static final String tag = "uexEmail_";
    public static final String UEXEMAIL = "uexEmail";

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
	 *            邮件附件路径,附件路径 只支持wgt://和wgts://, res:// 协议的路径
	 */

	public void open(String[] params) {
        //清缓存
        clearDir(Environment.getExternalStoragePublicDirectory(UEXEMAIL));
		if (params.length < 4) {
			return;
		}
		try {
			final String receiverEmail = params[0];
			final String subject = params[1];
			final String content = params[2];
			final String attachmentStr = params[3];
            final String mimeType = params[4];
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					String[] emails = receiverEmail.split(",");
					if (emails == null) {
						emails = new String[] { receiverEmail };
					}

					String[] attachmentArray = null;
					if(attachmentStr!=null){
						attachmentArray = attachmentStr.split(",");
					}
					if (attachmentArray == null) {
						attachmentArray = new String[] { attachmentStr };
					}
					Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
					intent.putExtra(Intent.EXTRA_EMAIL, emails);
					intent.putExtra(Intent.EXTRA_TEXT, content);
					intent.putExtra(Intent.EXTRA_SUBJECT, subject);
					ArrayList<Uri> imageUris = new ArrayList<Uri>();
					for (String attchment : attachmentArray) {
						File file = getFile(attchment.trim());
						if (file != null) {
							imageUris.add(Uri.fromFile(file));
						}
					}
					intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                    intent.setType("application/octet-stream");

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(Intent.createChooser(intent, EUExUtil.getString("plugin_uexEmail_select_app_to_send_email")));
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
	private File getFile(String inPath) {
		if(TextUtils.isEmpty(inPath)) {
            return null;
        }

        String realpath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        //先将assets文件写入到临时文件夹中
        File destFile = null;
        if (inPath.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
            String fileName = realpath.substring(realpath.lastIndexOf("/") + 1);
            //为res对应的文件生成一个临时文件到系统中
            destFile = new File(Environment.getExternalStoragePublicDirectory(UEXEMAIL), File.separator + fileName);
            try {
                if (!destFile.getParentFile().exists()) {
                    destFile.getParentFile().mkdirs();
                }
                destFile.deleteOnExit();
                destFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
           saveFileFromAssetsToSystem(mContext, realpath, destFile);
        } else {
            realpath = BUtility.getSDRealPath(inPath, mBrwView.getCurrentWidget().m_widgetPath,
					mBrwView.getCurrentWidget().m_wgtType);
            destFile = new File(realpath);
        }

        return destFile;
	}

    public static void clearDir(File dir) {
        if(dir.exists() && dir.isDirectory()) {
            File [] files = dir.listFiles();
            for(File file:files) {
                if(file.isDirectory()) {
                    clearDir(file);
                } else {
                    file.delete();
                }
            }
            dir.delete();
        }

    }
    public static boolean saveFileFromAssetsToSystem(Context context, String path, File destFile) {
        BufferedInputStream bis = null;
        BufferedOutputStream fos = null;
        try {
            InputStream is = context.getAssets().open(path);
            bis =  new BufferedInputStream(is);
            fos = new BufferedOutputStream(new FileOutputStream(destFile));
            byte [] buf = new byte [2048];
            int i;
            while ((i= bis.read(buf))!= -1) {
                fos.write(buf, 0, i);
            }
            fos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return false;
    }
	@Override
	protected boolean clean() {

		return false;
	}

}