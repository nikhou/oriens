package com.nikho.oriens;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GlwgTest {


    @BeforeAll
    static void setUp(){
        if (!glfwInit())
        {
            System.out.println("Error while GLFW initialization");
            System.exit(1);
        }
        glfwSetErrorCallback((err, desc)-> System.out.println("GLFW Error "+err+": "+desc));

    }

    @Test
    void windowTest(){
        long window = glfwCreateWindow(640, 480, "TestWindow", NULL, NULL);
        assertFalse(()-> 0==window);

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
    }

    @Test
    void resourcesTest() throws IOException {
        getClass().getClassLoader().getResource("shaders/basic.frag").openStream();
    }

    @AfterAll
    static void cleanUp(){
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
