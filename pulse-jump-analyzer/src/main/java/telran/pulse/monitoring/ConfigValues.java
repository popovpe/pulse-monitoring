package telran.pulse.monitoring;
import static telran.pulse.monitoring.common.AppLogging.logger;
import static telran.pulse.monitoring.Constants.*;


public class ConfigValues {

    static float factor;
    static {
        factor = getFactor(DEFAULT_FACTOR);
        logger.config(String.format("%s=%f", FACTOR_ENV_VAR, factor));
    }

    private static float getFactor(float defaultValue) {
        float result = defaultValue;
        try {
            result= Math.abs(Float.parseFloat(System.getenv(FACTOR_ENV_VAR)));
        } catch (RuntimeException e) {
            logger.severe(String.format("Error parsing %s enviroment variable as float", FACTOR_ENV_VAR));
        }
        return result;
    }

}
