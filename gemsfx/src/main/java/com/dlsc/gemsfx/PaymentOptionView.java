package com.dlsc.gemsfx;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;

public class PaymentOptionView extends ImageView {

    public PaymentOptionView() {
        InvalidationListener updateListener = it -> updateView();
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
                fileName = "AliPay";
                break;
            case AMAZON:
                fileName = "Amazon";
                break;
            case AMEX:
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
                    throw new IllegalArgumentException("theme can not be " + getTheme());
            }

            setImage(new Image(PaymentOptionView.class.getResource("paymentoptions/" + fileName).toExternalForm()));
        } else {
            setImage(null);
        }
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
