attribute vec4 position;
attribute vec4 normal;

uniform mat4 modelViewMatrix;// _mvmLoc
uniform mat4 normalMatrix; // _normalMatrixLoc

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec4 lightPosition[4];

varying vec3 N, E;
varying vec3 L[4];
varying float dist[4];

void main()
{
        vec3 pos = (modelViewMatrix * position).xyz;
        for (int i=0; i<4; i++)
        {
            vec3 light = (lightPosition[i]).xyz;
            L[i] = normalize( light - pos );
            /* todo: add parameter to handle light */
            dist[i] = length(light - pos)/ 2.5;
        }
        E = -pos;
        N = normalize( (normalMatrix * normal).xyz);
        gl_Position = projectionMatrix * modelViewMatrix * position;
}