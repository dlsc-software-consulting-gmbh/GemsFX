package com.dlsc.gemsfx;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.dlsc.gemsfx.util.StringUtils;

import java.util.Objects;

/**
 * A control for displaying different types of payment options, e.g. various
 * credit cards (MasterCard, American Express, Visa) but also new online options
 * such as PayPal or ApplePay. The default width is initialized with 100 pixels,
 * however the images are much bigger than that (500x300).
 */
public class PaymentOptionView extends ImageView {

    /**
     * Constructs a new view.
     */
    public PaymentOptionView() {
        InvalidationListener updateListener = it -> updateView();
        optionProperty().addListener(updateListener);
        themeProperty().addListener(updateListener);
        setFitWidth(100);
        setPreserveRatio(true);
        updateView();
    }

    private void updateView() {
        String fileName = "";
        switch (getOption()) {
            case UNKNOWN:
                break;
            case CREDIT_CARD:
                fileName = "CreditCard";
                break;
            case CHECKOUT2:
                fileName = "2checkout";
                break;
            case ALI_PAY:
                fileName = "AliPay";
                break;
            case AMAZON:
                fileName = "Amazon";
                break;
            case AMERICAN_EXPRESS:
                fileName = "AmericanExpress";
                break;
            case APPLE_PAY:
                fileName = "ApplePay";
                break;
            case BANCONTACT:
                fileName = "Bancontact";
                break;
            case BITCOIN:
                fileName = "Bitcoin";
                break;
            case BITPAY:
                fileName = "Bitpay";
                break;
            case CIRRUS:
                fileName = "Cirrus";
                break;
            case CLICKANDBUY:
                fileName = "Clickandbuy";
                break;
            case COINKITE:
                fileName = "CoinKite";
                break;
            case DINERSCLUB:
                fileName = "DinersClub";
                break;
            case DIRECTDEBIT:
                fileName = "DirectDebit";
                break;
            case DISCOVER:
                fileName = "Discover";
                break;
            case DWOLLA:
                fileName = "Dwolla";
                break;
            case EBAY:
                fileName = "Ebay";
                break;
            case EWAY:
                fileName = "Eway";
                break;
            case GIROPAY:
                fileName = "GiroPay";
                break;
            case GOOGLEWALLET:
                fileName = "GoogleWallet";
                break;
            case INGENICO:
                fileName = "Ingenico";
                break;
            case JCB:
                fileName = "JCB";
                break;
            case KLARNA:
                fileName = "Klarna";
                break;
            case LASER:
                fileName = "Laser";
                break;
            case MAESTRO:
                fileName = "Maestro";
                break;
            case MASTERCARD:
                fileName = "MasterCard";
                break;
            case MONERO:
                fileName = "Monero";
                break;
            case NETELLER:
                fileName = "Neteller";
                break;
            case OGONE:
                fileName = "Ogone";
                break;
            case OKPAY:
                fileName = "OkPay";
                break;
            case PAYBOX:
                fileName = "PayBox";
                break;
            case PAYMILL:
                fileName = "Paymill";
                break;
            case PAYONE:
                fileName = "Payone";
                break;
            case PAYONEER:
                fileName = "Payoneer";
                break;
            case PAYPAL:
                fileName = "Paypal";
                break;
            case PAYSAFECARD:
                fileName = "PaysafeCard";
                break;
            case PAYU:
                fileName = "PayU";
                break;
            case PAYZA:
                fileName = "Payza";
                break;
            case RIPPLE:
                fileName = "Ripple";
                break;
            case SAGE:
                fileName = "Sage";
                break;
            case SEPA:
                fileName = "Sepa";
                break;
            case SHOPIFY:
                fileName = "Shopify";
                break;
            case SKRILL:
                fileName = "Skrill";
                break;
            case SOLO:
                fileName = "Solo";
                break;
            case SQUARE:
                fileName = "Square";
                break;
            case STRIPE:
                fileName = "Stripe";
                break;
            case SWITCH:
                fileName = "Switch";
                break;
            case UKASH:
                fileName = "Ukash";
                break;
            case UNIONPAY:
                fileName = "UnionPay";
                break;
            case VERIFONE:
                fileName = "Verifone";
                break;
            case VERISIGN:
                fileName = "VeriSign";
                break;
            case VISA:
                fileName = "Visa";
                break;
            case WEBMONEY:
                fileName = "WebMoney";
                break;
            case WESTERNUNION:
                fileName = "WesternUnion";
                break;
            case WORLDPAY:
                fileName = "WorldPay";
                break;
            default:
                throw new IllegalArgumentException("option can not be " + getOption());
        }

        if (StringUtils.isNotBlank(fileName)) {
            switch (getTheme()) {
                case DARK:
                    fileName = fileName + "-dark.png";
                    break;
                case LIGHT:
                    fileName = fileName + "-light.png";
                    break;
                default:
                    throw new IllegalStateException("Unexpected theme: " + getTheme());
            }

            setImage(new Image(Objects.requireNonNull(PaymentOptionView.class.getResource("paymentoptions/" + fileName)).toExternalForm()));
        } else {
            setImage(null);
        }
    }

    // OPTION SUPPORT

    private final ObjectProperty<Option> option = new SimpleObjectProperty<>(this, "option", Option.MASTERCARD);

    public final Option getOption() {
        return option.get();
    }

