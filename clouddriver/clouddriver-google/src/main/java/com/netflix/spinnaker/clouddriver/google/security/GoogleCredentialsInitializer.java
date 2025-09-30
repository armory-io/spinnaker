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

package com.netflix.spinnaker.clouddriver.google.security;

import com.netflix.spinnaker.clouddriver.google.config.GoogleConfigurationProperties;
import com.netflix.spinnaker.clouddriver.security.CredentialsInitializerSynchronizable;
import com.netflix.spinnaker.credentials.CredentialsLifecycleHandler;
import com.netflix.spinnaker.credentials.CredentialsRepository;
import com.netflix.spinnaker.credentials.MapBackedCredentialsRepository;
import com.netflix.spinnaker.credentials.definition.AbstractCredentialsLoader;
import com.netflix.spinnaker.credentials.definition.BasicCredentialsLoader;
import com.netflix.spinnaker.credentials.definition.CredentialsDefinitionSource;
import com.netflix.spinnaker.credentials.definition.CredentialsParser;
import javax.annotation.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Google credentials initializer with dynamic loading support. */
@Configuration
public class GoogleCredentialsInitializer {

  @Bean
  @ConditionalOnMissingBean(
      value = GoogleNamedAccountCredentials.class,
      parameterizedContainer = CredentialsRepository.class)
  public CredentialsRepository<GoogleNamedAccountCredentials> googleCredentialsRepository(
      CredentialsLifecycleHandler<GoogleNamedAccountCredentials> eventHandler) {
    return new MapBackedCredentialsRepository<>("gce", eventHandler);
  }

  @Bean
  @ConditionalOnMissingBean(name = "googleCredentialsLoader")
  public AbstractCredentialsLoader<GoogleNamedAccountCredentials> googleCredentialsLoader(
      CredentialsParser<GoogleConfigurationProperties.ManagedAccount, GoogleNamedAccountCredentials>
          googleCredentialsParser,
      CredentialsRepository<GoogleNamedAccountCredentials> repository,
      GoogleConfigurationProperties googleConfigurationProperties,
      @Nullable
          CredentialsDefinitionSource<GoogleConfigurationProperties.ManagedAccount>
              googleCredentialsSource) {
    if (googleCredentialsSource == null) {
      googleCredentialsSource = googleConfigurationProperties::getAccounts;
    }
    return new BasicCredentialsLoader<>(
        googleCredentialsSource, googleCredentialsParser, repository);
  }

  /**
   * This bean implements the dynamic loading functionality that allows Spinnaker to synchronize
   * credentials between the database and running instances.
   */
  @Bean
  @ConditionalOnMissingBean(name = "googleCredentialsInitializerSynchronizable")
  public CredentialsInitializerSynchronizable googleCredentialsInitializerSynchronizable(
      AbstractCredentialsLoader<GoogleNamedAccountCredentials> googleCredentialsLoader) {
    return new CredentialsInitializerSynchronizable() {
      @Override
      public void synchronize() {
        googleCredentialsLoader.load();
      }
    };
  }
}
