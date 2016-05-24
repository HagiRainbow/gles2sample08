package com.example.control.gles2sample08;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by tommy on 2015/06/18.
 */
public class GLRenderer implements GLSurfaceView.Renderer {
    //システム
    private final Context mContext;
    private boolean validProgram=false; //シェーダプログラムが有効
    private float aspect;//アスペクト比
    private float viewlength = 5.0f; //視点距離
    private float   angle=0f; //回転角度
    private int   viewmode=0; //視点モード　0：俯瞰　1：一緒に回る

    //視点変更テスト変数
    private float alph=0f,beta=0f;

    //光源の座標　x,y,z
    private  float[] LightPos={0f,1.5f,3f,1f};//x,y,z,1

    //変換マトリックス
    private  float[] pMatrix=new float[16]; //プロジェクション変換マトリックス
    private  float[] mMatrix=new float[16]; //モデル変換マトリックス
    private  float[] cMatrix=new float[16]; //カメラビュー変換マトリックス

    //モデル座標系の原点
    private  float[] origin= {0f,0f,0f,1f};

    private Axes MyAxes= new Axes();  //原点周囲の軸表示とためのオブジェクトを作成
    private Cube MyCube = new Cube(); //原点に，外接球半径１の立方体オブジェクトを作成
    private Circle MyCircle =new Circle(64); //zx平面の原点に，半径１の円オブジェクト(64分割)を作成
    private Sphere MySphere=new Sphere(40,20); //原点に，半径１の球体オブジェクト（40スライス，20スタック）を作成
    private Line_PtoP line1= new Line_PtoP(); //2点を結ぶ直線オブジェクトを作成
    private Rectangular MyRectangular = new Rectangular();//xy平面の原点に一辺１の正方形を作成
    private RectangularWithTex SampleDroid;
    private RectangularWithTex Hello;

    //シェーダのattribute属性の変数に値を設定していないと暴走するのでそのための準備
    private static float[] DummyFloat = new float[6];
    private static final FloatBuffer DummyBuffer = BufferUtil.makeFloatBuffer(DummyFloat);

    GLRenderer(final Context context) {
        mContext = context;
    }

    //サーフェイス生成時に呼ばれる
    @Override
    public void onSurfaceCreated(GL10 gl10,EGLConfig eglConfig) {
        //プログラムの生成
        validProgram = GLES.makeProgram();

        //頂点配列の有効化
        GLES20.glEnableVertexAttribArray(GLES.positionHandle);
        GLES20.glEnableVertexAttribArray(GLES.normalHandle);
        GLES20.glEnableVertexAttribArray(GLES.texcoordHandle);

        //デプスバッファの有効化
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // カリングの有効化
        GLES20.glEnable(GLES20.GL_CULL_FACE); //裏面を表示しないチェックを行う

        // 裏面を描画しない
        GLES20.glFrontFace(GLES20.GL_CCW); //表面のvertexのindex番号はCCWで登録
        GLES20.glCullFace(GLES20.GL_BACK); //裏面は表示しない

        //光源色の指定 (r, g, b,a)
        GLES20.glUniform4f(GLES.lightAmbientHandle, 0.15f, 0.15f, 0.15f, 1.0f); //周辺光
        GLES20.glUniform4f(GLES.lightDiffuseHandle, 0.5f, 0.5f, 0.5f, 1.0f); //乱反射光
        GLES20.glUniform4f(GLES.lightSpecularHandle, 0.9f, 0.9f, 0.9f, 1.0f); //鏡面反射光

        //背景色の設定
        GLES20.glClearColor(0f, 0f, 0.2f, 1.0f);

        //テクスチャの有効化
        //GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        // 背景とのブレンド方法を設定します。
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);    // 単純なアルファブレンド

