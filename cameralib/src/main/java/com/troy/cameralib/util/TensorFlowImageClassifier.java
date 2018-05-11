

package com.troy.cameralib.util;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Trace;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/** A classifier specialized to label images using TensorFlow. */
public class TensorFlowImageClassifier implements Classifier {
    private static final String TAG = "TensorFlowInferenceInterface";

    // Only return this many results with at least this confidence.
    private static final int MAX_RESULTS = 3;
    private static final float THRESHOLD = 0.1f;

    // Config values.
    private String inputName;
    private String outputName;
    private int inputHight;
    private int inputWidth;
    private int imageMean;
    private float imageStd;

    // Pre-allocated buffers.
    private int[] intValues;
    private float[] floatValues;
    private int[] outputs;
    private String[] outputNames;

    private boolean logStats = false;

    private TensorFlowInferenceInterface inferenceInterface;

    private TensorFlowImageClassifier() {
    }

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager  The asset manager to be used to load assets.
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @param inputHight    The input size. A square image of inputSize x inputSize is assumed.
     * @param inputWidth    The input size. A square image of inputSize x inputSize is assumed.
     * @param imageMean     The assumed mean of the image values.
     * @param imageStd      The assumed std of the image values.
     * @param inputName     The label of the image input node.
     * @param outputName    The label of the output node.
     */
    public static Classifier create(
            AssetManager assetManager,
            String modelFilename,
            int inputHight,
            int inputWidth,
            int imageMean,
            float imageStd,
            String inputName,
            String outputName) {
        TensorFlowImageClassifier c = new TensorFlowImageClassifier();
        c.inputName = inputName;
        c.outputName = outputName;

        // Read the label names into memory.
        // TODO(andrewharp): make this handle non-assets.


        c.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);

        // The shape of the output is [N, NUM_CLASSES], where N is the batch size.
        Graph g = c.inferenceInterface.graph();
        final Operation inputOp = g.operation(inputName);
        if (inputOp == null) {
            Log.i(TAG, "inputOp is null!!!!!!!!!!!!!!1" + inputName);

        } else {
            Log.i(TAG, "inputOp 的结构是" + inputOp.output(0).shape().toString());


        }

        final Operation inputOp2 = g.operation("fyx_seqlen");
        if (inputOp2 == null) {
            Log.i(TAG, "inputOp is null!!!!!!!!!!!!!!1" + inputName);

        } else {
            Log.i(TAG, "inputOp2 fyx_seqlen 的结构是" + inputOp2.output(0).shape().toString());

        }
        final Operation outputOp = g.operation(outputName);
        if (outputOp == null) {
            Log.i(TAG, "outputOp is null!!!!!!!!!!!!!!1" + outputName);

        } else {
            Log.i(TAG, "outputOp  的结构是" + outputOp.output(0).shape().toString());

        }
        final int numClasses = (int) outputOp.output(0).shape().size(1);
        Log.i(TAG, "Read   labels, output layer size is " + numClasses);

        // Ideally, inputSize could have been retrieved from the shape of the input outputOp.
        // Alas,
        // the placeholder node for input in the graphdef typically used does not specify a
        // shape, so it
        // must be passed in as a parameter.
        c.inputHight = inputHight;
        c.inputWidth = inputWidth;
        c.imageMean = imageMean;
        c.imageStd = imageStd;

