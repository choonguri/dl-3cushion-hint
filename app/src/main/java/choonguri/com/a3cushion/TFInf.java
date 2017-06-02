package choonguri.com.a3cushion;

import android.content.res.AssetManager;
import android.os.Trace;
import android.text.TextUtils;
import android.util.Log;

import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import org.tensorflow.contrib.android.RunStats;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TFInf
 * Created by Choonghyun Seo on 2017. 6. 1.
 */

public class TFInf {

    private static final String TAG = "TFInf";
    private Graph g;
    private Session sess;
    private Session.Runner runner;
    private List<String> feedNames = new ArrayList<>();
    private List<Tensor> feedTensors = new ArrayList<>();
    private List<String> fetchNames = new ArrayList<>();
    private List<Tensor> fetchTensors = new ArrayList<>();
    private boolean enableStats;
    private RunStats runStats;

    public TFInf() {
        Log.i("TFInf", "Checking to see if TensorFlow native methods are already loaded");

        try {
            new RunStats();
            Log.i("TFInf", "TensorFlow native methods already loaded");
        } catch (UnsatisfiedLinkError var4) {
            Log.i("TFInf", "TensorFlow native methods not found, attempting to load via tensorflow_inference");

            try {
                System.loadLibrary("tensorflow_inference");
                Log.i("TFInf", "Successfully loaded TensorFlow native methods (RunStats error may be ignored)");
            } catch (UnsatisfiedLinkError var3) {
                throw new RuntimeException("Native TF methods not found; check that the correct native libraries are present in the APK.");
            }
        }

    }

    public int initializeTensorFlow(AssetManager var1, String var2) {
        boolean var3 = var2.startsWith("file:///android_asset/");
        Object var4 = null;

        try {
            String var5 = var3?var2.split("file:///android_asset/")[1]:var2;
            var4 = var1.open(var5);
        } catch (IOException var9) {
            if(var3) {
                Log.e("TFInf", "Failed to load model from \'" + var2 + "\': " + var9.toString());
                return 1;
            }

            try {
                var4 = new FileInputStream(var2);
            } catch (IOException var8) {
                Log.e("TFInf", "Failed to load model from \'" + var2 + "\': " + var8.toString());
                return 1;
            }
        }

        try {
            this.load((InputStream)var4);
            ((InputStream)var4).close();
            Log.i("TFInf", "Successfully loaded model from \'" + var2 + "\'");
            return 0;
        } catch (IOException var7) {
            Log.e("TFInf", "Failed to load model from \'" + var2 + "\': " + var7.toString());
            return 1;
        }
    }

    public int runInference(String[] var1) {
        this.closeFetches();
        String[] var2 = var1;
        int var3 = var1.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String var5 = var2[var4];
            Log.d(TAG, "var5="+var5);
            this.fetchNames.add(var5);
            TFInf.TensorId var6 = TFInf.TensorId.parse(var5);
            Log.d(TAG, "var6.name="+var6.name);
            this.runner.fetch(var6.name, var6.outputIndex);
        }

        byte var13;
        try {
            if(this.enableStats) {
                Session.Run var12 = this.runner.setOptions(RunStats.runOptions()).runAndFetchMetadata();
                this.fetchTensors = var12.outputs;
                this.runStats.add(var12.metadata);
            } else {
                this.fetchTensors = this.runner.run();
            }
            return 0;
        } catch (RuntimeException var10) {
            Log.e("TFInf", "Failed to run TensorFlow inference with inputs:[" + TextUtils.join(", ", this.feedNames) + "], outputs:[" + TextUtils.join(", ", this.fetchNames) + "]");
            Log.e("TFInf", "Inference exception: " + var10.toString());
            var10.printStackTrace();
            var13 = -1;
        } finally {
            this.closeFeeds();
            this.runner = this.sess.runner();
        }

