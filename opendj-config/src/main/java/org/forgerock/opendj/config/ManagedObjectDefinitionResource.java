/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at legal-notices/CDDLv1_0.txt
 * or http://forgerock.org/license/CDDLv1.0.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at legal-notices/CDDLv1_0.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2008 Sun Microsystems, Inc.
 *      Portions copyright 2014-2015 ForgeRock AS.
 */

package org.forgerock.opendj.config;

import static org.forgerock.util.Utils.closeSilently;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;

/**
 * A class for retrieving non-internationalized resource properties associated
 * with a managed object definition.
 * <p>
 * Resource properties are not available for the {@link TopCfgDefn}.
 */
public final class ManagedObjectDefinitionResource {

    /** Mapping from definition to property tables. */
    private final Map<AbstractManagedObjectDefinition<?, ?>, Properties> properties = new HashMap<>();

    /** The resource name prefix. */
    private final String prefix;

    /**
     * Creates a new resource instance for the named profile.
     *
     * @param profile
     *            The name of the profile.
     * @return Returns the resource instance for the named profile.
     */
    public static ManagedObjectDefinitionResource createForProfile(String profile) {
        return new ManagedObjectDefinitionResource("config.profiles." + profile);
    }

    /** Private constructor. */
    private ManagedObjectDefinitionResource(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Get the resource value associated with the specified key.
     *
     * @param d
     *            The managed object definition.
     * @param key
     *            The resource key.
     * @return Returns the resource value associated with the specified key.
     * @throws MissingResourceException
     *             If the key was not found.
     * @throws UnsupportedOperationException
     *             If the provided managed object definition was the
     *             {@link TopCfgDefn}.
     */
    public String getString(AbstractManagedObjectDefinition<?, ?> d, String key) {
        if (d.isTop()) {
            throw new UnsupportedOperationException("Profile resources are not available for the "
                + "Top configuration definition");
        }

        Properties p = getProperties(d);
        String result = p.getProperty(key);

        if (result == null) {
            String baseName = prefix + "." + d.getClass().getName();
            String path = baseName.replace('.', '/') + ".properties";

            throw new MissingResourceException("Can't find resource " + path + ", key " + key, baseName, key);
        }

        return result;
    }

    /**
     * Retrieve the properties table associated with a managed object,
     * lazily loading it if necessary.
     */
    private synchronized Properties getProperties(AbstractManagedObjectDefinition<?, ?> d) {
        Properties p = properties.get(d);

        if (p == null) {
            // Load the resource file.
            String baseName = prefix + "." + d.getClass().getName();
            String path = baseName.replace('.', '/') + ".properties";
            InputStream stream = ConfigurationFramework.getInstance().getClassLoader().getResourceAsStream(path);

            if (stream == null) {
                throw new MissingResourceException("Can't find resource " + path, baseName, "");
            }

            final InputStream is = new BufferedInputStream(stream);
            p = new Properties();
            try {
                p.load(is);
            } catch (IOException e) {
                throw new MissingResourceException("Can't load resource " + path
                        + " due to IO exception: " + e.getMessage(), baseName, "");
            } finally {
                closeSilently(is);
            }

            // Cache the resource.
            properties.put(d, p);
        }

        return p;
    }
}
