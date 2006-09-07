package org.apache.maven.continuum.web.view;

import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.cell.DisplayCell;
import org.extremecomponents.table.core.TableModel;
import org.extremecomponents.table.cell.Cell;
import org.extremecomponents.table.view.html.ColumnBuilder;

public class ProjectGroupPermissionsCell implements Cell
{
        public String getExportDisplay(TableModel model, Column column) {
            return null;
        }

        public String getHtmlDisplay(TableModel model, Column column) {
            ColumnBuilder columnBuilder = new ColumnBuilder(column);
            
            columnBuilder.tdStart();
            
            try {
                columnBuilder.getHtmlBuilder().input("checkbox");
                columnBuilder.getHtmlBuilder().checked();
                columnBuilder.getHtmlBuilder().xclose();
            } catch (Exception e) {}
            
            columnBuilder.tdEnd();
            
            return columnBuilder.toString();
        }

}
