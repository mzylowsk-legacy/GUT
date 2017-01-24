package com.example.mzylowski.lab6;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

public class GLRenderer implements GLSurfaceView.Renderer 
{
	// Macierze modelu, widoku i projekcji.
	private float[] modelMatrix = new float[16];
	private float[] viewMatrix = new float[16];
	private float[] projectionMatrix = new float[16];
	
	// Iloczyn macierzy modelu, widoku i projekcji.
	private float[] MVPMatrix = new float[16];
	private float[] MVMatrix = new float[16];
	
	// Bufory przechowujące pozycje, kolory i normalne wierzchołków trójkąta.
    private final FloatBuffer trianglePositionBuffer;
	private final FloatBuffer triangleColourBuffer;
	private final FloatBuffer triangleNormalBuffer;

	// Adresy zmiennych uniform w shaderach.
	private int MVPMatrixHandle;
	private int MVMatrixHandle;
	private int lightColorHandle;
	private int lightPositionHandle;
	
	// Adresy atrybutów w vertex shaderze.
	private int vertexPositionHandle;
	private int vertexColourHandle;
	private int vertexNormalHandle;

	// Rozmiar typu float w bajtach.
	private final int BYTES_PER_FLOAT = 4;
	
	// Informacja o tym, z ilu elementów składają się poszczególne atrybuty.
	private final int POSITION_DATA_SIZE = 3;
	private final int COLOUR_DATA_SIZE = 4;
	private final int NORMAL_DATA_SIZE = 3;
	private float a = 0.0f;
	public float z = 1.0f;

	public Color lightColor;
	
	// Wartości wykorzystywane przez naszą kamerę. Pierwsze trzy elementy opisują położenie obserwatora,
	// kolejne trzy wskazują na punkt, na który on patrzy, a ostatnie wartości definiują, w którym kierunku
	// jest "góra" (tzw. "up vector").
	private float[] camera;

    public void moveCamera(float x, float y)
    {
        this.camera[0] += x;
        this.camera[1] += y;

        this.camera[3] += x;
        this.camera[4] += y;
    }
	
