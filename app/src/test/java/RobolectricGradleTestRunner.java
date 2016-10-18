import android.os.Build;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;

public class RobolectricGradleTestRunner extends RobolectricTestRunner {

    public static final int EMULATE_SDK_VERSION = Build.VERSION_CODES.JELLY_BEAN_MR2; //Robolectric support API level 18,17, 16, but not 19

    public RobolectricGradleTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        String manifestProperty = System.getProperty("android.manifest");
        if (config.manifest().equals(Config.DEFAULT_MANIFEST) && manifestProperty != null) {
            String resProperty = System.getProperty("android.resources");
            String assetsProperty = System.getProperty("android.assets");
            return new AndroidManifest(Fs.fileFromPath(manifestProperty), Fs.fileFromPath(resProperty),Fs.fileFromPath(assetsProperty)){
                @Override
                public int getTargetSdkVersion() {
                    return EMULATE_SDK_VERSION;
                }
            };

        }
        AndroidManifest appManifest = super.getAppManifest(config);
        return appManifest;
    }
}
