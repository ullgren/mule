/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.resources.descriptor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.core.config.MuleManifest.getProductVersion;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.core.registry.SpiServiceRegistry;
import org.mule.runtime.extension.api.declaration.DescribingContext;
import org.mule.runtime.extension.api.declaration.spi.Describer;
import org.mule.runtime.extension.api.resources.GeneratedResource;
import org.mule.runtime.extension.api.resources.spi.GeneratedResourceFactory;
import org.mule.runtime.extension.api.runtime.ExtensionFactory;
import org.mule.runtime.module.extension.internal.DefaultDescribingContext;
import org.mule.runtime.module.extension.internal.introspection.DefaultExtensionFactory;
import org.mule.runtime.module.extension.internal.introspection.describer.AnnotationsBasedDescriber;
import org.mule.runtime.module.extension.internal.introspection.version.StaticVersionResolver;
import org.mule.runtime.module.extension.internal.resources.AbstractGeneratedResourceFactoryTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.test.heisenberg.extension.HeisenbergExtension;

import java.io.InputStream;
import java.util.Optional;

@SmallTest
public class MulePluginDescriptorGeneratorTestCase extends AbstractGeneratedResourceFactoryTestCase {

  private MulePluginDescriptorGenerator generator = new MulePluginDescriptorGenerator();

  private ExtensionModel extensionModel;

  @Before
  public void before() {
    Describer describer =
        new AnnotationsBasedDescriber(HeisenbergExtension.class, new StaticVersionResolver(getProductVersion()));
    ExtensionFactory extensionFactory = new DefaultExtensionFactory(new SpiServiceRegistry(), getClass().getClassLoader());
    final DescribingContext context = new DefaultDescribingContext(getClass().getClassLoader());

    extensionModel = extensionFactory.createFrom(describer.describe(context), context);
  }

  @Override
  protected Class<? extends GeneratedResourceFactory>[] getResourceFactoryTypes() {
    return new Class[] {MulePluginDescriptorGenerator.class};
  }

  @Test
  public void generate() throws Exception {
    InputStream in = getClass().getResourceAsStream("/heisenberg-test-mule-plugin.json");
    assertThat(in, is(notNullValue()));
    String expectedDescriptor = IOUtils.toString(in);
    Optional<GeneratedResource> resource = generator.generateResource(extensionModel);
    assertThat(resource.isPresent(), is(true));

    String actualDescriptor = new String(resource.get().getContent());
    assertEquals(actualDescriptor, expectedDescriptor, true);
  }
}
