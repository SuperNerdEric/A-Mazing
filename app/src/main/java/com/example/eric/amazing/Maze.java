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
    private static float squareCoords[] = {
            0.5f,  0.5f,   // top right
            0.5f, -0.5f,   // bottom right
            0.0f, -0.5f,   // bottom mid
            0.5f, -0.5f,   // bottom right
            -0.5f, -0.5f,   // bottom left
            -0.5f,  0.5f,  // top left
            -0.5f,  0.5f,  // top left
            0.0f,  0.5f,   // top mid
    };

    private static float winCoords[] = {
            0.0f,  0.5f,   // top mid
            0.5f,  0.5f,   // top right
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

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glLineWidth(35);

        // Draw the maze
        GLES20.glDrawElements(GLES20.GL_LINES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    //Checks if triangle has collided with the maze.
    public boolean  isCollided(float x, float y, float angle){

        //Account for height of triangle
        y = y+0.076f;

        float angleRadians = -(float) Math.toRadians(angle);

        float squareCoordsRotated[] = new float[16];

        //Create rotated coords
        for(int i=0; i<squareCoords.length; i+=2){
            squareCoordsRotated[i] = (float) (squareCoords[i]*Math.cos(angleRadians) - squareCoords[i+1]*Math.sin(angleRadians));
            squareCoordsRotated[i+1] = (float) (squareCoords[i+1]*Math.cos(angleRadians) + squareCoords[i]*Math.sin(angleRadians));
        }
        /*System.out.println("[TOP RIGHT, BOTTOM RIGHT, BOTTOM MID, BOTTOM RIGHT, BOTTOM LEFT, TOP LEFT, TOP LEFT, TOP MID]");
        System.out.println(Arrays.toString(squareCoords)+" rotated by " + angle + " degrees" + '\n');
        System.out.println(Arrays.toString(squareCoordsRotated) + '\n');

        float slopeTest = (squareCoordsRotated[3] - squareCoordsRotated[1]) / (squareCoordsRotated[2] - squareCoordsRotated[0]);
        System.out.println("TOP RIGHT TO BOTTOM RIGHT HAS SLOPE OF " + slopeTest);*/

        for(int i=0; i<squareCoordsRotated.length; i+=4) {

            //Check if x lies on the line
            if ((x>=squareCoordsRotated[i] && x<=squareCoordsRotated[i+2])
                    || (x<=squareCoordsRotated[i] && x>=squareCoordsRotated[i+2])) {

                //Check if y is lower than line at given x

                //Find slope of line
                float slope = (squareCoordsRotated[i+3] - squareCoordsRotated[i+1]) / (squareCoordsRotated[i+2] - squareCoordsRotated[i]);

                float yAtGivenX = Math.abs(slope*(squareCoordsRotated[i]-x)) + squareCoordsRotated[i+1];
                if(y>=(yAtGivenX-0.01f) && y<(yAtGivenX+0.116f)){
                    return true;
                }
            }
        }

        return false;
    }

    //Checks if triangle has completed the maze.
    public boolean  hasWon(float x, float y, float angle){

        //Account for height of triangle
        y = y+0.076f;

        float angleRadians = -(float) Math.toRadians(angle);

        float winCoordsRotated[] = new float[16];

        //Create rotated coords
        for(int i=0; i<winCoords.length; i+=2){
            winCoordsRotated[i] = (float) (winCoords[i]*Math.cos(angleRadians) - winCoords[i+1]*Math.sin(angleRadians));
            winCoordsRotated[i+1] = (float) (winCoords[i+1]*Math.cos(angleRadians) + winCoords[i]*Math.sin(angleRadians));
        }

        for(int i=0; i<winCoordsRotated.length; i+=4) {

            //Check if x lies on the line
            if ((x>=winCoordsRotated[i] && x<=winCoordsRotated[i+2])
                    || (x<=winCoordsRotated[i] && x>=winCoordsRotated[i+2])) {

                //Check if y is lower than line at given x

                //Find slope of line
                float slope = (winCoordsRotated[i+3] - winCoordsRotated[i+1]) / (winCoordsRotated[i+2] - winCoordsRotated[i]);

                float yAtGivenX = Math.abs(slope*(winCoordsRotated[i]-x)) + winCoordsRotated[i+1];
                if(y>=(yAtGivenX-0.01f) && y<(yAtGivenX+0.116f)){
                    return true;
                }
            }
        }

        return false;
    }

}