/*
 * Copyright 2011 Red Hat inc. and third party contributors as noted 
 * by the author tags.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ceylon.modules.api.runtime;

import java.lang.reflect.Modifier;

import ceylon.modules.CeylonRuntimeException;
import ceylon.modules.Configuration;
import ceylon.modules.spi.Constants;
import ceylon.modules.spi.runtime.ClassLoaderHolder;

import com.redhat.ceylon.common.JVMModuleUtil;

/**
 * Abstract Ceylon Modules runtime.
 * Useful for potential extension.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractRuntime implements ceylon.modules.spi.runtime.Runtime {

    public static final String MODULE_INFO_CLASS = ".$module_";
    public static final String OLD_MODULE_INFO_CLASS = ".module_";
    

    protected static void invokeRun(ClassLoaderHolder clh, String moduleName, String ceylonRunnableName, final String[] args) throws Exception {
        final Class<?> runClass;
        ClassLoader cl = clh.getClassLoader();
        ClassLoader oldClassLoader = SecurityActions.setContextClassLoader(cl);
        try {
            try {
                String javaClassName = JVMModuleUtil.javaClassNameFromCeylon(moduleName, ceylonRunnableName);
                if (ceylonRunnableName == null) {
                    ceylonRunnableName = moduleName + "::" + JVMModuleUtil.RUN_INFO_CLASS;
                }
                runClass = cl.loadClass(javaClassName);
            } catch (ClassNotFoundException cnfe) {
                String type = Character.isUpperCase(ceylonRunnableName.charAt(0)) ? "class" : "function";
                String msg = String.format("Could not find toplevel %s '%s'.", type, ceylonRunnableName);
                if (!moduleName.equals(Constants.DEFAULT.toString()) && !ceylonRunnableName.contains(".") && !ceylonRunnableName.contains("::")) {
                    msg += String.format(" Class and method names need to be fully qualified, maybe you meant '%s'?", moduleName + "::" + ceylonRunnableName);
                }
                //msg += " [" + clh + "]";
                throw new CeylonRuntimeException(msg);
            }
            
            if ((runClass.getModifiers()&Modifier.PUBLIC) == 0) {
                String type = Character.isUpperCase(ceylonRunnableName.charAt(0)) ? "class" : "function";
                String msg = String.format("Cannot run toplevel %s '%s': it should be shared.", type, ceylonRunnableName);
                throw new CeylonRuntimeException(msg);
            }
            try {
                SecurityActions.invokeRun(runClass, args);
            } catch (NoSuchMethodException ex) {
                String type = Character.isUpperCase(ceylonRunnableName.charAt(0)) ? "class" : "function";
                String msg = String.format("Cannot run toplevel %s '%s': it should have no parameters or they should all have default values.", type, ceylonRunnableName);
                throw new CeylonRuntimeException(msg);
            }
        } finally {
            SecurityActions.setContextClassLoader(oldClassLoader);
        }
    }
    
 

    public void execute(Configuration conf) throws Exception {
        String exe = conf.module;
        // FIXME: argument checks could be done earlier
        if (exe == null) {
            throw new CeylonRuntimeException("No initial module defined");
        }

        int p = exe.indexOf("/");
        if (p == 0) {
            throw new CeylonRuntimeException("Missing runnable info: " + exe);
        }
        if (p == exe.length() - 1) {
            throw new CeylonRuntimeException("Missing version info: " + exe);
        }

        String name = exe.substring(0, p > 0 ? p : exe.length());
        String mv = (p > 0 ? exe.substring(p + 1) : null);

        final ClassLoaderHolder clh = createClassLoader(name, mv, conf);

        execute(conf, name, clh);
    }

    protected void execute(Configuration conf, String name, ClassLoaderHolder clh) throws Exception {
        invokeRun(clh, name, conf.run, conf.arguments);
    }


}
