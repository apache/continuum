package org.apache.maven.continuum.web.view.buildresults;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.continuum.web.util.StateGenerator;

import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.cell.DisplayCell;
import org.extremecomponents.table.core.BaseModel;

import javax.servlet.http.HttpServletRequest;

/**
 * Used in BuildResults
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StateCell
    extends DisplayCell
{
    public void init(BaseModel model, Column column)
    {
        super.init(model, column);

        HttpServletRequest request = (HttpServletRequest) model.getPageContext().getRequest();

        Object value = column.getPropertyValue();

        int state = -1;

        if ( value instanceof Integer )
        {
            state = ( (Integer) value).intValue();
        }

        value = StateGenerator.generate( state, request.getContextPath() );

        column.setValue(value);

        column.setPropertyValue(value);
    }
}