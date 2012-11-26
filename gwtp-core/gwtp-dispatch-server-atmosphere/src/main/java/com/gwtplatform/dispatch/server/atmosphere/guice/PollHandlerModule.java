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

package com.gwtplatform.dispatch.server.atmosphere.guice;

import com.google.inject.AbstractModule;
import com.google.inject.internal.UniqueAnnotations;
import com.gwtplatform.dispatch.server.actionhandlervalidator.ActionHandlerValidatorClass;
import com.gwtplatform.dispatch.server.actionhandlervalidator.ActionHandlerValidatorMap;
import com.gwtplatform.dispatch.server.actionhandlervalidator.ActionHandlerValidatorMapImpl;
import com.gwtplatform.dispatch.server.actionvalidator.ActionValidator;
import com.gwtplatform.dispatch.server.atmosphere.actionhandler.PollActionHandler;
import com.gwtplatform.dispatch.server.atmosphere.AbstractPollDispatchServiceImpl;
import com.gwtplatform.dispatch.server.atmosphere.PollDispatch;
import com.gwtplatform.dispatch.server.guice.DispatchModule;
import com.gwtplatform.dispatch.server.guice.actionvalidator.DefaultActionValidator;
import com.gwtplatform.dispatch.shared.Action;
import com.gwtplatform.dispatch.shared.Result;

/**
 * Base module that will bind {@link Action}s to {@link ActionHandler}s and
 * {@link ActionValidator}s. Your own Guice modules should extend this class.
 *
 * @author Christian Goudreau
 * @author David Peterson
 */
public abstract class PollHandlerModule extends AbstractModule {

  private final DispatchModule dispatchModule;

  /**
   * Constructs a HandlerModule that uses the {@link DispatchModule} with
   * default configuration.
   */
  public PollHandlerModule() {
    this.dispatchModule = new DispatchModule();
  }

  /**
   * Constructs a {@link com.gwtplatform.dispatch.server.guice.HandlerModule} that uses the {@link DispatchModule}
   * with a custom configuration.
   *
   * @param dispatchModule The custom configured {@link DispatchModule}
   */
  public PollHandlerModule(DispatchModule dispatchModule) {
    this.dispatchModule = dispatchModule;
  }

  /**
   * @param <A> Type of {@link Action}
   * @param <R> Type of {@link Result}
   * @param actionClass Implementation of {@link Action} to link and bind
   * @param handlerClass Implementation of {@link ActionHandler} to link and
   *          bind
   */
  protected <A extends Action<R>, R extends Result> void bindPollHandler(
      Class<A> actionClass, Class<? extends PollActionHandler<A, R>> handlerClass) {
    bind(ActionHandlerValidatorMap.class).annotatedWith(
        UniqueAnnotations.create()).toInstance(
        new ActionHandlerValidatorMapImpl<A, R>(actionClass,
            new ActionHandlerValidatorClass<A, R>(handlerClass,
                DefaultActionValidator.class)));
  }

  /**
   * @param <A> Type of {@link Action}
   * @param <R> Type of {@link Result}
   * @param actionClass Implementation of {@link Action} to link and bind
   * @param handlerClass Implementation of {@link ActionHandler} to link and
   *          bind
   * @param actionValidator Implementation of {@link ActionValidator} to link
   *          and bind
   */
  protected <A extends Action<R>, R extends Result> void bindPollHandler(
      Class<A> actionClass, Class<? extends PollActionHandler<A, R>> handlerClass,
      Class<? extends ActionValidator> actionValidator) {
    bind(ActionHandlerValidatorMap.class).annotatedWith(
        UniqueAnnotations.create()).toInstance(
        new ActionHandlerValidatorMapImpl<A, R>(
            actionClass,
            new ActionHandlerValidatorClass<A, R>(handlerClass, actionValidator)));
  }

  @Override
  protected final void configure() {
    install(dispatchModule);
    bind(PollDispatch.class).to(PollDispatchImpl.class);
    bind(AbstractPollDispatchServiceImpl.class).to(PollDispatchServiceImpl.class);
    configureHandlers();
  }

  /**
   * Override this method to configure your handlers. Use calls to
   * {@link #bindPollHandler} to register actions that do not need any specific
   * security validation.
   */
  protected abstract void configureHandlers();

}