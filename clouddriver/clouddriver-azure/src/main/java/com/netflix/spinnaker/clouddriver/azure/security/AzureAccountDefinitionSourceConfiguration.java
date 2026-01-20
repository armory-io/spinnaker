package com.netflix.spinnaker.clouddriver.azure.security;

import com.netflix.spinnaker.clouddriver.azure.config.AzureConfigurationProperties;
import com.netflix.spinnaker.clouddriver.security.AccountDefinitionRepository;
import com.netflix.spinnaker.clouddriver.security.AccountDefinitionSource;
import com.netflix.spinnaker.credentials.definition.CredentialsDefinitionSource;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty({"account.storage.enabled", "account.storage.azure.enabled"})
public class AzureAccountDefinitionSourceConfiguration {
  @Bean
  public CredentialsDefinitionSource<AzureConfigurationProperties.ManagedAccount>
      azureAccountSource(
          AccountDefinitionRepository repository,
          Optional<List<CredentialsDefinitionSource<AzureConfigurationProperties.ManagedAccount>>>
              additionalSources,
          AzureConfigurationProperties accountProperties) {
    return new AccountDefinitionSource<>(
        repository,
        AzureConfigurationProperties.ManagedAccount.class,
        additionalSources.orElseGet(() -> List.of(accountProperties::getAccounts)));
  }
}
