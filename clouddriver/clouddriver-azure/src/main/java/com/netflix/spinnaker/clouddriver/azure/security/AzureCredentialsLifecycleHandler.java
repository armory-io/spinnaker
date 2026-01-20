package com.netflix.spinnaker.clouddriver.azure.security;

import com.netflix.spinnaker.clouddriver.azure.AzureCloudProvider;
import com.netflix.spinnaker.clouddriver.azure.resources.common.cache.provider.AzureInfrastructureProvider;
import com.netflix.spinnaker.credentials.CredentialsLifecycleHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty("azure.enabled")
public class AzureCredentialsLifecycleHandler
    implements CredentialsLifecycleHandler<AzureNamedAccountCredentials> {

  private final AzureCloudProvider provider;
  private final AzureInfrastructureProvider cloudProvider;
//  private final ObjectMapper objectMapper;
//  private final Registry registry;

  @Override
  public void credentialsAdded(AzureNamedAccountCredentials credentials) {
    log.info("Azure account, {}, was added. Scheduling caching agents", credentials.getName());
    provider.getId();
    cloudProvider.getAgents();
    // scheduleAgents(credentials);
  }

  @Override
  public void credentialsUpdated(AzureNamedAccountCredentials credentials) {
    log.info("Azure account, {}, was updated. Scheduling caching agents", credentials.getName());
    provider.getId();
    cloudProvider.getAgents();
    // removeAgentsForAccounts(Collections.singleton(credentials.getName()));
    // scheduleAgents(credentials);
  }

  @Override
  public void credentialsDeleted(AzureNamedAccountCredentials credentials) {
    log.info("Azure account, {}, was removed. Removing caching agents", credentials.getName());
    provider.getId();
    cloudProvider.getAgents();
    // removeAgentsForAccounts(Collections.singleton(credentials.getName()));
  }

//  private void removeAgentsForAccounts(Collection<String> namesOfDeletedAccounts) {
//    Collection<Agent> agents = Collections.newSetFromMap(new ConcurrentHashMap<>());
//    namesOfDeletedAccounts.forEach(
//        nameOfDeletedAccount -> {
//          AgentScheduler<?> scheduler = cloudProvider.getAgentScheduler();
//          List<Agent> agentsToDelete =
//              agents.stream()
//                  .filter(agent -> agent.handlesAccount(nameOfDeletedAccount))
//                  .collect(Collectors.toList());
//          if (scheduler != null) {
//            agentsToDelete.forEach(scheduler::unschedule);
//          }
//          agents.removeAll(agentsToDelete);
//        });
//  }
//
//  private void scheduleAgents(AzureNamedAccountCredentials credentials) {
//    Set<String> scheduledAccounts = ProviderUtils.getScheduledAccounts(cloudProvider);
//    if (!scheduledAccounts.contains(credentials.getAccountName())) {
//      for (AzureNamedAccountCredentials.AzureRegion region : credentials.getRegions()) {
//        List<Agent> newAgents = new LinkedList<>();
//        newAgents.add(
//            new AzureLoadBalancerCachingAgent(
//                provider,
//                credentials.getName(),
//                credentials.getCredentials(),
//                region.getName(),
//                objectMapper,
//                registry));
//        newAgents.add(
//            new AzureSecurityGroupCachingAgent(
//                provider,
//                credentials.getName(),
//                credentials.getCredentials(),
//                region.getName(),
//                objectMapper,
//                registry));
//        newAgents.add(
//            new AzureNetworkCachingAgent(
//                provider,
//                credentials.getName(),
//                credentials.getCredentials(),
//                region.getName(),
//                objectMapper));
//        newAgents.add(
//            new AzureCustomImageCachingAgent(
//                provider,
//                credentials.getName(),
//                credentials.getCredentials(),
//                region.getName(),
//                credentials.getVmCustomImages(),
//                objectMapper));
//        newAgents.add(
//            new AzureManagedImageCachingAgent(
//                provider,
//                credentials.getName(),
//                credentials.getCredentials(),
//                region.getName(),
//                objectMapper));
//        newAgents.add(
//            new AzureServerGroupCachingAgent(
//                provider,
//                credentials.getName(),
//                credentials.getCredentials(),
//                region.getName(),
//                objectMapper,
//                registry));
//        newAgents.add(
//            new AzureAppGatewayCachingAgent(
//                provider,
//                credentials.getName(),
//                credentials.getCredentials(),
//                region.getName(),
//                objectMapper,
//                registry));
//
//        if (cloudProvider.getAgentScheduler() != null) {
//          ProviderUtils.rescheduleAgents(cloudProvider, newAgents);
//        }
//        cloudProvider.getAgents().addAll(newAgents);
//      }
//    }
//  }
}
