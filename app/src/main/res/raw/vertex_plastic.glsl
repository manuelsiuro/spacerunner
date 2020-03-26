uniform vec4 vPosition;

varying vec3 vNormal;
varying vec3 vViewVec;

void main(void)
{
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

    // World-space lighting
    vNormal = gl_Normal;
    vViewVec = vPosition.xyz - gl_Vertex.xyz;

}