/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.github;

import com.intellij.notification.NotificationType;

/**
 * @author Aleksey Pivovarov
 */
public class GithubCreatePullRequestTest extends GithubCreatePullRequestTestBase {
  public void testSimple() throws Exception {
    registerDefaultCreatePullRequestDialogHandler(myLogin1 + ":master");

    GithubCreatePullRequestAction.createPullRequest(myProject, myProjectRoot);

    checkNotification(NotificationType.INFORMATION, "Successfully created pull request", null);
    checkRemoteConfigured();
    checkLastCommitPushed();
  }

  public void testParent() throws Exception {
    registerDefaultCreatePullRequestDialogHandler(myLogin2 + ":file2");

    GithubCreatePullRequestAction.createPullRequest(myProject, myProjectRoot);

    checkNotification(NotificationType.INFORMATION, "Successfully created pull request", null);
    checkRemoteConfigured();
    checkLastCommitPushed();
  }

  public void testCommitRef1() throws Exception {
    registerDefaultCreatePullRequestDialogHandler(myLogin1 + ":refs/heads/master");

    GithubCreatePullRequestAction.createPullRequest(myProject, myProjectRoot);

    checkNotification(NotificationType.INFORMATION, "Successfully created pull request", null);
    checkRemoteConfigured();
    checkLastCommitPushed();
  }
}
