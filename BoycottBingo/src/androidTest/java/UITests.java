import androidx.test.rule.ActivityTestRule;
import android.view.View;
import android.view.ViewGroup;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;
import uk.co.nathanwong.boycottbingo.MainActivity;
import uk.co.nathanwong.boycottbingo.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(JUnit4.class)
public class UITests {
    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityScreenshotTestRule<>(MainActivity.class);

    @Test
    public void testTakeScreenshot() {

        Screengrab.screenshot("1_initial");

        onView(nthChildOf(nthChildOf(withId(R.id.main_rows), 1), 0))
                .perform(click());

        onView(nthChildOf(nthChildOf(withId(R.id.main_rows), 2), 1))
                .perform(click());

        Screengrab.screenshot("2_selected");

//        onView(withId(R.id.fab)).perform(click());
//        assertThat();
    }

    private static Matcher<View> nthChildOf(final Matcher<View> parentMatcher, final int childPosition) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with "+childPosition+" child view of type parentMatcher");
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view.getParent() instanceof ViewGroup)) {
                    return parentMatcher.matches(view.getParent());
                }

                ViewGroup group = (ViewGroup) view.getParent();
                return parentMatcher.matches(view.getParent()) && group.getChildAt(childPosition).equals(view);
            }
        };
    }
}
