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

package org.eclipse.m2e.editor.xml;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;


/**
 * Hello fellow tester: everytime this test finds a regression add an 'x' here: everytime you do mindless test update
 * add an 'y' here:
 * 
 * @author mkleint
 */

@SuppressWarnings("restriction")
public class WarnIfGroupSameThanParentTest extends AbstractMavenProjectTestCase {

  public void testMNGEclipse2552() throws Exception {
    IProject[] projects = importProjects("projects/MNGECLIPSE-2552", new String[] {
        "child2552withDuplicateGroupAndVersion/pom.xml", "child2552withDuplicateGroup/pom.xml",
        "child2552withDuplicateVersion/pom.xml", "parent2552/pom.xml",},//
        new ResolverConfiguration(),//
        true // skipeSanityCheck, some projects are expected to have errors after import
    );
    waitForJobsToComplete();

    {
      // "child2552withDuplicateGroupAndVersion/pom.xml"
      IProject project = projects[0];
      IMarker[] markers = XmlEditorHelpers.findEditorHintWarningMarkers(project).toArray(new IMarker[0]);
      assertEquals(2, markers.length);
      XmlEditorHelpers.assertEditorHintWarningMarker(IMavenConstants.MARKER_POM_LOADING_ID,
          IMavenConstants.EDITOR_HINT_PARENT_GROUP_ID, null /* message */, 8 /* lineNumber */, 1 /* resolutions */,
          markers[0]);
      XmlEditorHelpers.assertEditorHintWarningMarker(IMavenConstants.MARKER_POM_LOADING_ID,
          IMavenConstants.EDITOR_HINT_PARENT_VERSION, null /* message */, 10 /* lineNumber */, 1 /* resolutions */,
          markers[1]);
      // Fix the problem - the marker should be removed
      copyContent(project, "pom_good.xml", "pom.xml");
      XmlEditorHelpers.assertNoEditorHintWarningMarkers(project);
    }

    {
      // "child2552withDuplicateGroup/pom.xml",
      IProject project = projects[1];
      IMarker[] markers = XmlEditorHelpers.findEditorHintWarningMarkers(project).toArray(new IMarker[0]);
      assertEquals(1, markers.length);
      XmlEditorHelpers.assertEditorHintWarningMarker(IMavenConstants.MARKER_POM_LOADING_ID,
          IMavenConstants.EDITOR_HINT_PARENT_GROUP_ID, null /* message */, 8 /* lineNumber */, 1 /* resolutions */,
          markers[0]);
      // Fix the problem - the marker should be removed
      copyContent(project, "pom_good.xml", "pom.xml");
      XmlEditorHelpers.assertNoEditorHintWarningMarkers(project);
    }

    {
      // "child2552withDuplicateVersion/pom.xml"
      IProject project = projects[2];
      IMarker[] markers = XmlEditorHelpers.findEditorHintWarningMarkers(project).toArray(new IMarker[0]);
      assertEquals(1, markers.length);
      XmlEditorHelpers.assertEditorHintWarningMarker(IMavenConstants.MARKER_POM_LOADING_ID,
          IMavenConstants.EDITOR_HINT_PARENT_VERSION, null /* message */, 9 /* lineNumber */, 1 /* resolutions */,
          markers[0]);
      // Fix the problem - the marker should be removed
      copyContent(project, "pom_good.xml", "pom.xml");
      XmlEditorHelpers.assertNoEditorHintWarningMarkers(project);
    }
  }
}
