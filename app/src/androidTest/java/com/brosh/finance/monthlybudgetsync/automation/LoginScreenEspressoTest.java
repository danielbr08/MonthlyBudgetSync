package com.brosh.finance.monthlybudgetsync.automation;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.login.Login;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI smoke test: login screen renders required controls without crashing.
 */
@RunWith(AndroidJUnit4.class)
public class LoginScreenEspressoTest {

    @Test
    public void loginScreen_displaysEmailPasswordAndLoginButton() {
        try (ActivityScenario<Login> scenario = ActivityScenario.launch(Login.class)) {
            onView(withId(R.id.Email)).check(matches(isDisplayed()));
            onView(withId(R.id.password)).check(matches(isDisplayed()));
            onView(withId(R.id.loginBtn)).check(matches(isDisplayed()));
        }
    }
}
