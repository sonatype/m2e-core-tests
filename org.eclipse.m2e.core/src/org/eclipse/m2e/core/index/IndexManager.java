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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;


public interface IndexManager {

  // well-known indexes

  String LOCAL_INDEX = "local"; //$NON-NLS-1$

  String WORKSPACE_INDEX = "workspace"; //$NON-NLS-1$

  // 

  IMutableIndex getWorkspaceIndex();

  IMutableIndex getLocalIndex();

  /**
   * For Maven projects, returns index of all repositories configured for the project. Index includes repositories
   * defined in the project pom.xml, inherited from parent projects and defined in enabled profiles in settings.xml. If
   * project is null or is not a maven project, returns index that includes repositories defined in profiles enabled by
   * default in settings.xml.
   */
  IIndex getIndex(IProject project) throws CoreException;

  /**
   * Returns index aggregating all indexes enabled for repositories defined in settings.xml
   * 
   * @return
   * @throws CoreException
   */
  IIndex getAllIndexes() throws CoreException;

  //

  void removeIndexListener(IndexListener listener);

  void addIndexListener(IndexListener listener);
}
