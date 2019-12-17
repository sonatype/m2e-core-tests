
package org.eclipse.m2e.tests.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;

import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ILifecycleMapping;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.eclipse.m2e.core.project.configurator.MojoExecutionKey;
import org.eclipse.m2e.tests.common.AbstractLifecycleMappingTest;
import org.eclipse.m2e.tests.common.WorkspaceHelpers;
import org.eclipse.m2e.tests.configurators.TestProjectConfigurator;


public class PluginExecutionActionsTest extends AbstractLifecycleMappingTest {
  public void testMojoExecutionIgnore() throws Exception {
    IMavenProjectFacade facade = importMavenProject(
        "projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionActionsTest",
        "testMojoExecutionIgnore/pom.xml");
    assertNotNull("Expected not null MavenProjectFacade", facade);
    IProject project = facade.getProject();
    WorkspaceHelpers.assertNoErrors(project);
    WorkspaceHelpers.assertNoWarnings(project);

    ILifecycleMapping lifecycleMapping = projectConfigurationManager.getLifecycleMapping(facade);
    assertNotNull(lifecycleMapping);

    List<AbstractProjectConfigurator> configurators = lifecycleMapping.getProjectConfigurators(facade, monitor);
    assertEquals(configurators.toString(), 0, configurators.size());

    Map<MojoExecutionKey, List<AbstractBuildParticipant>> buildParticipants = lifecycleMapping.getBuildParticipants(
        facade, monitor);
    assertEquals(buildParticipants.keySet().toString(), 1, buildParticipants.size()); // only one mojo execution
    assertEquals(0, buildParticipants.values().iterator().next().size()); // no build participants
  }

  public void testMojoExecutionIgnoreWithMessage() throws Exception {
    IMavenProjectFacade facade = importMavenProject(
        "projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionActionsTest",
        "testMojoExecutionIgnoreWithMessage/pom.xml");
    assertNotNull("Expected not null MavenProjectFacade", facade);
    IProject project = facade.getProject();
    WorkspaceHelpers.assertNoErrors(project);

    ILifecycleMapping lifecycleMapping = projectConfigurationManager.getLifecycleMapping(facade);
    assertNotNull(lifecycleMapping);

    List<AbstractProjectConfigurator> configurators = lifecycleMapping.getProjectConfigurators(facade, monitor);
    assertEquals(configurators.toString(), 0, configurators.size());

    Map<MojoExecutionKey, List<AbstractBuildParticipant>> buildParticipants = lifecycleMapping.getBuildParticipants(
        facade, monitor);
    assertEquals(buildParticipants.keySet().toString(), 1, buildParticipants.size()); // only one mojo execution
    assertEquals(0, buildParticipants.values().iterator().next().size()); // no build participants

    WorkspaceHelpers.assertWarningMarker(IMavenConstants.MARKER_LIFECYCLEMAPPING_ID,
        "Ignore plugin execution test message", 17 /*lineNumber*/, "pom.xml", project);
  }

  public void testMojoExecutionExecute() throws Exception {
    IMavenProjectFacade facade = importMavenProject(
        "projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionActionsTest",
        "testMojoExecutionExecute/pom.xml");
    assertNotNull("Expected not null MavenProjectFacade", facade);
    IProject project = facade.getProject();
    WorkspaceHelpers.assertNoErrors(project);
    WorkspaceHelpers.assertNoWarnings(project);

    ILifecycleMapping lifecycleMapping = projectConfigurationManager.getLifecycleMapping(facade);
    assertNotNull(lifecycleMapping);

    List<AbstractProjectConfigurator> configurators = lifecycleMapping.getProjectConfigurators(facade, monitor);
    assertEquals(configurators.toString(), 0, configurators.size());

    Map<MojoExecutionKey, List<AbstractBuildParticipant>> buildParticipants = lifecycleMapping.getBuildParticipants(
        facade, monitor);
    assertEquals(buildParticipants.keySet().toString(), 1, buildParticipants.size()); // only one mojo execution
    assertEquals(1, buildParticipants.values().iterator().next().size()); // one 
    assertTrue(buildParticipants.values().iterator().next().get(0) instanceof MojoExecutionBuildParticipant);
  }

