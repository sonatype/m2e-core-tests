/*******************************************************************************
 * Copyright (c) 2008-2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Sonatype, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.m2e.editor.composites;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.m2e.editor.internal.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;


/**
 * List editor composite
 * 
 * @author Eugene Kuleshov
 */
public class ListEditorComposite<T> extends Composite {

  TableViewer viewer;

  Button addButton;

  Button removeButton;

  Button selectButton;

  boolean readOnly = false;

  protected FormToolkit toolkit;

  public ListEditorComposite(Composite parent, int style, boolean includeSearch) {
    super(parent, style);
    toolkit = new FormToolkit(parent.getDisplay());

    GridLayout gridLayout = new GridLayout(2, false);
    gridLayout.marginWidth = 1;
    gridLayout.marginHeight = 1;
    gridLayout.verticalSpacing = 1;
    setLayout(gridLayout);

    final Table table = toolkit.createTable(this, SWT.FLAT | SWT.MULTI | style);
    table.setData("name", "list-editor-composite-table"); //$NON-NLS-1$ //$NON-NLS-2$
    final TableColumn column = new TableColumn(table, SWT.NONE);
    table.addControlListener(new ControlAdapter() {
      public void controlResized(ControlEvent e) {
        column.setWidth(table.getClientArea().width);
      }
    });

    viewer = new TableViewer(table);

    int vSpan = includeSearch ? 3 : 2;
    GridData viewerData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, vSpan);
    viewerData.widthHint = 100;
    viewerData.heightHint = includeSearch ? 125 : 50;
    viewerData.minimumHeight = includeSearch ? 125 : 50;
    table.setLayoutData(viewerData);
    viewer.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.TRUE);

    viewerData.verticalSpan = createButtons(includeSearch);

    viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        selectionUpdated();
      }
    });

    toolkit.paintBordersFor(this);
  }

  public ListEditorComposite(Composite parent, int style) {
    this(parent, style, false);
  }

  public void setLabelProvider(ILabelProvider labelProvider) {
    viewer.setLabelProvider(labelProvider);
  }

  public void setContentProvider(ListEditorContentProvider<T> contentProvider) {
    viewer.setContentProvider(contentProvider);
  }

  public void setInput(List<T> input) {
    viewer.setInput(input);
    viewer.setSelection(new StructuredSelection());
  }

  public Object getInput() {
    return viewer.getInput();
  }

  public void setOpenListener(IOpenListener listener) {
    viewer.addOpenListener(listener);
  }

  public void addSelectionListener(ISelectionChangedListener listener) {
    viewer.addSelectionChangedListener(listener);
  }

  public void setSelectListener(SelectionListener listener) {
    if(selectButton != null) {
      selectButton.addSelectionListener(listener);
      selectButton.setEnabled(true);
    }
  }

  public void setAddListener(SelectionListener listener) {
    addButton.addSelectionListener(listener);
    addButton.setEnabled(true);
  }

  public void setRemoveListener(SelectionListener listener) {
    removeButton.addSelectionListener(listener);
  }

  public TableViewer getViewer() {
    return viewer;
  }

  public int getSelectionIndex() {
    return viewer.getTable().getSelectionIndex();
  }

  public void setSelectionIndex(int n) {
    viewer.getTable().setSelection(n);
  }

  @SuppressWarnings("unchecked")
  public List<T> getSelection() {
    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    return selection == null ? Collections.emptyList() : selection.toList();
  }

  public void setSelection(List<T> selection) {
    viewer.setSelection(new StructuredSelection(selection), true);
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
    addButton.setEnabled(!readOnly);
    if(selectButton != null) {
      selectButton.setEnabled(!readOnly);
    }
    selectionUpdated();
  }

  public void refresh() {
    if(!viewer.getTable().isDisposed()) {
      viewer.refresh(true);
    }
  }

  public void setCellModifier(ICellModifier cellModifier) {
    viewer.setColumnProperties(new String[] {"?"});

    TextCellEditor editor = new TextCellEditor(viewer.getTable());
    viewer.setCellEditors(new CellEditor[] {editor});
    viewer.setCellModifier(cellModifier);
  }

  protected void selectionUpdated() {
    updateButtons(!readOnly && !viewer.getSelection().isEmpty());
  }

  protected void updateButtons(boolean enable) {
    removeButton.setEnabled(enable);
  }

  public void setDoubleClickListener(IDoubleClickListener listener) {
    viewer.addDoubleClickListener(listener);
  }

  protected int createButtons(boolean includeSearch) {
    int n = 2;

    if(includeSearch) {
      n++ ;
      createSelectButton();
    }
    createAddButton();
    createRemoveButton();

    return n;
  }

  protected void createSelectButton() {
    selectButton = createButton(Messages.ListEditorComposite_btnAdd);
  }

  protected void createAddButton() {
    addButton = createButton(Messages.ListEditorComposite_btnCreate);
  }

  protected void createRemoveButton() {
    removeButton = createButton(Messages.ListEditorComposite_btnDelete);
  }

  protected Button createButton(String text) {
    Button button = toolkit.createButton(this, text, SWT.FLAT);
    GridData gd = new GridData(SWT.FILL, SWT.TOP, false, false);
    gd.verticalIndent = 0;
    button.setLayoutData(gd);
    button.setEnabled(false);
    return button;
  }
}