	public GLRenderer()
	{
		lightColor = Color.WHITE;
		camera = new float[] {0.f, 0.f, 1.5f,  0.f, 0.f, 0.f,  0.f, 1.f, 0.f};
		
		// Współrzędne wierzchołków trójkąta (składowe X, Y, Z każdego punktu).
		final float[] trianglePositionData = {
	            -0.5f, -0.5f, 0.0f, 
	             0.5f, -0.5f, 0.0f,
	             0.0f,  0.5f, 0.0f };
		
		// Kolory wierzchołków trójkąta (składowe R, G, B, A).
		final float[] triangleColourData = {
	            0.5f, 0.5f, 0.5f, 1.0f,
	            0.5f, 0.5f, 0.5f, 1.0f,
	            0.5f, 0.5f, 0.5f, 1.0f };
		
		// Normalne wierzchołków trójkąta (składowe X, Y, Z).
		final float[] triangleNormalData = {
				0.0f, 0.0f, 1.0f,				
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f };

        // Współrzędne wierzchołków trójkąta (składowe X, Y, Z każdego punktu).
        final float[] trianglePositionData2 = {
                 0.5f,  0.5f,  0.0f,
                -0.5f,  0.5f,  0.0f,
                -0.5f, -0.5f,  0.0f,

                 0.5f,  0.5f,  0.0f,
                -0.5f, -0.5f,  0.0f,
                 0.5f, -0.5f,  0.0f,

                -0.5f,  0.5f,  0.0f,
                 0.5f,  0.5f,  0.0f,
                 0.5f,  0.5f, -1.0f,

                -0.5f,  0.5f,  0.0f,
                 0.5f,  0.5f, -1.0f,
                -0.5f,  0.5f, -1.0f,

                 0.5f,  0.5f,  0.0f,
                 0.5f, -0.5f,  0.0f,
                 0.5f, -0.5f, -1.0f,

                 0.5f,  0.5f,  0.0f,
                 0.5f, -0.5f, -1.0f,
                 0.5f,  0.5f, -1.0f,

        };
		
		// Przygotowanie buforów zawierających pozycje, kolory i normalne wierzchołków.
		trianglePositionBuffer = ByteBuffer.allocateDirect(trianglePositionData.length * BYTES_PER_FLOAT * 7)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();
        trianglePositionBuffer.put(trianglePositionData);
        trianglePositionBuffer.put(trianglePositionData2);

		triangleColourBuffer = ByteBuffer.allocateDirect(triangleColourData.length * BYTES_PER_FLOAT)
		        .order(ByteOrder.nativeOrder()).asFloatBuffer();
		triangleColourBuffer.put(triangleColourData).position(0);
		
		triangleNormalBuffer = ByteBuffer.allocateDirect(triangleNormalData.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();							
		triangleNormalBuffer.put(triangleNormalData).position(0);
	}
	
	@Override
	public void onSurfaceCreated(GL10 nieUzywac, EGLConfig config) 
	{
		// Kolor tła.
		GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

		createShaders();       
	}	
	
	private void createShaders()
	{
		// Kody shaderów.
		final String vertexShader =
		  // Zmienne "globalne", przekazywane z programu głownego.
			"uniform mat4 MVPMatrix; \n"
		  + "uniform mat4 MVMatrix; \n"
			
		  // Atrybuty wierzchołka, przekazywane z programu głównego.
		  + "attribute vec4 vertexPosition; \n"
		  + "attribute vec4 vertexColour; \n"			  
		  + "attribute vec3 vertexNormal; \n"
		  
		  // Zmienne stworzone przez vertex shader, dostępne we fragment shaderze w zinterpolowanej postaci.
		  + "varying vec4 interpolatedColour; \n"
		  + "varying vec3 interpolatedPosition \n;"
		  + "varying vec3 interpolatedNormal; \n"

		  + "void main() \n"
		  + "{ \n"
		  + "    interpolatedColour = vertexColour; \n"
		  + "    interpolatedPosition = vec3(MVMatrix * vertexPosition); \n"
		  + "    interpolatedNormal = vec3(MVMatrix * vec4(vertexNormal, 0.0)); \n"
		  // Wyznaczenie "ostatecznej" pozycji wierzchołka.
		  + "    gl_Position = MVPMatrix * vertexPosition; \n"		                                            			 
		  + "} \n";
		
		final String fragmentShader =
		  // Zmniejszenie domyślnego poziomu precyzji.
			"precision mediump float; \n"
				
		  // Zmienne przekazane z vertex shadera.
		  + "varying vec4 interpolatedColour; \n"
		  + "varying vec3 interpolatedPosition \n;"
		  + "varying vec3 interpolatedNormal; \n"
		  +	"uniform vec4 lightColor; \n"
		  +	"uniform vec3 lightPosition; \n"

		  + "void main() \n"
		  + "{ \n"
		  + "    float distance = length(lightPosition - interpolatedPosition); \n"
		  + "    vec3 lightVector = normalize(lightPosition - interpolatedPosition); \n"
		  + "    float diffuse = max( dot(interpolatedNormal, lightVector), 0.0 ); \n"
		  + "    diffuse = diffuse * (1.0 / (1.0 + (0.10 * distance))); \n"
		  + "    diffuse = diffuse + 0.2; \n"
		  + "    gl_FragColor = interpolatedColour * diffuse * lightColor; \n"
		  + "} \n";
		//-0.5f, -0.5f, 0.0f,
		// Kompilacja vertex shadera.
		int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		if (vertexShaderHandle == 0)
		{
			Log.e("KSG", "Nie można utworzyć vertex shadera.");
			return;
		}
		else
		{
			GLES20.glShaderSource(vertexShaderHandle, vertexShader);
			GLES20.glCompileShader(vertexShaderHandle);
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
			if (compileStatus[0] == 0) 
			{
				Log.e("KSG", "Błąd kompilacji vertex shadera.");
				Log.e("KSG", GLES20.glGetShaderInfoLog(vertexShaderHandle));
				GLES20.glDeleteShader(vertexShaderHandle);
				vertexShaderHandle = 0;
				return;
			}
		}
		
		// Kompilacja fragment shadera.
		int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		if (fragmentShaderHandle == 0)
		{
			Log.e("KSG", "Nie można utworzyć fragment shadera.");
			return;
		}
		else
		{
			GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);
			GLES20.glCompileShader(fragmentShaderHandle);
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
			if (compileStatus[0] == 0) 
			{
				Log.e("KSG", "Błąd kompilacji fragment shadera.");
				Log.e("KSG", GLES20.glGetShaderInfoLog(fragmentShaderHandle));
				GLES20.glDeleteShader(fragmentShaderHandle);
				fragmentShaderHandle = 0;
				return;
			}
		}
		
		// Linkowanie shaderów.
		int programHandle = GLES20.glCreateProgram();
		if (programHandle == 0)
		{
			Log.e("KSG", "Nie można podlinkować shaderów.");
			return;
		}
		else
		{
			GLES20.glAttachShader(programHandle, vertexShaderHandle);			
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);
			
			GLES20.glBindAttribLocation(programHandle, 0, "vertexPosition");
			GLES20.glBindAttribLocation(programHandle, 1, "vertexColour");
			GLES20.glBindAttribLocation(programHandle, 2, "vertexNormal");
			
			GLES20.glLinkProgram(programHandle);
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
			if (linkStatus[0] == 0) 
			{
				Log.e("KSG", "Błąd linkowania shaderów.");
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
				return;
			}
		}
        
