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
        if(name.startsWith("org.apache.maven") || name.startsWith("org.codehaus.plexus"))
            throw new ClassNotFoundException(name);
        return super.loadClass(name, resolve);
    }

    public URL getResource(String name) {
        if(isMaskedResourcePrefix(name))
            return null;
        return super.getResource(name);
    }

    private boolean isMaskedResourcePrefix(String name) {
        return name.startsWith("org/apache/maven")
            || name.startsWith("org/codehaus/plexus")
            || name.startsWith("META-INF/plexus")
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
}
