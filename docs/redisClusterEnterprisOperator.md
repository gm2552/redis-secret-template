# Redis Enterprise Cluster Operator Install

This section will take you through the instructions to install the [Redis Enterprise Cluster operator](https://docs.redis.com/latest/kubernetes/) as well as the necessary TAP services toolkit resources.  It will also have you deploy a Redis Enterprise cluster and database instance.

*NOTE*  The default deployment options of the Redis Enterprise Cluster operator requires an operator per namespace, and cluster instances are deployed in the same namespace as the operator.

## Install the Redis Operator

To install the operator, you will need to run the following help commands:

```
kubectl apply -f https://raw.githubusercontent.com/RedisLabs/redis-enterprise-k8s-docs/v6.2.10-45/bundle.yaml -n service-instances
```

Validate the operator is installed with the following command:

```
kubectl get pods -l name=redis-enterprise-operator -n service-instances
```

You should see something similar to the following:

```
NAME                                         READY   STATUS    RESTARTS   AGE
redis-enterprise-operator-74849d8c69-pnqvq   2/2     Running   0          39s
```

## Deploy a Redis Enterprise Cluster and Database

Navigate to the directory where you cloned the repo, and run the following commands to deploy an instance of a Redis cluster to the *service-instances* namespace.

```
cd templates
ytt -f redisEnterpriseClusterOperator/redisEnterpriseClusterTemplate.yaml -v service_namespace=service-instances -v instance_name=redis-test | kubectl apply -f-
```

If successful, a secret compliant with the service binding spec should be generated.  Verify the secret exists by running the following command:

```
kubectl get secret redis-test-redis-secret -n service-instances
```

You should see something similar to the following:

```
NAME                                 TYPE     DATA   AGE
redis-standalone-test-redis-secret   Opaque   5      59s 
```

### Create ClusterInstanceClass and ResourceClaims

To make the Redis instances discoverable by the tanzu cli, you will need to install the ClusterInstanceClass CRs.  You can do so by running the following command from the *template* directory.

```
kubectl apply -f redisEnterpriseClusterOperator/redisInstanceClasses.yaml
```

Validate that the class exists and that you can see your new Redis Enterprise Cluster instance exists.  Run the following command:

```
tanzu service class list
```

You should see something similar to the following:

```
  NAME                     DESCRIPTION                  
  ...           
  redis-enteprise-cluster  Redis Enterprise Cluster   
```

Run the following command to view your unclaimed Redis instance:

```
tanzu service claimable list --class redis-enterprise-cluster -n service-instances
```

You should see something similar to the following:

```
  NAME                                NAMESPACE          KIND    APIVERSION  
  redis-test-redis-secret  service-instances  Secret  v1 
```

### Create a Resource Claim

This step will manually create a claim for your Redis instance and make it available to other workload namespaces.  Run the following command to create the resource claim (this assumes that you will deploy workloads to a namespace named `workloads`):

```
ytt -f redisResourceClaimTemplate.yaml -v service_namespace=service-instances -v instance_name=redis-test -v workload_namespace=workloads | kubectl apply -f-
```

Verify that the resource claim is available with the following command:

```
tanzu service claims list -n workloads
```

You should see something similar to the following:

```
  NAME           READY  REASON            
  ...           
  redis-test     True   Ready             
```

You are now ready to start deploying workloads using this service claim.