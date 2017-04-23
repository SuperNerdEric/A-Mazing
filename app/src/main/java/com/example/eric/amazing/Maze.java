package com.example.eric.amazing;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Eric on 4/12/2017.
 */

public class Maze {

    private ShortBuffer drawListBuffer;

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private FloatBuffer vertexBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;
    private static float squareCoords[] = {   // in counterclockwise order:
            -0.5f,  0.5f,   // top right
            -0.5f, -0.5f,   // bottom right
            0.0f, -0.5f,   // bottom mid
            -0.5f, -0.5f,   // bottom right
            0.5f, -0.5f,   // bottom left
            0.5f,  0.5f,  // top left
            0.5f,  0.5f,  // top left
            0.0f,  0.5f,   // top mid
    };

    private short drawOrder[] = { 0, 1, 2, 3, 4, 5, 6, 7}; // order to draw vertices

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.0f, 0.0f, 0.0f, 1.0f };

    private final int mProgram;

    public Maze() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(squareCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);




    }

    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public void draw(float[] mvpMatrix) {

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the maze vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the maze coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the maze
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        GLES20.glLineWidth(2);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glLineWidth(30);

        // Draw the maze
        GLES20.glDrawElements(GLES20.GL_LINES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    //Checks if triangle has collided with the maze.
    public boolean  isCollided(float x, float y, float angle){
        y = y+0.076f;
        float test = squareCoords[0];

        float angleRadians = (float) Math.toRadians(angle);

        for(int i=0; i<squareCoords.length/2; i++){
            float newx1;
            float newy1;

            newy1 = (float) (squareCoords[i]*Math.cos(angleRadians) - squareCoords[i+1]*Math.sin(angleRadians));
            newx1 = (float) (squareCoords[i+1]*Math.cos(angleRadians) + squareCoords[i]*Math.sin(angleRadians));

            float newx2;
            float newy2;

            newx2 = (float) -(squareCoords[i+2]*Math.cos(angleRadians) - squareCoords[i+3]*Math.sin(angleRadians));
            newy2 = (float) (squareCoords[i+3]*Math.cos(angleRadians) + squareCoords[i+2]*Math.sin(angleRadians));

            //Check if x lies on the line
            if((x > newx1 && x < newx2) || (x < newx1 && x > newx2)){
                //Check height of line at given x

                float xLength = newx1 - newx2;
                float yLength = newy1 - newy2;

                //Find y based on x and angle
                float xLengthNewTriangle = newx1 - x;
                float yNewTriangle = (float) (angleRadians * xLengthNewTriangle);

                //Check if y has collided but has not passed already
                if(y>(yNewTriangle+newy1) && y<(yNewTriangle+newy1+0.116f)){
                    return true;
                }
            }
        }

        return false;
    }

}