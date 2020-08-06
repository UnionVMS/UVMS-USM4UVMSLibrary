package eu.europa.ec.fisheries.uvms.init;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReadinessServiceImpl implements ReadinessService {

    private boolean usmReady;
    private boolean configReady = true; // until config readiness is implemented

    @Override
    public boolean isUsmReady() {
        return usmReady;
    }

    @Override
    public boolean isConfigReady() {
        return configReady;
    }

    @Override
    public boolean isReady() {
        return usmReady && configReady;
    }

    @Override
    public void setUsmReady(boolean usmReady) {
        this.usmReady = usmReady;
    }
}
