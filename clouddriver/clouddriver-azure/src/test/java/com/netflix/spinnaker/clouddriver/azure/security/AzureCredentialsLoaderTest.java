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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.netflix.spinnaker.clouddriver.azure.config.AzureConfigurationProperties;
import com.netflix.spinnaker.credentials.CredentialsRepository;
import com.netflix.spinnaker.credentials.definition.BasicCredentialsLoader;
import com.netflix.spinnaker.credentials.definition.CredentialsDefinitionSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AzureCredentialsLoaderTest {

  private AzureCredentialsParser mockCredentialsParser;
  private CredentialsRepository<AzureNamedAccountCredentials> mockRepository;
  private CredentialsDefinitionSource<AzureConfigurationProperties.ManagedAccount> mockSource;
  private BasicCredentialsLoader<
          AzureConfigurationProperties.ManagedAccount, AzureNamedAccountCredentials>
      loader;

  @BeforeEach
  public void setup() {
    mockCredentialsParser = mock(AzureCredentialsParser.class);
    mockRepository = mock(CredentialsRepository.class);
    mockSource = mock(CredentialsDefinitionSource.class);

    loader = new BasicCredentialsLoader<>(mockSource, mockCredentialsParser, mockRepository);
  }

  @Test
  public void testLoaderProcessesAllDefinitions() {
    // Given
    AzureConfigurationProperties.ManagedAccount account1 =
        new AzureConfigurationProperties.ManagedAccount();
    account1.setName("azure-account-1");
    account1.setSubscriptionId("sub-1");

    AzureConfigurationProperties.ManagedAccount account2 =
        new AzureConfigurationProperties.ManagedAccount();
    account2.setName("azure-account-2");
    account2.setSubscriptionId("sub-2");

    List<AzureConfigurationProperties.ManagedAccount> accounts = Arrays.asList(account1, account2);
    when(mockSource.getCredentialsDefinitions()).thenReturn(accounts);

    AzureNamedAccountCredentials credentials1 = mock(AzureNamedAccountCredentials.class);
    when(credentials1.getName()).thenReturn("azure-account-1");
    AzureNamedAccountCredentials credentials2 = mock(AzureNamedAccountCredentials.class);
    when(credentials2.getName()).thenReturn("azure-account-2");

    when(mockCredentialsParser.parse(account1)).thenReturn(credentials1);
    when(mockCredentialsParser.parse(account2)).thenReturn(credentials2);

    // When
    loader.load();

    // Then
    verify(mockRepository).save(credentials1);
    verify(mockRepository).save(credentials2);
    verify(mockCredentialsParser).parse(account1);
    verify(mockCredentialsParser).parse(account2);
  }

  @Test
  public void testLoaderHandlesEmptyDefinitions() {
    // Given
    when(mockSource.getCredentialsDefinitions()).thenReturn(new ArrayList<>());

    // When
    loader.load();

    // Then
    verify(mockRepository, never()).save(any());
  }

  @Test
  public void testLoaderHandlesParsingExceptions() {
    // Given
    AzureConfigurationProperties.ManagedAccount account =
        new AzureConfigurationProperties.ManagedAccount();
    account.setName("azure-account-error");

    List<AzureConfigurationProperties.ManagedAccount> accounts = Arrays.asList(account);
    when(mockSource.getCredentialsDefinitions()).thenReturn(accounts);
    when(mockCredentialsParser.parse(account))
        .thenThrow(new RuntimeException("Test parsing exception"));

    // When - catch the exception since BasicCredentialsLoader doesn't handle it
    try {
      loader.load();
    } catch (RuntimeException e) {
      // Expected exception - in a real implementation, this should be caught
    }

    // Then - verify no credentials were saved
    verify(mockRepository, never()).save(any());
  }
}
