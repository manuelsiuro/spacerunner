package com.msa.spacerunner.engine;

import android.opengl.Matrix;

import com.msa.spacerunner.engine.CharGraphic;
import com.msa.spacerunner.engine.GeometryBuilder;

/**
 *
 *  Similar to number graphic;
 *  Used to draw specific strings using the nums drawable
 */
public class StringGraphic extends CharGraphic
{

    /**
     *  Constructor.
     * @param x - X position to draw at
     * @param y - Y position to draw at
     * @param type  - Type of string to draw (see inside method)
     * @param scale - Scale the string size
     */
    public StringGraphic(float x, float y, int type, float scale)
    {
        float scaleY = 1.0f, scaleX = 1.0f;

        points = GeometryBuilder.getSquare().points;
        if (type == 0) //TIME:
        {
            texPoints = new float[]{
                    0.1f, 0.5f,
                    0.1f, 0.25f,
                    0.6f, 0.5f,
                    0.6f, 0.5f,
                    0.1f, 0.25f,
                    0.6f, 0.25f
            };
            scaleX = 5.0f;
        }

        if (type == 1) //GAME OVER
        {
            texPoints = new float[]{
                    0.0f, 0.75f,
                    0.0f, 0.5f,
                    0.6f, 0.75f,
                    0.6f, 0.75f,
                    0.0f, 0.5f,
                    0.6f, 0.5f
            };
            scaleX = 6.0f;
        }

        if (type == 2) //COMPLETE
        {
            texPoints = new float[]{
                    0.0f, 1.0f,
                    0.0f, 0.75f,
                    0.6f, 1.0f,
                    0.6f, 1.0f,
                    0.0f, 0.75f,
                    0.6f, 0.75f
            };
            scaleX = 6.0f;
        }

        if (type == 3) //AGAIN
        {
            texPoints = new float[]{
                    0.5f, 0.5f,
                    0.5f, 0.25f,
                    0.9f, 0.5f,
                    0.9f, 0.5f,
                    0.5f, 0.25f,
                    0.9f, 0.25f
            };
            scaleX = 4.0f;
        }

        if (type == 4) //BACK
        {
            texPoints = new float[]{
                    0.6f, 0.75f,
                    0.6f, 0.5f,
                    0.9f, 0.75f,
                    0.9f, 0.75f,
                    0.6f, 0.5f,
                    0.9f, 0.5f
            };
            scaleX = 3.0f;
        }

        mvm = new float[16];
        Matrix.setIdentityM(mvm, 0);
        Matrix.translateM(mvm, 0, x, y, 0.0f);
        Matrix.scaleM(mvm, 0, scale*scaleX, scale*scaleY, 1.0f);

    }
}
