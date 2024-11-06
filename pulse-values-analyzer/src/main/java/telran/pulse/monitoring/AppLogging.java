package telran.pulse.monitoring;

import java.util.logging.*;

public class AppLogging {
    
    public static Logger logger = configureLoggingFramework();

    private static Logger configureLoggingFramework() {
		logger = Logger.getLogger("logger");
		logger.setUseParentHandlers(false);
		logger.setLevel(parseLogLevelOrDefault(System.getenv("LOGGING_LEVEL"), Level.INFO));
		Handler handler = new ConsoleHandler();
		handler.setFormatter(new Formatter() {

			@Override
			public String format(LogRecord record) {
				StringBuilder builder = new StringBuilder();
				builder.append("[").append(record.getLevel()).append("] - ");
				builder.append(record.getSourceMethodName()).append(" - ");
				builder.append(formatMessage(record)).append("\n");
				return builder.toString();
			}
		});
		handler.setLevel(Level.FINEST);
		logger.addHandler(handler);
		return logger;
	}

	private static Level parseLogLevelOrDefault(String varName, Level defaultLevel) {
		Level logLevel = null;
		try {
			logLevel = Level.parse(varName);
		} catch (RuntimeException e) {
			logLevel = defaultLevel;
		}
		return logLevel;
	}
}
