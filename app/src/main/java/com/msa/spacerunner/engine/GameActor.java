package com.msa.spacerunner.engine;

import android.graphics.Color;

import com.msa.spacerunner.collision.BoundingBox3D;

public class GameActor {

    public enum ActorType {
        barrier, pit, coin, floor, empty, speed, slow, end, move_right, move_left, move_up, move_down
    }


    public float[] ambient;
    public float[] diffuse;
    public float[] specular;
    public float shine;
    private PointsPackage points;
    private ActorType _type;
    public BoundingBox3D boundingBox3D;

    public GameActor(ActorType t, BoundingBox3D boundingBox3D) {
        this.boundingBox3D = boundingBox3D;
        if(t == null) {return;}
        _type = t;
        switch (_type) {
            case barrier:
            case move_right:
            case move_left:
            case move_up:
            case move_down:
                points = GeometryBuilder.getCube();
                ambient = new float[]{0.2f, 0.2f, 0.2f, 1.0f};
                diffuse = new float[]{0.4f, 0.4f, 0.4f, 1.0f};
                specular = getRandomSpecularColor();
                shine = 2.0f;
                break;
            case floor:
                points = GeometryBuilder.getPlane();
                ambient = new float[]{0.2f, 0.2f, 0.2f, 1.0f};
                diffuse = new float[]{0.4f, 0.4f, 0.4f, 1.0f};
                specular = getRandomFloorSpecularColor();
                shine = 1.0f;
                break;
            case coin:
                points = GeometryBuilder.getTetrahedron();
                ambient = new float[]{0.3f, 0.3f, 0.1f, 0.8f};
                diffuse = new float[]{0.7f, 0.7f, 0.2f, 0.8f};
                specular = new float[]{1.0f, 1.0f, 0.3f, 1.0f};
                shine = 5.0f;
                break;
            case speed:
                points = GeometryBuilder.getCube();
                ambient = new float[]{0.3f, 0.3f, 0.1f, 0.8f};
                diffuse = new float[]{0.7f, 0.7f, 0.2f, 0.8f};
                specular = new float[]{0.0f, 1.0f, 0.0f, 1.0f};
                shine = 5.0f;
                break;
            case slow:
                points = GeometryBuilder.getCube();
                ambient = new float[]{0.3f, 0.3f, 0.1f, 0.8f};
                diffuse = new float[]{0.7f, 0.7f, 0.2f, 0.8f};
                specular = new float[]{1.0f, 0.0f, 0.0f, 1.0f};
                shine = 5.0f;
                break;
            case pit:
                points = GeometryBuilder.getFourSides();
                ambient = new float[]{0.2f, 0.2f, 0.2f, 1.0f};
                diffuse = new float[]{0.2f, 0.4f, 0.4f, 1.0f};
                specular = new float[]{0.0f, 1.0f, 0.0f, 1.0f};
                shine = 1.0f;
                break;
            case empty:
                // ??
                break;
            case end:
                points = GeometryBuilder.getCube();
                ambient = new float[]{0.3f, 0.3f, 0.1f, 0.8f};
                diffuse = new float[]{0.7f, 0.7f, 0.2f, 0.8f};
                specular = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
                shine = 5.0f;
                break;
        }
    }

    private static float[] getRandomSpecularColor() {
        int i = (int) (Math.random() * 100);
        String color;

        if(i > 75) {
            color = "#E65100";
        } else if (i > 50){
            color = "#EF6C00";
        } else if (i > 25){
            color = "#FB8C00";
        } else {
            color = "#FFB74D";
        }

        return formatFloatColor(color);
    }

    private static float[] getRandomFloorSpecularColor() {
        int i = (int) (Math.random() * 100);
        String color;

        if(i>75) {
            color = "#212121";
        } else if (i > 50){
            color = "#424242";
        } else if (i > 25){
            color = "#616161";
        } else {
            color = "#9E9E9E";
        }

        return formatFloatColor(color);
    }

    private static float[] formatFloatColor(String color) {
        int parsedColor = Color.parseColor(color);
        float r = Color.red(parsedColor) / 255f;
        float g = Color.green(parsedColor) / 255f;
        float b = Color.blue(parsedColor) / 255f;

        return new float[]{r, g, b, 1.0f};
    }

    public ActorType getType() {
        return _type;
    }

    public PointsPackage getPoints() {
        return points;
    }


}
