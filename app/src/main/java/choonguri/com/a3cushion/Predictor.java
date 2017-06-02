package choonguri.com.a3cushion;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.IOException;

/**
 * Predictor
 * Created by Choonghyun Seo on 2017. 6. 1.
 */

public class Predictor {

    private static final String TAG = "Predictor";

    private static int IMAGE_WIDTH = 64;
    private static int IMAGE_HEIGHT = 32;
    private static int CHANNEL = 3;
    private static float NORMALIZATION_CONST = 2550.0f;

    private TFInf inferenceInterface;
    private float[] outputs;
    private String[] outputNames;
    private String inputName;
    private String outputName;

    public static Predictor create(
            AssetManager assetManager,
            String modelFilename,
            String inputName,
            String outputName)
            throws IOException {
        Predictor c = new Predictor();

        c.inputName = inputName;
        c.outputName = outputName;

        c.inferenceInterface = new TFInf();
        if (c.inferenceInterface.initializeTensorFlow(assetManager, modelFilename) != 0) {
            throw new RuntimeException("TF initialization failed");
        }
        // The shape of the output is [N, NUM_CLASSES], where N is the batch size.
        int numClasses =
                (int) c.inferenceInterface.graph().operation(outputName).output(0).shape().size(1);
        Log.d(TAG, "numClasses="+numClasses);

        // Pre-allocate buffers.
        c.outputNames = new String[]{outputName};
        c.outputs = new float[numClasses];

        return c;
    }

    public float[] getResult(Bitmap bitmap) {
        int[] intValues = new int[IMAGE_WIDTH * IMAGE_HEIGHT];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        float[] inputFloatValue = new float[IMAGE_WIDTH * IMAGE_HEIGHT * CHANNEL];
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            inputFloatValue[i * 3] = ((val >> 16) & 0xFF) / NORMALIZATION_CONST;
            inputFloatValue[i * 3 + 1] = ((val >> 8) & 0xFF) / NORMALIZATION_CONST;
            inputFloatValue[i * 3 + 2] = (val & 0xFF) / NORMALIZATION_CONST;
        }

        // Copy the input data into TensorFlow.
        inferenceInterface.fillNodeFloat(inputName, new int[]{1, IMAGE_WIDTH * IMAGE_HEIGHT, CHANNEL}, inputFloatValue);
        inferenceInterface.fillNodeFloat("Placeholder_1", new int[]{1}, new float[]{1.0f}); // dropout

        // Run the inference call.
        inferenceInterface.runInference(outputNames);

        // Copy the output Tensor back into the output array.
        inferenceInterface.readNodeFloat(outputName, outputs);

        return outputs;
    }

    public void close() {
        inferenceInterface.close();
    }
}
