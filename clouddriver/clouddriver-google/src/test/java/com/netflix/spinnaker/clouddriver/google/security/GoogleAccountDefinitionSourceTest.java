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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.netflix.spinnaker.clouddriver.google.config.GoogleConfigurationProperties;
import com.netflix.spinnaker.clouddriver.security.AccountDefinitionRepository;
import com.netflix.spinnaker.clouddriver.security.AccountDefinitionSource;
import com.netflix.spinnaker.credentials.definition.CredentialsDefinitionSource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class GoogleAccountDefinitionSourceTest {

  @Test
  public void testGoogleAccountSourceReturnsAccountDefinitions() {
    // Create test account
    GoogleConfigurationProperties.ManagedAccount mockAccount =
        new GoogleConfigurationProperties.ManagedAccount();
    mockAccount.setName("test-google-account");
    mockAccount.setProject("test-project-id");

    // Mock repository
    AccountDefinitionRepository repository = mock(AccountDefinitionRepository.class);
    doReturn(Collections.singletonList(mockAccount)).when(repository).listByType(eq("google"));

    // Create configuration and additional sources
    GoogleConfigurationProperties configurationProperties =
        mock(GoogleConfigurationProperties.class);
    Optional<List<CredentialsDefinitionSource<GoogleConfigurationProperties.ManagedAccount>>>
        emptyAdditionalSources = Optional.empty();

    // Create the source configuration
    GoogleAccountDefinitionSourceConfiguration sourceConfig =
        new GoogleAccountDefinitionSourceConfiguration();

    // Get credentials definition source
    CredentialsDefinitionSource<GoogleConfigurationProperties.ManagedAccount> source =
        sourceConfig.googleAccountSource(
            repository, emptyAdditionalSources, configurationProperties);

    // Verify source is created correctly
    assertThat(source).isNotNull();
    assertThat(source).isInstanceOf(AccountDefinitionSource.class);

    // Get account definitions
    List<GoogleConfigurationProperties.ManagedAccount> accounts =
        source.getCredentialsDefinitions();

    // Verify accounts are returned correctly
    assertThat(accounts).isNotEmpty();
    assertThat(accounts).hasSize(1);
    assertThat(accounts.get(0).getName()).isEqualTo("test-google-account");
    assertThat(accounts.get(0).getProject()).isEqualTo("test-project-id");
  }
}
