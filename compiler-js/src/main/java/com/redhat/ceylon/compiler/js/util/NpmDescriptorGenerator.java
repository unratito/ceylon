package com.redhat.ceylon.compiler.js.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.ceylon.cmr.api.ArtifactContext;
import com.redhat.ceylon.model.typechecker.model.Annotation;
import com.redhat.ceylon.model.typechecker.model.Module;
import com.redhat.ceylon.model.typechecker.model.ModuleImport;

import net.minidev.json.JSONObject;

/** Creates the package.json file for the specified module. This is for
 * integration with NPM (Node's Package Manager).
 * 
 * @author Enrique Zamudio
 */
public class NpmDescriptorGenerator {

    private final Module mod;
    private final boolean src;
    private final boolean resources;

    public NpmDescriptorGenerator(Module module, boolean withSources, boolean withResources) {
        mod = module;
        src = withSources;
        resources = withResources;
    }

    public String generateDescriptor() throws IOException {
        Map<String,Object> desc = new HashMap<>();
        desc.put("name", mod.getNameAsString());
        desc.put("version", mod.getVersion());
        for (Annotation ann : mod.getAnnotations()) {
            List<String> args = ann.getPositionalArguments();
            if ("doc".equals(ann.getName())) {
                desc.put("description", args.get(0));
            } else if ("license".equals(ann.getName())) {
                desc.put("license", args.get(0));
            } else if ("by".equals(ann.getName()) && !args.isEmpty()) {
                desc.put("author", args.get(0));
                desc.put("contributors", args);
            }
        }
        if (!mod.getImports().isEmpty()) {
            Map<String,String> deps = new HashMap<>(mod.getImports().size());
            Map<String,String> opts = new HashMap<>(mod.getImports().size());
            for (ModuleImport imp : mod.getImports()) {
                if (imp.isOptional()) {
                    opts.put(imp.getModule().getNameAsString(), imp.getModule().getVersion());
                } else {
                    deps.put(imp.getModule().getNameAsString(), imp.getModule().getVersion());
                }
            }
            if (!deps.isEmpty()) {
                desc.put("dependencies", deps);
            }
            if (!opts.isEmpty()) {
                desc.put("optionalDependencies", opts);
            }
        }
        //TODO list resource files, src archive, etc
        List<String> files = new ArrayList<>((src?6:4)+(resources ? 1 : 0));
        files.add(mod.getNameAsString()+"-"+mod.getVersion()+ArtifactContext.JS);
        files.add(mod.getNameAsString()+"-"+mod.getVersion()+ArtifactContext.JS+ArtifactContext.SHA1);
        files.add(mod.getNameAsString()+"-"+mod.getVersion()+ArtifactContext.JS_MODEL);
        files.add(mod.getNameAsString()+"-"+mod.getVersion()+ArtifactContext.JS_MODEL+ArtifactContext.SHA1);
        if (src) {
            files.add(mod.getNameAsString()+ArtifactContext.SRC);
            files.add(mod.getNameAsString()+ArtifactContext.SRC+ArtifactContext.SHA1);
        }
        if (resources) {
            files.add(ArtifactContext.RESOURCES);
        }
        desc.put("files", files);
        StringWriter sw = new StringWriter();
        JSONObject.writeJSON(desc, sw);
        return sw.toString();
    }

}
