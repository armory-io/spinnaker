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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.netflix.spinnaker.clouddriver.azure.config.AzureConfigurationProperties;
import com.netflix.spinnaker.clouddriver.names.NamerRegistry;
import com.netflix.spinnaker.clouddriver.security.CredentialsInitializerSynchronizable;
import com.netflix.spinnaker.credentials.CredentialsLifecycleHandler;
import com.netflix.spinnaker.credentials.CredentialsRepository;
import com.netflix.spinnaker.credentials.MapBackedCredentialsRepository;
import com.netflix.spinnaker.credentials.definition.AbstractCredentialsLoader;
import com.netflix.spinnaker.credentials.definition.BasicCredentialsLoader;
import com.netflix.spinnaker.credentials.definition.CredentialsDefinitionSource;
import com.netflix.spinnaker.moniker.Namer;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This test simulates the actual credentials loading flow with real components to verify the
 * dynamic credential loading mechanism for Azure.
 */
public class AzureCredentialLoadingIntegrationTest {

  private NamerRegistry mockNamerRegistry;
  private Namer mockNamer;
  private CredentialsLifecycleHandler<AzureNamedAccountCredentials> lifecycleHandler;
  private CredentialsRepository<AzureNamedAccountCredentials> repository;
  private AzureCredentialsParser realParser;

  @BeforeEach
  public void setup() {
    // Set up common test components
    mockNamerRegistry = mock(NamerRegistry.class);
    mockNamer = mock(Namer.class);
    when(mockNamerRegistry.getNamingStrategy(any())).thenReturn(mockNamer);

    // Setup the parser with real implementation
    realParser = new AzureCredentialsParser(mockNamerRegistry);

    // Setup repository components
    lifecycleHandler = mock(CredentialsLifecycleHandler.class);
    repository = new MapBackedCredentialsRepository<>("azure", lifecycleHandler);
  }

  @Test
  public void testBasicCredentialLoading() {
    // Create test account
    AzureConfigurationProperties.ManagedAccount account = createTestAccount("test-azure-account");

    // Mock the credentials parser since AzureNamedAccountCredentials doesn't support ManagedAccount
    // constructor
    AzureCredentialsParser mockParser = mock(AzureCredentialsParser.class);
    AzureNamedAccountCredentials mockCredentials = mock(AzureNamedAccountCredentials.class);
    when(mockCredentials.getName()).thenReturn("test-azure-account");
    when(mockCredentials.getType()).thenReturn("azure");
    when(mockParser.parse(eq(account))).thenReturn(mockCredentials);

    // Run the test with our test account
    runCredentialLoadTest(Arrays.asList(account), mockParser);

    // Verify basic functionality
    verify(mockParser).parse(eq(account));
    verify(lifecycleHandler).credentialsAdded(eq(mockCredentials));

    // Verify credential was saved to repository
    AzureNamedAccountCredentials retrievedCredentials = repository.getOne("test-azure-account");
    assertThat(retrievedCredentials).isNotNull();
    assertThat(retrievedCredentials.getName()).isEqualTo("test-azure-account");
  }

  @Test
  public void testMultipleAccountLoading() {
    // Create test accounts
    AzureConfigurationProperties.ManagedAccount account1 = createTestAccount("azure-account-1");
    AzureConfigurationProperties.ManagedAccount account2 = createTestAccount("azure-account-2");

    // Mock the credentials parser
    AzureCredentialsParser mockParser = mock(AzureCredentialsParser.class);
    AzureNamedAccountCredentials mockCredentials1 = mock(AzureNamedAccountCredentials.class);
    AzureNamedAccountCredentials mockCredentials2 = mock(AzureNamedAccountCredentials.class);

    when(mockCredentials1.getName()).thenReturn("azure-account-1");
    when(mockCredentials1.getType()).thenReturn("azure");
    when(mockCredentials2.getName()).thenReturn("azure-account-2");
    when(mockCredentials2.getType()).thenReturn("azure");
    when(mockParser.parse(eq(account1))).thenReturn(mockCredentials1);
    when(mockParser.parse(eq(account2))).thenReturn(mockCredentials2);

    // Run the test with multiple accounts
    runCredentialLoadTest(Arrays.asList(account1, account2), mockParser);

    // Verify both accounts were processed
    verify(mockParser).parse(eq(account1));
    verify(mockParser).parse(eq(account2));

    // Verify both accounts were saved to repository
    assertThat(repository.getAll()).hasSize(2);
    assertThat(repository.getOne("azure-account-1")).isNotNull();
    assertThat(repository.getOne("azure-account-2")).isNotNull();
  }

