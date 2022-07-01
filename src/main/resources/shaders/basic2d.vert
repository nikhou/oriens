#version 110
uniform mat4 MVP;
attribute vec3 vColor;
attribute vec2 vPosition;
varying vec3 color;

void main()
{
    gl_Position = MVP * vec4(vPosition, 0.0, 1.0);
    color = vColor;
}