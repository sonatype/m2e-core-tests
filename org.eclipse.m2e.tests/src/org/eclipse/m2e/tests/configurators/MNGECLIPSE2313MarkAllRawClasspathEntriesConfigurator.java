/*******************************************************************************
 * Copyright (c) 2008-2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Sonatype, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.m2e.tests.configurators;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IClasspathEntryDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;


public class MNGECLIPSE2313MarkAllRawClasspathEntriesConfigurator extends AbstractProjectConfigurator implements
    IJavaProjectConfigurator {

  public static final IClasspathAttribute ATTR = JavaCore.newClasspathAttribute(
      MNGECLIPSE2313MarkAllRawClasspathEntriesConfigurator.class.getName(), "bar");

  public MNGECLIPSE2313MarkAllRawClasspathEntriesConfigurator() {
  }

  public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) {
  }

  public void configureClasspath(IMavenProjectFacade facade, IClasspathDescriptor classpath, IProgressMonitor monitor) {
  }

  public void configureRawClasspath(ProjectConfigurationRequest request, IClasspathDescriptor classpath,
      IProgressMonitor monitor) {

    for(IClasspathEntryDescriptor entry : classpath.getEntryDescriptors()) {
      entry.setClasspathAttribute(ATTR.getName(), ATTR.getValue());
    }

  }

}
