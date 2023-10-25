package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PhoneNumberFieldSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

/**
 * A control for entering phone numbers. The control supports a list of available country codes and works based on the
 * {@link CountryCallingCode CountryCallingCode} interface. This interface allows customizing the country codes and their
 * respective area codes, in case the default values are not sufficient or the numbers change.
 */
public class PhoneNumberField extends Control {

    /**
     * Default style class for css styling.
     */
    public static final String DEFAULT_STYLE_CLASS = "phone-number-field";

    /**
     * Default mask for the phone number field.
     */
    public static final String DEFAULT_MASK = "(___) ___-____";

    /**
     * Default character used to represent a digit in the mask.
     */
    public static final char DEFAULT_MASK_DIGIT_CHAR = '_';

    /**
     * List of supported characters a mask can have.
     */
    public static final List<Character> DEFAULT_MASK_SUPPORTED_CHARS = Arrays.asList(DEFAULT_MASK_DIGIT_CHAR, '-', '(', ')', ' ');

    private final PhoneNumberParser parser;
    private final PhoneNumberFormatter formatter;

    /**
     * Builds a new phone number field with the default settings. The available country calling codes is taken from
     * {@link CountryCallingCode.Defaults Defaults}.
     */
    public PhoneNumberField() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        getAvailableCountryCodes().setAll(CountryCallingCode.Defaults.values());
        setMaskProvider(code -> {
            if (isForceLocalNumber()) {
                return DEFAULT_MASK;
            }
            return Optional.ofNullable(code).map(CountryCallingCode::localNumberMask).orElse("");
        });
        parser = new PhoneNumberParser();
        formatter = new PhoneNumberFormatter();
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(PhoneNumberField.class.getResource("phone-number-field.css")).toExternalForm();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PhoneNumberFieldSkin(this, formatter.textField);
    }

    // VALUES
    private final StringProperty phoneNumber = new SimpleStringProperty(this, "phoneNumber") {
        private boolean selfUpdate;
        @Override
        public void set(String newPhoneNumber) {
            if (selfUpdate) {
                return;
            }

            try {
                selfUpdate = true;

                // Make sure the phone number is valid
                if (parser.isInvalid(newPhoneNumber)) {
                    newPhoneNumber = null;
                }

                // Set the value first, so that the binding will be triggered
                super.set(newPhoneNumber);

                if (isForceLocalNumber()) {
                    // No need to infer the country code, just use the local phone number
                    setCountryCallingCode(null);
                    setLocalPhoneNumber(newPhoneNumber);
                    formatter.updateFormattedLocalPhoneNumber(newPhoneNumber);
                    return;
                }

                // Set depending fields here
                PhoneNumber parsedNumber = parser.call(newPhoneNumber);

                if (parsedNumber == null) {
                    setCountryCallingCode(null);
                    setLocalPhoneNumber(null);
                    formatter.updateFormattedLocalPhoneNumber(null);
                } else {
                    setCountryCallingCode(parsedNumber.countryCallingCode);
                    setLocalPhoneNumber(parsedNumber.localPhoneNumber);
                    formatter.updateFormattedLocalPhoneNumber(parsedNumber.localPhoneNumber);
                }
            }
            finally {
                selfUpdate = false;
            }
        }
    };

    /**
     * @return The phone number property acting as main value for the control.  This is always represented international format
     * without the (+) plus sign.
     */
    public final StringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public final String getPhoneNumber() {
        return phoneNumberProperty().get();
    }

    public final void setPhoneNumber(String phoneNumber) {
        phoneNumberProperty().set(phoneNumber);
    }

    private final ObjectProperty<CountryCallingCode> countryCallingCode = new SimpleObjectProperty<>(this, "countryCallingCode") {
        private boolean selfUpdate;
        @Override
        public void set(CountryCallingCode newCountryCallingCode) {
            if (selfUpdate) {
                return;
            }

            try {
                selfUpdate = true;

                // Set the value first, so that the binding will be triggered
                super.set(newCountryCallingCode);

                // Set the mask first
                setMask(getMaskProvider().call(newCountryCallingCode));

                // For now replace the entire text, it might be good to preserve the local number and just change the country code
                if (isForceLocalNumber()) {
                    setPhoneNumber(null);
                } else {
                    setPhoneNumber(Optional.ofNullable(newCountryCallingCode)
                        .map(CountryCallingCode::phonePrefix)
                        .map(String::valueOf)
                        .orElse(null));
                }

            } finally {
                selfUpdate = false;
            }
        }
    };

    /**
     * @return The selected country calling code.  Use this property if you want to define a default (pre-selected) country.
     * It can also be used in conjunction with {@link #disableCountryCodeProperty() disableCountryCode} to avoid changing the country part.
     */
    public final ObjectProperty<CountryCallingCode> countryCallingCodeProperty() {
        return countryCallingCode;
    }

    public final CountryCallingCode getCountryCallingCode() {
        return countryCallingCodeProperty().get();
    }

    private void setCountryCallingCode(CountryCallingCode countryCallingCode) {
        countryCallingCodeProperty().set(countryCallingCode);
    }

    private final ReadOnlyStringWrapper localPhoneNumber = new ReadOnlyStringWrapper(this, "localPhoneNumber");

    /**
     * @return Read only property that exposes the local part of the phone number.
     */
    public final ReadOnlyStringProperty localPhoneNumberProperty() {
        return localPhoneNumber.getReadOnlyProperty();
    }

    public final String getLocalPhoneNumber() {
        return localPhoneNumber.get();
    }

    private void setLocalPhoneNumber(String localPhoneNumber) {
        this.localPhoneNumber.set(localPhoneNumber);
    }

    private final ReadOnlyStringWrapper formattedLocalPhoneNumber = new ReadOnlyStringWrapper(this, "formattedLocalPhoneNumber");

    /**
     * @return Read only property that exposes the local phone number expressed as the mask format defines.
     */
    public final ReadOnlyStringProperty formattedLocalPhoneNumberProperty() {
        return formattedLocalPhoneNumber.getReadOnlyProperty();
    }

    public final String getFormattedLocalPhoneNumber() {
        return formattedLocalPhoneNumber.get();
    }

    // SETTINGS

    private final ObservableList<CountryCallingCode> availableCountryCodes = FXCollections.observableArrayList();

    /**
     * @return The list of available countries from which the user can select one and put it into the
     * {@link #countryCallingCodeProperty() countryCallingCode}.
     */
    public final ObservableList<CountryCallingCode> getAvailableCountryCodes() {
        return availableCountryCodes;
    }

    private final ObservableList<CountryCallingCode> preferredCountryCodes = FXCollections.observableArrayList();

    /**
     * @return The list of preferred countries that are shown first in the list of available countries.  If one country calling code
     * is added to this list, but is not present in the {@link #getAvailableCountryCodes()} then it will be ignored and not shown.
     */
    public final ObservableList<CountryCallingCode> getPreferredCountryCodes() {
        return preferredCountryCodes;
    }

    private final BooleanProperty disableCountryCode = new SimpleBooleanProperty(this, "disableCountryCode");

    /**
     * @return Flag to disable the country selector button.  This is useful if you want to force the user to enter a local number but
     * make split reference that the {@link #phoneNumberProperty() phoneNumber} is still international.
     */
    public final BooleanProperty disableCountryCodeProperty() {
        return disableCountryCode;
    }

    public final boolean isDisableCountryCode() {
        return disableCountryCodeProperty().get();
    }

    public final void setDisableCountryCode(boolean disableCountryCode) {
        disableCountryCodeProperty().set(disableCountryCode);
    }

    private final BooleanProperty forceLocalNumber = new SimpleBooleanProperty(this, "forceLocalNumber") {
        private CountryCallingCode lastCountryCallingCode;
        @Override
        public void set(boolean newForceLocal) {
            super.set(newForceLocal);

            final String localPhone = Optional.ofNullable(getLocalPhoneNumber()).orElse("");

            if (newForceLocal) {
                lastCountryCallingCode = getCountryCallingCode();
                setPhoneNumber(localPhone);
            } else if (lastCountryCallingCode != null) {
                String prefix = String.valueOf(lastCountryCallingCode.phonePrefix());
                if ((lastCountryCallingCode.countryCode() + localPhone).equals(prefix)) {
                    setPhoneNumber(prefix);
                } else {
                    setPhoneNumber(lastCountryCallingCode.countryCode() + localPhone);
                }
            } else {
                setPhoneNumber(localPhone);
            }
        }
    };

    /**
     * @return Flag to force the user to enter a local number. This will change the default behaviour of the control and
     * will have a local number on the {@link #phoneNumberProperty() phone number}.
     */
    public final BooleanProperty forceLocalNumberProperty() {
        return forceLocalNumber;
    }

    public final boolean isForceLocalNumber() {
        return forceLocalNumberProperty().get();
    }

    public final void setForceLocalNumber(boolean forceLocalNumber) {
        forceLocalNumberProperty().set(forceLocalNumber);
    }

    private final ObjectProperty<Callback<CountryCallingCode, Node>> countryCodeViewFactory = new SimpleObjectProperty<>(this, "countryCodeViewFactory");

    /**
     * @return Factory that allows to replace the node used to graphically represent each country calling code.
     */
    public final ObjectProperty<Callback<CountryCallingCode, Node>> countryCodeViewFactoryProperty() {
        return countryCodeViewFactory;
    }

    public final Callback<CountryCallingCode, Node> getCountryCodeViewFactory() {
        return countryCodeViewFactoryProperty().get();
    }

    public final void setCountryCodeViewFactory(Callback<CountryCallingCode, Node> countryCodeViewFactory) {
        countryCodeViewFactoryProperty().set(countryCodeViewFactory);
    }

    private final ReadOnlyStringWrapper mask = new ReadOnlyStringWrapper(this, "mask");

    /**
     * @return The mask the control will use to format phone numbers.
     */
    public final ReadOnlyStringProperty maskProperty() {
        return mask.getReadOnlyProperty();
    }

    public final String getMask() {
        return mask.get();
    }

    private void setMask(String mask) {
        this.mask.set(mask);
    }

    private final ReadOnlyStringWrapper maskRemaining = new ReadOnlyStringWrapper(this, "maskRemaining");

    /**
     * @return A property that allows to know whether the mask is completed or not.
     */
    public final ReadOnlyStringProperty maskRemainingProperty() {
        return maskRemaining.getReadOnlyProperty();
    }

    public final String getMaskRemaining() {
        return maskRemaining.get();
    }

    private void setMaskRemaining(String mask) {
        this.maskRemaining.set(mask);
    }

    private final ObjectProperty<Callback<CountryCallingCode, String>> maskProvider = new SimpleObjectProperty<>(this, "maskProvider") {
        @Override
        public void set(Callback<CountryCallingCode, String> maskProvider) {
            super.set(Objects.requireNonNull(maskProvider));
        }
    };

    /**
     * @return The mask provider used to determine the mask for a given country calling code.
     */
    public final ObjectProperty<Callback<CountryCallingCode, String>> maskProviderProperty() {
        return maskProvider;
    }

    public final Callback<CountryCallingCode, String> getMaskProvider() {
        return maskProviderProperty().get();
    }

    public final void setMaskProvider(Callback<CountryCallingCode, String> maskProvider) {
        maskProviderProperty().set(maskProvider);
    }

    /**
     * Represents a country calling code. The country calling code is used to identify the country and the area code.  This ones
     * should go according the ITU-T E.164 recommendation.
     * <a href="https://en.wikipedia.org/wiki/List_of_country_calling_codes">List_of_country_calling_codes</a>.
     */
    public interface CountryCallingCode {

        /**
         * @return The code assigned to the country.
         */
        int countryCode();

        /**
         * @return Designated area codes within the country.
         */
        int[] areaCodes();

        /**
         * @return The Alpha-2 code of the country as described in the ISO-3166 international standard.
         */
        String iso2Code();

        /**
         * @return The default mask defined for the country.  This is arbitrary adopted by the control, but can be replaced by
         * using a custom {@link #maskProviderProperty() maskProvider}.
         */
        String localNumberMask();

        /**
         * @return The first area code if there is any in the country.
         */
        default Integer defaultAreaCode() {
            return areaCodes().length > 0 ? areaCodes()[0] : null;
        }

        /**
         * @return The concatenation of country code and {@link #defaultAreaCode() default area code}.
         */
        default int phonePrefix() {
            StringBuilder value = new StringBuilder();
            value.append(countryCode());
            Integer defaultAreaCode = defaultAreaCode();
            if (defaultAreaCode != null) {
                value.append(defaultAreaCode);
            }
            return Integer.parseInt(value.toString());
        }

        /**
         * Default country calling codes offered by the control.
         */
        enum Defaults implements CountryCallingCode {

            AFGHANISTAN(93, "AF"),
            ALAND_ISLANDS(358, "AX", 18),
            ALBANIA(355, "AL"),
            ALGERIA(213, "DZ"),
            AMERICAN_SAMOA(1, "AS", 684),
            ANDORRA(376, "AD"),
            ANGOLA(244, "AO"),
            ANGUILLA(1, "AI", 264),
            ANTIGUA_AND_BARBUDA(1, "AG", 268),
            ARGENTINA(54, "AR"),
            ARMENIA(374, "AM"),
            ARUBA(297, "AW"),
            AUSTRALIA(61, "AU"),
            AUSTRALIA_ANTARCTIC_TERRITORIES(672, "AQ", 1),
            AUSTRIA(43, "AT"),
            AZERBAIJAN(994, "AZ"),
            BAHAMAS(1, "BS", 242),
            BAHRAIN(973, "BH"),
            BANGLADESH(880, "BD"),
            BARBADOS(1, "BB", 246),
            BELARUS(375, "BY"),
            BELGIUM(32, "BE"),
            BELIZE(501, "BZ"),
            BENIN(229, "BJ"),
            BERMUDA(1, "BM", 441),
            BHUTAN(975, "BT"),
            BOLIVIA(591, "BO"),
            BONAIRE(599, "BQ", 7),
            BOSNIA_AND_HERZEGOVINA(387, "BA"),
            BOTSWANA(267, "BW"),
            BRAZIL(55, "BR"),
            BRITISH_INDIAN_OCEAN_TERRITORY(246, "IO"),
            BRITISH_VIRGIN_ISLANDS(1, "VG", 284),
            BRUNEI(673, "BN"),
            BULGARIA(359, "BG"),
            BURKINA_FASO(226, "BF"),
            BURUNDI(257, "BI"),
            CAMBODIA(855, "KH"),
            CAMEROON(237, "CM"),
            CANADA(1, "CA"),
            CAPE_VERDE(238, "CV"),
            CAYMAN_ISLANDS(1, "KY", 345),
            CENTRAL_AFRICAN_REPUBLIC(236, "CF"),
            CHAD(235, "TD"),
            CHILE(56, "CL"),
            CHINA(86, "CN"),
            CHRISTMAS_ISLAND(61, "CX", "(_____) _____", 89164),
            COCOS_ISLANDS(61, "CC", "(_____) _____", 89162),
            COLOMBIA(57, "CO"),
            COMOROS(269, "KM"),
            CONGO(242, "CG"),
            COOK_ISLANDS(682, "CK"),
            COSTA_RICA(506, "CR"),
            CROATIA(385, "HR"),
            CUBA(53, "CU"),
            CURACAO(599, "CW", 9),
            CYPRUS(357, "CY"),
            CZECH_REPUBLIC(420, "CZ"),
            DEMOCRATIC_REPUBLIC_OF_THE_CONGO(243, "CD"),
            DENMARK(45, "DK"),
            DJIBOUTI(253, "DJ"),
            DOMINICA(1, "DM", 767),
            DOMINICAN_REPUBLIC(1, "DO", 809, 829, 849),
            EAST_TIMOR(670, "TL"),
            ECUADOR(593, "EC"),
            EGYPT(20, "EG"),
            EL_SALVADOR(503, "SV"),
            EQUATORIAL_GUINEA(240, "GQ"),
            ERITREA(291, "ER"),
            ESTONIA(372, "EE"),
            ETHIOPIA(251, "ET"),
            FALKLAND_ISLANDS(500, "FK"),
            FAROE_ISLANDS(298, "FO"),
            FIJI(679, "FJ"),
            FINLAND(358, "FI"),
            FRANCE(33, "FR"),
            FRENCH_GUIANA(594, "GF"),
            FRENCH_POLYNESIA(689, "PF"),
            GABON(241, "GA"),
            GAMBIA(220, "GM"),
            GEORGIA(995, "GE"),
            GERMANY(49, "DE"),
            GHANA(233, "GH"),
            GIBRALTAR(350, "GI"),
            GREECE(30, "GR"),
            GREENLAND(299, "GL"),
            GRENADA(1, "GD", 473),
            GUADELOUPE(590, "GP"),
            GUAM(1, "GU", 671),
            GUATEMALA(502, "GT"),
            GUERNSEY(44, "GG", "(____) ______", 1481, 7781, 7839, 7911),
            GUINEA(224, "GN"),
            GUINEA_BISSAU(245, "GW"),
            GUYANA(592, "GY"),
            HAITI(509, "HT"),
            HONDURAS(504, "HN"),
            HONG_KONG(852, "HK"),
            HUNGARY(36, "HU"),
            ICELAND(354, "IS"),
            INDIA(91, "IN"),
            INDONESIA(62, "ID"),
            IRAN(98, "IR"),
            IRAQ(964, "IQ"),
            IRELAND(353, "IE"),
            ISLE_OF_MAN(44, "IM", "(____) ______", 1624, 7524, 7624, 7924),
            ISRAEL(972, "IL"),
            ITALY(39, "IT"),
            IVORY_COAST(225, "CI"),
            JAMAICA(1, "JM", 658, 876),
            JAN_MAYEN(47, "SJ", 79),
            JAPAN(81, "JP"),
            JERSEY(44, "JE", "(____) ______", 1534),
            JORDAN(962, "JO"),
            KAZAKHSTAN(7, "KZ", 6, 7),
            KENYA(254, "KE"),
            KIRIBATI(686, "KI"),
            KOREA_NORTH(850, "KP"),
            KOREA_SOUTH(82, "KR"),
            KOSOVO(383, "XK"),
            KUWAIT(965, "KW"),
            KYRGYZSTAN(996, "KG"),
            LAOS(856, "LA"),
            LATVIA(371, "LV"),
            LEBANON(961, "LB"),
            LESOTHO(266, "LS"),
            LIBERIA(231, "LR"),
            LIBYA(218, "LY"),
            LIECHTENSTEIN(423, "LI"),
            LITHUANIA(370, "LT"),
            LUXEMBOURG(352, "LU"),
            MACAU(853, "MO"),
            MACEDONIA(389, "MK"),
            MADAGASCAR(261, "MG"),
            MALAWI(265, "MW"),
            MALAYSIA(60, "MY"),
            MALDIVES(960, "MV"),
            MALI(223, "ML"),
            MALTA(356, "MT"),
            MARSHALL_ISLANDS(692, "MH"),
            MARTINIQUE(596, "MQ"),
            MAURITANIA(222, "MR"),
            MAURITIUS(230, "MU"),
            MAYOTTE(262, "YT", 269, 639),
            MEXICO(52, "MX"),
            MICRONESIA(691, "FM"),
            MOLDOVA(373, "MD"),
            MONACO(377, "MC"),
            MONGOLIA(976, "MN"),
            MONTENEGRO(382, "ME"),
            MONTSERRAT(1, "MS", 664),
            MOROCCO(212, "MA"),
            MOZAMBIQUE(258, "MZ"),
            MYANMAR(95, "MM"),
            NAMIBIA(264, "NA"),
            NAURU(674, "NR"),
            NEPAL(977, "NP"),
            NETHERLANDS(31, "NL"),
            NEW_CALEDONIA(687, "NC"),
            NEW_ZEALAND(64, "NZ"),
            NICARAGUA(505, "NI"),
            NIGER(227, "NE"),
            NIGERIA(234, "NG"),
            NIUE(683, "NU"),
            NORFOLK_ISLAND(672, "NF", 3),
            NORTHERN_MARIANA_ISLANDS(1, "MP", 670),
            NORWAY(47, "NO"),
            OMAN(968, "OM"),
            PAKISTAN(92, "PK"),
            PALAU(680, "PW"),
            PALESTINE(970, "PS"),
            PANAMA(507, "PA"),
            PAPUA_NEW_GUINEA(675, "PG"),
            PARAGUAY(595, "PY"),
            PERU(51, "PE"),
            PHILIPPINES(63, "PH"),
            POLAND(48, "PL"),
            PORTUGAL(351, "PT"),
            PUERTO_RICO(1, "PR", 787, 930),
            QATAR(974, "QA"),
            REUNION(262, "RE"),
            ROMANIA(40, "RO"),
            RUSSIA(7, "RU"),
            RWANDA(250, "RW"),
            SAINT_HELENA(290, "SH"),
            SAINT_KITTS_AND_NEVIS(1, "KN", 869),
            SAINT_LUCIA(1, "LC", 758),
            SAINT_PIERRE_AND_MIQUELON(508, "PM"),
            SAINT_VINCENT_AND_THE_GRENADINES(1, "VC", 784),
            SAMOA(685, "WS"),
            SAN_MARINO(378, "SM"),
            SAO_TOME_AND_PRINCIPE(239, "ST"),
            SAUDI_ARABIA(966, "SA"),
            SENEGAL(221, "SN"),
            SERBIA(381, "RS"),
            SEYCHELLES(248, "SC"),
            SIERRA_LEONE(232, "SL"),
            SINGAPORE(65, "SG"),
            SLOVAKIA(421, "SK"),
            SLOVENIA(386, "SI"),
            SOLOMON_ISLANDS(677, "SB"),
            SOMALIA(252, "SO"),
            SOUTH_AFRICA(27, "ZA"),
            SOUTH_SUDAN(211, "SS"),
            SPAIN(34, "ES"),
            SRI_LANKA(94, "LK"),
            SUDAN(249, "SD"),
            SURINAME(597, "SR"),
            SVALBARD_AND_JAN_MAYEN(47, "SJ"),
            SWAZILAND(268, "SZ"),
            SWEDEN(46, "SE"),
            SWITZERLAND(41, "CH"),
            SYRIA(963, "SY"),
            TAIWAN(886, "TW"),
            TAJIKISTAN(992, "TJ"),
            TANZANIA(255, "TZ"),
            THAILAND(66, "TH"),
            TOGO(228, "TG"),
            TOKELAU(690, "TK"),
            TONGA(676, "TO"),
            TRINIDAD_AND_TOBAGO(1, "TT", 868),
            TUNISIA(216, "TN"),
            TURKEY(90, "TR"),
            TURKMENISTAN(993, "TM"),
            TURKS_AND_CAICOS_ISLANDS(1, "TC", 649),
            TUVALU(688, "TV"),
            UGANDA(256, "UG"),
            UKRAINE(380, "UA"),
            UNITED_ARAB_EMIRATES(971, "AE"),
            UNITED_KINGDOM(44, "GB"),
            UNITED_STATES(1, "US"),
            URUGUAY(598, "UY"),
            UZBEKISTAN(998, "UZ"),
            VANUATU(678, "VU"),
            VATICAN_CITY(379, "VA"),
            VENEZUELA(58, "VE"),
            VIETNAM(84, "VN"),
            VIRGIN_ISLANDS(1, "VI", 340),
            WALLIS_AND_FUTUNA(681, "WF"),
            WESTERN_SAHARA(212, "EH"),
            YEMEN(967, "YE"),
            ZAMBIA(260, "ZM"),
            ZANZIBAR(255, "TZ"),
            ZIMBABWE(263, "ZW")
            ;

            private final int countryCode;
            private final String iso2Code;
            private final int[] areaCodes;
            private final String localNumberMask;

            Defaults(int countryCode, String iso2Code, int... areaCodes) {
                this(countryCode, iso2Code, DEFAULT_MASK, areaCodes);
            }

            Defaults(int countryCode, String iso2Code, String localNumberMask, int... areaCodes) {
                this.countryCode = countryCode;
                this.iso2Code = iso2Code;
                this.areaCodes = Optional.ofNullable(areaCodes).orElse(new int[0]);
                this.localNumberMask = localNumberMask;
            }

            @Override
            public int countryCode() {
                return countryCode;
            }

            @Override
            public int[] areaCodes() {
                return areaCodes;
            }

            @Override
            public String iso2Code() {
                return iso2Code;
            }

            @Override
            public String localNumberMask() {
                return localNumberMask;
            }
        }

    }

    /**
     * For internal use only.
     */
    private final class PhoneNumberParser implements Callback<String, PhoneNumber> {

        @Override
        public PhoneNumber call(String phoneNumber) {
            Map<CountryCallingCode, String> localPhoneNumbers = new HashMap<>();
            TreeMap<Integer, List<CountryCallingCode>> scores = new TreeMap<>();

            for (CountryCallingCode code : getAvailableCountryCodes()) {
                CountryCallingCodeScore score = rankAndSplit(code, phoneNumber);
                if (score.rank > 0) {
                    scores.computeIfAbsent(score.rank, s -> new ArrayList<>()).add(code);
                    localPhoneNumbers.put(code, score.localPhoneNumber);
                }
            }

            Map.Entry<Integer, List<CountryCallingCode>> highestScore = scores.lastEntry();
            if (highestScore == null) {
                return null;
            }

            // Need to pick from the list the one that best matches
            CountryCallingCode code = inferBestMatch(highestScore.getValue());
            String localPhoneNumber = localPhoneNumbers.get(code);

            return new PhoneNumber(code, localPhoneNumber);
        }

        private CountryCallingCode inferBestMatch(List<CountryCallingCode> matchingCodes) {
            CountryCallingCode code;
            if (matchingCodes.size() > 1) {
                // TODO Here there will be some ambiguity since two countries have same score
                // we need some sort of logic here, for now using the last one.
                code = matchingCodes.get(matchingCodes.size() - 1);
            } else {
                code = matchingCodes.get(0);
            }

            return code;
        }

        private CountryCallingCodeScore rankAndSplit(CountryCallingCode code, String phoneNumber) {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                String countryPrefix = String.valueOf(code.countryCode());

                if (code.areaCodes().length == 0) {
                    if (phoneNumber.startsWith(countryPrefix)) {
                        String localNumber = phoneNumber.length() > countryPrefix.length() ?
                            phoneNumber.substring(countryPrefix.length()) :
                            null;
                        return new CountryCallingCodeScore(1, localNumber);
                    }
                } else {
                    for (int areaCode : code.areaCodes()) {
                        String areaCodePrefix = countryPrefix + areaCode;
                        if (phoneNumber.startsWith(areaCodePrefix)) {
                            String localNumber = phoneNumber.length() > areaCodePrefix.length() ?
                                phoneNumber.substring(areaCodePrefix.length()) :
                                phoneNumber.substring(countryPrefix.length());
                            return new CountryCallingCodeScore(2, localNumber);
                        }
                    }
                }
            }

            return new CountryCallingCodeScore(0, null);
        }

        private boolean isInvalid(String newPhoneNumber) {
            if (newPhoneNumber != null && !newPhoneNumber.isEmpty()) {
                for (char c : newPhoneNumber.toCharArray()) {
                    if (!Character.isDigit(c)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * For internal use only.
     */
    private final class PhoneNumberFormatter implements UnaryOperator<TextFormatter.Change> {

        private boolean selfUpdate;
        private final TextField textField;

        private PhoneNumberFormatter() {
            textField = new TextField();
            textField.setTextFormatter(new TextFormatter<>(this));
            formattedLocalPhoneNumber.bind(textField.textProperty());
            maskProperty().addListener(obs -> compileMask(getMask()));
            compileMask(getMask());
        }

        @Override
        public TextFormatter.Change apply(TextFormatter.Change change) {
            if (selfUpdate) {
                return change;
            }

            try {
                selfUpdate = true;

                if (getCountryCallingCode() == null && !isForceLocalNumber()) {
                    return null;
                }

                if (change.isAdded() || change.isDeleted() || change.isReplaced()) {
                    if (change.isAdded() || change.isReplaced()) {
                        String text = change.getText();
                        if (!text.matches("[0-9]+")) {
                            return null;
                        }
                    }

                }

                if (change.isAdded()) {
                    change = numbersAdded(change);
                } else if (change.isReplaced()) {
                    return null;
                } else if (change.isDeleted()) {
                    change = numbersRemoved(change);
                }

            } finally {
                selfUpdate = false;
            }


            return change;
        }

        private void updateFormattedLocalPhoneNumber(String newPhoneNumber) {
            if (selfUpdate) {
                // Ignore when I'm the one who initiated the update
                return;
            }

            try {
                selfUpdate = true;
                String formattedLocalPhoneNumber = doFormat(newPhoneNumber);
                textField.setText(formattedLocalPhoneNumber);
                updateMaskRemaining(formattedLocalPhoneNumber);
            } finally {
                selfUpdate = false;
            }
        }

        private void compileMask(String mask) {
            if (mask != null && !mask.isEmpty()) {
                for (char c : mask.toCharArray()) {
                    if (!DEFAULT_MASK_SUPPORTED_CHARS.contains(c)) {
                        throw new IllegalArgumentException("Mask contains unsupported character: " + c);
                    }
                }
                maskRemaining.set(mask);
            } else {
                maskRemaining.set("");
            }
        }

        private boolean isUnMasked() {
            return getMask() == null || getMask().isEmpty();
        }

        private TextFormatter.Change numbersAdded(TextFormatter.Change change) {
            if (isUnMasked()) {
                return change;
            }

            String remainingMask = getMaskRemaining();
            String originalText = change.getText();
            StringBuilder newText = new StringBuilder();

            for (char number : originalText.toCharArray()) {
                if (remainingMask.isEmpty()) {
                    newText.append(number);
                    continue;
                }

                char maskC;

                do {
                    maskC = remainingMask.charAt(0);
                    remainingMask = remainingMask.substring(1);
                    if (maskC == DEFAULT_MASK_DIGIT_CHAR) {
                        newText.append(number);
                    } else {
                        newText.append(maskC);
                    }
                } while (maskC != DEFAULT_MASK_DIGIT_CHAR && !remainingMask.isEmpty());
            }

            change.setText(newText.toString());
            change.setCaretPosition(change.getCaretPosition() + newText.length() - originalText.length());
            change.setAnchor(change.getCaretPosition());

            setMaskRemaining(remainingMask);
            setPhoneNumber(undoFormat(change.getControlNewText()));

            return change;
        }

        private TextFormatter.Change numbersRemoved(TextFormatter.Change change) {
            if (isUnMasked()) {
                return change;
            }

            String originalText = change.getControlNewText();
            int start = change.getRangeStart();

            for (int i = originalText.length() - 1; i > 0; i--) {
                char c = originalText.charAt(i);
                if (!Character.isDigit(c)) {
                    start--;
                } else {
                    break;
                }
            }

            change.setRange(start, change.getRangeEnd());

            String formattedPhone = change.getControlNewText();
            setPhoneNumber(undoFormat(formattedPhone));
            updateMaskRemaining(formattedPhone);

            return change;
        }

        private String undoFormat(String formattedPhoneNumber) {
            StringBuilder phoneNumber = new StringBuilder();

            if (getCountryCallingCode() != null) {
                phoneNumber.append(getCountryCallingCode().countryCode());
            }

            if (formattedPhoneNumber != null && !formattedPhoneNumber.isEmpty()) {
                for (char  c: formattedPhoneNumber.toCharArray()) {
                    if (Character.isDigit(c)) {
                        phoneNumber.append(c);
                    }
                }
            }

            return phoneNumber.toString();
        }

        private String doFormat(String phoneNumber) {
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                return phoneNumber;
            }

            if (isUnMasked()) {
                return phoneNumber;
            }

            StringBuilder formattedPhoneNumber = new StringBuilder();

            for (char maskChar : getMask().toCharArray()) {
                if (phoneNumber.isEmpty()) {
                    if (maskChar == DEFAULT_MASK_DIGIT_CHAR) {
                        break;
                    }
                    formattedPhoneNumber.append(maskChar);

                } else {
                    char numberChar = phoneNumber.charAt(0);

                    if (maskChar == DEFAULT_MASK_DIGIT_CHAR) {
                        formattedPhoneNumber.append(numberChar);
                        phoneNumber = phoneNumber.substring(1);
                    } else {
                        formattedPhoneNumber.append(maskChar);
                        if (numberChar == maskChar) {
                            phoneNumber = phoneNumber.substring(1);
                        }
                    }
                }
            }

            formattedPhoneNumber.append(phoneNumber);

            return formattedPhoneNumber.toString();
        }

        private void updateMaskRemaining(String formattedPhoneNumber) {
            if (isUnMasked()) {
                setMaskRemaining("");
            } else {
                if (formattedPhoneNumber == null || formattedPhoneNumber.isEmpty()) {
                    setMaskRemaining(getMask());
                } else if (formattedPhoneNumber.length() > getMask().length()) {
                    setMaskRemaining("");
                } else {
                    setMaskRemaining(getMask().substring(formattedPhoneNumber.length()));
                }
            }
        }
    }

    /**
     * For internal use only.
     */
    private static class PhoneNumber {
        CountryCallingCode countryCallingCode;
        String localPhoneNumber;
        PhoneNumber(CountryCallingCode countryCallingCode, String localPhoneNumber) {
            this.countryCallingCode = countryCallingCode;
            this.localPhoneNumber = localPhoneNumber;
        }
    }

    /**
     * For internal use only.
     */
    private static class CountryCallingCodeScore {
        int rank;
        String localPhoneNumber;
        CountryCallingCodeScore(int rank, String localPhoneNumber) {
            this.rank = rank;
            this.localPhoneNumber = localPhoneNumber;
        }
    }

}


