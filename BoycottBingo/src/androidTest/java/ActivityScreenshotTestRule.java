import android.app.Activity;
import androidx.test.rule.ActivityTestRule;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;

class ActivityScreenshotTestRule<A extends Activity> extends ActivityTestRule<A> {

    ActivityScreenshotTestRule(Class<A> activityClass) {
        super(activityClass);
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();
    }

    @Override
    protected void afterActivityLaunched() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());

        super.afterActivityLaunched();
    }
}
