package com.example.a3droplets.mycamera;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClientOption;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyWaterMask {

    public static Bitmap addMarkFlag(Bitmap srcbitmap) {
        int w = srcbitmap.getWidth();
        int h = srcbitmap.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        Canvas mcanvas = new Canvas(newBitmap);
        //往位图中开始画入原始图片
        mcanvas.drawBitmap(srcbitmap,0,0,null);
        Paint textPaint = new Paint();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String time = sdf.format(new Date(System.currentTimeMillis()));
        BDLocation bdLocation = new BDLocation();
        StringBuffer sb = new StringBuffer(256);

        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);//反编译获得具体位置，只有网络定位才可以

        String Time = "时间："+time;
        String latitude = "纬度："+bdLocation.getLatitude();
        String longitude = "经度：" + bdLocation.getLongitude();
        String direction = "方向：" + bdLocation.getDirection();
        String addrstr = "地址：" + bdLocation.getAddrStr();

        textPaint.setColor(Color.YELLOW);
        textPaint.setTextSize(100);

        mcanvas.drawText(Time,(float)(w*1)/30,(float)(h*14)/18,textPaint);
        mcanvas.drawText(latitude,(float)(w*1)/30,(float)(h*14)/17,textPaint);
        mcanvas.drawText(longitude,(float)(w*1)/30,(float)(h*14)/16,textPaint);
        mcanvas.drawText(direction,(float)(w*1)/30,(float)(h*14)/15,textPaint);
        mcanvas.drawText(addrstr,(float)(w*1)/30,(float)(h*14)/14,textPaint);
        mcanvas.save();
        mcanvas.restore();
        return newBitmap;
    }
}