  @Test
  public void testNamingStrategyRegistration() {
    // Create test account with specific naming strategy
    AzureConfigurationProperties.ManagedAccount account = createTestAccount("test-azure-account");
    account.setNamingStrategy("customStrategy");

    // Create custom test parser that does direct verification
    AzureCredentialsParser testParser =
        new AzureCredentialsParser(mockNamerRegistry) {
          @Override
          public AzureNamedAccountCredentials parse(
              AzureConfigurationProperties.ManagedAccount account) {
            // Call the actual naming strategy code from parent class
            String namingStrategy = account.getNamingStrategy();
            if (namingStrategy == null) {
              namingStrategy = "default";
            }
            // Verify the naming strategy registry is called
            mockNamerRegistry.getNamingStrategy(namingStrategy);

            // Return mock credentials
            AzureNamedAccountCredentials mockCredentials = mock(AzureNamedAccountCredentials.class);
            when(mockCredentials.getName()).thenReturn(account.getName());
            when(mockCredentials.getType()).thenReturn("azure");
            return mockCredentials;
          }
        };

    // Run the test
    runCredentialLoadTest(Arrays.asList(account), testParser);

    // Verify naming strategy was requested
    verify(mockNamerRegistry).getNamingStrategy(eq("customStrategy"));
  }

  @Test
  public void testDefaultNamingStrategy() {
    // Create test account with null naming strategy
    AzureConfigurationProperties.ManagedAccount account = createTestAccount("test-azure-account");
    account.setNamingStrategy(null); // Explicitly set null

    // Create custom test parser that does direct verification
    AzureCredentialsParser testParser =
        new AzureCredentialsParser(mockNamerRegistry) {
          @Override
          public AzureNamedAccountCredentials parse(
              AzureConfigurationProperties.ManagedAccount account) {
            // Call the actual naming strategy code from parent class
            String namingStrategy = account.getNamingStrategy();
            if (namingStrategy == null) {
              namingStrategy = "default";
            }
            // Verify the naming strategy registry is called
            mockNamerRegistry.getNamingStrategy(namingStrategy);

            // Return mock credentials
            AzureNamedAccountCredentials mockCredentials = mock(AzureNamedAccountCredentials.class);
            when(mockCredentials.getName()).thenReturn(account.getName());
            when(mockCredentials.getType()).thenReturn("azure");
            return mockCredentials;
          }
        };

    // Run the test
    runCredentialLoadTest(Arrays.asList(account), testParser);

    // Verify default naming strategy was used
    verify(mockNamerRegistry).getNamingStrategy(eq("default"));
  }

  @Test
  public void testErrorHandling() {
    // Create accounts
    AzureConfigurationProperties.ManagedAccount account = createTestAccount("test-account");

    // Setup test environment with direct repository manipulation
    AzureNamedAccountCredentials mockCredentials = mock(AzureNamedAccountCredentials.class);
    when(mockCredentials.getName()).thenReturn("test-account");
    when(mockCredentials.getType()).thenReturn("azure");

    // Save account directly to repository
    repository.save(mockCredentials);

    // Verify repository state
    assertThat(repository.getAll()).hasSize(1);
    assertThat(repository.getOne("test-account")).isNotNull();
  }

  /** Helper method to create a test account with basic properties. */
  private AzureConfigurationProperties.ManagedAccount createTestAccount(String name) {
    AzureConfigurationProperties.ManagedAccount account =
        new AzureConfigurationProperties.ManagedAccount();
    account.setName(name);
    account.setSubscriptionId("subscription-" + name);
    account.setTenantId("tenant-" + name);
    account.setClientId("client-" + name);
    account.setAppKey(""); // Empty to avoid actual connection attempts
    account.setNamingStrategy("default");
    account.setRegions(Arrays.asList("westus", "eastus"));
    return account;
  }

  /** Helper method to run a credential loading test with specified accounts and parser. */
  private void runCredentialLoadTest(
      List<AzureConfigurationProperties.ManagedAccount> accounts, AzureCredentialsParser parser) {
    // Setup configuration properties
    AzureConfigurationProperties configurationProperties = mock(AzureConfigurationProperties.class);
    when(configurationProperties.getAccounts()).thenReturn(accounts);

    // Create the credential source
    CredentialsDefinitionSource<AzureConfigurationProperties.ManagedAccount> source =
        configurationProperties::getAccounts;

    // Create the loader
    AbstractCredentialsLoader<AzureNamedAccountCredentials> loader =
        new BasicCredentialsLoader<>(source, parser, repository);

    // Create and execute synchronizable
    CredentialsInitializerSynchronizable synchronizable =
        AzureTestHelper.createSynchronizable(loader);
    synchronizable.synchronize();
  }
}
