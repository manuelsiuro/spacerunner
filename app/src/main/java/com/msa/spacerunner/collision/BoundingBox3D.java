package com.msa.spacerunner.collision;


import java.io.Serializable;

// doc maybe help to extends later
// https://github.com/jzy3d/jzy3d-api/blob/master/jzy3d-api/src/api/org/jzy3d/maths/BoundingBox3d.java

/**
 * Represents a 3D Axis Aligned Bounding Box.
 *
 * <p>Used to check for collisions between 2 objects.</p>
 */
public class BoundingBox3D implements Serializable {

    /**
     * The minimum / maximum coordinates on all axes.
     */
    public Point3D min, max;


    /**
     * Bounding box constructor.
     *
     * @param min The point with the minimum values for all coordinates.
     * @param max The point with the maximum values for all coordinates.
     */
    @SuppressWarnings("unused")
    public BoundingBox3D(Point3D min, Point3D max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Constructs a bounding box from the object's position and its dimensions.
     *
     * @param position Object's center position.
     * @param dimensions A float array with the XYZ dimensions.
     */
    public BoundingBox3D(Point3D position, float[] dimensions) {
        this.min = new Point3D(
                position.getX() - dimensions[0]/2,
                position.getY() - dimensions[1]/2,
                position.getZ() - dimensions[2]/2);
        this.max = new Point3D(
                position.getX() + dimensions[0]/2,
                position.getY() + dimensions[1]/2,
                position.getZ() + dimensions[2]/2);
    }

    /**
     * Tests if the current box intersects with a second.
     *
     * @param box2 The second box to intersect with.
     * @return True if they intersect, false otherwise.
     */
    public boolean intersects(BoundingBox3D box2) {
        /*float[] min1 = this.min.toArray();
        float[] min2 = box2.min.toArray();
        float[] max1 = this.max.toArray();
        float[] max2 = box2.max.toArray();*/

        return this.min.x <= box2.min.x
                && box2.min.x <= this.max.x || this.min.x <= box2.max.x
                && box2.max.x <= this.max.x || this.min.y <= box2.min.y
                && box2.min.y <= this.max.y || this.min.y <= box2.max.y
                && box2.max.y <= this.max.y || this.min.z <= box2.min.z
                && box2.min.z <= this.max.z || this.min.z <= box2.max.z
                && box2.max.z <= this.max.z;

        /*return  max1[0] > min2[0] &&
                min1[0] < max2[0] &&
                max1[1] > min2[1] &&
                min1[1] < max2[1] &&
                max1[2] > min2[2] &&
                min1[2] < max2[2];*/
    }

    /** Return true if b2 is contained by this. */
    public boolean contains(BoundingBox3D box2) {
        return this.min.x <= box2.min.x
                && box2.max.x <= this.max.x
                && this.min.y <= box2.min.y
                && box2.max.y <= this.max.y
                && this.min.z <= box2.min.z
                && box2.max.z <= this.max.z;
    }

    public Point3D getCenter() {
        return new Point3D((this.min.x + this.max.x) / 2, (this.min.y + this.max.y) / 2, (this.min.z + this.max.z) / 2);
    }

    public static BoundingBox3D getBB3D(float x, float y, float z) {
        Point3D min = new Point3D(x, y, z);
        Point3D max = new Point3D(x + 1.0f, y + 1.0f, z + 1.0f);
        return new BoundingBox3D(min, max);
    }
}

