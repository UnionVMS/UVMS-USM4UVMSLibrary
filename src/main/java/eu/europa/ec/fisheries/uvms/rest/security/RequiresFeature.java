package eu.europa.ec.fisheries.uvms.rest.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresFeature {
    UnionVMSFeature value();
}
