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

package com.gwtplatform.dispatch.server.atmosphere.actionhandler;

import com.gwtplatform.dispatch.server.actionhandler.ActionHandler;
import com.gwtplatform.dispatch.server.atmosphere.PollCallback;
import com.gwtplatform.dispatch.server.atmosphere.PollExecutionContext;
import com.gwtplatform.dispatch.shared.Action;
import com.gwtplatform.dispatch.shared.ActionException;
import com.gwtplatform.dispatch.shared.Result;

/**
 * Instances of this interface will handle specific types of {@link com.gwtplatform.dispatch.shared.Action}
 * classes.
 * <p />
 * <b>Important!</b> Your action handlers must be thread safe since they will be
 * bound as singletons. For details, see <a href=
 * "http://code.google.com/p/google-guice/wiki/Scopes#Scopes_and_Concurrency">
 * http://code.google.com/p/google-guice/wiki/Scopes#Scopes_and_Concurrency</a>.
 *
 * @param <A> The type of the action extending {@link com.gwtplatform.dispatch.shared.Action}.
 * @param <R> The type of the result extending {@link com.gwtplatform.dispatch.shared.Result}.
 *
 * @author David Peterson
 */
public interface PollActionHandler<A extends Action<R>, R extends Result> extends ActionHandler<A, R> {

  /**
   * Handles the specified action. If you want to build a compound action that
   * can rollback automatically upon failure, call
   * {@link com.gwtplatform.dispatch.server.atmosphere.PollExecutionContext#execute(com.gwtplatform.dispatch.shared.Action,
   * com.gwtplatform.dispatch.server.atmosphere.PollCallback)}. See <a
   * href="http://code.google.com/p/gwt-dispatch/wiki/CompoundActions" >here</a>
   * for details.
   *
   * @param action The action.
   * @param context The {@link com.gwtplatform.dispatch.server.atmosphere.PollExecutionContext}.
   * @throws com.gwtplatform.dispatch.shared.ActionException if there is a problem performing the specified
   *           action.
   */
  void execute(A action, PollExecutionContext context, PollCallback<A, R> callback) throws ActionException;

  /**
   * Undoes the specified action. If you want to build a compound action that
   * can rollback automatically upon failure, call
   * {@link com.gwtplatform.dispatch.server.atmosphere.PollExecutionContext#undo(com.gwtplatform.dispatch.shared.Action, com.gwtplatform.dispatch.shared.Result,
   * com.gwtplatform.dispatch.server.atmosphere.PollCallback)}. See <a
   * href="http://code.google.com/p/gwt-dispatch/wiki/CompoundActions" >here</a>
   * for details.
   *
   * @param action The action.
   * @param result The result of the action.
   * @param context The {@link com.gwtplatform.dispatch.server.atmosphere.PollExecutionContext}.
   * @throws com.gwtplatform.dispatch.shared.ActionException if there is a problem performing the specified
   *           action.
   */
  void undo(A action, R result, PollExecutionContext context, PollCallback<A, R> callback) throws ActionException;
}
