package com.gwtplatform.samples.basic.client.application;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.samples.basic.client.application.response.ResponsePresenter;
import com.gwtplatform.samples.basic.client.application.response.ResponseView;

/**
 * Applications injection configuration.
 */
public class ApplicationModule extends AbstractPresenterModule {
  @Override
  public void configure() {
    // Application Presenter
    bindPresenter(ApplicationPresenter.class, ApplicationPresenter.MyView.class,
        ApplicationView.class, ApplicationPresenter.MyProxy.class);

    // ResponsePresenter
    bindPresenter(ResponsePresenter.class, ResponsePresenter.MyView.class,
        ResponseView.class, ResponsePresenter.MyProxy.class);
  }
}
