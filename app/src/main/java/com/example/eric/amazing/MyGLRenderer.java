package com.example.eric.amazing;

import android.opengl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.GL10;

import de.matthiasmann.twl.utils.PNGDecoder;

/**
 * Created by Eric on 4/12/2017.
 */

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Triangle mTriangle;
    private Maze mMaze;
    private StartBlock mStartBlock;
    private FinishBlock mFinishBlock;


    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // initialize a triangle
        mTriangle = new Triangle();

        // initialize a maze
        mMaze = new Maze();
    }

    private float[] mRotationMatrixMaze = new float[16];
    private float[] mRotationMatrixStartBlock = new float[16];
    private float[] mRotationMatrixFinishBlock = new float[16];

    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        mTriangle = new Triangle();
        float[] scratchTriangle = new float[16];

        mMaze = new Maze();
        float[] scratchMaze = new float[16];

        mStartBlock = new StartBlock();
        float[] scratchStartBlock = new float[16];

        mFinishBlock = new FinishBlock();
        float[] scratchFinishBlock = new float[16];

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        //Rotate the maze to the angle
        Matrix.setRotateM(mRotationMatrixMaze, 0, mAngle, 0, 0, -1.0f);

        //Set start block rotation
        Matrix.setRotateM(mRotationMatrixStartBlock, 0, mAngle, 0, 0, -1.0f);

        //Set finish block rotation
        Matrix.setRotateM(mRotationMatrixFinishBlock, 0, mAngle, 0, 0, -1.0f);

        //Move the triangle to the maze entrance
        Matrix.translateM(mTriangle.mModelMatrix, 0, -0.4f, -0.6f, 0f);

        //Translate triangle to its current position
        Matrix.translateM(mTriangle.mModelMatrix, 0, mTranslateX, mTranslateY, 0f);

        //Check for win
        if(mMaze.hasWon(-0.4f+ mTranslateX, -0.6f+ mTranslateY, mAngle)){

            InputStream in = MainActivity.is;

            try {
                PNGDecoder decoder = new PNGDecoder(in);

                System.out.println("width="+decoder.getWidth());
                System.out.println("height="+decoder.getHeight());

                ByteBuffer buf = ByteBuffer.allocateDirect(4*decoder.getWidth()*decoder.getHeight());
                decoder.decode(buf, decoder.getWidth()*4, PNGDecoder.Format.RGBA);
                buf.flip();
                unused.glTexImage2D(unused.GL_TEXTURE_2D, 0, unused.GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, unused.GL_RGBA, unused.GL_UNSIGNED_BYTE, buf);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            //Automatically move triangle up unless colliding with a wall
            if (!mMaze.isCollided(-0.4f + mTranslateX, -0.6f + mTranslateY, mAngle)) {
                mTranslateY += 0.01f;
            }
            if (mTranslateY > 1.50f) {
                mTranslateY = 0.0f;
                mTranslateX = 0.0f;
                this.setAngle(0);
            }
        }

        Matrix.multiplyMM(scratchMaze, 0, mMVPMatrix, 0, mRotationMatrixMaze, 0);

        // Combine the model's translation & rotation matrix
        // with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratchTriangle, 0, mMVPMatrix, 0, mTriangle.mModelMatrix, 0);

        Matrix.multiplyMM(scratchStartBlock, 0, mMVPMatrix, 0, mRotationMatrixStartBlock, 0);
        Matrix.multiplyMM(scratchFinishBlock, 0, mMVPMatrix, 0, mRotationMatrixStartBlock, 0);

        //Draw start block
        mStartBlock.draw(scratchStartBlock);

        //Draw finish block
        mFinishBlock.draw(scratchFinishBlock);

        //Draw maze
        mMaze.draw(scratchMaze);

        //Draw triangle
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

    //Triangles current positions relative to starting position
    private volatile float mTranslateX;
    private volatile float mTranslateY;

    //When rotating the maze the triangle will be translated to stay in the same position inside of the maze
    public void translateTriangleBasedOnAngle(float angle){
        float angleRadians = -(float) Math.toRadians(angle);

        double newX = ((mTranslateX-0.4f)*Math.cos(angleRadians) - (mTranslateY-0.6f)*Math.sin(angleRadians));
        double newY = ((mTranslateY-0.6f)*Math.cos(angleRadians) + (mTranslateX-0.4f)*Math.sin(angleRadians));

        mTranslateX = (float) (newX + 0.4f);
        mTranslateY = (float) (newY + 0.6f);
    }

}