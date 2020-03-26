package com.msa.spacerunner.models;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ModelUtils {

    public static void glSetVertexAttrib(FloatBuffer fb, int key, int size) {
        fb.position(0);
        GLES20.glVertexAttribPointer(key, size, GLES20.GL_FLOAT, false, 0, fb);
        GLES20.glEnableVertexAttribArray(key);
    }

    public static FloatBuffer makeFloatBuffer(float[] arr) {
        int iSize = arr.length;
        ByteBuffer bb = ByteBuffer.allocateDirect(iSize * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(arr);
        fb.position (0);
        return fb;
    }
}
