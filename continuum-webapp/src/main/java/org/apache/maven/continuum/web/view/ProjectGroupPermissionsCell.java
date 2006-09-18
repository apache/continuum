package org.apache.maven.continuum.web.view;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.cell.Cell;
import org.extremecomponents.table.core.TableModel;
import org.extremecomponents.table.view.html.ColumnBuilder;

/**
 * 
 * @deprecated use of cells is discouraged due to lack of i18n and design in java code.
 *             Use jsp:include instead.
 * 
 * @author <a href="mailto:hisidro@exist.com">Henry Isidro</a>
 * @version $Id$
 */
public class ProjectGroupPermissionsCell
    implements Cell
{
    public String getExportDisplay( TableModel model, Column column )
    {
        return null;
    }

    public String getHtmlDisplay( TableModel model, Column column )
    {
        ColumnBuilder columnBuilder = new ColumnBuilder( column );

        columnBuilder.tdStart();

        try
        {
            columnBuilder.getHtmlBuilder().input( "checkbox" );
            columnBuilder.getHtmlBuilder().checked();
            columnBuilder.getHtmlBuilder().xclose();
        }
        catch ( Exception e )
        {
        }

        columnBuilder.tdEnd();

        return columnBuilder.toString();
    }

}
