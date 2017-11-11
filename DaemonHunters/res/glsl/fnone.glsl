uniform sampler2D colorTexture;
uniform float time;
uniform int width;
uniform int height;

varying vec2 f_texcoord;

void main(void) {
	float x = floor(f_texcoord.x*width)/width;
	float y = floor(f_texcoord.y*height)/height;
	
	vec3 texel = texture2D(colorTexture, f_texcoord.xy).rgb;
	
    gl_FragColor = vec4(texel, 1.0);
}