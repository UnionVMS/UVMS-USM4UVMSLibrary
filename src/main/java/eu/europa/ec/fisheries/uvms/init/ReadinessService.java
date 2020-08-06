package eu.europa.ec.fisheries.uvms.init;

public interface ReadinessService {

    boolean isUsmReady();

    boolean isConfigReady();

    boolean isReady();

    void setUsmReady(boolean usmReady);
}
