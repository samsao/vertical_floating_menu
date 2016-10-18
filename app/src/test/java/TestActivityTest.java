import android.app.Activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.junit.Assert.assertNotEquals;

@RunWith(RobolectricGradleTestRunner.class)
public class TestActivityTest {

    private Activity activity;

    @Before
    public void setup(){
        activity = Robolectric.buildActivity(Activity.class).create().get();
    }

    @Test
    public void shouldSuccess() {
        assertNotEquals(activity, null);
    }


}
