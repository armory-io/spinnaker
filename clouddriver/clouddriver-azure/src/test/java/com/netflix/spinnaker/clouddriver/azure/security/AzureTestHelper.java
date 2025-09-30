/*
 * Copyright 2025 Harness, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.azure.security;

import com.netflix.spinnaker.clouddriver.security.CredentialsInitializerSynchronizable;
import com.netflix.spinnaker.credentials.definition.AbstractCredentialsLoader;

/** Helper class for testing Azure credentials functionality. */
public class AzureTestHelper {

  /** Creates a CredentialsInitializerSynchronizable for testing. */
  public static CredentialsInitializerSynchronizable createSynchronizable(
      AbstractCredentialsLoader loader) {
    return new CredentialsInitializerSynchronizable() {
      @Override
      public void synchronize() {
        loader.load();
      }
    };
  }
}
