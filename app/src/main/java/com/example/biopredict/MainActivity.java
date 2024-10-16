package com.example.biopredict;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    EditText inputFieldMeanRadius;
    EditText inputFieldMeanPerimeter;
    EditText inputFieldMeanArea;

    Button predictBtn;

    TextView resultTV;

    Interpreter interpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        try {
            interpreter = new Interpreter(loadModelFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        inputFieldMeanRadius = findViewById(R.id.editTextMeanRadius);
        inputFieldMeanPerimeter = findViewById(R.id.editTextMeanPerimeter);
        inputFieldMeanArea = findViewById(R.id.editTextMeanArea);

        predictBtn = findViewById(R.id.button);
        resultTV = findViewById(R.id.textView);

        predictBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String radiusInput = inputFieldMeanRadius.getText().toString();
                String perimeterInput = inputFieldMeanPerimeter.getText().toString();
                String areaInput = inputFieldMeanArea.getText().toString();



                float radiusValue = Float.parseFloat(radiusInput);
                float perimeterValue = Float.parseFloat(perimeterInput);
                float areaValue = Float.parseFloat(areaInput);

                float[][] inputs = new float[1][3]; // Cambiado a 3 para tres características
                inputs[0][0] = radiusValue;
                inputs[0][1] = perimeterValue;
                inputs[0][2] = areaValue;

                float result = doInference(inputs);

                resultTV.setText("Result: " + result);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public float doInference(float[][] input) {
        float[][] output = new float[1][1]; // La salida es también de tamaño 1
        interpreter.run(input, output);
        return output[0][0]; // Devolver el resultado
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        try {
            AssetFileDescriptor assetFileDescriptor = this.getAssets().openFd("linear_model.tflite");
            FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
            FileChannel fileChannel = fileInputStream.getChannel();
            long startOffset = assetFileDescriptor.getStartOffset();
            long length = assetFileDescriptor.getLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length);
        } catch (IOException e) {
            // Maneja el error aquí
            throw new IOException("Error al cargar el archivo del modelo TensorFlow Lite: " + e.getMessage());
        }
    }
}