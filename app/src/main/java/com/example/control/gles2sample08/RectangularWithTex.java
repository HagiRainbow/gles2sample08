package com.example.control.gles2sample08;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by tommy on 2015/07/02.
 */
public class RectangularWithTex {
    //bufferの定義
    private FloatBuffer vertexBuffer;
    private ByteBuffer indexBuffer;
    private FloatBuffer normalBuffer;
    private FloatBuffer texcoordBuffer;

    //頂点座標番号列
    private byte[] indexs= {
            0,2,1,3
    };
    //拡頂点の法線ベクトル
    private float[] normals= {
            0f,0f,1f,
            0f,0f,1f,
            0f,0f,1f,
            0f,0f,1f
    };

    //テクスチャコード
    private final float textcoords[] = {
        0f, 0f,     // 左上 0
        1f, 0f,	    // 右上 1
        0f, 1f,	    // 左下 2
        1f, 1f	    // 右下 3
    };

    private int TextureId;

    RectangularWithTex(String text, float textSize, int txtcolor, int bkcolor) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        paint.getTextBounds(text, 0, text.length(), new Rect(0, 0, (int) textSize * text.length(), (int) textSize));

        int textWidth = (int) paint.measureText(text);
        int textHeight = (int) (Math.abs(fontMetrics.top) + fontMetrics.bottom);

        if (textWidth == 0) textWidth = 10;
        if (textHeight == 0) textHeight = 10;

        int bitmapsize=2; //現時点でNexus7ではビットマップは正方形で一辺の長さは2のべき乗でなければならない
        while (bitmapsize<textWidth) bitmapsize*=2;
        while (bitmapsize<textHeight) bitmapsize*=2;

        Bitmap bitmap = Bitmap.createBitmap(bitmapsize, bitmapsize, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        paint.setColor(bkcolor);
        canvas.drawRect(new Rect(0, 0, bitmapsize, bitmapsize), paint);
        paint.setColor(txtcolor);
        canvas.drawText(text, bitmapsize / 2 - textWidth / 2, bitmapsize / 2 - (fontMetrics.ascent + fontMetrics.descent) / 2, paint);

        final int FIRST_INDEX = 0;
        final int DEFAULT_OFFSET = 0;
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, DEFAULT_OFFSET);
        TextureId = textures[FIRST_INDEX];
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+TextureUnitNumber);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureId);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        float top=.5f;
        float bottom=-top;
        float right=.5f;
        float left=-right;

        //頂点座標
        float[] vertexs= {
                left, top, 0f,     //左上 0
                right, top, 0f,    //右上 1
                left, bottom, 0f,  //左下 2
                right, bottom, 0f      //右下 3
        };

        vertexBuffer = BufferUtil.makeFloatBuffer(vertexs);
        indexBuffer = BufferUtil.makeByteBuffer(indexs);
        normalBuffer = BufferUtil.makeFloatBuffer(normals);
        texcoordBuffer = BufferUtil.makeFloatBuffer(textcoords);

    }

    RectangularWithTex(Context mContext, int id) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        //これをつけないと読み込み時にサイズが勝手に変更されてしまう
        //現時点でNexus7では正方形で一辺が2のべき乗サイズでなければならない
        //元のファイルの段階で大きさをそろえておく必要がある

        final Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), id, options);
        int bitmapWidth=bitmap.getWidth();
        int bitmapHeight=bitmap.getHeight();
        float aspect = (float) bitmapWidth / (float) bitmapHeight;
        final int FIRST_INDEX = 0;
        final int DEFAULT_OFFSET = 0;
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, DEFAULT_OFFSET);
        TextureId = textures[FIRST_INDEX];
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureId);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        float height = 1f;
        float width = aspect;
        float top=height*.5f;
        float bottom=-top;
        float right=width*.5f;
        float left=-right;

        //頂点座標
        float[] vertexs= {
                left, top, 0f,     //左上 0
                right, top, 0f,    //右上 1
                left, bottom, 0f,  //左下 2
                right, bottom, 0f      //右下 3
        };

        vertexBuffer = BufferUtil.makeFloatBuffer(vertexs);
        indexBuffer = BufferUtil.makeByteBuffer(indexs);
        normalBuffer = BufferUtil.makeFloatBuffer(normals);
        texcoordBuffer = BufferUtil.makeFloatBuffer(textcoords);

    }

    public void draw(float r,float g,float b,float a, float shininess) {
        int TextureUnitNumber=0;
        GLES.enableTexture();
        // テクスチャの指定
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + TextureUnitNumber);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureId);
        GLES20.glUniform1i(GLES.textureHandle, TextureUnitNumber); //テクスチャユニット番号を指定する

        GLES20.glVertexAttribPointer(GLES.texcoordHandle, 2, GLES20.GL_FLOAT, false, 0, texcoordBuffer);
        GLES20.glVertexAttribPointer(GLES.positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        //頂点での法線ベクトル
        GLES20.glVertexAttribPointer(GLES.normalHandle, 3,
                GLES20.GL_FLOAT, false, 0, normalBuffer);

        //周辺光反射
        GLES20.glUniform4f(GLES.materialAmbientHandle, r, g, b, a);

        //拡散反射
        GLES20.glUniform4f(GLES.materialDiffuseHandle, r, g, b, a);

        //鏡面反射
        GLES20.glUniform4f(GLES.materialSpecularHandle, 1f, 1f, 1f, a);
        GLES20.glUniform1f(GLES.materialShininessHandle, shininess);

        //shadingを使わない時に使う単色の設定 (r, g, b,a)
        //GLES20.glUniform4f(GLES.objectColorHandle, r, g, b, a);

        indexBuffer.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP,
                4, GLES20.GL_UNSIGNED_BYTE, indexBuffer);

        GLES.disableTexture();

    }

    public void draw() {
        int TextureUnitNumber=0;
        GLES.enableTexture();
        // テクスチャの指定
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + TextureUnitNumber);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureId);
        GLES20.glUniform1i(GLES.textureHandle, TextureUnitNumber); //テクスチャユニット番号を指定する

        GLES20.glVertexAttribPointer(GLES.texcoordHandle, 2, GLES20.GL_FLOAT, false, 0, texcoordBuffer);
        GLES20.glVertexAttribPointer(GLES.positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        //頂点での法線ベクトル
        GLES20.glVertexAttribPointer(GLES.normalHandle, 3,
                GLES20.GL_FLOAT, false, 0, normalBuffer);

        indexBuffer.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP,
                4, GLES20.GL_UNSIGNED_BYTE, indexBuffer);

        GLES.disableTexture();

    }

}
