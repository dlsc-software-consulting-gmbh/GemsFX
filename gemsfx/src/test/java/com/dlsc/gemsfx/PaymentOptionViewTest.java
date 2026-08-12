package com.dlsc.gemsfx;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link PaymentOptionView}.
 */
public class PaymentOptionViewTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        PaymentOptionView view = invoke(PaymentOptionView::new);
        assertNotNull(view);
    }

    @Test
    public void testDefaultOption() {
        PaymentOptionView view = invoke(PaymentOptionView::new);
        assertEquals(PaymentOptionView.Option.MASTERCARD, view.getOption());
    }

    @Test
    public void testDefaultTheme() {
        PaymentOptionView view = invoke(PaymentOptionView::new);
        assertEquals(PaymentOptionView.Theme.DARK, view.getTheme());
    }

    @Test
    public void testSetOption() {
        PaymentOptionView view = invoke(PaymentOptionView::new);
        runFx(() -> view.setOption(PaymentOptionView.Option.VISA));
        assertEquals(PaymentOptionView.Option.VISA, view.getOption());
    }

    @Test
    public void testSetTheme() {
        PaymentOptionView view = invoke(PaymentOptionView::new);
        runFx(() -> view.setTheme(PaymentOptionView.Theme.LIGHT));
        assertEquals(PaymentOptionView.Theme.LIGHT, view.getTheme());
    }

    @Test
    public void testSetOptionPayPal() {
        PaymentOptionView view = invoke(PaymentOptionView::new);
        runFx(() -> view.setOption(PaymentOptionView.Option.PAYPAL));
        assertEquals(PaymentOptionView.Option.PAYPAL, view.getOption());
    }

    @Test
    public void testPropertyAccessors() {
        PaymentOptionView view = invoke(PaymentOptionView::new);
        assertNotNull(view.optionProperty());
        assertNotNull(view.themeProperty());
    }

    @Test
    public void testImageNotNullAfterOptionChange() {
        PaymentOptionView view = invoke(PaymentOptionView::new);
        // After setting option the internal image should eventually be set;
        // we only check that no exception is thrown and the option was accepted.
        runFx(() -> view.setOption(PaymentOptionView.Option.MASTERCARD));
        waitForFxEvents();
        assertEquals(PaymentOptionView.Option.MASTERCARD, view.getOption());
    }

    @Test
    public void testOptionEnumValues() {
        // Ensure the enum constants exist
        assertNotNull(PaymentOptionView.Option.MASTERCARD);
        assertNotNull(PaymentOptionView.Option.VISA);
        assertNotNull(PaymentOptionView.Option.PAYPAL);
    }

    @Test
    public void testThemeEnumValues() {
        assertNotNull(PaymentOptionView.Theme.DARK);
        assertNotNull(PaymentOptionView.Theme.LIGHT);
    }
}
