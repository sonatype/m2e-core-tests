/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
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

package org.eclipse.m2e.tests.ui.editing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;

import org.apache.maven.model.Dependency;

import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.ui.internal.editing.AddExclusionOperation;
import org.eclipse.m2e.core.ui.internal.editing.PomEdits;
import org.eclipse.m2e.core.ui.internal.editing.PomEdits.OperationTuple;


@SuppressWarnings("restriction")
public class AddExclusionOperationTest extends AbstractOperationTest {
  private IDOMModel tempModel;

  private Dependency d;

  private IStructuredDocument document;

  private ArtifactKey e;

  @Before
  public void setUp() throws Exception {
    tempModel = (IDOMModel) StructuredModelManager.getModelManager().createUnManagedStructuredModelFor(
        "org.eclipse.m2e.core.pomFile");
    document = tempModel.getStructuredDocument();

    d = new Dependency();
    d.setArtifactId("BBBB");
    d.setGroupId("AAA");
    d.setVersion("1.0");

    e = new ArtifactKey("g", "a", "1.0", null);
  }

  @Test
  public void testMissingDependency_noDependenciesElement() throws Exception {
    document.setText(StructuredModelManager.getModelManager(), //
        "<project></project>");
    PomEdits.performOnDOMDocument(new OperationTuple(tempModel, new AddExclusionOperation(d, e)));
    assertEquals("Expected no dependency: " + d.toString() + "\n" + document.getText(), 0,
        dependencyCount(tempModel, d));
  }

  @Test
  public void testMissingDependency_emptyDependenciesElement() throws Exception {
    document.setText(StructuredModelManager.getModelManager(), //
        "<project><dependencies>" + //
            "</dependencies></project>");
    PomEdits.performOnDOMDocument(new OperationTuple(tempModel, new AddExclusionOperation(d, e)));
    assertEquals("Expected no dependency: " + d.toString() + "\n" + document.getText(), 0,
        dependencyCount(tempModel, d));
  }

  @Test
  public void testMissingDependency_withDependencies() throws Exception {
    document.setText(StructuredModelManager.getModelManager(), //
        "<project><dependencies>" + //
            "<dependency><groupId>AAA</groupId><artifactId>BBB</artifactId><version>1.0</version></dependency>" + //
            "<dependency><groupId>AAAB</groupId><artifactId>BBB</artifactId><version>1.0</version></dependency>" + //
            "</dependencies></project>");
    PomEdits.performOnDOMDocument(new OperationTuple(tempModel, new AddExclusionOperation(d, e)));
    assertEquals("Expected no dependency: " + d.toString() + "\n" + document.getText(), 0,
        dependencyCount(tempModel, d));
    assertEquals("Dependency Count: \n" + document.getText(), 2, getDependencyCount(tempModel));
  }

  @Test
  public void testAddExclusion() throws Exception {
    document.setText(StructuredModelManager.getModelManager(), //
        "<project><dependencies>" + //
            "<dependency><groupId>AAA</groupId><artifactId>BBB</artifactId><version>1.0</version></dependency>" + //
            "<dependency><groupId>AAAB</groupId><artifactId>BBB</artifactId><version>1.0</version></dependency>" + //
            "<dependency><groupId>AAA</groupId><artifactId>BBBB</artifactId><version>1.0</version></dependency>" + //
            "</dependencies></project>");
    PomEdits.performOnDOMDocument(new OperationTuple(tempModel, new AddExclusionOperation(d, e)));
    assertEquals("Expected no dependency: " + d.toString() + "\n" + document.getText(), 1,
        dependencyCount(tempModel, d));
    assertTrue("Has exclusion " + e.toString() + "\n" + document.getText(), hasExclusion(tempModel, d, e));
    assertEquals("Exclusions", 1, getExclusionCount(tempModel, d));
    assertEquals("Dependency Count: \n" + document.getText(), 3, getDependencyCount(tempModel));
  }

  @Test
  public void testAddExclusion_duplicateExclusion() throws Exception {
    document
        .setText(
            StructuredModelManager.getModelManager(), //
            "<project><dependencies>"
                + //
                "<dependency><groupId>AAA</groupId><artifactId>BBB</artifactId><version>1.0</version></dependency>"
                + //
                "<dependency><groupId>AAAB</groupId><artifactId>BBB</artifactId><version>1.0</version></dependency>"
                + //
                "<dependency><groupId>AAA</groupId><artifactId>BBBB</artifactId><version>1.0</version>"
                + //
                "<exclusions><exclusion><groupId>g</groupId><artifactId>a</artifactId><version>1.0</version></exclusion></exclusions></dependency>"
                + //
                "</dependencies></project>");
    PomEdits.performOnDOMDocument(new OperationTuple(tempModel, new AddExclusionOperation(d, e)));
    assertEquals("Expected no dependency: " + d.toString() + "\n" + document.getText(), 1,
        dependencyCount(tempModel, d));
    assertTrue("Has exclusion " + e.toString() + "\n" + document.getText(), hasExclusion(tempModel, d, e));
    assertEquals("Exclusions", 1, getExclusionCount(tempModel, d));
    assertEquals("Dependency Count: \n" + document.getText(), 3, getDependencyCount(tempModel));
  }

  @Test
  public void testAddExclusion_existingExclusion() throws Exception {
    document
        .setText(
            StructuredModelManager.getModelManager(), //
            "<project><dependencies>"
                + //
                "<dependency><groupId>AAA</groupId><artifactId>BBB</artifactId><version>1.0</version></dependency>"
                + //
                "<dependency><groupId>AAAB</groupId><artifactId>BBB</artifactId><version>1.0</version></dependency>"
                + //
                "<dependency><groupId>AAA</groupId><artifactId>BBBB</artifactId><version>1.0</version>"
                + //
                "<exclusions><exclusion><groupId>g</groupId><artifactId>b</artifactId><version>1.0</version></exclusion></exclusions></dependency>"
                + //
                "</dependencies></project>");
    PomEdits.performOnDOMDocument(new OperationTuple(tempModel, new AddExclusionOperation(d, e)));
    assertEquals("Expected no dependency: " + d.toString() + "\n" + document.getText(), 1,
        dependencyCount(tempModel, d));
    assertTrue("Has exclusion " + e.toString() + "\n" + document.getText(), hasExclusion(tempModel, d, e));

    ArtifactKey key = new ArtifactKey("g", "b", "1.0", null);
    assertTrue("Existing Exclusion Present " + key.toString() + "\n" + document.getText(),
        hasExclusion(tempModel, d, key));
    assertEquals("Exclusions", 2, getExclusionCount(tempModel, d));
    assertEquals("Dependency Count: \n" + document.getText(), 3, getDependencyCount(tempModel));
  }
}
