package com.nikho.oriens;

import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;

public class GLProgramError extends RuntimeException{
    GLProgramError(int program){
        super(glGetProgramInfoLog(program));
    }
}