  public void testMojoExecutionExecuteWithMessage() throws Exception {
    IMavenProjectFacade facade = importMavenProject(
        "projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionActionsTest",
        "testMojoExecutionExecuteWithMessage/pom.xml");
    assertNotNull("Expected not null MavenProjectFacade", facade);
    IProject project = facade.getProject();
    WorkspaceHelpers.assertNoErrors(project);

    ILifecycleMapping lifecycleMapping = projectConfigurationManager.getLifecycleMapping(facade);
    assertNotNull(lifecycleMapping);

    List<AbstractProjectConfigurator> configurators = lifecycleMapping.getProjectConfigurators(facade, monitor);
    assertEquals(configurators.toString(), 0, configurators.size());

    Map<MojoExecutionKey, List<AbstractBuildParticipant>> buildParticipants = lifecycleMapping.getBuildParticipants(
        facade, monitor);
    assertEquals(buildParticipants.keySet().toString(), 1, buildParticipants.size()); // only one mojo execution
    assertEquals(1, buildParticipants.values().iterator().next().size()); // one 
    assertTrue(buildParticipants.values().iterator().next().get(0) instanceof MojoExecutionBuildParticipant);

    WorkspaceHelpers.assertWarningMarker(IMavenConstants.MARKER_LIFECYCLEMAPPING_ID,
        "Execute plugin execution test message", 18 /*lineNumber*/, "pom.xml", project);
  }

  public void testMojoExecutionConfigurator() throws Exception {
    IMavenProjectFacade facade = importMavenProject(
        "projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionActionsTest",
        "testMojoExecutionConfigurator/pom.xml");
    assertNotNull("Expected not null MavenProjectFacade", facade);
    IProject project = facade.getProject();
    WorkspaceHelpers.assertNoErrors(project);
    WorkspaceHelpers.assertNoWarnings(project);

    ILifecycleMapping lifecycleMapping = projectConfigurationManager.getLifecycleMapping(facade);
    assertNotNull(lifecycleMapping);

    List<AbstractProjectConfigurator> configurators = lifecycleMapping.getProjectConfigurators(facade, monitor);
    assertEquals(configurators.toString(), 1, configurators.size());
    assertTrue(configurators.get(0) instanceof TestProjectConfigurator);
  }

  public void testMojoExecutionError() throws Exception {
    IMavenProjectFacade facade = importMavenProject(
        "projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionActionsTest",
        "testMojoExecutionError/pom.xml");
    assertNotNull("Expected not null MavenProjectFacade", facade);
    IProject project = facade.getProject();
    WorkspaceHelpers.assertNoWarnings(project);

    ILifecycleMapping lifecycleMapping = projectConfigurationManager.getLifecycleMapping(facade);
    assertNotNull(lifecycleMapping);

    List<AbstractProjectConfigurator> configurators = lifecycleMapping.getProjectConfigurators(facade, monitor);
    assertEquals(configurators.toString(), 0, configurators.size());

    Map<MojoExecutionKey, List<AbstractBuildParticipant>> buildParticipants = lifecycleMapping.getBuildParticipants(
        facade, monitor);
    assertEquals(buildParticipants.keySet().toString(), 1, buildParticipants.size()); // only one mojo execution
    assertEquals(0, buildParticipants.values().iterator().next().size());

    WorkspaceHelpers
        .assertErrorMarker(
            IMavenConstants.MARKER_LIFECYCLEMAPPING_ID,
            "Plugin execution marked as error in lifecycle configuration: org.eclipse.m2e.test.lifecyclemapping:test-lifecyclemapping-plugin:1.0.0:test-goal-1 (execution: standard, phase: compile)",
            18 /*lineNumber*/, "pom.xml", project);
  }

  public void testMojoExecutionErrorWithMessage() throws Exception {
    IMavenProjectFacade facade = importMavenProject(
        "projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionActionsTest",
        "testMojoExecutionErrorWithMessage/pom.xml");
    assertNotNull("Expected not null MavenProjectFacade", facade);
    IProject project = facade.getProject();
    WorkspaceHelpers.assertNoWarnings(project);

    ILifecycleMapping lifecycleMapping = projectConfigurationManager.getLifecycleMapping(facade);
    assertNotNull(lifecycleMapping);

    List<AbstractProjectConfigurator> configurators = lifecycleMapping.getProjectConfigurators(facade, monitor);
    assertEquals(configurators.toString(), 0, configurators.size());

    Map<MojoExecutionKey, List<AbstractBuildParticipant>> buildParticipants = lifecycleMapping.getBuildParticipants(
        facade, monitor);
    assertEquals(buildParticipants.keySet().toString(), 1, buildParticipants.size()); // only one mojo execution
    assertEquals(0, buildParticipants.values().iterator().next().size());

    WorkspaceHelpers.assertErrorMarker(IMavenConstants.MARKER_LIFECYCLEMAPPING_ID,
        "Error plugin execution test message", 18 /*lineNumber*/, "pom.xml", project);
  }
}
