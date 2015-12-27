package org.theronin.expensetracker.data;

import org.theronin.expensetracker.model.Currency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class SupportedCurrencies {

    private final List<Currency> list;

    public SupportedCurrencies() {
        List<Currency> topTwenty = new ArrayList<>();
        List<Currency> rest = new ArrayList<>();
        createList(topTwenty, rest);
        this.list = createFullList(topTwenty, rest);
    }

    private List<Currency> createFullList(List<Currency> topTwenty, List<Currency> rest) {
        List<Currency> complete = new ArrayList<>();
        Collections.sort(rest, new Comparator<Currency>() {
            @Override
            public int compare(Currency lhs, Currency rhs) {
                return lhs.name.compareTo(rhs.name);
            }
        });
        complete.addAll(topTwenty);
        complete.addAll(rest);
        return complete;
    }

    public List<Currency> getList() {
        return list;
    }

    public CharSequence[] getCodes() {
        CharSequence[] codes = new CharSequence[list.size()];
        int i = 0;
        for (Currency currency : list) {
            codes[i] = currency.code;
            i ++;
        }
        return codes;
    }

    public CharSequence[] getNames() {
        CharSequence[] names = new CharSequence[list.size()];
        int i = 0;
        for (Currency currency : list) {
            names[i] = currency.name;
            i ++;
        }
        return names;
    }

    public Currency findCurrency(String code) {
        for (Currency currency : list) {
            if (currency.code.equals(code)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Currency not supported");
    }

    private void createList(List<Currency> topTwenty, List<Currency> rest) {
        //Top 20 (According to wikipedia)
        topTwenty.add(new Currency("USD", "$", "United States Dollar"));
        topTwenty.add(new Currency("EUR", "€", "Euro"));
        topTwenty.add(new Currency("JPY", "¥", "Japanese Yen"));
        topTwenty.add(new Currency("GBP", "£", "British Pound"));
        topTwenty.add(new Currency("AUD", "$", "Australian Dollar"));

        topTwenty.add(new Currency("CHF", "CHF", "Swiss Franc"));
        topTwenty.add(new Currency("CAD", "$", "Canadian Dollar"));
        topTwenty.add(new Currency("MXN", "$", "Mexico Peso"));
        topTwenty.add(new Currency("CNY", "¥", "Chinese Yuan"));
        topTwenty.add(new Currency("NZD", "$", "New Zealand Dollar"));

        topTwenty.add(new Currency("SEK", "kr", "Swedish Krona"));
        topTwenty.add(new Currency("AWG", "ƒ", "Arubin Florin"));
        topTwenty.add(new Currency("HKD", "$", "Hong Kong dollar"));
        topTwenty.add(new Currency("NOK", "kr", "Norwegian Krone"));
        topTwenty.add(new Currency("SGD", "$", "Singapore Dollar"));

        topTwenty.add(new Currency("TRY", "\\u20BA", "Turkish Lira"));
        topTwenty.add(new Currency("KRW", "₩", "South Korean Won"));
        topTwenty.add(new Currency("ZAR", "R", "South African Rand"));
        topTwenty.add(new Currency("BRL", "$", "Brazilian Real"));
        topTwenty.add(new Currency("INR", "₹", "Indian Rupee"));

        //The rest
        rest.add(new Currency("DZD", "جد", "Algerian Dinar"));
        rest.add(new Currency("NAD", "$", "Namibian Dollar"));
        rest.add(new Currency("GHS", "GH¢", "Ghanaian Cedi"));
        rest.add(new Currency("EGP", "£ ", "Egyptian Pound"));
        rest.add(new Currency("BGN", "лв", "Bulgarian Lev"));
        rest.add(new Currency("PAB", "B/", "Balboa Panamérn"));
        rest.add(new Currency("BOB", "$b", "Bolivian Boliviano"));
        rest.add(new Currency("DKK", "kr", "Danish Krone"));
        rest.add(new Currency("BWP", "P", "Botswana Pula"));
        rest.add(new Currency("LBP", "ل.ل", "Lebanese Pound"));
        rest.add(new Currency("TZS", "Sh", "Tanzanian Shilling"));
        rest.add(new Currency("VND", "₫", "Vietnamese Dong"));
        rest.add(new Currency("AOA", "Kz", "Angolan Kwanza"));
        rest.add(new Currency("KHR", "៛", "Cambodian Riel"));
        rest.add(new Currency("MYR", "RM", "Malaysian Ringgit"));
        rest.add(new Currency("KYD", "$", "Caymanian Dollar"));
        rest.add(new Currency("LYD", " د.ل", "Libyan Dinar"));
        rest.add(new Currency("UAH", "₴", "Ukrainian Hryvnia"));
        rest.add(new Currency("JOD", "JD", "Jordanian Dinar"));
        rest.add(new Currency("SAR", "ر.س", "Saudi Arabian Riyal"));
        rest.add(new Currency("GIP", "£", "Gibraltar Pound"));
        rest.add(new Currency("BYR", "p", "Belarusian Ruble"));
        rest.add(new Currency("XPF", "F", "Change Franc Pacifique (CFP Franc)"));
        rest.add(new Currency("MRO", "UM", "Mauritanian Ouguiya"));
        rest.add(new Currency("HRK", "kn", "Croatian Kuna"));
        rest.add(new Currency("DJF", "fdj", "Djiboutian Franc"));
        rest.add(new Currency("SZL", "L", "Swazi Lilangeni"));
        rest.add(new Currency("THB", "฿", "Thai Baht"));
        rest.add(new Currency("BND", "$", "Bruneian Dollar"));
        rest.add(new Currency("ISK", "kr", "Icelandic Krona"));
        rest.add(new Currency("UYU", "$U", "Uruguayan Peso"));
        rest.add(new Currency("NIO", "C$", "Nicaraguan Córdoba"));
        rest.add(new Currency("LAK", "₭", "Laotian Kip"));
        rest.add(new Currency("SYP", "£", "Syrian Pound"));
        rest.add(new Currency("MAD", "م.د.", "Moroccan Dirham"));
        rest.add(new Currency("MZN", "MT", "Mozambican Metical"));
        rest.add(new Currency("PHP", "₱", "Philippine Peso"));
        rest.add(new Currency("NPR", "Rs", "Nepalese Rupee"));
        rest.add(new Currency("NGN", "₦", "Nigerian Naira"));
        rest.add(new Currency("ZWD", "Z$", "Zimbabwean Dollar"));
        rest.add(new Currency("CRC", "₡", "Costa Rican Colón"));
        rest.add(new Currency("AED", ".د.ب", "Emirati Dirham"));
        rest.add(new Currency("MWK", "MK", "Malawian Kwacha"));
        rest.add(new Currency("LKR", "Rs", "Sri Lankan Rupee"));
        rest.add(new Currency("PKR", "Rs", "Pakistani Rupee"));
        rest.add(new Currency("HUF", "Ft", "Hungarian Forint"));
        rest.add(new Currency("BMD", "$", "Bermudian Dollar"));
        rest.add(new Currency("LSL", "L or M", "Lesotho Loti"));
        rest.add(new Currency("MNT", "₮", "Mongolian Tughrik"));
        rest.add(new Currency("AMD", "\\u058F", "Armenian Dram"));
        rest.add(new Currency("UGX", "USh", "Ugandan Shilling"));
        rest.add(new Currency("QAR", "ق.ر ", "Qatari Riyal"));
        rest.add(new Currency("JMD", "J$", "Jamaican Dollar"));
        rest.add(new Currency("GEL", "ლ", "Georgian Lari"));
        rest.add(new Currency("AFN", "؋", "Afghan Afghani"));
        rest.add(new Currency("SBD", "SI$", "Solomon Islander Dollar"));
        rest.add(new Currency("KPW", "₩", "North Korean Won"));
        rest.add(new Currency("BDT", "Tk", "Bangladeshi Taka"));
        rest.add(new Currency("YER", "﷼", "Yemeni Rial"));
        rest.add(new Currency("HTG", "G", "Haitian Gourde"));
        rest.add(new Currency("XOF", "FCFA", "CFA Franc"));
        rest.add(new Currency("MGA", "Ar", "Malagasy Ariary"));
        rest.add(new Currency("ANG", "ƒ", "Dutch Guilder"));
        rest.add(new Currency("LRD", "$", "Liberian Dollar"));
        rest.add(new Currency("RWF", "FRw, RF, R₣", "Rwandan franc"));
        rest.add(new Currency("MOP", "MOP$", "Macau Pataca"));
        rest.add(new Currency("SSP", "£", "South Sudanese pound"));
        rest.add(new Currency("CZK", "Kč", "Czech Koruna"));
        rest.add(new Currency("TJS", "TJS", "Tajikistani Somoni"));
        rest.add(new Currency("BTN", "Nu.", "Bhutanese Ngultrum"));
        rest.add(new Currency("COP", "$", "Colombian Peso"));
        rest.add(new Currency("TMT", "T", "Turkmenistan <anat"));
        rest.add(new Currency("MUR", "Rs", "Mauritian Rupee"));
        rest.add(new Currency("IDR", "Rp", "Indonesian Rupiah"));
        rest.add(new Currency("HNL", "L", "Honduran Lempira"));
        rest.add(new Currency("FJD", "$", "Fijian Dollar"));
        rest.add(new Currency("ETB", "Br", "Ethiopian Birr"));
        rest.add(new Currency("PEN", "S/", "Peruvian Nuevo Sol"));
        rest.add(new Currency("BZD", "BZ$", "Belize Dollar"));
        rest.add(new Currency("ILS", "₪", "Israeli Shekel"));
        rest.add(new Currency("DOP", "$", "Dominican Peso"));
        rest.add(new Currency("TWD", "NT$", "Taiwan New Dollar"));
        rest.add(new Currency("MDL", "L", "Moldovan Leu"));
        rest.add(new Currency("BSD", "B$", "Bahamian Dollar"));
        rest.add(new Currency("MVR", "rf", "Maldivian Rufiyaa"));
        rest.add(new Currency("SRD", "$", "Surinamese Dollar"));
        rest.add(new Currency("CUP", "₱", "Cuban Peso"));
        rest.add(new Currency("BBD", "$", "Barbadian Dollar"));
        rest.add(new Currency("KMF", "KMF", "Comoran Franc"));
        rest.add(new Currency("GMD", "GMD", "Gambian Dalasi"));
        rest.add(new Currency("VEF", "Bs", "Venezuelan Bolivar"));
        rest.add(new Currency("GTQ", "Q", "Guatemalan Quetzal"));
        rest.add(new Currency("CUC", "$", "Cuban Convertible Peso"));
        rest.add(new Currency("CLP", "$", "Chilean Peso"));
        rest.add(new Currency("ZMW", "ZMK", "Zambian Kwacha"));
        rest.add(new Currency("LTL", "Lt", "Lithuanian Litas"));
        rest.add(new Currency("ALL", "lek", "Albanian Lek"));
        rest.add(new Currency("XCD", "EC$", "East Caribbean dollar"));
        rest.add(new Currency("KZT", "₸", "Kazakhstani Tenge"));
        rest.add(new Currency("RUB", "RUB", "Rouble Russe"));
        rest.add(new Currency("TTD", "TT$", "Trinidadian Dollar"));
        rest.add(new Currency("OMR", "ع.ر.", "Omani Rial"));
        rest.add(new Currency("MMK", "K", "Burmese Kyat"));
        rest.add(new Currency("PLN", "zł", "Polish złoty"));
        rest.add(new Currency("PYG", "₲", "Paraguayan Guarani"));
        rest.add(new Currency("KES", "KSh", "Kenyan Shilling"));
        rest.add(new Currency("MKD", "ден", "Macedonian Denar"));
        rest.add(new Currency("AZN", "ман", "Azerbaijani Manat"));
        rest.add(new Currency("TOP", "T$", "Tongan Pa'anga"));
        rest.add(new Currency("VUV", "VT", "Ni-Vanuatu Vatu"));
        rest.add(new Currency("GNF", "GNF", "Guinean Franc"));
        rest.add(new Currency("WST", "$", "Samoan Tālā"));
        rest.add(new Currency("IQD", "ع.د", "Iraqi Dinar"));
        rest.add(new Currency("ERN", "ናቕፋ", "Eritrean Nakfa"));
        rest.add(new Currency("BAM", "KM", "Bosnian Convertible Marka"));
        rest.add(new Currency("SCR", "Rs", "Seychellois Rupee"));
        rest.add(new Currency("CVE", "$", "Cape Verdean Escudo"));
        rest.add(new Currency("KWD", "ك", "Kuwaiti Dinar"));
        rest.add(new Currency("BIF", "BIF", "Burundian Franc"));
        rest.add(new Currency("PGK", "K", "Papua New Guinean Kina"));
        rest.add(new Currency("SOS", "S", "Somali Shilling"));
        rest.add(new Currency("UZS", "лв", "Uzbekistani Som"));
        rest.add(new Currency("IRR", "IRR", "Iranian Rial"));
        rest.add(new Currency("SLL", "Le", "Sierra Leonean Leone"));
        rest.add(new Currency("TND", "TND", "Tunisian Dinar"));
        rest.add(new Currency("GYD", "$", "Guyanese Dollar"));
        rest.add(new Currency("FKP", "£", "Falkland Island Pound"));
        rest.add(new Currency("KGS", "лв", "Kyrgyzstani Som"));
        rest.add(new Currency("ARS", "$", "Argentine Peso"));
        rest.add(new Currency("RON", "lei", "Romanian Leu"));
        rest.add(new Currency("RSD", "РСД", "Serbian Dinar"));
        rest.add(new Currency("BHD", ".د.ب or BD", "Bahraini Dinar"));
        rest.add(new Currency("SDG", "SDG", "Sudanese Pound"));
    }
}
