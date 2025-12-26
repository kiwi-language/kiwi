package org.metavm.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SingleLineFormatter extends Formatter {
    
    // Equivalent to %d{HH:mm:ss.SSS}
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        // 1. Time
        sb.append(dtf.format(record.getInstant()));
        sb.append(" ");

        // 2. Thread [%thread]
        // Note: Thread name might be truncated in very constrained native environments, 
        // but usually works fine.
        sb.append("[").append(Thread.currentThread().getName()).append("] ");

        // 3. Level %-5level
        String level = record.getLevel().getLocalizedName();
        sb.append(String.format("%-5s", level));
        sb.append(" ");

        // 4. Logger %logger{36}
        // Simple logic to shorten package names if desired, or just use raw name
        sb.append(record.getLoggerName());
        sb.append(" - ");

        // 5. Message %msg
        sb.append(formatMessage(record));
        sb.append(System.lineSeparator());

        // 6. Exception Stack Trace (if any)
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            record.getThrown().printStackTrace(pw);
            sb.append(sw);
        }

        return sb.toString();
    }
}