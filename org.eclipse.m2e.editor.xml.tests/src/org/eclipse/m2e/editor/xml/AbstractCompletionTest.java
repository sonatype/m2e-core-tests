/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Sonatype, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.m2e.editor.xml;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;
import org.junit.After;
import org.junit.Before;

@SuppressWarnings("restriction")
public abstract class AbstractCompletionTest extends AbstractPOMEditorTestCase {
  protected ICompletionProposalComputer xmlContentAssistProcessor = null;

  protected IFile loadProjectsAndFiles() throws Exception {
    return null;
  }

  @Before
public void setUp() throws Exception {
    super.setUp();
    xmlContentAssistProcessor = new PomContentAssistProcessor();
    xmlContentAssistProcessor.sessionStarted();
  }

  @After
	public void tearDown() throws Exception {
    try {
      xmlContentAssistProcessor.sessionEnded();
    } finally {
      super.tearDown();
    }
  }

  @SuppressWarnings("unchecked")
  protected List<ICompletionProposal> getProposals(int offset) throws Exception {
    return xmlContentAssistProcessor.computeCompletionProposals(
      new CompletionProposalInvocationContext(sourceViewer, offset), 
      new NullProgressMonitor());
  }
}
