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

package org.eclipse.m2e.core.internal.index;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.index.IIndex;
import org.eclipse.m2e.core.index.IndexedArtifact;
import org.eclipse.m2e.core.index.IndexedArtifactFile;
import org.eclipse.m2e.core.index.SearchExpression;


/**
 * CompositeIndex
 * 
 * @author igor
 */
public class CompositeIndex implements IIndex {

  private List<IIndex> indexes;

  public CompositeIndex(List<IIndex> indexes) {
    this.indexes = indexes;
  }

  public IndexedArtifactFile getIndexedArtifactFile(ArtifactKey artifact) throws CoreException {
    for(IIndex index : indexes) {
      IndexedArtifactFile aif = index.getIndexedArtifactFile(artifact);
      if(aif != null) {
        // first one wins
        return aif;
      }
    }

    // did not find anything
    return null;
  }

  public IndexedArtifactFile identify(File file) throws CoreException {
    for(IIndex index : indexes) {
      IndexedArtifactFile aif = index.identify(file);
      if(aif != null) {
        // first one wins
        return aif;
      }
    }

    // did not find anything
    return null;
  }

  public Collection<IndexedArtifact> find(SearchExpression groupId, SearchExpression artifactId, SearchExpression version, SearchExpression packaging)
      throws CoreException {
    Set<IndexedArtifact> result = new LinkedHashSet<IndexedArtifact>();
    for(IIndex index : indexes) {
      Collection<IndexedArtifact> findResults = index.find(groupId, artifactId, version, packaging);
      if(findResults != null) {
        result.addAll(findResults);
      }
    }
    return result;
  }

  public Map<String, IndexedArtifact> search(SearchExpression term, String searchType) throws CoreException {
    Map<String, IndexedArtifact> result = new HashMap<String, IndexedArtifact>();
    for(IIndex index : indexes) {
      Map<String, IndexedArtifact> iresult = index.search(term, searchType);
      if(iresult != null) {
        result.putAll(iresult);
      }
    }
    return result;
  }

  public Map<String, IndexedArtifact> search(SearchExpression term, String searchType, int classifier) throws CoreException {
    Map<String, IndexedArtifact> result = new HashMap<String, IndexedArtifact>();
    for(IIndex index : indexes) {
      Map<String, IndexedArtifact> iresult = index.search(term, searchType, classifier);
      if(iresult != null) {
        result.putAll(iresult);
      }
    }
    return result;
  }

}
