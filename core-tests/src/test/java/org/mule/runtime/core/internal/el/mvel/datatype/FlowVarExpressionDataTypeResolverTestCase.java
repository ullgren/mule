/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.core.internal.el.mvel.datatype;

import static org.mule.runtime.core.internal.el.mvel.MessageVariableResolverFactory.FLOW_VARS;

import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.core.api.event.BaseEvent;

public class FlowVarExpressionDataTypeResolverTestCase extends AbstractVarExpressionDataTypeResolverTestCase {

  public FlowVarExpressionDataTypeResolverTestCase() {
    super(new FlowVarExpressionDataTypeResolver(), FLOW_VARS);
  }

  @Override
  protected BaseEvent setVariable(BaseEvent event, Object propertyValue, DataType dataType) {
    return BaseEvent.builder(event).addVariable(PROPERTY_NAME, propertyValue, dataType).build();
  }
}