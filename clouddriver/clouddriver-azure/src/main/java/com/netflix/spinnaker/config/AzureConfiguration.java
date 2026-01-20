/*
 * Copyright 2015 The original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.config;

import com.netflix.spinnaker.clouddriver.azure.AzureCloudProvider;
import com.netflix.spinnaker.clouddriver.azure.config.AzureConfigurationProperties;
import com.netflix.spinnaker.clouddriver.azure.health.AzureHealthIndicator;
import com.netflix.spinnaker.clouddriver.azure.security.AzureNamedAccountCredentials;
import com.netflix.spinnaker.clouddriver.security.CredentialsInitializerSynchronizable;
import com.netflix.spinnaker.credentials.CredentialsLifecycleHandler;
import com.netflix.spinnaker.credentials.CredentialsRepository;
import com.netflix.spinnaker.credentials.MapBackedCredentialsRepository;
import com.netflix.spinnaker.credentials.definition.AbstractCredentialsLoader;
import com.netflix.spinnaker.credentials.definition.BasicCredentialsLoader;
import com.netflix.spinnaker.credentials.definition.CredentialsDefinitionSource;
import com.netflix.spinnaker.credentials.poller.Poller;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties
@EnableScheduling
@ConditionalOnProperty("azure.enabled")
@ComponentScan("com.netflix.spinnaker.clouddriver.azure")
public class AzureConfiguration {
  @Bean
  @ConfigurationProperties("azure")
  AzureConfigurationProperties azureConfigurationProperties() {
    return new AzureConfigurationProperties();
  }

  @Bean
  AzureHealthIndicator azureHealthIndicator() {
    return new AzureHealthIndicator();
  }

  @Bean
  @ConditionalOnMissingBean(name = "azureCredentialsRepository")
  public CredentialsRepository<AzureNamedAccountCredentials> azureCredentialsRepository(
      CredentialsLifecycleHandler<AzureNamedAccountCredentials> eventHandler) {
    return new MapBackedCredentialsRepository<>((String) AzureCloudProvider.getID(), eventHandler);
  }

  @Bean
  @ConditionalOnMissingBean(name = "azureRegistryCredentialsLoader")
  public AbstractCredentialsLoader<AzureNamedAccountCredentials> azureRegistryCredentialsLoader(
      @Nullable
          CredentialsDefinitionSource<AzureConfigurationProperties.ManagedAccount>
              azureRegistryCredentialsDefinitionSource,
      AzureConfigurationProperties azureConfigurationProperties,
      @Qualifier("clouddriverUserAgentApplicationName") String clouddriverUserAgentApplicationName,
      CredentialsRepository<AzureNamedAccountCredentials> azureCredentialsRepository) {
    if (azureRegistryCredentialsDefinitionSource == null) {
      azureRegistryCredentialsDefinitionSource = azureConfigurationProperties::getAccounts;
    }

    return new BasicCredentialsLoader<>(
        azureRegistryCredentialsDefinitionSource,
        a ->
            (new AzureNamedAccountCredentials(
                a.getName(),
                a.getEnvironment() != null ? a.getEnvironment() : a.getName(),
                a.getAccountType() != null ? a.getAccountType() : a.getName(),
                a.getClientId(),
                a.getAppKey(),
                a.getTenantId(),
                a.getSubscriptionId(),
                a.getRegions(),
                a.getVmImages(),
                a.getCustomImages(),
                a.getDefaultResourceGroup(),
                a.getDefaultKeyVault(),
                a.getUseSshPublicKey(),
                clouddriverUserAgentApplicationName,
                a.getPermissions().build())),
        azureCredentialsRepository);
  }

  @Bean
  @ConditionalOnMissingBean(name = "azureCredentialsInitializerSynchronizable")
  public CredentialsInitializerSynchronizable azureCredentialsInitializerSynchronizable(
      AbstractCredentialsLoader<AzureNamedAccountCredentials> azureRegistryCredentialsLoader) {
    final Poller<AzureNamedAccountCredentials> poller =
        new Poller<>(azureRegistryCredentialsLoader);
    return new CredentialsInitializerSynchronizable() {
      @Override
      public void synchronize() {
        poller.run();
      }
    };
  }
}
