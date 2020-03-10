package com.msa.spacerunner.shaders;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShadersUtils {

    public static String getShaderFile(Context context, String fileName) {
        StringBuilder contentFile = new StringBuilder();
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            InputStreamReader fileReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String read;
            while ((read = bufferedReader.readLine()) != null)
                contentFile.append(read).append("\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentFile.toString();
    }

    public static int createProgram(String vertexShaderSource, String fragmentShaderSource) {
        //Make sure these are valid
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderSource);
        GLES20.glCompileShader(vertexShader);
        String vertexShaderCompileLog = GLES20.glGetShaderInfoLog(vertexShader);
        Log.i("Vertex Shader Compile", vertexShaderCompileLog + "\n");

        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderSource);
        GLES20.glCompileShader(fragmentShader);
        String fragmentShaderCompileLog = GLES20.glGetShaderInfoLog(fragmentShader);
        Log.i("fragment Shader Compile", fragmentShaderCompileLog + "\n");

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);

        return program;
    }
}
