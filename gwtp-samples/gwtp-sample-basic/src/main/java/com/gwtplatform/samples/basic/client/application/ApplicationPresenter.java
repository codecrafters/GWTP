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

import com.google.gwt.user.client.ui.Button;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;
import com.gwtplatform.samples.basic.client.application.response.ResponsePresenter;
import com.gwtplatform.samples.basic.shared.FieldVerifier;
import com.gwtplatform.samples.basic.shared.place.NameTokens;

/**
 * @author Philippe Beaudoin
 */
public class ApplicationPresenter extends Presenter<ApplicationPresenter.MyView, ApplicationPresenter.MyProxy> {
  /**
   * Main View.
   */
  public interface MyView extends View {
    String getName();

    Button getSendButton();

    void resetAndFocus();

    void setError(String errorText);
  }

  /**
   * Main Place.
   */
  @ProxyStandard
  @NameToken(NameTokens.home)
  public interface MyProxy extends Proxy<ApplicationPresenter>, Place {
  }

  private final PlaceManager placeManager;

  /**
   * Main Presenter.
   */
  @Inject
  public ApplicationPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
      PlaceManager placeManager) {
    super(eventBus, view, proxy);

    this.placeManager = placeManager;
  }

  @Override
  protected void onReset() {
    super.onReset();
    getView().resetAndFocus();
  }

  @Override
  protected void revealInParent() {
    RevealRootContentEvent.fire(this, this);
  }

  /**
   * Send the name from the nameField to the server and wait for a response.
   */
  private void sendNameToServer() {
    getView().setError("");
    String textToServer = getView().getName();
    if (!FieldVerifier.isValidName(textToServer)) {
      getView().setError("Please enter at least four characters");
      return;
    }

    // Then, we transmit it to the ResponsePresenter, which will do the server call
    placeManager.revealPlace(new PlaceRequest(ResponsePresenter.nameToken).with(
        ResponsePresenter.textToServerParam, textToServer));
  }
}
