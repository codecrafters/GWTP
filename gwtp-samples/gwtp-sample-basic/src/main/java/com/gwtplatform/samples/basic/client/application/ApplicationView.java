/**
 * Copyright 2011 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.gwtplatform.samples.basic.client.application;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * @author Philippe Beaudoin
 */
public class ApplicationView extends ViewImpl implements ApplicationPresenter.MyView {
  /**
   * Main View.
   */
  public interface Binder extends UiBinder<Widget, ApplicationView> {
  }

  @UiField
  Label errorLabel;
  @UiField
  TextBox nameField;
  @UiField
  Button sendButton;

  @Inject
  public ApplicationView(final Binder binder) {
    initWidget(binder.createAndBindUi(this));
  }

  @Override
  public String getName() {
    return nameField.getText();
  }

  @Override
  public Button getSendButton() {
    return sendButton;
  }

  @Override
  public void resetAndFocus() {
    // Focus the cursor on the name field when the app loads
    nameField.setFocus(true);
    nameField.selectAll();
  }

  @Override
  public void setError(String errorText) {
    errorLabel.setText(errorText);
  }

}
