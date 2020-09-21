package com.dlsc.gemsfx;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PaymentOptionView extends ImageView {

    public PaymentOptionView() {
        final InvalidationListener updateListener = it -> updateView();
        optionProperty().addListener(updateListener);
        themeProperty().addListener(updateListener);
        updateView();
    }

    private void updateView() {
        String fileName = "";
        switch (getOption()) {
            case UNKNOWN:
                break;
            case CHECKOUT2:
                fileName = "2checkout";
                break;
            case ALI_PAY:
                break;
            case AMAZON:
                break;
            case AMEX:
                break;
            case APPLE_PAY:
                break;
            case BANCONTACT:
                break;
            case BITCOIN:
                break;
            case BITPAY:
                break;
            case CIRRUS:
                break;
            case CLICKANDBUY:
                break;
            case COINKITE:
                break;
            case DINERSCLUB:
                break;
            case DIRECTDEBIT:
                break;
            case DISCOVER:
                break;
            case DWOLLA:
                break;
            case EBAY:
                break;
            case EWAY:
                break;
            case GIROPAY:
                break;
            case GOOGLEWALLET:
                break;
            case INGENICO:
                break;
            case JCB:
                break;
            case KLARNA:
                break;
            case LASER:
                break;
            case MAESTRO:
                break;
            case MASTERCARD:
                fileName = "MasterCard";
                break;
            case MONERO:
                break;
            case NETELLER:
                break;
            case OGONE:
                break;
            case OKPAY:
                break;
            case PAYBOX:
                break;
            case PAYMILL:
                break;
            case PAYONE:
                break;
            case PAYONEER:
                break;
            case PAYPAL:
                break;
            case PAYSAFECARD:
                break;
            case PAYU:
                break;
            case PAYZA:
                break;
            case RIPPLE:
                break;
            case SAGE:
                break;
            case SEPA:
                break;
            case SHOPIFY:
                break;
            case SKRILL:
                break;
            case SOLO:
                break;
            case SQUARE:
                break;
            case STRIPE:
                break;
            case SWITCH:
                break;
            case UKASH:
                break;
            case UNIONPAY:
                break;
            case VERIFONE:
                break;
            case VERISIGN:
                break;
            case VISA:
                break;
            case WEBMONEY:
                break;
            case WESTERNUNION:
                break;
            case WORLDPAY:
                break;
            default:
                throw new IllegalArgumentException("option can not be " + getOption());
        }

        switch (getTheme()) {
            case DARK:
                fileName = fileName + "-dark.png";
                break;
            case LIGHT:
                fileName = fileName + "-light.png";
                break;
            default:
                throw new IllegalArgumentException("theme can not be " + getTheme());
        }

        setImage(new Image(PaymentOptionView.class.getResource("paymentoptions/" + fileName).toExternalForm()));
    }

    // TYPE

    private final ObjectProperty<Option> option = new SimpleObjectProperty<>(this, "option", Option.MASTERCARD);

    public final Option getOption() {
        return option.get();
    }

    public final ObjectProperty<Option> optionProperty() {
        return option;
    }

    public final void setOption(Option option) {
        this.option.set(option);
    }

    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>(this, "style", Theme.DARK);

    public final Theme getTheme() {
        return theme.get();
    }

    public final ObjectProperty<Theme> themeProperty() {
        return theme;
    }

    public final void setTheme(Theme theme) {
        this.theme.set(theme);
    }

    public enum Theme {
        DARK,
        LIGHT
    }

    public enum Option {
        UNKNOWN,
        CHECKOUT2,
        ALI_PAY,
        AMAZON,
        AMEX,
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
