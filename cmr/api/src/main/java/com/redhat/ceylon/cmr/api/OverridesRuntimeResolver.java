package com.redhat.ceylon.cmr.api;

import com.redhat.ceylon.model.cmr.RuntimeResolver;
import com.redhat.ceylon.model.typechecker.model.Module;

/**
 * Implements {@link RuntimeResolver} using {@link Overrides}.
 */
public class OverridesRuntimeResolver implements com.redhat.ceylon.model.cmr.RuntimeResolver {

    private Overrides overrides;

    public OverridesRuntimeResolver(Overrides overrides) {
        this.overrides = overrides;
    }
    
    private String findOverride(String name, String version) {
        final ArtifactContext context = new ArtifactContext(name, version, ArtifactContext.CAR, ArtifactContext.JAR);
        ArtifactContext override = overrides.applyOverrides(context);
        return override.getVersion();
    }
    
    @Override
    public String resolveVersion(String moduleName, String moduleVersion) {
        if (Module.DEFAULT_MODULE_NAME.equals(moduleName)) {
            // JBoss Modules turns default/null into default:main
            return null;
        }
        return findOverride(moduleName, moduleVersion);
    }
}
