attribute vec2 v_coord;
varying vec2 f_texcoord;
 
void main(void) {
  gl_Position = vec4(v_coord, 1.0, 1.0);
  f_texcoord = (v_coord-1.0)/2;
}