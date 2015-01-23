package com.redhat.ceylon.compiler.js;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.redhat.ceylon.compiler.loader.ModelEncoder;
import com.redhat.ceylon.compiler.typechecker.model.Declaration;
import com.redhat.ceylon.compiler.typechecker.model.Module;
import com.redhat.ceylon.compiler.typechecker.model.Setter;
import com.redhat.ceylon.compiler.typechecker.model.Value;

/** A container for things we need to keep per-module. */
public class JsOutput {
    private File outfile;
    private File modfile;
    private Writer writer;
    protected String clalias = "";
    protected final Module module;
    protected boolean modelDone;
    private final Set<File> s = new HashSet<File>();
    final Map<String,String> requires = new HashMap<String,String>();
    protected final MetamodelVisitor mmg;
    final String encoding;

    protected JsOutput(Module m, String encoding) throws IOException {
        this.encoding = encoding == null ? "UTF-8" : encoding;
        module = m;
        mmg = new MetamodelVisitor(m);
    }
    protected Writer getWriter() throws IOException {
        if (writer == null) {
            outfile = File.createTempFile("ceylon-jsout-", ".tmp");
            writer = new OutputStreamWriter(new FileOutputStream(outfile), encoding);
        }
        return writer;
    }
    protected File close() throws IOException {
        if (writer != null) {
            writer.close();
        }
        return outfile;
    }
    File getModelFile() {
        return modfile;
    }

    void addSource(File src) {
        s.add(src);
    }
    Set<File> getSources() { return s; }

    /** Writes the model file to a temporary file. */
    protected void writeModelFile() throws IOException {
        modfile = File.createTempFile("ceylon-jsmod-", ".tmp");
        try (OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(modfile), encoding)) {
            JsCompiler.beginWrapper(fw);
            fw.write("ex$.$CCMM$=");
            ModelEncoder.encodeModel(mmg.getModel(), fw);
            fw.write(";\n");
            JsCompiler.endWrapper(fw);
        } finally {
        }
    }

    /** Write the function to retrieve or define the model JSON map. */
    protected void writeModelRetriever() throws IOException {
        out("require('", JsCompiler.scriptPath(module), "-model').$CCMM$");
    }

    public void encodeModel(final JsIdentifierNames names) throws IOException {
        if (!modelDone) {
            modelDone = true;
            writeModelFile();
            out("\nvar _CTM$;function $CCMM$(){if (_CTM$===undefined)_CTM$=");
            writeModelRetriever();
            out(";return _CTM$;}\n");
            getWriter().write("ex$.$CCMM$=$CCMM$;\n");
            if (!JsCompiler.isCompilingLanguageModule()) {
                Module clm = module.getLanguageModule();
                clalias = names.moduleAlias(clm) + ".";
                require(clm, names);
                out(clalias, "$addmod$(ex$,'", module.getNameAsString(), "/", module.getVersion(), "');\n");
            }
        }
    }

    public void outputFile(File f) {
        try(BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line = null;
            while ((line = r.readLine()) != null) {
                final String c = line.trim();
                if (!c.isEmpty()) {
                    getWriter().write(c);
                    getWriter().write('\n');
                }
            }
        } catch(IOException ex) {
            throw new CompilerErrorException("Reading from " + f);
        }
    }

    public String getLanguageModuleAlias() {
        return clalias;
    }

    public void require(final Module mod, final JsIdentifierNames names) {
        final String path = JsCompiler.scriptPath(mod);
        final String modAlias = names.moduleAlias(mod);
        if (requires.put(path, modAlias) == null) {
            out("var ", modAlias, "=require('", path, "');\n");
            if (modAlias != null && !modAlias.isEmpty()) {
                out(clalias, "$addmod$(", modAlias,",'", mod.getNameAsString(), "/", mod.getVersion(), "');\n");
            }
        }
    }

    /** Print generated code to the Writer.
     * @param code The main code
     * @param codez Optional additional strings to print after the main code. */
    public void out(String code, String... codez) {
        try {
            getWriter().write(code);
            for (String s : codez) {
                getWriter().write(s);
            }
        }
        catch (IOException ioe) {
            throw new RuntimeException("Generating JS code", ioe);
        }
    }

    public void publishUnsharedDeclarations(JsIdentifierNames names) {
        //#489 lists with unshared toplevel members of each package
        for (com.redhat.ceylon.compiler.typechecker.model.Package pkg : module.getPackages()) {
            ArrayList<Declaration> unsharedDecls = new ArrayList<>(pkg.getMembers().size());
            for (Declaration d : pkg.getMembers()) {
                if (!d.isShared() && !(d.isAnonymous() && d.getName().startsWith("anonymous#"))) {
                    unsharedDecls.add(d);
                }
            }
            if (!unsharedDecls.isEmpty()) {
                out("ex$.$pkgunsh$", pkg.getNameAsString().replace('.', '$'), "={");
                boolean first=true;
                for (Declaration d : unsharedDecls) {
                    //TODO only use quotes when absolutely necessary
                    if (d.isAnonymous()) {
                        if (first)first=false;else out(",");
                        out("'", names.getter(d, true),
                                "':", names.name(d));
                    } else if (d instanceof Setter) {
                        //ignore
                        if (((Setter) d).getGetter() == null) {
                            if (first)first=false;else out(",");
                            out("'", d.getName(), "':", names.setter(d));
                        }
                    } else if (d instanceof Value) {
                        if (first)first=false;else out(",");
                        out("'", d.getName(), "':", names.getter(d, true));
                    } else {
                        if (first)first=false;else out(",");
                        out("'", d.getName(), "':", names.name(d));
                    }
                }
                out("};\n");
            }
        }
    }

}