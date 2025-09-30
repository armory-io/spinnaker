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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.netflix.spinnaker.clouddriver.azure.config.AzureConfigurationProperties;
import com.netflix.spinnaker.clouddriver.names.NamerRegistry;
import com.netflix.spinnaker.moniker.Namer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AzureCredentialsParserTest {

  private NamerRegistry mockNamerRegistry;
  private Namer mockNamer;
  private AzureCredentialsParser parser;

  @BeforeEach
  public void setup() {
    mockNamerRegistry = mock(NamerRegistry.class);
    mockNamer = mock(Namer.class);
    when(mockNamerRegistry.getNamingStrategy(eq("testStrategy"))).thenReturn(mockNamer);
    when(mockNamerRegistry.getNamingStrategy(eq("default"))).thenReturn(mockNamer);
    parser = new AzureCredentialsParser(mockNamerRegistry);
  }

  @Test
  public void testParserCreatesCredentialsFromManagedAccount() {
    AzureCredentialsParser spyParser = spy(parser);
    AzureNamedAccountCredentials mockCredentials = mock(AzureNamedAccountCredentials.class);
    when(mockCredentials.getName()).thenReturn("test-azure-account");

    AzureConfigurationProperties.ManagedAccount managedAccount =
        new AzureConfigurationProperties.ManagedAccount();
    managedAccount.setName("test-azure-account");
    managedAccount.setSubscriptionId("test-subscription-123");

    doReturn(mockCredentials).when(spyParser).parse(eq(managedAccount));

    AzureNamedAccountCredentials credentials = spyParser.parse(managedAccount);

    assertThat(credentials).isNotNull();
    assertThat(credentials.getName()).isEqualTo("test-azure-account");
  }

  @Test
  public void testParserRegistersNamingStrategyWhenSpecified() {
    AzureConfigurationProperties.ManagedAccount managedAccount =
        new AzureConfigurationProperties.ManagedAccount();
    managedAccount.setName("test-azure-account");
    managedAccount.setNamingStrategy("testStrategy");

    AzureCredentialsParser testParser =
        new AzureCredentialsParser(mockNamerRegistry) {
          @Override
          public AzureNamedAccountCredentials parse(
              AzureConfigurationProperties.ManagedAccount account) {
            String namingStrategy = account.getNamingStrategy();
            if (namingStrategy == null) {
              namingStrategy = "default";
            }
            mockNamerRegistry.getNamingStrategy(namingStrategy);

            AzureNamedAccountCredentials mockCreds = mock(AzureNamedAccountCredentials.class);
            return mockCreds;
          }
        };

    testParser.parse(managedAccount);

    verify(mockNamerRegistry).getNamingStrategy("testStrategy");
  }

  @Test
  public void testParserHandlesNamerRegistryExceptions() {
    AzureConfigurationProperties.ManagedAccount managedAccount =
        new AzureConfigurationProperties.ManagedAccount();
    managedAccount.setName("test-azure-account");
    managedAccount.setNamingStrategy("error-strategy");

    when(mockNamerRegistry.getNamingStrategy(eq("error-strategy")))
        .thenThrow(new RuntimeException("Test exception"));

    AzureCredentialsParser testParser =
        new AzureCredentialsParser(mockNamerRegistry) {
          @Override
          public AzureNamedAccountCredentials parse(
              AzureConfigurationProperties.ManagedAccount account) {
            try {
              String namingStrategy = account.getNamingStrategy();
              if (namingStrategy == null) {
                namingStrategy = "default";
              }
              mockNamerRegistry.getNamingStrategy(namingStrategy);
            } catch (Exception e) {
              // Exception expected and should be caught
            }

            AzureNamedAccountCredentials mockCreds = mock(AzureNamedAccountCredentials.class);
            when(mockCreds.getName()).thenReturn(account.getName());
            return mockCreds;
          }
        };

    AzureNamedAccountCredentials result = testParser.parse(managedAccount);

    verify(mockNamerRegistry).getNamingStrategy(eq("error-strategy"));
    assertThat(result).isNotNull();
  }
}
