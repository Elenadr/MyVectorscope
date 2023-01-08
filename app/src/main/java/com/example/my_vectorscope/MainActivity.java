package com.example.my_vectorscope;

import static android.provider.MediaStore.Images.Media.getBitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ImageView image;
    ImageView graticule;
    ArrayList<Float> Cuadratura_Final = new ArrayList<>();
    ArrayList<Float> Fase_Final = new ArrayList<>();
    public static float [] cuadratura_final;
    public static float [] fase_final;

    Bitmap bitmap;
    Bitmap imagenFinal;
    Bitmap outputBM;
    int outWidth;
    int outHeight;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        graticule = findViewById(R.id.graticule);

        setContentView(R.layout.activity_main);
        //Asociamos el espacion del image view con image en el código
        image = findViewById(R.id.testImage);
        //graticule = findViewById(R.id.graticule);

        //si hay algo en saveInstance
        if (savedInstanceState != null) {
            //Saca el estado de salida con el key guardado en putparceable
            imagenFinal = savedInstanceState.getParcelable("bitmap");
            Log.d("STATE-RESTORE", "bitmap created");
            //Lo pone en el imageview.
            image.setImageBitmap(imagenFinal);
            Log.d("RESTORING...", "onRestoreInstanceState()");
        }
    }

    @SuppressLint("IntentReset")
    public void onclick(View view) {

        // Acceder a la galería
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Es un intent de tipo imagen
        intent.setType("image/*");
        //Esperar un resultado. Requestcode 0 para que no tarde
        startActivityForResult(Intent.createChooser(intent, "Seleccione una aplicación: "), 0);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            cuadratura_final = null;
            fase_final = null;
            //Sacar los datos de la imagen seleccionada
            assert data != null;
            Uri targetUri = data.getData();
            try {
                //convertir uri a bitmap
                bitmap = getBitmap(getApplicationContext().getContentResolver(), targetUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //redimensionar imagenes
            final int maxSize = 548;
            int inWidth = bitmap.getWidth();
            int inHeight = bitmap.getHeight();
            if (inWidth > inHeight) {
                outWidth = maxSize;
                outHeight = (inHeight * maxSize) / inWidth;
            } else {
                outHeight = maxSize;
                outWidth = (inWidth * maxSize) / inHeight;
            }
            imagenFinal = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, true);
            Log.d("Tamaño", "tamaño" + "Altura " + outHeight + "Anchura " + outWidth + imagenFinal.getClass().getSimpleName());

            //Poner esos datos en el imageview
            image.setImageBitmap(imagenFinal);
            RGBtoYIQ();
        }
    }

    // Función de conversión RGB a YIQ
    public void RGBtoYIQ() {
        float r, g, b;
        float Y, I, Q;
        int dimensiones = 3;
        ArrayList<Float> Cuadratura = new ArrayList<>();
        ArrayList<Float> Fase = new ArrayList<>();
        outputBM = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);

        //Matriz directamente permutada
        float[][][] matrizYIQ = new float[outWidth][outHeight][3];
        for (int x = 0; x < matrizYIQ.length; x++) { // matriz.length = 180, columnas
            for (int y = 0; y < matrizYIQ[x].length; y++) { //matrix.length[x] = 135,filas
                for (int z = 0; z < matrizYIQ[x][y].length; z++) { //matriz.length[x][y] = 3 dimensiones

                    int pixel = imagenFinal.getPixel(x, y);
                    r = (pixel >> 16) & 0xff;
                    g = (pixel >> 8) & 0xff;
                    b = pixel & 0xff;
                    //Para pasar de uint 8 a double hay qye dividir entre 255
                    Y = (float) (Math.round(((0.299 * r + 0.587 * g + 0.114 * b) / 255.0) * 1000.0) / 10000.0);
                    (matrizYIQ[x][y][0]) = Y;

                    //Log.i("!!!!!!", "Y" + Y);
                    I = (float) (Math.round(((r * 0.595879 + g * -0.274133 + b * -0.321746) / 255.0) * 10000.0) / 10000.0);
                    (matrizYIQ[x][y][1]) = I;
                    Q = (float) (Math.round(((r * 0.211205 + g * -0.523083 + b * 0.311878) / 255.0) * 10000.0) / 10000.0);
                    (matrizYIQ[x][y][2]) = Q;
                }
            }
        }
        // FIN DEL Cálculo de la matriz tridimensional.
        //Definir nuevo array multidimensional para reorganizar valores
        //Tendrá filas  = filas * columnas
        // columnas = 3, siempre para YIQ
        float[][] matriz2D = new float[outWidth * outHeight][dimensiones];
        //Inicializamos la u, nueva fila de la nueva matriz en cero
        int u = 0;
        // Recorremos las dimensiones
        for (int k = 0; k < dimensiones; k++) {
            for (int j = 0; j < outHeight; j++) {
                for (int i = 0; i < outWidth; i++) {
                    if (k == 0) {
                        //Rellenar primera columna con La información relativa a Luminancia
                        matriz2D[u][0] = matrizYIQ[i][j][k];
                    } else if (k == 1) {
                        //Rellenar segunda columna con La información relativa a in-phase
                        matriz2D[u][1] = matrizYIQ[i][j][k];
                    } else {
                        //Rellenar tercera columna con La información relativa a quadratura
                        matriz2D[u][2] = matrizYIQ[i][j][k];
                    }
                    //Una vez relleno dato, saltamos a la siguiente fila
                    u++;
                }
            }
            // Para la siguiente dimensión, empezamos en la fila 0, reiniciamos la u
            u = 0;
        }

        // Lista ordenada con Cuadratura y fase
        for (int i = 0; i < matriz2D.length; i++) {
            for (int j = 0; j < matriz2D[0].length; j++) {
                if (j == 1) {
                    Cuadratura.add(i, (matriz2D[i][1]));
                } else if (j == 2) {
                    Fase.add(i, matriz2D[i][2]);
                }
            }
        }

        cuadratura_final = new float[Cuadratura.size()];
        for (int i = 0; i < Cuadratura.size(); i++) {
            cuadratura_final[i] = Cuadratura.get(i);

        }
        fase_final = new float[Fase.size()];
        for (int i = 0; i < Fase.size(); i++) {
            fase_final[i] = Fase.get(i);
        }
    }
}