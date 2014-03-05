/*************************************************************************************
 * Copyright (c) 2011-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/

package org.eclipse.m2e.tests.profiles;

import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import org.eclipse.core.resources.IProject;

import org.apache.maven.model.Profile;

import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.profiles.core.internal.ProfileData;
import org.eclipse.m2e.profiles.core.internal.ProfileState;


@SuppressWarnings("restriction")
public class MavenProfileManagerTest extends AbstractMavenProfileTest {

  @Test
  public void testUserSelectedProfiles() throws Exception {
    String pomPath = "projects/profiles/embedded-profiles/pom.xml";
    IProject project = importProject(pomPath);
    waitForJobsToComplete();
    assertNotNull(pomPath + " could not be imported", project);

    IMavenProjectFacade facade = getFacade(project);
    List<ProfileData> profiles = profileManager.getProfileDatas(facade, monitor);
    assertEquals(3, facade.getMavenProject(monitor).getActiveProfiles().size());
    assertEquals(profiles.toString(), 5, profiles.size());

    //Check default profile status when no user profile is selected
    for(ProfileData p : profiles) {
      String pid = p.getId();
      if("active-by-default".equals(pid)) {
        assertTrue(p.isAutoActive());//has <activeByDefault>true</activeByDefault>
        assertEquals(ProfileState.Active, p.getActivationState());
        assertFalse(p.isUserSelected());
      } else if("inactive-settings-profile".equals(pid)) {
        assertFalse(p.isAutoActive());
        assertEquals(ProfileState.Inactive, p.getActivationState());
        assertFalse(p.isUserSelected());
      } else if("active-settings-profile".equals(pid)) {
        assertTrue(p.isAutoActive());
        assertEquals(ProfileState.Active, p.getActivationState());
        assertFalse(p.isUserSelected());
      } else if("activebydefault-settings-profile".equals(pid)) {
        assertTrue(p.isAutoActive());
        assertEquals(ProfileState.Active, p.getActivationState());
        assertFalse(p.isUserSelected());
      } else if("my-profile".equals(pid)) {
        assertFalse(p.isAutoActive());
        assertEquals(ProfileState.Inactive, p.getActivationState());
        assertFalse(p.isUserSelected());
      }
    }

    profileManager.updateActiveProfiles(facade, Arrays.asList("my-profile"), true, false, monitor);

    facade = getFacade(project);
    assertEquals(3, facade.getMavenProject(monitor).getActiveProfiles().size());

    profiles = profileManager.getProfileDatas(facade, monitor);
    //When a user manually selects a profile, all profiles enabled by default in the pom 
    // are rendered inactive, the ones from settings are still active
    for(ProfileData p : profiles) {
      String pid = p.getId();
      if("active-by-default".equals(pid)) {
        // <activeByDefault> profiles are disabled when user explicitly use profiles
        assertFalse(p.isAutoActive());
        assertEquals(ProfileState.Inactive, p.getActivationState());
        assertFalse(p.isUserSelected());
      } else if("inactive-settings-profile".equals(pid)) {
        assertFalse(p.isAutoActive());
        assertEquals(ProfileState.Inactive, p.getActivationState());
        assertFalse(p.isUserSelected());
      } else if("active-settings-profile".equals(pid)) {
        assertTrue(p.isAutoActive());
        assertEquals(ProfileState.Active, p.getActivationState());
        assertFalse(p.isUserSelected());
      } else if("activebydefault-settings-profile".equals(pid)) {
        assertTrue(p.isAutoActive());
        assertEquals(ProfileState.Active, p.getActivationState());
        assertFalse(p.isUserSelected());
      } else if("my-profile".equals(pid)) {
        assertFalse(p.isAutoActive());
        assertEquals(ProfileState.Active, p.getActivationState());
        assertTrue(p.isUserSelected());
      }
    }

  }

  @Test
  public void testDisabledProfiles() throws Exception {
    String pomPath = "projects/profiles/disabled-profiles/pom.xml";
    IProject project = importProject(pomPath);
    waitForJobsToComplete();
    assertNotNull(pomPath + " could not be imported", project);

    IMavenProjectFacade facade = getFacade(project);

    List<ProfileData> profiles = profileManager.getProfileDatas(facade, monitor);

    assertEquals(profiles.toString(), 4, profiles.size());
    for(ProfileData p : profiles) {
      assertNotEquals(ProfileState.Disabled, p.getActivationState());
    }
    List<String> changedProfiles = Arrays
        .asList("!test-disabled-profile, !active-settings-profile, inactive-settings-profile,!activebydefault-settings-profile");

    profileManager.updateActiveProfiles(facade, changedProfiles, true, false, monitor);

    facade = getFacade(project);

    assertEquals(1, facade.getMavenProject(monitor).getActiveProfiles().size());

    profiles = profileManager.getProfileDatas(facade, monitor);

    for(ProfileData p : profiles) {
      String pid = p.getId();
      if("inactive-settings-profile".equals(pid)) {
        assertEquals(p.toString(), ProfileState.Active, p.getActivationState());
      } else {
        assertEquals(p.toString(), ProfileState.Disabled, p.getActivationState());
      }
      assertTrue(p.isUserSelected());
    }
  }

  @Test
  public void testParentFromRemote() throws Exception {
    String pomPath = "projects/profiles/profiles-from-parent/pom.xml";
    IProject project = importProject(pomPath);
    waitForJobsToComplete();
    assertNotNull(pomPath + " could not be imported", project);

    IMavenProjectFacade facade = getFacade(project);
    List<ProfileData> profiles = profileManager.getProfileDatas(facade, monitor);
    assertEquals(profiles.toString(), 5, profiles.size());
    for(ProfileData p : profiles) {
      String pid = p.getId();
      if("other-parent-profile".equals(pid)) {
        assertFalse(p.isAutoActive());//parent profile activation is not inherited
        assertEquals(ProfileState.Inactive, p.getActivationState());
      } else if("inactive-settings-profile".equals(pid)) {
        assertFalse(p.isAutoActive());
        assertEquals(ProfileState.Inactive, p.getActivationState());
      } else if("active-settings-profile".equals(pid)) {
        assertTrue(p.isAutoActive());
        assertEquals(ProfileState.Active, p.getActivationState());
      } else if("activebydefault-settings-profile".equals(pid)) {
        assertTrue(p.isAutoActive());
        assertEquals(ProfileState.Active, p.getActivationState());
      } else if("parent-profile".equals(pid)) {
        assertFalse(p.isAutoActive());
      } else {
        fail("Unexpected profile " + pid);
      }
    }
  }

  @Test
  public void testGetAvailableSettingsProfiles() throws Exception {
    Map<Profile, Boolean> profiles = profileManager.getAvailableSettingsProfiles();
    assertEquals(3, profiles.size());
    for(Entry<Profile, Boolean> p : profiles.entrySet()) {
      String pid = p.getKey().getId();
      if("inactive-settings-profile".equals(pid)) {
        assertFalse(p.getValue());
      } else if("active-settings-profile".equals(pid)) {
        assertTrue(p.getValue());
      } else if("activebydefault-settings-profile".equals(pid)) {
        assertTrue(p.getValue());
      } else {
        fail("Unexpected profile " + pid);
      }
    }
  }
}