    /**
     * The option determines which graphic will be shown, e.g. Option.MASTER_CARD.
     *
     * @return the currently displayed payment option
     */
    public final ObjectProperty<Option> optionProperty() {
        return option;
    }

    public final void setOption(Option option) {
        this.option.set(option);
    }


    // THEME SUPPORT

    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>(this, "style", Theme.DARK);

    public final Theme getTheme() {
        return theme.get();
    }

    /**
     * The theme determines if the view displays the dark or the light version
     * of a payment option graphic.
     *
     * @return the currently used theme (dark, light)
     */
    public final ObjectProperty<Theme> themeProperty() {
        return theme;
    }

    public final void setTheme(Theme theme) {
        this.theme.set(theme);
    }

    /**
     * The payment option supports two different themes. A dark and a light
     * theme. The light theme consists of payment option graphics with a light /
     * white background. The dark theme uses different solid background colors
     * for each option.
     *
     * @see PaymentOptionView#setTheme(Theme)
     */
    public enum Theme {

        /**
         * The dark theme returns graphics with solid color backgrounds, other than
         * white.
         */
        DARK,

        /**
         * The light theme returns each payment option graphic with a white background.
         */
        LIGHT
    }

    /**
     * The list of supported payment options consisting of popular credit cards and online
     * payment options.
     *
     * @see PaymentOptionView#setOption(Option)
     */
    public enum Option {
        /**
         * An unknown or unsupported payment option.
         */
        UNKNOWN,
        /**
         * A generic credit card payment option.
         */
        CREDIT_CARD,
        /**
         * The 2Checkout payment provider.
         */
        CHECKOUT2,
        /**
         * The AliPay payment provider.
         */
        ALI_PAY,
        /**
         * The Amazon payments provider.
         */
        AMAZON,
        /**
         * The American Express card network.
         */
        AMERICAN_EXPRESS,
        /**
         * The Apple Pay payment provider.
         */
        APPLE_PAY,
        /**
         * The Bancontact payment provider.
         */
        BANCONTACT,
        /**
         * The Bitcoin payment option.
         */
        BITCOIN,
        /**
         * The BitPay payment provider.
         */
        BITPAY,
        /**
         * The Cirrus card network.
         */
        CIRRUS,
        /**
         * The ClickandBuy payment provider.
         */
        CLICKANDBUY,
        /**
         * The CoinKite payment provider.
         */
        COINKITE,
        /**
         * The Diners Club card network.
         */
        DINERSCLUB,
        /**
         * The direct debit payment option.
         */
        DIRECTDEBIT,
        /**
         * The Discover card network.
         */
        DISCOVER,
        /**
         * The Dwolla payment provider.
         */
        DWOLLA,
        /**
         * The eBay payment provider.
         */
        EBAY,
        /**
         * The eWAY payment provider.
         */
        EWAY,
        /**
         * The giropay payment provider.
         */
        GIROPAY,
        /**
         * The Google Wallet payment provider.
         */
        GOOGLEWALLET,
        /**
         * The Ingenico payment provider.
         */
        INGENICO,
        /**
         * The JCB card network.
         */
        JCB,
        /**
         * The Klarna payment provider.
         */
        KLARNA,
        /**
         * The Laser card network.
         */
        LASER,
        /**
         * The Maestro card network.
         */
        MAESTRO,
        /**
         * The MasterCard card network.
         */
        MASTERCARD,
        /**
         * The Monero payment option.
         */
        MONERO,
        /**
         * The Neteller payment provider.
         */
        NETELLER,
        /**
         * The Ogone payment provider.
         */
        OGONE,
        /**
         * The OKPAY payment provider.
         */
        OKPAY,
        /**
         * The Paybox payment provider.
         */
        PAYBOX,
        /**
         * The Paymill payment provider.
         */
        PAYMILL,
        /**
         * The PAYONE payment provider.
         */
        PAYONE,
        /**
         * The Payoneer payment provider.
         */
        PAYONEER,
        /**
         * The PayPal payment provider.
         */
        PAYPAL,
        /**
         * The paysafecard payment provider.
         */
        PAYSAFECARD,
        /**
         * The PayU payment provider.
         */
        PAYU,
        /**
         * The Payza payment provider.
         */
        PAYZA,
        /**
         * The Ripple payment option.
         */
        RIPPLE,
        /**
         * The Sage payment provider.
         */
        SAGE,
        /**
         * The SEPA bank transfer payment option.
         */
        SEPA,
        /**
         * The Shopify payment provider.
         */
        SHOPIFY,
        /**
         * The Skrill payment provider.
         */
        SKRILL,
        /**
         * The Solo card network.
         */
        SOLO,
        /**
         * The Square payment provider.
         */
        SQUARE,
        /**
         * The Stripe payment provider.
         */
        STRIPE,
        /**
         * The Switch card network.
         */
        SWITCH,
        /**
         * The Ukash payment provider.
         */
        UKASH,
        /**
         * The UnionPay card network.
         */
        UNIONPAY,
        /**
         * The Verifone payment provider.
         */
        VERIFONE,
        /**
         * The VeriSign payment provider.
         */
        VERISIGN,
        /**
         * The Visa card network.
         */
        VISA,
        /**
         * The WebMoney payment provider.
         */
        WEBMONEY,
        /**
         * The Western Union payment provider.
         */
        WESTERNUNION,
        /**
         * The WorldPay payment provider.
         */
        WORLDPAY
    }
}
