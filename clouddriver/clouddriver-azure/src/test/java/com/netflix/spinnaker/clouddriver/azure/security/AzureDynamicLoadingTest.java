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

import static org.mockito.Mockito.*;

import com.netflix.spinnaker.clouddriver.security.CredentialsInitializerSynchronizable;
import com.netflix.spinnaker.credentials.definition.AbstractCredentialsLoader;
import org.junit.jupiter.api.Test;

public class AzureDynamicLoadingTest {

  @Test
  public void testCredentialsInitializerSynchronizableCallsLoader() {
    // Arrange
    AbstractCredentialsLoader mockLoader = mock(AbstractCredentialsLoader.class);

    CredentialsInitializerSynchronizable synchronizable =
        AzureTestHelper.createSynchronizable(mockLoader);

    // Act
    synchronizable.synchronize();

    // Assert
    verify(mockLoader, times(1)).load();
    System.out.println("TEST PASSED: Azure synchronize method correctly calls loader.load()");
  }
}
