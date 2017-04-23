package com.example.eric.amazing;

import android.opengl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Eric on 4/12/2017.
 */

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Triangle mTriangle;
    private Maze mMaze;


    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // initialize a triangle
        mTriangle = new Triangle();

        // initialize a maze
        mMaze = new Maze();
    }

    private float[] mRotationMatrix = new float[16];
    private float[] mRotationMatrix2 = new float[16];

    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        mTriangle = new Triangle();
        float[] scratchTriangle = new float[16];

        mMaze = new Maze();
        float[] scratchMaze = new float[16];

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);


        // Create a rotation transformation for the triangle
        //long time = SystemClock.uptimeMillis() % 4000L;
        //float angle = 0.090f * ((int) time);
        //Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, -1.0f);
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);

        //Move the triangle to the maze entrance
        Matrix.translateM(mTriangle.mModelMatrix, 0, 0.4f, -0.6f, 0f);

        //Continuously move triangle
        Matrix.translateM(mTriangle.mModelMatrix, 0, 0, mTranslate, 0f);

        if(!mMaze.isCollided(0.4f, -0.6f+mTranslate, mAngle)){
            mTranslate += 0.01f;
        }
        if(mTranslate>1.50f){
            mTranslate = 0.0f;
        }

        Matrix.multiplyMM(scratchMaze, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        // Combine the model's translation & rotation matrix
        // with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratchTriangle, 0, mMVPMatrix, 0, mTriangle.mModelMatrix, 0);

        //Draw maze
        mMaze.draw(scratchMaze);

        // Draw triangle
        mTriangle.draw(scratchTriangle);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {

    }

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public volatile float mAngle;

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }

    private volatile float mTranslate;

    public float getmTranslate() {
        return mTranslate;
    }

    public void setmTranslate(float mTranslate) {
        this.mTranslate = mTranslate;
    }

}