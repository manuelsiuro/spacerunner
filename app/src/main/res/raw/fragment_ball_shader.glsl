precision mediump float;

uniform vec4 u_ballColor;

varying vec4 varyingColor;
varying vec3 varyingNormal;
varying vec3 varyingPos;

uniform vec3 lightDir;

void main() {
    /*float Ns = 100.0;// for shine
    float kd = 0.9;
    float ks = 0.9;

    vec4 light = vec4(1.0, 1.0, 1.0, 1.0);
    vec4 lightS = vec4(1.0, 1.0, 1.0, 1.0); // the specular highlight is red

    vec3 Nn = normalize(varyingNormal);
    vec3 Ln = normalize(lightDir);

    vec4 diffuse = kd * light * max(dot(Nn, Ln), 0.0);
    vec3 Ref = reflect(Nn, Ln); // get the reflectance vect
    float dotV = max(dot(Ref, normalize(varyingPos)), 0.0);  // since we are in eye space, the eye position is at 0, 0, 0, so the view direction is simply (varyingPos- (0, 0,
    vec4 specular = lightS*ks*pow(dotV, Ns);

    gl_FragColor = varyingColor*diffuse + specular;*/
    //vec4 fColor = vec4(1.0, 0.0, 0.0, 0.5);



    gl_FragColor = u_ballColor;
}