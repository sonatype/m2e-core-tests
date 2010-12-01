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

package org.eclipse.m2e.core.wizards;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.icu.text.DateFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.apache.lucene.search.BooleanQuery;

import org.eclipse.m2e.core.MavenImages;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.core.IMavenConstants;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.index.IIndex;
import org.eclipse.m2e.core.index.IndexManager;
import org.eclipse.m2e.core.index.IndexedArtifact;
import org.eclipse.m2e.core.index.IndexedArtifactFile;
import org.eclipse.m2e.core.index.UserInputSearchExpression;
import org.eclipse.m2e.core.internal.Messages;


/**
 * MavenPomSelectionComposite
 * 
 * @author Eugene Kuleshov
 */
public class MavenPomSelectionComponent extends Composite {

  /* (non-Javadoc)
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  public void dispose() {
    if(searchJob != null) {
      searchJob.cancel();
    }
    super.dispose();
  }

  Text searchText = null;

  TreeViewer searchResultViewer = null;

  Button javadocCheckBox;

  Button sourcesCheckBox;

  Button testCheckBox;

  /**
   * One of {@link IIndex#SEARCH_ARTIFACT}, {@link IIndex#SEARCH_CLASS_NAME},
   */
  String queryType;

  SearchJob searchJob;

  private IStatus status;

  private ISelectionChangedListener selectionListener;

  public static final String P_SEARCH_INCLUDE_JAVADOC = "searchIncludesJavadoc"; //$NON-NLS-1$

  public static final String P_SEARCH_INCLUDE_SOURCES = "searchIncludesSources"; //$NON-NLS-1$

  public static final String P_SEARCH_INCLUDE_TESTS = "searchIncludesTests"; //$NON-NLS-1$

  private static final long SHORT_DELAY = 150L;

  private static final long LONG_DELAY = 500L;

  HashSet<String> artifactKeys = new HashSet<String>();

  public MavenPomSelectionComponent(Composite parent, int style) {
    super(parent, style);
    createSearchComposite();
  }

