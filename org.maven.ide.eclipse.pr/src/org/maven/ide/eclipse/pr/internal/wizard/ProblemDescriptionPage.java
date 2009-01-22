/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.pr.internal.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.maven.ide.eclipse.pr.internal.ProblemReportingImages;
import org.maven.ide.eclipse.wizards.AbstractMavenWizardPage;


/**
 * A problem description page
 * 
 * @author Anton Kraev
 */
public class ProblemDescriptionPage extends AbstractMavenWizardPage {

  String description = "";
  String summary = "";

  protected ProblemDescriptionPage() {
    super("problemDescriptionPage");
    setTitle("Problem details");
    setDescription("Enter problem summary and description");
    setImageDescriptor(ProblemReportingImages.REPORT_WIZARD);
    setPageComplete(false);
  }

  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout());
    setControl(composite);

    Label summaryLabel = new Label(composite, SWT.NONE);
    summaryLabel.setData("name", "summaryLabel");
    summaryLabel.setText("Problem &summary:");

    final Text summaryText = new Text(composite, SWT.BORDER);
    summaryText.setData("name", "summaryText");
    summaryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    summaryText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        summary = summaryText.getText();
        updatePage();
      }
    });

    Label descriptionLabel = new Label(composite, SWT.NONE);
    descriptionLabel.setData("name", "descriptionLabel");
    descriptionLabel.setText("Problem &description:");

    final Text descriptionText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.WRAP);
    descriptionText.setData("name", "descriptionText");
    descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    descriptionText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        description = descriptionText.getText();
        updatePage();
      }
    });

  }

  protected void updatePage() {
    boolean isSummaryBlank = summary.trim().length()==0;
    boolean isDescriptionBlank = description.trim().length()==0;
    boolean isComplete = true;
    
    if(isSummaryBlank) {
      if(isDescriptionBlank) {
        setErrorMessage("Problem summary and description should not be blank");
      } else {
        setErrorMessage("Problem summary should not be blank");
      }
      isComplete = false;
    } else if(isDescriptionBlank) {
      setErrorMessage("Problem description should not be blank");
      isComplete = false;
    }
    
    setPageComplete(isComplete);
    if(isComplete) {
      setErrorMessage(null);
    }
  }

  public String getProblemSummary() {
    return summary.trim();
  }

  public String getProblemDescription() {
    return description.trim();
  }

}