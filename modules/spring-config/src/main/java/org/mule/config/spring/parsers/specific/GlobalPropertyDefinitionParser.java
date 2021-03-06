/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.config.spring.parsers.specific;

import static org.springframework.util.SystemPropertyUtils.PLACEHOLDER_PREFIX;
import static org.springframework.util.SystemPropertyUtils.PLACEHOLDER_SUFFIX;
import static org.springframework.util.SystemPropertyUtils.resolvePlaceholders;

import org.mule.config.spring.parsers.assembly.BeanAssembler;
import org.mule.config.spring.parsers.generic.MuleOrphanDefinitionParser;

import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class GlobalPropertyDefinitionParser extends MuleOrphanDefinitionParser
{
    private final String VALUE_ATTR = "value";
    private final String NAME_ATTR = "name";

    public GlobalPropertyDefinitionParser()
    {
        super(true);
        addIgnored(NAME_ATTR);
        addIgnored(VALUE_ATTR);
    }

    protected Class getBeanClass(Element element)
    {
        return String.class;
    }

    protected void postProcess(ParserContext context, BeanAssembler assembler, Element element)
    {
        String name = element.getAttribute(NAME_ATTR);
        if (name.indexOf(' ') != -1)
        {
            logger.warn("Environment property name should not contain spaces: \"" + name + "\"");
        }

        solvePlaceholderValue(assembler, element, name);

        super.postProcess(context, assembler, element);
    }

    private void solvePlaceholderValue(BeanAssembler assembler, Element element, String name)
    {
        String solvedValue = assembler.resolvePlaceholder(wrapPlaceholder(name));

        if (solvedValue != null)
        {
            // In this case the value from the configuration management
            // has to be taken.
            assembler.getBean().addConstructorArgValue(solvedValue);
        }
        else
        {
            assembler.getBean().addConstructorArgValue(solveGlobalPropertyValue(assembler, element));
        }
    }

    private String solveGlobalPropertyValue(BeanAssembler assembler, Element element)
    {
        String value = element.getAttribute(VALUE_ATTR);
        String solvedValue = assembler.resolvePlaceholder(value);
        if (solvedValue == null)
        {
            solvedValue = resolvePlaceholders(value);
        }

        return value;
    }

    private String wrapPlaceholder(String name)
    {
        return PLACEHOLDER_PREFIX + name + PLACEHOLDER_SUFFIX;
    }
}
