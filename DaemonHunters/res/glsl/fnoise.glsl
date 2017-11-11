uniform sampler2D colorTexture;
uniform float time;
uniform int width;
uniform int height;

varying vec2 f_texcoord;

float rand(vec2 co){
    return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453 * time);
}

const float val=0.1;

void main(void) {
	float x = floor(f_texcoord.x*width)/width;
	float y = floor(f_texcoord.y*height)/height;
    float rand = rand(vec2(x,y));
	
	vec3 texel = texture2D(colorTexture, f_texcoord.xy).rgb * (1.0 - val + val*rand);
	
    gl_FragColor = vec4(texel, 1.0);
}