  private void createSearchComposite() {
    GridLayout gridLayout = new GridLayout(2, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    setLayout(gridLayout);

    Label searchTextlabel = new Label(this, SWT.NONE);
    searchTextlabel.setText(Messages.MavenPomSelectionComponent_search_title);
    searchTextlabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

    searchText = new Text(this, SWT.BORDER | SWT.SEARCH);
    searchText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
    searchText.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.keyCode == SWT.ARROW_DOWN) {
          searchResultViewer.getTree().setFocus();
          selectFirstElementInTheArtifactTreeIfNoSelectionHasBeenMade();
        }
      }
    });

    searchText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        scheduleSearch(searchText.getText(), true);
      }
    });

    Label searchResultsLabel = new Label(this, SWT.NONE);
    searchResultsLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
    searchResultsLabel.setText(Messages.MavenPomSelectionComponent_lblResults);

    Tree tree = new Tree(this, SWT.BORDER | SWT.SINGLE);
    tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    tree.setData("name", "searchResultTree"); //$NON-NLS-1$ //$NON-NLS-2$
    tree.addFocusListener(new FocusListener() {

      public void focusGained(FocusEvent e) {
        selectFirstElementInTheArtifactTreeIfNoSelectionHasBeenMade();
      }

      public void focusLost(FocusEvent e) {

      }
    });

    searchResultViewer = new TreeViewer(tree);
  }

  void selectFirstElementInTheArtifactTreeIfNoSelectionHasBeenMade() {
    //
    // If we have started a new search when focus is passed to the tree viewer we will automatically select
    // the first element if no element has been selected from a previous expedition into the tree viewer.
    //
    if(searchResultViewer.getTree().getItemCount() > 0 && searchResultViewer.getSelection().isEmpty()) {
      Object artifact = searchResultViewer.getTree().getTopItem().getData();
      searchResultViewer.setSelection(new StructuredSelection(artifact), true);
    }
  }

  protected boolean showClassifiers() {
    return (queryType != null && IIndex.SEARCH_ARTIFACT.equals(queryType));
  }

  private void setupButton(final Button button, String label, final String prefName, int horizontalIndent) {
    button.setText(label);
    GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
    gd.horizontalIndent = horizontalIndent;
    button.setLayoutData(gd);
    boolean check = MavenPlugin.getDefault().getPreferenceStore().getBoolean(prefName);
    button.setSelection(check);
    button.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        boolean checked = button.getSelection();
        MavenPlugin.getDefault().getPreferenceStore().setValue(prefName, checked);
        scheduleSearch(searchText.getText(), false);
      }
    });
  }

  public void init(String queryText, String queryType, Set<ArtifactKey> artifacts) {
    this.queryType = queryType;

    if(queryText != null) {
      searchText.setText(queryText);
    }

    if(artifacts != null) {
      for(ArtifactKey a : artifacts) {
        artifactKeys.add(a.getGroupId() + ":" + a.getArtifactId()); //$NON-NLS-1$
        artifactKeys.add(a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getVersion()); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }

    searchResultViewer.setContentProvider(new SearchResultContentProvider());
    searchResultViewer.setLabelProvider(new SearchResultLabelProvider(artifactKeys, queryType));
    searchResultViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if(!selection.isEmpty()) {
          if(selection.size() == 1) {
            IndexedArtifactFile f = getSelectedIndexedArtifactFile(selection.getFirstElement());
            // int severity = artifactKeys.contains(f.group + ":" + f.artifact) ? IStatus.ERROR : IStatus.OK;
            int severity = IStatus.OK;
            String date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(f.date);
            setStatus(
                severity,
                NLS.bind(Messages.MavenPomSelectionComponent_detail1, f.fname,
                    (f.size != -1 ? NLS.bind(Messages.MavenPomSelectionComponent_details2, date, f.size) : date)));
          } else {
            setStatus(IStatus.OK, NLS.bind(Messages.MavenPomSelectionComponent_selected, selection.size()));
          }
        } else {
          setStatus(IStatus.ERROR, Messages.MavenPomSelectionComponent_nosel);
        }
      }
    });
    setupClassifiers();
    setStatus(IStatus.ERROR, ""); //$NON-NLS-1$
    scheduleSearch(queryText, false);
  }

  protected void setupClassifiers() {
    if(showClassifiers()) {
      Composite includesComp = new Composite(this, SWT.NONE);
      includesComp.setLayout(new GridLayout(3, true));
      GridData gd = new GridData(SWT.LEFT, SWT.TOP, true, false);
      includesComp.setLayoutData(gd);

      javadocCheckBox = new Button(includesComp, SWT.CHECK);
      setupButton(javadocCheckBox, Messages.MavenPomSelectionComponent_btnJavadoc, P_SEARCH_INCLUDE_JAVADOC, 0);

      sourcesCheckBox = new Button(includesComp, SWT.CHECK);
      setupButton(sourcesCheckBox, Messages.MavenPomSelectionComponent_btnSource, P_SEARCH_INCLUDE_SOURCES, 10);

      testCheckBox = new Button(includesComp, SWT.CHECK);
      setupButton(testCheckBox, Messages.MavenPomSelectionComponent_btnTests, P_SEARCH_INCLUDE_TESTS, 10);
    }
  }

  public IStatus getStatus() {
    return this.status;
  }

  public void addDoubleClickListener(IDoubleClickListener listener) {
    searchResultViewer.addDoubleClickListener(listener);
  }

  public void addSelectionChangedListener(ISelectionChangedListener listener) {
    this.selectionListener = listener;
  }

  void setStatus(int severity, String message) {
    this.status = new Status(severity, IMavenConstants.PLUGIN_ID, 0, message, null);
    if(selectionListener != null) {
      selectionListener.selectionChanged(new SelectionChangedEvent(searchResultViewer, searchResultViewer
          .getSelection()));
    }
  }

  public IndexedArtifact getIndexedArtifact() {
    IStructuredSelection selection = (IStructuredSelection) searchResultViewer.getSelection();
    Object element = selection.getFirstElement();
    if(element instanceof IndexedArtifact) {
      return (IndexedArtifact) element;
    }
    TreeItem[] treeItems = searchResultViewer.getTree().getSelection();
    if(treeItems.length == 0) {
      return null;
    }
    return (IndexedArtifact) treeItems[0].getParentItem().getData();
  }

  public IndexedArtifactFile getIndexedArtifactFile() {
    IStructuredSelection selection = (IStructuredSelection) searchResultViewer.getSelection();
    return getSelectedIndexedArtifactFile(selection.getFirstElement());
  }

  IndexedArtifactFile getSelectedIndexedArtifactFile(Object element) {
    if(element instanceof IndexedArtifact) {
      return ((IndexedArtifact) element).getFiles().iterator().next();
    }
    return (IndexedArtifactFile) element;
  }

  void scheduleSearch(String query, boolean delay) {
    if(query != null && query.length() > 2) {
      if(searchJob == null) {
        IndexManager indexManager = MavenPlugin.getDefault().getIndexManager();
        searchJob = new SearchJob(queryType, indexManager);
      } else {
        if(!searchJob.cancel()) {
          //for already running ones, just create new instance so that the previous one can piecefully die
          //without preventing the new one from completing first
          IndexManager indexManager = MavenPlugin.getDefault().getIndexManager();
          searchJob = new SearchJob(queryType, indexManager);
        }
      }
      searchJob.setQuery(query.toLowerCase());
      searchJob.schedule(delay ? LONG_DELAY : SHORT_DELAY);
    } else {
      if(searchJob != null) {
        searchJob.cancel();
      }
    }
  }

  /**
   * Search Job
   */
  private class SearchJob extends Job {

    private IndexManager indexManager;

    private String query;

    private String field;

    private volatile boolean stop = false;

    public SearchJob(String field, IndexManager indexManager) {
      super(Messages.MavenPomSelectionComponent_searchJob);
      this.field = field;
      this.indexManager = indexManager;
    }

    public void setQuery(String query) {
      this.query = query;
    }

    public boolean shouldRun() {
      stop = false;
      return super.shouldRun();
    }

    public int getClassifier() {
      int classifier = IIndex.SEARCH_JARS;
      if(MavenPlugin.getDefault().getPreferenceStore().getBoolean(P_SEARCH_INCLUDE_JAVADOC)) {
        classifier = classifier + IIndex.SEARCH_JAVADOCS;
      }
      if(MavenPlugin.getDefault().getPreferenceStore().getBoolean(P_SEARCH_INCLUDE_SOURCES)) {
        classifier = classifier + IIndex.SEARCH_SOURCES;
      }
      if(MavenPlugin.getDefault().getPreferenceStore().getBoolean(P_SEARCH_INCLUDE_TESTS)) {
        classifier = classifier + IIndex.SEARCH_TESTS;
      }
      return classifier;
    }

    protected IStatus run(IProgressMonitor monitor) {
      int classifier = showClassifiers() ? getClassifier() : IIndex.SEARCH_ALL;
      if(searchResultViewer == null || searchResultViewer.getControl() == null
          || searchResultViewer.getControl().isDisposed()) {
        return Status.CANCEL_STATUS;
      }
      if(query != null) {
        String activeQuery = query;
        try {
          setResult(IStatus.OK, NLS.bind(Messages.MavenPomSelectionComponent_searching, activeQuery.toLowerCase()),
              null);

          // TODO: cstamas identified this as "user input", true?
          Map<String, IndexedArtifact> res = indexManager.getAllIndexes().search( new UserInputSearchExpression(activeQuery), field, classifier);
          setResult(IStatus.OK, NLS.bind(Messages.MavenPomSelectionComponent_results, activeQuery, res.size()), res);
        } catch(BooleanQuery.TooManyClauses ex) {
          setResult(IStatus.ERROR, Messages.MavenPomSelectionComponent_toomany,
              Collections.<String, IndexedArtifact> emptyMap());
        } catch(final RuntimeException ex) {
          setResult(IStatus.ERROR, NLS.bind(Messages.MavenPomSelectionComponent_error, ex.toString()),
              Collections.<String, IndexedArtifact> emptyMap());
        } catch(final Exception ex) {
          setResult(IStatus.ERROR, NLS.bind(Messages.MavenPomSelectionComponent_error, ex.getMessage()),
              Collections.<String, IndexedArtifact> emptyMap());
        }
      }
      return Status.OK_STATUS;
    }

    protected void canceling() {
      stop = true;
    }

    private void setResult(final int severity, final String message, final Map<String, IndexedArtifact> result) {
      if(stop)
        return;
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          setStatus(severity, message);
          if(result != null) {
            if(!searchResultViewer.getControl().isDisposed()) {
              searchResultViewer.setInput(result);
            }
          }
        }
      });
    }
  }

  public static class SearchResultLabelProvider extends LabelProvider implements IColorProvider {
    private final Set<String> artifactKeys;

    private final String queryType;

    public SearchResultLabelProvider(Set<String> artifactKeys, String queryType) {
      this.artifactKeys = artifactKeys;
      this.queryType = queryType;
    }

    public String getText(Object element) {
      if(element instanceof IndexedArtifact) {
        IndexedArtifact a = (IndexedArtifact) element;
        String name = (a.getClassname() == null ? "" : a.getClassname() + "   " + a.getPackageName() + "   ") + a.getGroupId() + "   " + a.getArtifactId(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        return name;
      } else if(element instanceof IndexedArtifactFile) {
        IndexedArtifactFile f = (IndexedArtifactFile) element;
//        String displayName = getRepoDisplayName(f.repository);
        return f.version + " [" + f.type + (f.classifier != null ? ", " + f.classifier : "") + "]"; //unless there is something reasonably short " [" + displayName + "]";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      }
      return super.getText(element);
    }

    protected String getRepoDisplayName(String repo) {
      return repo;
    }

    public Color getForeground(Object element) {
      if(element instanceof IndexedArtifactFile) {
        IndexedArtifactFile f = (IndexedArtifactFile) element;
        if(artifactKeys.contains(f.group + ":" + f.artifact + ":" + f.version)) { //$NON-NLS-1$ //$NON-NLS-2$
          return Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
        }
      } else if(element instanceof IndexedArtifact) {
        IndexedArtifact i = (IndexedArtifact) element;
        if(artifactKeys.contains(i.getGroupId() + ":" + i.getArtifactId())) { //$NON-NLS-1$
          return Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
        }
      }
      return null;
    }

    public Color getBackground(Object element) {
      return null;
    }

    public Image getImage(Object element) {
      if(element instanceof IndexedArtifactFile) {
        IndexedArtifactFile f = (IndexedArtifactFile) element;
        if(f.sourcesExists == IIndex.PRESENT) {
          return MavenImages.IMG_VERSION_SRC;
        }
        return MavenImages.IMG_VERSION;
      } else if(element instanceof IndexedArtifact) {
        // IndexedArtifact i = (IndexedArtifact) element;
        return MavenImages.IMG_JAR;
      }
      return null;
    }

  }

  public static class SearchResultContentProvider implements ITreeContentProvider {
    private static Object[] EMPTY = new Object[0];

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    public Object[] getElements(Object inputElement) {
      if(inputElement instanceof Map) {
        return ((Map<?, ?>) inputElement).values().toArray();
      }
      return EMPTY;
    }

    public Object[] getChildren(Object parentElement) {
      if(parentElement instanceof IndexedArtifact) {
        IndexedArtifact a = (IndexedArtifact) parentElement;
        return a.getFiles().toArray();
      }
      return EMPTY;
    }

    public boolean hasChildren(Object element) {
      return element instanceof IndexedArtifact;
    }

    public Object getParent(Object element) {
      return null;
    }

    public void dispose() {

    }

  }
}
