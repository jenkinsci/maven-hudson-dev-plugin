package org.mortbay.jetty.plugin;

import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.io.IOException;

/**
 * {@link ClassLoader} that provide isolation
 * between Maven that runs Hudson and Maven that runs inside Hudson.
 *
 * @author Kohsuke Kawaguchi
 */
final class MaskingClassLoader extends ClassLoader {
    public MaskingClassLoader(ClassLoader parent) {
        super(parent);
    }

    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        for (String pkg : PACKAGES)
            if(name.startsWith(pkg))
                throw new ClassNotFoundException(name);
        return super.loadClass(name, resolve);
    }

    public URL getResource(String name) {
        if(isMaskedResourcePrefix(name))
            return null;
        return super.getResource(name);
    }

    private boolean isMaskedResourcePrefix(String name) {
        for (String p : PREFIXES)
            if (name.startsWith(p))
                return true;
        return name.startsWith("META-INF/plexus")
            || name.startsWith("META-INF/maven");
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        if(isMaskedResourcePrefix(name))
            return EMPTY_ENUMERATION;
        return super.getResources(name);
    }

    private static final Enumeration<URL> EMPTY_ENUMERATION = new Enumeration<URL>() {
        public boolean hasMoreElements() {
            return false;
        }

        public URL nextElement() {
            throw new NoSuchElementException();
        }
    };

    private static final String[] PACKAGES = {
        "org.apache.maven.",
        "org.codehaus.plexus.",
        "org.sonatype."
    };

    private static final String[] PREFIXES;

    static {
        PREFIXES = new String[PACKAGES.length];
        for (int i=0; i<PACKAGES.length; i++)
            PREFIXES[i] = PACKAGES[i].replace('.','/');
    }
}
