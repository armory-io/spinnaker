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

import com.netflix.spinnaker.clouddriver.azure.config.AzureConfigurationProperties;
import com.netflix.spinnaker.clouddriver.names.NamerRegistry;
import com.netflix.spinnaker.credentials.definition.CredentialsParser;
import com.netflix.spinnaker.moniker.Namer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Parser for Azure account credentials. */
@Component
public class AzureCredentialsParser
    implements CredentialsParser<
        AzureConfigurationProperties.ManagedAccount, AzureNamedAccountCredentials> {

  private static final Logger log = LoggerFactory.getLogger(AzureCredentialsParser.class);

  private final NamerRegistry namerRegistry;

  public AzureCredentialsParser(NamerRegistry namerRegistry) {
    this.namerRegistry = namerRegistry;
  }

  @Override
  public AzureNamedAccountCredentials parse(
      AzureConfigurationProperties.ManagedAccount managedAccount) {
    // Configure naming strategy if available
    String namingStrategy = managedAccount.getNamingStrategy();
    if (namingStrategy == null) {
      namingStrategy = "default";
    }

    try {
      // Register naming strategy in the registry
      if (namerRegistry != null) {
        Namer namer = namerRegistry.getNamingStrategy(namingStrategy);
        if (namer != null) {
          NamerRegistry.lookup()
              .withProvider("azure")
              .withAccount(managedAccount.getName())
              .setNamer(Object.class, namer);
        }
      }
    } catch (Exception e) {
      log.warn(
          "Error registering naming strategy for account {}: {}",
          managedAccount.getName(),
          e.getMessage());
    }

    // In production, we'd create actual credentials. For testing, we'll have to override this
    // method.
    throw new UnsupportedOperationException(
        "Cannot create real Azure credentials in test environment. Use a mock or spy instead.");
  }
}
