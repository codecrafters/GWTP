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

package com.gwtplatform.dispatch.server.atmosphere;

import com.gwtplatform.dispatch.server.actionhandler.ActionResult;
import com.gwtplatform.dispatch.server.actionhandlervalidator.ActionHandlerValidatorInstance;
import com.gwtplatform.dispatch.server.actionhandlervalidator.ActionHandlerValidatorRegistry;
import com.gwtplatform.dispatch.server.actionvalidator.ActionValidator;
import com.gwtplatform.dispatch.server.atmosphere.actionhandler.PollActionHandler;
import com.gwtplatform.dispatch.shared.Action;
import com.gwtplatform.dispatch.shared.ActionException;
import com.gwtplatform.dispatch.shared.Result;
import com.gwtplatform.dispatch.shared.ServiceException;
import com.gwtplatform.dispatch.shared.UnsupportedActionException;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the server-side implementation of the {@link com.gwtplatform.dispatch.server.Dispatch} service with an arbitrary action type, for which the
 * client-side async service is {@link com.gwtplatform.dispatch.shared.DispatchAsync}.
 * <p />
 * This class is closely related to {@link com.gwtplatform.dispatch.server.guice.DispatchServiceImpl}.
 * In fact, this class wouldn't be needed, but we use it
 * to workaround a GWT limitation described in {@link com.gwtplatform.dispatch.shared.DispatchAsync}.
 *
 * @see com.gwtplatform.dispatch.shared.DispatchAsync
 * @see com.gwtplatform.dispatch.server.Dispatch
 * @see com.gwtplatform.dispatch.server.guice.DispatchImpl
 * @see com.gwtplatform.dispatch.shared.DispatchService
 * @see com.gwtplatform.dispatch.shared.DispatchServiceAsync
 * @see com.gwtplatform.dispatch.server.guice.DispatchServiceImpl
 *
 * @author Christian Goudreau
 * @author David Peterson
 */
public abstract class AbstractPollDispatchImpl implements PollDispatch {

  private static class DefaultExecutionContext implements PollExecutionContext {

    private final List<ActionResult<?, ?>> actionResults;
    private final AbstractPollDispatchImpl dispatch;

    private DefaultExecutionContext(AbstractPollDispatchImpl dispatch) {
      this.dispatch = dispatch;
      this.actionResults = new ArrayList<ActionResult<?, ?>>();
    }

    @Override
    public <A extends Action<R>, R extends Result> void execute(final A action, final PollCallback<A, R> callback)
        throws ActionException, ServiceException {
      dispatch.doExecute(action, this, new PollCallback<A, R>() {
          @Override
          public void onSuccess(R r) {
              actionResults.add(new ActionResult<A, R>(action, (R) r, true));
              callback.onSuccess(r);
          }
      });
    }

    @Override
    public <A extends Action<R>, R extends Result> void undo(final A action, final R result, final PollCallback<A, R> callback)
        throws ActionException, ServiceException {
      dispatch.doUndo(action, result, this, new PollCallback<A, R>() {
          @Override
          public void onSuccess(R r) {
              actionResults.add(new ActionResult<A, R>(action, result, false));
              callback.onSuccess(r);
          }
      });
    }

      /**
     * Rolls back all logged executed actions.
     *
     * @throws com.gwtplatform.dispatch.shared.ActionException If there is an action exception while rolling back.
     * @throws com.gwtplatform.dispatch.shared.ServiceException If there is a low level problem while rolling back.
     */
    private void rollback() throws ActionException, ServiceException {
      DefaultExecutionContext ctx = new DefaultExecutionContext(dispatch);
      for (int i = actionResults.size() - 1; i >= 0; i--) {
        ActionResult<?, ?> actionResult = actionResults.get(i);
        rollback(actionResult, ctx);
      }
    }

    private <A extends Action<R>, R extends Result> void rollback(ActionResult<A, R> actionResult, PollExecutionContext ctx) throws ActionException,
    ServiceException {
      if (actionResult.isExecuted()) {
        dispatch.doUndo(actionResult.getAction(), actionResult.getResult(), ctx, new PollCallback<A, R>() {
            @Override
            public void onSuccess(R result) {
            }
        });
      } else {
        dispatch.doExecute(actionResult.getAction(), ctx, new PollCallback<A, R>() {
            @Override
            public void onSuccess(R result) {
            }
        });
      }
    }
  }

  private static final String actionValidatorMessage = " couldn't allow access to action : ";

