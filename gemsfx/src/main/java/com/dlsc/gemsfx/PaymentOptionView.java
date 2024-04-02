package com.dlsc.gemsfx;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;

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
            fileName = switch (getTheme()) {
                case DARK -> fileName + "-dark.png";
                case LIGHT -> fileName + "-light.png";
            };

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
        UNKNOWN,
        CREDIT_CARD,
        CHECKOUT2,
        ALI_PAY,
        AMAZON,
        AMERICAN_EXPRESS,
        APPLE_PAY,
        BANCONTACT,
        BITCOIN,
        BITPAY,
        CIRRUS,
        CLICKANDBUY,
        COINKITE,
        DINERSCLUB,
        DIRECTDEBIT,
        DISCOVER,
        DWOLLA,
        EBAY,
        EWAY,
        GIROPAY,
        GOOGLEWALLET,
        INGENICO,
        JCB,
        KLARNA,
        LASER,
        MAESTRO,
        MASTERCARD,
        MONERO,
        NETELLER,
        OGONE,
        OKPAY,
        PAYBOX,
        PAYMILL,
        PAYONE,
        PAYONEER,
        PAYPAL,
        PAYSAFECARD,
        PAYU,
        PAYZA,
        RIPPLE,
        SAGE,
        SEPA,
        SHOPIFY,
        SKRILL,
        SOLO,
        SQUARE,
        STRIPE,
        SWITCH,
        UKASH,
        UNIONPAY,
        VERIFONE,
        VERISIGN,
        VISA,
        WEBMONEY,
        WESTERNUNION,
        WORLDPAY
    }
}