        // Pobranie adresów zmiennych w shaderach.
        MVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "MVPMatrix");
        MVMatrixHandle = GLES20.glGetUniformLocation(programHandle, "MVMatrix");
		lightPositionHandle = GLES20.glGetUniformLocation(programHandle, "lightPosition");
		lightColorHandle = GLES20.glGetUniformLocation(programHandle, "lightColor");
        vertexPositionHandle = GLES20.glGetAttribLocation(programHandle, "vertexPosition");
        vertexColourHandle = GLES20.glGetAttribLocation(programHandle, "vertexColour");
        vertexNormalHandle = GLES20.glGetAttribLocation(programHandle, "vertexNormal");
        
        // Wykorzystanie utworzonych shaderów podczas rysowania.
        GLES20.glUseProgram(programHandle); 
	}
	
	@Override
	public void onSurfaceChanged(GL10 nieUzywac, int width, int height) 
	{
		Log.d("KSG", "Rozdzielczość: " + width + " x " + height);
		
		// Rozciągnięcie widoku OpenGL ES do rozmiarów ekranu.
		GLES20.glViewport(0, 0, width, height);

		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glDepthMask(true);

		// Przygotowanie macierzy projekcji perspektywicznej z uwzględnieniem Field of View.
		final float ratio = (float) width / height;
		final float fov = 60;
		final float near = 1.0f;
		final float far = 10000.0f;
		final float top = (float) (Math.tan(fov * Math.PI / 360.0f) * near);
		final float bottom = -top;
		final float left = ratio * bottom;
		final float right = ratio * top;
		
		Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
	}	

	@Override
	public void onDrawFrame(GL10 nieUzywac) 
	{
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);	
		
		Matrix.setIdentityM(viewMatrix, 0);
        Matrix.setLookAtM(viewMatrix, 0, camera[0], camera[1], camera[2],  camera[3], camera[4], camera[5],  camera[6], camera[7], camera[8]);
        
        // Transformacja i rysowanie trójkąta.
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, -1.0f, 0.0f, -0.5f);
		a += 0.5f;
		Matrix.rotateM(modelMatrix, 0, a, 0.0f, 1.0f, 0.0f);
        drawShape(trianglePositionBuffer, triangleColourBuffer, triangleNormalBuffer, 3, 0);

        Matrix.setIdentityM(modelMatrix, 0);
        a += 0.5f;
        Matrix.translateM(modelMatrix, 0, 1.0f, 0.0f, -1.0f);
        Matrix.rotateM(modelMatrix, 0, a, 1.0f, 1.0f, 1.0f);
        drawShape(trianglePositionBuffer, triangleColourBuffer, triangleNormalBuffer, 3, 1);
        drawShape(trianglePositionBuffer, triangleColourBuffer, triangleNormalBuffer, 3, 2);
        drawShape(trianglePositionBuffer, triangleColourBuffer, triangleNormalBuffer, 3, 3);
        drawShape(trianglePositionBuffer, triangleColourBuffer, triangleNormalBuffer, 3, 4);
        drawShape(trianglePositionBuffer, triangleColourBuffer, triangleNormalBuffer, 3, 5);
        drawShape(trianglePositionBuffer, triangleColourBuffer, triangleNormalBuffer, 3, 6);

	}	
	
	private void drawShape(final FloatBuffer positionBuffer, final FloatBuffer colourBuffer, final FloatBuffer normalBuffer, final int numberOfVertices, final int start)
	{		
		positionBuffer.position(start * 9);
		GLES20.glVertexAttribPointer(vertexPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, positionBuffer);
        GLES20.glEnableVertexAttribArray(vertexPositionHandle);
        
        colourBuffer.position(0);
        GLES20.glVertexAttribPointer(vertexColourHandle, COLOUR_DATA_SIZE, GLES20.GL_FLOAT, false, 0, colourBuffer);  
        GLES20.glEnableVertexAttribArray(vertexColourHandle);
        
        normalBuffer.position(0);
        GLES20.glVertexAttribPointer(vertexNormalHandle, NORMAL_DATA_SIZE, GLES20.GL_FLOAT, false, 0, normalBuffer);
        GLES20.glEnableVertexAttribArray(vertexNormalHandle);

		// Przemnożenie macierzy modelu, widoku i projekcji.
		Matrix.multiplyMM(MVMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, MVMatrix, 0);

        // Przekazanie zmiennych uniform.
		GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, MVPMatrix, 0);
		GLES20.glUniformMatrix4fv(MVMatrixHandle, 1, false, MVMatrix, 0);
		GLES20.glUniform4f(lightColorHandle, lightColor.r, lightColor.g, lightColor.b, 1.0f);
		GLES20.glUniform3f(lightPositionHandle, 0.0f, 2.0f, z);
        
        // Narysowanie obiektu.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, numberOfVertices);                               
	}
}