  private final ActionHandlerValidatorRegistry actionHandlerValidatorRegistry;

  protected AbstractPollDispatchImpl(ActionHandlerValidatorRegistry actionHandlerValidatorRegistry) {
    this.actionHandlerValidatorRegistry = actionHandlerValidatorRegistry;
  }

  @Override
  public <A extends Action<R>, R extends Result> void execute(A action, final PollDispatchCallback callback)
          throws ActionException, ServiceException {
    DefaultExecutionContext ctx = new DefaultExecutionContext(this);
    try {
      doExecute(action, ctx, new PollCallback<A, R>() {
        @Override
        public void onSuccess(R r) {
            callback.onSuccess(r);
        }
    });
    } catch (ActionException e) {
      ctx.rollback();
      throw e;
    } catch (ServiceException e) {
      ctx.rollback();
      throw e;
    }
  }

  @Override
  public <A extends Action<R>, R extends Result> void undo(A action, R result, final PollDispatchCallback callback)
          throws ActionException, ServiceException {
    DefaultExecutionContext ctx = new DefaultExecutionContext(this);
    try {
      doUndo(action, result, ctx, new PollCallback<A, R>() {
          @Override
          public void onSuccess(R r) {
              callback.onSuccess(null);
          }
      });
    } catch (ActionException e) {
      ctx.rollback();
      throw e;
    } catch (ServiceException e) {
      ctx.rollback();
      throw e;
    }
  }

  /**
   * Every single action will be executed by this function and validated by the {@link com.gwtplatform.dispatch.server.actionvalidator.ActionValidator}.
   *
   * @param <A> Type of associated {@link com.gwtplatform.dispatch.shared.Action} type.
   * @param <R> Type of associated {@link com.gwtplatform.dispatch.shared.Result} type.
   * @param action The {@link com.gwtplatform.dispatch.shared.Action} to execute
   * @param ctx The {@link com.gwtplatform.dispatch.server.ExecutionContext} associated with the {@link com.gwtplatform.dispatch.shared.Action}
   * @throws com.gwtplatform.dispatch.shared.ActionException
   * @throws com.gwtplatform.dispatch.shared.ServiceException
   */
  private <A extends Action<R>, R extends Result> void doExecute(A action, PollExecutionContext ctx, PollCallback<A, R> callback)
          throws ActionException, ServiceException {
    PollActionHandler<A, R> handler = findHandler(action);

    ActionValidator actionValidator = findActionValidator(action);

    try {
      if (actionValidator.isValid(action)) {
        handler.execute(action, ctx, callback);
      } else {
        throw new ServiceException(actionValidator.getClass().getName() + actionValidatorMessage + action.getClass().getName());
      }
    } catch (ActionException e) {
      throw e;
    } catch (Exception e) {
      String newMessage = "Service exception executing action \"" + action.getClass().getSimpleName() + "\", " + e.toString();
      ServiceException rethrown = new ServiceException(newMessage);
      rethrown.initCause(e);
      throw rethrown;
    }
  }

  private <A extends Action<R>, R extends Result> void doUndo(A action, R result, PollExecutionContext ctx, PollCallback<A, R> callback)
          throws ActionException, ServiceException {

    ActionValidator actionValidator = findActionValidator(action);

    PollActionHandler<A, R> handler = findHandler(action);
    try {
      if (actionValidator.isValid(action)) {
        handler.undo(action, result, ctx, callback);
      } else {
        throw new ServiceException(actionValidator.getClass().getName() + actionValidatorMessage + action.getClass().getName());
      }
    } catch (ActionException e) {
      throw e;
    } catch (Exception cause) {
      throw new ServiceException(cause);
    }
  }

  private <A extends Action<R>, R extends Result> ActionValidator findActionValidator(A action) throws UnsupportedActionException {
    ActionHandlerValidatorInstance handlerValidator = actionHandlerValidatorRegistry.findActionHandlerValidator(action);
    if (handlerValidator == null) {
      throw new UnsupportedActionException(action);
    }

    return handlerValidator.getActionValidator();
  }

  @SuppressWarnings("unchecked")
  private <A extends Action<R>, R extends Result> PollActionHandler<A, R> findHandler(A action) throws UnsupportedActionException {
    ActionHandlerValidatorInstance handlerValidator = actionHandlerValidatorRegistry.findActionHandlerValidator(action);

    if (handlerValidator == null) {
      throw new UnsupportedActionException(action);
    }

    return (PollActionHandler<A, R>) handlerValidator.getActionHandler();
  }
}