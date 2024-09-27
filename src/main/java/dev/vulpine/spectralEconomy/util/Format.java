package dev.vulpine.spectralEconomy.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class Format {

    public static String format(BigDecimal number) {

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

        return decimalFormat.format(number);

    }

}
