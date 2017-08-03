package com.logansoft.lubo.loganmeeting.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by jun on 2016/10/29.
 */
public class FileUtil {

    /**
     * 从本地读取文本内容
     * @param context 上下文，用来打Toast
     * @param dirPath 文件根路径
     * @param fileName 文件名
     * @return 文件文本内容
     */
    public static String readTextFromLocalData(Context context, String dirPath, String fileName){
        BufferedReader br = null;
        String result = "";
        File file = new File(dirPath + File.separator + fileName);
        if(!file.exists() || file.isDirectory()){
            Toast.makeText(context, "文件不存在！", Toast.LENGTH_SHORT).show();
            return result;
        }
        try {
            br = new BufferedReader(new FileReader(file));
            String len = null;
            while((len = br.readLine()) != null){
                result += len;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 写文本文件到本地
     * @param context 上下文
     * @param text 需要写入的文本
     * @param dirPath 文件根路径
     * @param filePath 文件名
     * @return 是否写入
     */
    public static boolean writeTextToLocalData(Context context, String text, String dirPath, String filePath){
        BufferedWriter bw = null;
        File file = null;
        try {
            file = new File(dirPath + File.separator + filePath);
            bw = new BufferedWriter(new FileWriter(file));
            if(!file.isFile()){
                return false;
            }
            if(file.exists()){
                boolean delete = file.delete();
                if(delete){
                    boolean newFile = file.createNewFile();
                }else{
                    return false;
                }
            }
            bw.write(text);
            bw.flush();
            return  false;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(bw != null)
                    bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 从本地读取图片
     * @param context 上下文
     * @param dirPath 文件根路径
     * @param fileName 文件名
     * @return 图片的Bitmap对象
     */
    public static Bitmap readPictureFromLocalData(Context context, String dirPath, String fileName){
        File file = new File(dirPath + File.separator + fileName);
        if(!file.exists() || !file.isFile()){
            Toast.makeText(context, "文件不存在！", Toast.LENGTH_SHORT).show();
            return null;
        }
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    /**
     * 写图片到本地
     * @param context 上下文
     * @param bitmap 需要写入的图片的Bitmap对象
     * @param dirPath 文件根路径
     * @param fileName 文件名
     * @param format 图片格式--Bitmap.CompressFormat.PNG--Bitmap.CompressFormat.WEBP--Bitmap.CompressFormat.JPEG
     * @return
     */
    public static boolean writePictureToLocalData(Context context, Bitmap bitmap, String dirPath, String fileName, Bitmap.CompressFormat format){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(dirPath + File.separator + fileName);
            return bitmap.compress(format, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if(fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
