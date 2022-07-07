package com.nikho.oriens;

import static org.lwjgl.opengl.GL20.*;

public class GLShaderError extends RuntimeException{
    public GLShaderError(int shader) {
        super(glGetShaderInfoLog(shader));
    }
}
