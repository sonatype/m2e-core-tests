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

package org.eclipse.m2e.core.index;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.m2e.core.embedder.ArtifactKey;


/**
 * @author igor
 */
public interface IMutableIndex extends IIndex {

  // index content manipulation

  public void addArtifact(File pomFile, ArtifactKey artifactKey);

  public void removeArtifact(File pomFile, ArtifactKey artifactKey);

  // reindexing

  public void updateIndex(boolean force, IProgressMonitor monitor) throws CoreException;

}
