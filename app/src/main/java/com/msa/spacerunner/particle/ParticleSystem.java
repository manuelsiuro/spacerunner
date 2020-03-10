package com.msa.spacerunner.particle;

import android.graphics.Color;

import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.glDrawArrays;

public class ParticleSystem {
    public static final int BYTES_PER_FLOAT = 4;

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int VECTOR_COMPONENT_COUNT = 3;
    private static final int PARTICLE_START_TIME_COMPONENT_COUNT = 1;

    private static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT
                    + COLOR_COMPONENT_COUNT
                    + VECTOR_COMPONENT_COUNT
                    + PARTICLE_START_TIME_COMPONENT_COUNT;

    private static final int STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT;

    private final float[] particels;
    private final VertexArray vertexArray;
    private final int maxParticleCount;

    private int currentParticleCount;
    private int nextParticle;

    public ParticleSystem(int maxParticleCount) {

        particels = new float[maxParticleCount*TOTAL_COMPONENT_COUNT];
        vertexArray = new VertexArray(particels);
        this.maxParticleCount = maxParticleCount;
    }

    /**
     * @param position
     * @param color
     * @param diretion
     * @param particleStartTime
     */
    public void addParticle(Geometry.Point position, int color, Geometry.Vector diretion, float particleStartTime){
        final int particleOffset = nextParticle * TOTAL_COMPONENT_COUNT;

        int currentOffset = particleOffset;
        nextParticle++;

        if(currentParticleCount < maxParticleCount){
            currentParticleCount++;
        }


        if(nextParticle == maxParticleCount){
            nextParticle = 0;
        }

        particels[currentOffset++] = position.x;
        particels[currentOffset++] = position.y;
        particels[currentOffset++] = position.z;

        particels[currentOffset++] = Color.red(color)/255f;
        particels[currentOffset++] = Color.green(color)/255f;
        particels[currentOffset++] = Color.blue(color)/255f;

        particels[currentOffset++] = diretion.x;
        particels[currentOffset++] = diretion.y;
        particels[currentOffset++] = diretion.z;

        particels[currentOffset++] = particleStartTime;

        vertexArray.updateBuffer(particels, particleOffset, TOTAL_COMPONENT_COUNT);
    }

    /*public void bindData(ParticleShaderProgram particleProgram){
        int dataOffset = 0;
        vertexArray.setVertexAttribPointer(dataOffset, particleProgram.getaPositionLocation(), POSITION_COMPONENT_COUNT, STRIDE);
        dataOffset += POSITION_COMPONENT_COUNT;

        vertexArray.setVertexAttribPointer(dataOffset, particleProgram.getaColorLocation(), COLOR_COMPONENT_COUNT, STRIDE);
        dataOffset += COLOR_COMPONENT_COUNT;

        vertexArray.setVertexAttribPointer(dataOffset, particleProgram.getaDirectionVectorLocation(), VECTOR_COMPONENT_COUNT, STRIDE);
        dataOffset += VECTOR_COMPONENT_COUNT;

        vertexArray.setVertexAttribPointer(dataOffset, particleProgram.getaParticleStartTimeLocation(), PARTICLE_START_TIME_COMPONENT_COUNT, STRIDE);
    }*/

    public void draw(){
        glDrawArrays(GL_POINTS, 0, currentParticleCount);
    }
}