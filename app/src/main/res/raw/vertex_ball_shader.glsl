uniform mat4 uMVPMatrix;
uniform mat4 uMVMatrix;
uniform mat4 uNormalMat;

attribute vec4 vPosition;
attribute vec4 vColor;
attribute vec3 vNormal;

varying vec4 varyingColor;
varying vec3 varyingNormal;
varying vec3 varyingPos;

void main() {
    varyingColor = vColor;
    vec4 t = uNormalMat*vec4(vNormal, 0.0);
    varyingNormal.xyz = t.xyz;
    t = uMVMatrix*vPosition;
    varyingPos.xyz = t.xyz;

    gl_Position =    uMVPMatrix  * vPosition;
}