        SampleDroid = new RectangularWithTex(mContext,R.drawable.sample); //xy平面の原点にオブジェクトを作成
        //                                                    res -> drawable -> sample.png (256×256)が入っている
        Hello = new RectangularWithTex("Hello",20, Color.WHITE, Color.parseColor("#000F00C0")); //xy平面の原点にオブジェクトを作成
    }

    //画面サイズ変更時に呼ばれる
    @Override
    public void onSurfaceChanged(GL10 gl10,int w,int h) {
        //ビューポート変換
        GLES20.glViewport(0,0,w,h);
        aspect=(float)w/(float)h;
    }

    //毎フレーム描画時に呼ばれる
    @Override
    public void onDrawFrame(GL10 glUnused) {
        if (!validProgram) return;
        //シェーダのattribute属性の変数に値を設定していないと暴走するのでここでセットしておく。この位置でないといけない
        GLES20.glVertexAttribPointer(GLES.positionHandle, 3, GLES20.GL_FLOAT, false, 0, DummyBuffer);
        GLES20.glVertexAttribPointer(GLES.normalHandle, 3, GLES20.GL_FLOAT, false, 0, DummyBuffer);
        GLES20.glVertexAttribPointer(GLES.texcoordHandle, 2, GLES20.GL_FLOAT, false, 0, DummyBuffer);

        GLES.disableTexture();  //テクスチャ機能を無効にする。（デフォルト）
        GLES.enableShading();   //シェーディング機能を有効にする。（デフォルト）

        float[] tmpPos1= new float[4];
        float[] tmpPos2 = new float[4];

        //画面のクリア
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT |
                GLES20.GL_DEPTH_BUFFER_BIT);

        //プロジェクション変換（射影変換）--------------------------------------
        //透視変換（遠近感を作る）
        //カメラは原点に有り，z軸の負の方向を向いていて，上方向はy軸＋方向である。
        GLES.gluPerspective(pMatrix,
                45.0f,  //Y方向の画角
                aspect, //アスペクト比
                1.0f,   //ニアクリップ　　　z=-1から
                100.0f);//ファークリップ　　Z=-100までの範囲を表示することになる
        GLES.setPMatrix(pMatrix);

        //カメラビュー変換（視野変換）-----------------------------------
        //カメラ視点が原点になるような変換
        Matrix.setLookAtM(cMatrix, 0,
                (float) (viewlength * Math.sin(beta) * Math.cos(alph)),  //カメラの視点 x
                (float) (viewlength * Math.sin(alph)),                    //カメラの視点 y
                (float) (viewlength * Math.cos(beta) * Math.cos(alph)),  //カメラの視点 z
                0.0f, 0.0f, 0.0f, //カメラの視線方向の代表点
                0.0f, 1.0f, 0.0f);//カメラの上方向
        if (viewmode!=0) {
            if (viewmode==2) Matrix.rotateM(cMatrix, 0, -angle * 2, 0, 1, 0);
            Matrix.translateM(cMatrix, 0, 0f, 0f, -1.2f);
            Matrix.rotateM(cMatrix, 0, -angle, 0, 1, 0);
        }

        //カメラビュー変換はこれで終わり。
        GLES.setCMatrix(cMatrix);

        //cMatrixをセットしてから光源位置をセット
        GLES.setLightPosition(LightPos);

        //座標軸の描画
        GLES.disableShading(); //シェーディング機能は使わない
        Matrix.setIdentityM(mMatrix, 0);//モデル変換行列mMatrixを単位行列にする。
        GLES.updateMatrix(mMatrix);//現在の変換行列をシェーダに指定
        //座標軸の描画本体
        //引数 r, g, b, a, shininess(1以上の値　大きな値ほど鋭くなる), linewidth
        //shininessは使用していない
        MyAxes.draw(1f, 1f, 1f, 1f, 10.f, 2f);//座標軸の描画本体
        GLES.enableShading(); //シェーディング機能を使う設定に戻す

        //MyCubeの描画
        Matrix.setIdentityM(mMatrix, 0);  //ここではすでに設定されているので省略可
        Matrix.rotateM(mMatrix, 0, angle, 0, 1, 0);
        Matrix.translateM(mMatrix, 0, 0f, 0f, 1.2f);
        Matrix.rotateM(mMatrix, 0, angle * 2, 0, 1, 0);
        Matrix.scaleM(mMatrix, 0, 0.4f, 0.4f, 0.4f);
        GLES.updateMatrix(mMatrix);//現在の変換行列をシェーダに指定
        //MyCubeの描画本体
        // r, g, b, a, shininess(1以上の値　大きな値ほど鋭くなる)
        MyCube.draw(0f, 1f, 0f, 1f, 20.f);
        Matrix.multiplyMV(tmpPos1, 0, mMatrix, 0, origin, 0); //中心の取得

        //円の描画
        GLES.disableShading(); //シェーディング機能は使わない
        Matrix.setIdentityM(mMatrix, 0);
        Matrix.scaleM(mMatrix, 0, 1.2f, 1.2f, 1.2f);
        GLES.updateMatrix(mMatrix);//現在の変換行列をシェーダに指定
        //円の描画本体
        // r, g, b, a, shininess(1以上の値　大きな値ほど鋭くなる), linewidth
        //shininessは使用していない
        MyCircle.draw(1f, 1f, 0.1f, 1f, 10.f, 1f);
        GLES.enableShading(); //シェーディング機能を使う設定に戻す

        //円の描画
        GLES.disableShading(); //シェーディング機能は使わない
        Matrix.setIdentityM(mMatrix, 0);
        Matrix.scaleM(mMatrix, 0, .8f, .8f, .8f);
        GLES.updateMatrix(mMatrix);//現在の変換行列をシェーダに指定
        //円の描画本体
        // r, g, b, a, shininess(1以上の値　大きな値ほど鋭くなる), linewidth
        //shininessは使用していない
        MyCircle.draw(1f, 1f, 0.1f, 1f, 10.f, 1f);
        GLES.enableShading(); //シェーディング機能を使う設定に戻す

        //MySphereの描画
        Matrix.setIdentityM(mMatrix, 0);
        Matrix.rotateM(mMatrix, 0, 1.5f * angle, 0, 1, 0);
        Matrix.translateM(mMatrix, 0, 0.8f, 0f, 0f);
        Matrix.scaleM(mMatrix, 0, 0.2f, 0.2f, 0.2f);
        GLES.updateMatrix(mMatrix);//現在の変換行列をシェーダに指定
        //MySphereの描画本体
        // r, g, b, a, shininess(1以上の値　大きな値ほど鋭くなる)
        MySphere.draw(0f, 1f, 1f, 1f, 5.f);
        Matrix.multiplyMV(tmpPos2, 0, mMatrix, 0, origin, 0); //中心の取得

        //光源を表す白い球の描画
        Matrix.setIdentityM(mMatrix, 0);
        Matrix.translateM(mMatrix, 0, LightPos[0], LightPos[1], LightPos[2]);
        Matrix.scaleM(mMatrix, 0, 0.1f, 0.1f, 0.1f);
        GLES.updateMatrix(mMatrix);//現在の変換行列をシェーダに指定
        //MySphereの描画本体
        // r, g, b, shininess(1以上の値　大きな値ほど鋭くなる)
        GLES.disableShading(); //shadingせずに単色で表示
        MySphere.draw(0f, 1f, 1f, 1f, 5.f);
        GLES.enableShading();

        //物体の中心点を線で結ぶ
        GLES.disableShading(); //シェーディング機能は使わない
        Matrix.setIdentityM(mMatrix, 0);
        GLES.updateMatrix(mMatrix);//現在の変換行列をシェーダに指定
        line1.setVertexs(tmpPos1, LightPos);
        line1.draw(1f, 1f, 1f, 1f, 0f, 2f);
        line1.setVertexs(tmpPos2, LightPos);
        line1.draw(1f, 1f, 1f, 1f, 0f, 2f);
        line1.setVertexs(tmpPos1, tmpPos2);
        line1.draw(1f, 1f, 1f, 1f, 0f, 2f);
        GLES.enableShading(); //シェーディング機能を使う設定に戻す

        //四角形を描く
        Matrix.setIdentityM(mMatrix, 0);
        Matrix.translateM(mMatrix, 0, 0f, .5f, 0f);
        Matrix.scaleM(mMatrix, 0, 0.5f, 0.5f, 0.5f);
        GLES.updateMatrix(mMatrix);//現在の変換行列をシェーダに指定
        MyRectangular.draw(1f, .5f, .5f, 1f, 5f);

        //Sampleの絵を張り付ける
        Matrix.setIdentityM(mMatrix, 0);
        Matrix.translateM(mMatrix, 0, .25f, 0f, 0f);
        Matrix.scaleM(mMatrix, 0, 0.5f, 0.5f, 0.5f);
        GLES.updateMatrix(mMatrix);//現在の変換行列をシェーダに指定
        SampleDroid.draw(1f, 1f, 1f, 1f, 5f);

        //Helloを張り付ける
        Matrix.setIdentityM(mMatrix, 0);
        Matrix.translateM(mMatrix, 0, -.25f, 0f, 0f);
        Matrix.scaleM(mMatrix, 0, 0.5f, 0.5f, 0.5f);
        GLES.updateMatrix(mMatrix);//現在の変換行列をシェーダに指定
        Hello.draw(1f, 1f, 0f, 1f, 5f);

        angle+=0.5;

    }

    private float Scroll[] = {0f, 0f}; //１本指のドラッグ[rad]
    public void setScrollValue(float DeltaX, float DeltaY) {
        Scroll[0] += DeltaX * 0.01;
        if (3.14f<Scroll[0]) Scroll[0]=3.14f;
        if (Scroll[0]<-3.14) Scroll[0]=-3.14f;
        Scroll[1] -= DeltaY * 0.01;
        if (1.57f<Scroll[1]) Scroll[1]=1.57f;
        if (Scroll[1]<-1.57) Scroll[1]=-1.57f;
        alph=Scroll[1];
        beta=Scroll[0];
    }

    public void toggleViewmode() {
        viewmode += 1;
        if (viewmode==3) viewmode=0;
    }
}