        return var13;
    }

    public Graph graph() {
        return this.g;
    }

    public void enableStatLogging(boolean var1) {
        this.enableStats = var1;
        if(this.enableStats && this.runStats == null) {
            this.runStats = new RunStats();
        }

    }

    public String getStatString() {
        return this.runStats == null?"":this.runStats.summary();
    }

    public void close() {
        this.closeFeeds();
        this.closeFetches();
        this.sess.close();
        this.g.close();
        if(this.runStats != null) {
            this.runStats.close();
        }

        this.runStats = null;
        this.enableStats = false;
    }

    public void fillNodeFloat(String var1, Object obj) {
        this.addFeed(var1, Tensor.create(obj));
    }

    public void fillNodeFloat(String var1, int[] var2, float[] var3) {
        this.addFeed(var1, Tensor.create(this.mkDims(var2), FloatBuffer.wrap(var3)));
    }

    public void fillNodeInt(String var1, int[] var2, int[] var3) {
        this.addFeed(var1, Tensor.create(this.mkDims(var2), IntBuffer.wrap(var3)));
    }

    public void fillNodeDouble(String var1, int[] var2, double[] var3) {
        this.addFeed(var1, Tensor.create(this.mkDims(var2), DoubleBuffer.wrap(var3)));
    }

    public void fillNodeByte(String var1, int[] var2, byte[] var3) {
        this.addFeed(var1, Tensor.create(DataType.UINT8, this.mkDims(var2), ByteBuffer.wrap(var3)));
    }

    public void fillNodeFromFloatBuffer(String var1, IntBuffer var2, FloatBuffer var3) {
        this.addFeed(var1, Tensor.create(this.mkDims(var2), var3));
    }

    public void fillNodeFromIntBuffer(String var1, IntBuffer var2, IntBuffer var3) {
        this.addFeed(var1, Tensor.create(this.mkDims(var2), var3));
    }

    public void fillNodeFromDoubleBuffer(String var1, IntBuffer var2, DoubleBuffer var3) {
        this.addFeed(var1, Tensor.create(this.mkDims(var2), var3));
    }

    public void fillNodeFromByteBuffer(String var1, IntBuffer var2, ByteBuffer var3) {
        this.addFeed(var1, Tensor.create(DataType.UINT8, this.mkDims(var2), var3));
    }

    public int readNodeFloat(String var1, float[] var2) {
        return this.readNodeIntoFloatBuffer(var1, FloatBuffer.wrap(var2));
    }

    public int readNodeInt(String var1, int[] var2) {
        return this.readNodeIntoIntBuffer(var1, IntBuffer.wrap(var2));
    }

    public int readNodeDouble(String var1, double[] var2) {
        return this.readNodeIntoDoubleBuffer(var1, DoubleBuffer.wrap(var2));
    }

    public int readNodeByte(String var1, byte[] var2) {
        return this.readNodeIntoByteBuffer(var1, ByteBuffer.wrap(var2));
    }

    public int readNodeIntoFloatBuffer(String var1, FloatBuffer var2) {
        Tensor var3 = this.getTensor(var1);
        if(var3 == null) {
            return -1;
        } else {
            var3.writeTo(var2);
            return 0;
        }
    }

    public int readNodeIntoIntBuffer(String var1, IntBuffer var2) {
        Tensor var3 = this.getTensor(var1);
        if(var3 == null) {
            return -1;
        } else {
            var3.writeTo(var2);
            return 0;
        }
    }

    public int readNodeIntoDoubleBuffer(String var1, DoubleBuffer var2) {
        Tensor var3 = this.getTensor(var1);
        if(var3 == null) {
            return -1;
        } else {
            var3.writeTo(var2);
            return 0;
        }
    }

    public int readNodeIntoByteBuffer(String var1, ByteBuffer var2) {
        Tensor var3 = this.getTensor(var1);
        if(var3 == null) {
            return -1;
        } else {
            var3.writeTo(var2);
            return 0;
        }
    }

    private void load(InputStream var1) throws IOException {
        this.g = new Graph();
        this.sess = new Session(this.g);
        this.runner = this.sess.runner();
        long var2 = System.currentTimeMillis();
        Trace.beginSection("initializeTensorFlow");
        Trace.beginSection("readGraphDef");
        byte[] var4 = new byte[var1.available()];
        int var5 = var1.read(var4);
        if(var5 != var4.length) {
            throw new IOException("read error: read only " + var5 + " of the graph, expected to read " + var4.length);
        } else {
            Trace.endSection();
            Trace.beginSection("importGraphDef");

            try {
                this.g.importGraphDef(var4);
            } catch (IllegalArgumentException var8) {
                throw new IOException("Not a valid TensorFlow Graph serialization: " + var8.getMessage());
            }

            Trace.endSection();
            Trace.endSection();
            long var6 = System.currentTimeMillis();
            Log.i("TFInf", "Model load took " + (var6 - var2) + "ms, TensorFlow version: " + TensorFlow.version());
        }
    }

    private long[] mkDims(int[] var1) {
        long[] var2 = new long[var1.length];

        for(int var3 = 0; var3 < var1.length; ++var3) {
            var2[var3] = (long)var1[var3];
        }

        return var2;
    }

    private long[] mkDims(IntBuffer var1) {
        if(var1.hasArray()) {
            return this.mkDims(var1.array());
        } else {
            int[] var2 = new int[var1.remaining()];
            var1.duplicate().get(var2);
            return this.mkDims(var2);
        }
    }

    private void addFeed(String var1, Tensor var2) {
        TFInf.TensorId var3 = TFInf.TensorId.parse(var1);
        Log.d(TAG, "var3.name="+var3.name);
        Log.d(TAG, "var3.outputIndex="+var3.outputIndex);
        Log.d(TAG, "var2="+var2);
        this.runner.feed(var3.name, var3.outputIndex, var2);
        this.feedNames.add(var1);
        this.feedTensors.add(var2);
    }

    private Tensor getTensor(String var1) {
        int var2 = 0;

        for(Iterator var3 = this.fetchNames.iterator(); var3.hasNext(); ++var2) {
            String var4 = (String)var3.next();
            if(var4.equals(var1)) {
                Log.d(TAG, "fetchTensors.size()="+fetchTensors.size());
                return (Tensor)this.fetchTensors.get(var2);
            }
        }

        return null;
    }

    private void closeFeeds() {
        Iterator var1 = this.feedTensors.iterator();

        while(var1.hasNext()) {
            Tensor var2 = (Tensor)var1.next();
            var2.close();
        }

        this.feedTensors.clear();
        this.feedNames.clear();
    }

    private void closeFetches() {
        Iterator var1 = this.fetchTensors.iterator();

        while(var1.hasNext()) {
            Tensor var2 = (Tensor)var1.next();
            var2.close();
        }

        this.fetchTensors.clear();
        this.fetchNames.clear();
    }

    private static class TensorId {
        String name;
        int outputIndex;

        private TensorId() {
        }

        public static TFInf.TensorId parse(String var0) {
            TFInf.TensorId var1 = new TFInf.TensorId();
            int var2 = var0.lastIndexOf(58);
            if(var2 < 0) {
                var1.outputIndex = 0;
                var1.name = var0;
                return var1;
            } else {
                try {
                    var1.outputIndex = Integer.parseInt(var0.substring(var2 + 1));
                    var1.name = var0.substring(0, var2);
                } catch (NumberFormatException var4) {
                    var1.outputIndex = 0;
                    var1.name = var0;
                }

                return var1;
            }
        }
    }
}
