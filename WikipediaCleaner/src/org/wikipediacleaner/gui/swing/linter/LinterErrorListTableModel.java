/*
 *  WPCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2013  Nicolas Vervelle
 *
 *  See README.txt file for licensing information.
 */

package org.wikipediacleaner.gui.swing.linter;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;

import org.wikipediacleaner.api.check.CheckWikiDetection;
import org.wikipediacleaner.api.constants.WikiConfiguration;
import org.wikipediacleaner.api.linter.LinterError;
import org.wikipediacleaner.i18n.GT;


/**
 * A table model for a list of Linter errors.
 */
public class LinterErrorListTableModel extends AbstractTableModel {

  /** Serialization */
  private static final long serialVersionUID = 6291117363909928449L;

  /** Wiki configuration */
  private final WikiConfiguration config;

  /** List of errors */
  private final List<LinterError> errors;

  /** Text area */
  private final JTextComponent textPane;

  public final static int COLUMN_START = 0;
  public final static int COLUMN_END = COLUMN_START + 1;
  public final static int COLUMN_TYPE = COLUMN_END + 1;
  public final static int COLUMN_PARAMETERS = COLUMN_TYPE + 1;
  public final static int COLUMN_TEMPLATE = COLUMN_PARAMETERS + 1;
  public final static int COLUMN_GOTO = COLUMN_TEMPLATE + 1;

  public final static int NB_COLUMNS_WITHOUT_GOTO = COLUMN_GOTO;
  public final static int NB_COLUMNS_WITH_GOTO = COLUMN_GOTO + 1;

  /**
   * @param config Wiki configuration.
   * @param errors List of Linter errors.
   * @param textPane Text area.
   */
  public LinterErrorListTableModel(
      WikiConfiguration config,
      List<LinterError> errors,
      JTextComponent textPane) {
    this.config = config;
    this.errors = errors;
    this.textPane = textPane;
  }

  /**
   * Configure a column model.
   * 
   * @param model Column model.
   */
  public void configureColumnModel(TableColumnModel model) {
    TableColumn column;

    column = model.getColumn(COLUMN_END);
    column.setMinWidth(60);
    column.setPreferredWidth(60);
    column.setMaxWidth(100);

    if (textPane != null) {
      column = model.getColumn(COLUMN_GOTO);
      column.setMinWidth(30);
      column.setPreferredWidth(30);
      column.setMaxWidth(30);
      LinterErrorRenderer detectionRenderer = new LinterErrorRenderer(textPane);
      column.setCellEditor(detectionRenderer);
      column.setCellRenderer(detectionRenderer);
    }

    column = model.getColumn(COLUMN_PARAMETERS);
    column.setMinWidth(100);
    column.setPreferredWidth(300);

    column = model.getColumn(COLUMN_START);
    column.setMinWidth(60);
    column.setPreferredWidth(60);
    column.setMaxWidth(100);

    column = model.getColumn(COLUMN_TEMPLATE);
    column.setMinWidth(100);
    column.setPreferredWidth(300);

    column = model.getColumn(COLUMN_TYPE);
    column.setMinWidth(100);
    column.setPreferredWidth(300);
  }

  /**
   * @return Number of columns.
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  @Override
  public int getColumnCount() {
    if (textPane != null) {
      return NB_COLUMNS_WITH_GOTO;
    }
    return NB_COLUMNS_WITHOUT_GOTO;
  }

  /**
   * @return Number of rows.
   * @see javax.swing.table.TableModel#getRowCount()
   */
  @Override
  public int getRowCount() {
    return (errors != null) ? errors.size() : 0;
  }

  /**
   * @param rowIndex Row index.
   * @param columnIndex Column index.
   * @return Value at row and column.
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if ((errors != null) && (rowIndex >= 0) && (rowIndex < errors.size())) {
      LinterError error = errors.get(rowIndex);
      switch (columnIndex) {
      case COLUMN_END:
        return error.getEndOffset();
      case COLUMN_GOTO:
        return error;
      case COLUMN_PARAMETERS:
      {
        StringBuilder tmp = new StringBuilder();
        Map<String, String> params = error.getParameters();
        if (params != null) {
          for (Entry<String, String> param : params.entrySet()) {
            if (tmp.length() > 0) {
              tmp.append(";");
            }
            tmp.append(param.getKey());
            tmp.append("=");
            tmp.append(param.getValue());
          }
        }
        return tmp.toString();
      }
      case COLUMN_START:
        return error.getStartOffset();
      case COLUMN_TEMPLATE:
        if (error.getTemplateName() != null) {
          return error.getTemplateName();
        }
        if (error.isMutiPartTemplateBlock()) {
          return GT._("Multiple templates");
        }
        return "";
      case COLUMN_TYPE:
        return error.getTypeName(config);
      }
    }
    return null;
  }

  /**
   * @param rowIndex Row index.
   * @param columnIndex Column index.
   * @return True if the cell is editable
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    if (columnIndex == COLUMN_GOTO) {
      return (textPane != null);
    }
    return super.isCellEditable(rowIndex, columnIndex);
  }

  /**
   * @param column Column index.
   * @return Name of column.
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName(int column) {
    switch (column) {
    case COLUMN_END:
      return GT._("End");
    case COLUMN_GOTO:
      return "";
    case COLUMN_PARAMETERS:
      return GT._("Parameters");
    case COLUMN_START:
      return GT._("Start");
    case COLUMN_TEMPLATE:
      return GT._("Template");
    case COLUMN_TYPE:
      return GT._("Type");
    }
    return super.getColumnName(column);
  }

  /**
   * @param columnIndex Column index.
   * @return Class of that data in the column.
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
  public Class<?> getColumnClass(int columnIndex) {
    switch (columnIndex) {
    case COLUMN_END:
      return Integer.class;
    case COLUMN_GOTO:
      return CheckWikiDetection.class;
    case COLUMN_PARAMETERS:
      return String.class;
    case COLUMN_START:
      return Integer.class;
    case COLUMN_TEMPLATE:
      return String.class;
    case COLUMN_TYPE:
      return String.class;
    }
    return super.getColumnClass(columnIndex);
  }

}
