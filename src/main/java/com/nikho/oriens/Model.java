package com.nikho.oriens;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Model {
    private static Set<Integer> buffers= new HashSet<Integer>();
    private static Set<Integer> vertexArrays= new HashSet<Integer>();
    private static Set<Integer> programs= new HashSet<Integer>();

    String getResource(String resourceName) throws IOException {
        if (!resourceName.contains("."))
        {
            return new String(getClass().getClassLoader().getResourceAsStream(resourceName+".json").readAllBytes());
        }
        return new String(getClass().getClassLoader().getResourceAsStream(resourceName).readAllBytes());
    }

    int vertexArrayObject;
    public int program;

    Model(String source) throws IOException {
        JSONObject modelInfo = new JSONObject(getResource(source));
        //loading vertices
        float[] vertices;
        JSONArray verticesData = modelInfo.getJSONArray("vertices");
        vertices = new float[verticesData.length()];
        for(int i = 0; i<verticesData.length(); i++)
            vertices[i]=verticesData.getFloat(i);

        addShaderProgram(modelInfo.getString("program"));

        vertexArrayObject = glGenVertexArrays();
        vertexArrays.add(vertexArrayObject);
        int vertexBufferObject = glGenBuffers();
        buffers.add(vertexBufferObject);

        glBindVertexArray(vertexBufferObject);
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0,2,GL_FLOAT,false,2*Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
    }

    public void addShaderProgram(String name) throws IOException {
        int vertexShader;
        int fragmentShader;
        JSONObject shaderProgramInfo = new JSONObject(getResource(name+".json"));
        //load vertex shader
        vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, getResource("shaders/"+shaderProgramInfo.getJSONObject("vertex").getString("name")+".vert"));
        glCompileShader(vertexShader);
        //checking shader status
        {int success;
            success=glGetShaderi(vertexShader, GL_COMPILE_STATUS);
            if (success==0)throw new GLShaderError(vertexShader);}
        //loading fragment shader
        fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, getResource("shaders/"+shaderProgramInfo.getJSONObject("fragment").getString("name")+".frag"));
        glCompileShader(fragmentShader);
        //checking shader status
        {   int success;
            success=glGetShaderi(vertexShader, GL_COMPILE_STATUS);
            if (success==0)throw new GLShaderError(fragmentShader);}
        //linking program
        program=glCreateProgram();
        programs.add(program);
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);

        //test program status
        {   int success;
            success=glGetProgrami(program, GL_LINK_STATUS);
            if (success==0)throw new GLProgramError(program);}

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public void draw(){
        glUseProgram(program);
        glBindVertexArray(vertexArrayObject);

        glDrawArrays(GL_TRIANGLES,0,6);
    }

    static void cleanUp(){
        vertexArrays.forEach(GL30::glDeleteVertexArrays);
        buffers.forEach(GL15::glDeleteBuffers);
        programs.forEach(GL20::glDeleteProgram);
    }
}
