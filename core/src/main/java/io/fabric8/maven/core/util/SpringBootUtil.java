/**
 * Copyright 2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.fabric8.maven.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.SortedMap;

import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Utility methods to access spring-boot resources.
 */
public class SpringBootUtil {

    private static final transient Logger LOG = LoggerFactory.getLogger(SpringBootUtil.class);

    /**
     * Returns the spring boot configuration (supports `application.properties` and `application.yml`)
     * or an empty properties object if not found, it assumes first profile as default profile.
     *
     * @param compileClassLoader compile class loader
     * @return properties object
     */
    public static Properties getSpringBootApplicationProperties(URLClassLoader compileClassLoader) {
        return getSpringBootApplicationProperties(null, compileClassLoader);
    }

    /**
     * Returns the spring boot configuration (supports `application.properties` and `application.yml`)
     * or an empty properties object if not found
     *
     * @param springActiveProfile currently active spring-boot profile
     * @param compileClassLoader compile class loader
     * @return properties object
     */
    public static Properties getSpringBootApplicationProperties(String springActiveProfile, URLClassLoader compileClassLoader) {
        URL ymlResource = compileClassLoader.findResource("application.yml");
        URL propertiesResource = compileClassLoader.findResource("application.properties");

        Properties props = getPropertiesFromApplicationYamlResource(springActiveProfile, ymlResource);
        props.putAll(getPropertiesResource(propertiesResource));
        return props;
    }

    public static Properties getPropertiesFromApplicationYamlResource(String springActiveProfile, URL ymlResource) {
        return YamlUtil.getPropertiesFromYamlResource(springActiveProfile, ymlResource);
    }

    /**
     * Returns the given properties resource on the project classpath if found or an empty properties object if not
     */
    protected static Properties getPropertiesResource(URL resource) {
        Properties answer = new Properties();
        if (resource != null) {
            try(InputStream stream = resource.openStream()) {
                answer.load(stream);
            } catch (IOException e) {
                throw new IllegalStateException("Error while reading resource from URL " + resource, e);
            }
        }
        return answer;
    }

    /**
     * Determine the spring-boot devtools version for the current project
     */
    public static Optional<String> getSpringBootDevToolsVersion(MavenProject mavenProject) {
        return getSpringBootVersion(mavenProject);
    }

    /**
     * Determine the spring-boot major version for the current project
     */
    public static Optional<String> getSpringBootVersion(MavenProject mavenProject) {
        return Optional.ofNullable(MavenUtil.getDependencyVersion(mavenProject, SpringBootConfigurationHelper.SPRING_BOOT_GROUP_ID, SpringBootConfigurationHelper.SPRING_BOOT_ARTIFACT_ID));
    }

    public static String getSpringBootActiveProfile(MavenProject mavenProject) {
        if (mavenProject != null && mavenProject.getProperties() != null) {
            if (mavenProject.getProperties().get("spring.profiles.active") != null) {
                return mavenProject.getProperties().get("spring.profiles.active").toString();
            }
        }
        return null;
    }

}
