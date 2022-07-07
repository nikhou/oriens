package com.nikho.oriens;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GlwgTest {
    String getResource(String resourceName) throws IOException {
        return new String(getClass().getClassLoader().getResourceAsStream(resourceName).readAllBytes());
    }
    ClassLoader classLoader = getClass().getClassLoader();
    static long window;

    @BeforeAll
    static void setUp(){
        if (!glfwInit())
        {
            System.out.println("Error while GLFW initialization");
            System.exit(1);
        }
        glfwSetErrorCallback((err, desc)-> System.out.println("GLFW Error "+err+": "+desc));
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        window = glfwCreateWindow(640, 480, "TestWindow", NULL, NULL);
        assertFalse(()-> 0==window);
        glfwMakeContextCurrent(window);
        GL.createCapabilities();

    }

    @Test
    void windowTest(){


        //glfwFreeCallbacks(window);

    }

    @Test
    void resourcesTest() throws IOException {
        getClass().getClassLoader().getResource("shaders/basic.frag").openStream();
    }

    @Test
    void jsonTest() throws IOException {
        String shaderProgramName = "basic_shader_program";
        JSONObject shaderProgramInfo = new JSONObject(new String(classLoader.getResourceAsStream(shaderProgramName+".json").readAllBytes()));
        Assertions.assertEquals("gShader", shaderProgramInfo.getJSONObject("vertex").getString("name"));
    }

    @Test
    void programTest() throws IOException {
        int vertexShader;
        int fragmentShader;
        int program;
        String shaderProgramName = "basic_shader_program";
        JSONObject shaderProgramInfo = new JSONObject(new String(classLoader.getResourceAsStream(shaderProgramName+".json").readAllBytes()));

        vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, getResource("shaders/"+shaderProgramInfo.getJSONObject("vertex").getString("name")+".vert"));
        glCompileShader(vertexShader);
        //check shader status
        {int success;
        success=glGetShaderi(vertexShader, GL_COMPILE_STATUS);
        assertEquals(1, success);}

        fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, getResource("shaders/"+shaderProgramInfo.getJSONObject("fragment").getString("name")+".frag"));
        glCompileShader(vertexShader);
        //check shader status
        {   int success;
            success=glGetShaderi(vertexShader, GL_COMPILE_STATUS);
            assertEquals(1, success);}

        program=glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);

        //test program status
        {   int success;
            success=glGetProgrami(program, GL_LINK_STATUS);
            assertEquals(1, success);}

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    @Test
    void modelProgramTest() throws IOException {
        Model testModel = new Model("models/triangle.json");
        //testModel.addShaderProgram("basic_shader_program");
        assertNotEquals(0, testModel.program);
    }

    @AfterAll
    static void cleanUp(){
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
