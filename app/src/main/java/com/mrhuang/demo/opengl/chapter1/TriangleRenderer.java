package com.mrhuang.demo.opengl.chapter1;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.mrhuang.demo.R;
import com.mrhuang.demo.opengl.RawReader;
import com.mrhuang.demo.opengl.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TriangleRenderer implements GLSurfaceView.Renderer {


    private Context context;
    private int aPositionHandle;
    private int programId;
    private FloatBuffer vertexBuffer;

    private final float[] vertexData = {
            0f, 0f, 0f,
            1f, -1f, 0f,
            1f, 1f, 0f
    };

    public TriangleRenderer(Context context) {
        this.context = context;

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String vertexShader = RawReader.readRawTextFile(context, R.raw.vertex_shader);
        String fragmentShader = RawReader.readRawTextFile(context, R.raw.fragment_shader);
        programId = ShaderUtils.createProgram(vertexShader, fragmentShader);
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //通过所设置的颜色来清空颜色缓冲区
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);//白色不透明
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        //告知OpenGL所要使用的Program
        GLES20.glUseProgram(programId);
        //启用指向三角形顶点数据的句柄
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        //绑定三角形的坐标数据
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);
//        //绑定颜色数据
//        GLES20.glUniform4fv(mColorId, 1, TRIANGLE_COORDS, 0);
        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        //禁用指向三角形的顶点数据
        GLES20.glDisableVertexAttribArray(programId);
    }


}
