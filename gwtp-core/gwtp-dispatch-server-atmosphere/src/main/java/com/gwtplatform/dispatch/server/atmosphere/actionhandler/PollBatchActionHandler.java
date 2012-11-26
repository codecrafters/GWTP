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

import com.gwtplatform.dispatch.server.atmosphere.PollCallback;
import com.gwtplatform.dispatch.server.atmosphere.PollExecutionContext;
import com.gwtplatform.dispatch.shared.Action;
import com.gwtplatform.dispatch.shared.ActionException;
import com.gwtplatform.dispatch.shared.BatchAction;
import com.gwtplatform.dispatch.shared.BatchAction.OnException;
import com.gwtplatform.dispatch.shared.Result;
import com.gwtplatform.dispatch.shared.BatchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This handles {@link com.gwtplatform.dispatch.shared.BatchAction} requests, which are a set of multiple
 * actions that need to all be executed successfully in sequence for the whole
 * action to succeed.
 *
 * @author David Peterson
 */
public class PollBatchActionHandler extends
        AbstractPollActionHandler<BatchAction, BatchResult> {

  public PollBatchActionHandler() {
    super(BatchAction.class);
  }

  @Override
  public void execute(final BatchAction action, PollExecutionContext context, final PollCallback<BatchAction, BatchResult> callback)
      throws ActionException {
    OnException onException = action.getOnException();
    final List<Action<?>> actions = Arrays.asList(action.getActions());
    final List<Result> results = Collections.synchronizedList(new ArrayList<Result>(actions.size()));
    for (final Action<?> a : actions) {
      try {
        context.execute(a, new PollCallback() {
          @Override
          public void onSuccess(Result result) {
            results.add(actions.indexOf(a), result);
            checkIfReady(results, callback);
          }
        });
      } catch (Exception e) {
        if (onException == OnException.ROLLBACK) {
          if (e instanceof ActionException) {
            throw (ActionException) e;
          }
          if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
          } else {
            throw new ActionException(e);
          }
        }
      }
    }
  }

  private void checkIfReady(List<Result> results, PollCallback<BatchAction, BatchResult> callback) {
    for (Result r : results) {
      if (r == null) {
        return;
      }
    }
    callback.onSuccess(new BatchResult(results));
  }

  @Override
  public void undo(BatchAction action, BatchResult result,
                   PollExecutionContext context, PollCallback<BatchAction, BatchResult> callback) throws ActionException {
    // No action necessary - the sub actions should automatically rollback
  }

}