        // Pre-allocate buffers.
        c.outputNames = new String[]{outputName};
        c.intValues = new int[inputWidth * inputHight];
        c.floatValues = new float[inputWidth * inputHight * 1];
        c.outputs =
                new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                        -1, -1, -1, -1, -1, -1};

        return c;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public String recognizeImage(final Bitmap bitmap) {
        // Log this method so that it can be analyzed with systrace.
        Log.i(TAG, "recognizeImage开始  " + inferenceInterface.getStatString() + "----" +
                bitmap.getWidth() + "------" + bitmap.getHeight());


        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),
                bitmap.getHeight());
        Log.i(TAG, "recognizeImage  intValues.length  " + intValues.length + floatValues.length);
        StringBuilder imagefloat = new StringBuilder();

        for (int i = 0; i < 6000; i++) {
            int val = intValues[i];

            floatValues[i] = (float) ((val & 0xFF) / 255f);
            //     imagefloat.append(floatValues[i]+" ");


//            floatValues[i * 3 + 1] = (((val >> 8) & 0xFF) - imageMean) / imageStd;
//            floatValues[i * 3 + 2] = ((val & 0xFF) - imageMean) / imageStd;

//            int b = val & 0xff;
//            floatValues[i] = (float)((0xff - b)/255.0);
            //     Log.i(TAG, "recognizeImage   floatValues[i]" +  floatValues[i]);

        }
        Log.i(TAG, "recognizeImage处理图片完成开始feed" + imagefloat);

        // Copy the input data into TensorFlow.
        inferenceInterface.feed(inputName, floatValues, 1, inputHight, inputWidth, 1);
        int[] ints = {64};
        inferenceInterface.feed("fyx_seqlen", ints, 1);
        Log.i(TAG, "recognizeImage  feed完成,开始运行" + inferenceInterface.getStatString());

        // Run the inference call.
        inferenceInterface.run(outputNames, false);
        Log.i(TAG, "recognizeImage  运行完成,开始fetch");

        // Copy the output Tensor back into the output array.
        Trace.beginSection("fetch");
        inferenceInterface.fetch(outputName, outputs);
        Trace.endSection();
        Log.i(TAG, "recognizeImage  fetch完成");

        // Find the best classifications.
//    PriorityQueue<Recognition> pq =
//        new PriorityQueue<Recognition>(
//            3,
//            new Comparator<Recognition>() {
//              @Override
//              public int compare(Recognition lhs, Recognition rhs) {
//                // Intentionally reversed to put high confidence at the head of the queue.
//                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
//              }
//            });
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < outputs.length; ++i) {
            //   stringBuilder.append(outputs[i]-1);
             if (outputs[i] == 0) {
                stringBuilder.append(" ");
            } else {
                stringBuilder.append(outputs[i] - 1);
            }

        }
        Log.i(TAG, "recognizeImage  拼接完成" + stringBuilder);
        //       final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
//    int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
//    for (int i = 0; i < recognitionsSize; ++i) {
//      recognitions.add(pq.poll());
//    }
        Trace.endSection(); // "recognizeImage"
        return stringBuilder.toString().replace("-2", "");
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public Bitmap recognizeImage(int[] imageints) {
        StringBuilder imagefloat = new StringBuilder();
        Log.i(TAG, "recognizeImage开始 int[] " + imageints.length);


        for (int i = 0; i < imageints.length; i++) {

            int val = imageints[i];
            floatValues[i] = (float) ((val & 0xFF));

            imagefloat.append(floatValues[i] + " ");


        }
        Log.i(TAG, "recognizeImage处理图片完成开始feed" + imagefloat);

        // Copy the input data into TensorFlow.
        inferenceInterface.feed(inputName, floatValues, 1, inputHight, inputWidth, 1);
        int[] ints = {64};

        inferenceInterface.feed("fyx_seqlen", ints, 1);
        Log.i(TAG, "recognizeImage  feed完成,开始运行" + inferenceInterface.getStatString());

        // Run the inference call.
        inferenceInterface.run(outputNames, false);
        Log.i(TAG, "recognizeImage  运行完成,开始fetch");

        // Copy the output Tensor back into the output array.
        Trace.beginSection("fetch");
        inferenceInterface.fetch(outputName, outputs);
        Trace.endSection();
        Log.i(TAG, "recognizeImage  fetch完成");


        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < outputs.length; ++i) {

            stringBuilder.append(outputs[i]);

        }

        Log.i(TAG, "recognizeImage  拼接完成" + stringBuilder);

        Bitmap result = Bitmap.createBitmap(200, 30, Bitmap.Config.RGB_565);
        result.setPixels(imageints, 0, 200, 0, 0, 200, 30);
        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(result, 200, 30);
        if (!result.isRecycled()) {
            result.recycle();
        }
        return resizeBmp;

    }

    @Override
    public void enableStatLogging(boolean logStats) {
        this.logStats = logStats;
    }

    @Override
    public String getStatString() {
        return inferenceInterface.getStatString();
    }

    @Override
    public void close() {
        inferenceInterface.close();
    }


}
