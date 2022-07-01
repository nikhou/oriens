package com.nikho.oriens;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.IOException;
import java.nio.*;

import java.util.Arrays;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    ClassLoader classLoader = getClass().getClassLoader();

    private long window;

    float[] vertices = {
            // first triangle
            -0.9f, -0.5f, 0.0f,  // left
            -0.0f, -0.5f, 0.0f,  // right
            -0.45f, 0.5f, 0.0f,  // top
            // second triangle
            0.0f, -0.5f, 0.0f,  // left
            0.9f, -0.5f, 0.0f,  // right
            0.45f, 0.5f, 0.0f   // top
    };

    float[] squareVertices = {
            -1f, -1f,   //botRight
            1f, -1f,    //botLeft
            -1f, 1f,    //topRight
            1f, 1f,     //topLeft
    };

    int[] indices = {
            0, 1, 2,
            1, 2, 3
    };

    private int vertexShader;
    private int fragmentShader;
    private int program;

    int VBO, VAO, EBO;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "! "+"Java version: "+System.getProperty("java.version"));

        init();
        loop();

        glDeleteVertexArrays(VAO);
        glDeleteBuffers(VBO);
        glDeleteBuffers(EBO);
        glDeleteProgram(program);

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();

        //glfwSetErrorCallback(null).free();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            assert vidmode != null;
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        //scaling while resizing
        //glfwSetFramebufferSizeCallback(window, (window, width, height)->{glViewport(0,0,width, height);});
        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);
        // Make the window visible
        glfwShowWindow(window);

        // Create the OpenGL bindings available for use
        GL.createCapabilities();

        // Loading Shaders
        try {
            @SuppressWarnings("ConstantConditions")
            String inShader = new String(classLoader.getResourceAsStream("shaders/gShader.vert").readAllBytes());
            vertexShader = glCreateShader(GL_VERTEX_SHADER);
            glShaderSource(vertexShader, inShader);
            glCompileShader(vertexShader);

            //Check shader status
            {int[] success = new int[1];
            glGetShaderiv(vertexShader, GL_COMPILE_STATUS, success);
            if(success[0]==0) throw new RuntimeException(inShader);}

            inShader = new String(classLoader.getResourceAsStream("shaders/gShader.frag").readAllBytes());
            fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
            glShaderSource(fragmentShader, inShader);
            glCompileShader(fragmentShader);

            //Check shader status
            {int[] success = new int[1];
            glGetShaderiv(vertexShader, GL_COMPILE_STATUS, success);
            if(success[0]==0) throw new RuntimeException(inShader);}
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            System.out.println(glGetShaderInfoLog(vertexShader, 512));
        }

        // Creating program
        program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        try {
            int[] success = new int[1];
            glGetProgramiv(program,GL_LINK_STATUS, success);
            if(success[0]==0) throw new RuntimeException("ERROR::SHADER::PROGRAM::LINKING_FAILED");
        } catch (RuntimeException e) {
            System.out.println(glGetProgramInfoLog(program));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        VAO = glGenVertexArrays();
        VBO = glGenBuffers();
        EBO = glGenBuffers();

        glBindVertexArray(VAO);

        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, squareVertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glVertexAttribPointer(0,2, GL_FLOAT, false, 2*Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW'
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer

            glUseProgram(program);
            glBindVertexArray(VAO);

            glDrawElements(GL_TRIANGLES, 3, GL_UNSIGNED_INT, 0);
            //glDrawArrays(GL_TRIANGLES, 0, 6);
            glBindVertexArray(0);

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
