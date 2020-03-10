precision highp float;

uniform float writeWords;
uniform float shine;

uniform vec4 ambientProduct, diffuseProduct, specularProduct;
uniform vec4 emissive;

varying vec3 N, E;
varying vec3 L[4];
varying float dist[4];

void main()
{
    vec4 ambient  = vec4(0.0, 0.0, 0.0, 0.0);
    vec4 diffuse  = vec4(0.0, 0.0, 0.0, 0.0);
    vec4 specular = vec4(0.0, 0.0, 0.0, 0.0);

    for (int i = 0; i < 4; i++)
    {
        vec3 H = normalize(L[i] + E); //Blinn halfway vector

        float dotLN = dot(L[i], N);

        //Diffuse reflection
        float lamb = max(dotLN, 0.0);
        diffuse += ((diffuseProduct * lamb)/ dist[i]);

        //Specular Reflection
        float spec = pow(max(dot(N, H), 0.0), shine);
        specular += ((specularProduct * spec)/ dist[i]);
        if (dotLN < 0.0) specular = vec4(0.0, 0.0, 0.0, 1.0);
    }

    vec4 fColor = ambient + diffuse + specular + emissive;
    //vec4 fColor = specular;
    fColor.a = 1.0;

    gl_FragColor = fColor;
}