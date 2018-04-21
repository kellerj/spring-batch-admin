package org.springframework.batch.admin.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

public class BatchJobApplicationContextFactoryBean implements FactoryBean<ApplicationContextFactory[]>, ApplicationContextAware {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());
    
    private List<Resource> resources = new ArrayList<Resource>();
    private ApplicationContext applicationContext;
    private String excludeJobFilePrefixes;

    /**
     * A set of resources to load using a
     * {@link GenericApplicationContextFactory}. Each resource should be a
     * Spring configuration file which is loaded into an application context
     * whose parent is the current context. In a configuration file the
     * resources can be given as a pattern (e.g.
     * <code>classpath*:/config/*-context.xml</code>).
     *
     * @param resources
     */
    public void setResources(Resource[] resources) {
        this.resources = Arrays.asList(resources);
    }

    /**
     * Create an {@link ApplicationContextFactory} from each resource provided
     * in {@link #setResources(Resource[])}.
     *
     * @return an array of {@link ApplicationContextFactory}
     * @throws Exception
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    @Override
    public ApplicationContextFactory[] getObject() throws Exception {

        if (resources == null) {
            return new ApplicationContextFactory[0];
        }

        List<ApplicationContextFactory> applicationContextFactories = new ArrayList<ApplicationContextFactory>();
        String[] excludeJobPrefixList = StringUtils.split(excludeJobFilePrefixes, ',');
        for (Resource resource : resources) {
            if ( StringUtils.startsWithAny(resource.getFilename(), excludeJobPrefixList) ) {
                LOG.warn("SKIPPING {} due to match of exclude prefix list: {}", resource.getFilename(), excludeJobFilePrefixes);
                continue;
            }
            LOG.info("Loading Batch Job Definitions from: {}", resource.getFilename());
            GenericApplicationContextFactory factory = new GenericApplicationContextFactory(resource);
            factory.setCopyConfiguration(true);
            factory.setApplicationContext(applicationContext);
            applicationContextFactories.add(factory);
        }
        return applicationContextFactories.toArray(new ApplicationContextFactory[applicationContextFactories.size()]);
    }

    /**
     * The type of object returned by this factory - an array of
     * {@link ApplicationContextFactory}.
     *
     * @return array of {@link ApplicationContextFactory}
     * @see FactoryBean#getObjectType()
     */
    @Override
    public Class<?> getObjectType() {
        return ApplicationContextFactory[].class;
    }

    /**
     * Optimization hint for bean factory.
     * @return true
     * @see FactoryBean#isSingleton()
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * An application context that can be used as a parent context for all the
     * factories.
     *
     * @param applicationContext the {@link ApplicationContext} to set
     * @throws BeansException
     * @see ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setExcludeJobFilePrefixes(String excludeJobFilePrefixes) {
        this.excludeJobFilePrefixes = excludeJobFilePrefixes;
    }

}
