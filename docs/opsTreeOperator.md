# Opstree Solutions Redis Operator Install

This section will take you through the instructions to install the [Opstree Solutions Redis operator](https://ot-container-kit.github.io/redis-operator/guide/) as well as the necessary TAP services toolkit resources.  It will also have you deploy a Redis standalone instance.

## Install the Redis Operator

To install the operator, you will need to run the following help commands:

```
helm repo add ot-helm https://ot-container-kit.github.io/helm-charts/
helm upgrade redis-operator ot-helm/redis-operator --install --namespace redis-operator
```

Validate the operator is installed with the following command:

```
kubectl get pods -n redis-operator
```

You should see something similar to the following:

```
NAME                              READY   STATUS    RESTARTS   AGE
redis-operator-74b6cbf5c5-td8t7   1/1     Running   0          2m11s
```

## Deploy a Standalone Redis Instance

Navigate to the directory where you cloned the repo, and run the following commands to deploy an instance of a standalone Redis server to the *service-instances* namespace.

```
cd templates
ytt -f opsTreeOperator/redisStandaloneTemplate.yaml -v service_namespace=service-instances -v instance_name=redis-standalone-test | kubectl apply -f-
```

It may take a few minutes for the cluster pod to get up and running.

If successful, a secret compliant with the service binding spec should be generated.  Verify the secret exists by running the following command:

```
kubectl get secret redis-standalone-test-redis-secret -n service-instances
```

You should see something similar to the following:

```
NAME                                 TYPE     DATA   AGE
redis-standalone-test-redis-secret   Opaque   5      59s 
```

### Create ClusterInstanceClass and ResourceClaims

To make the Redis instances discoverable by the tanzu cli, you will need to install the ClusterInstanceClass CRs.  You can do so by running the following command from the *template* directory.

```
kubectl apply -f opsTreeOperator/redisInstanceClasses.yaml
```

Validate that the class exists and that you can see your new standalone Redis instance exists.  Run the following command:

```
tanzu service class list
```

You should see something similar to the following:

```
  NAME              DESCRIPTION                                     
  ...                        
  redis-cluster     Redis Cluster (Multi Instance Leader/Follower) from Opstree
  redis-standalone  Standalone Redis (Single Instance) from Opstree
```

Run the following command to view your unclaimed Redis instance:

```
tanzu service claimable list --class redis-standalone -n service-instances
```

You should see something similar to the following:

```
  NAME                                NAMESPACE          KIND    APIVERSION  
  redis-standalone-test-redis-secret  service-instances  Secret  v1   
```

### Create a Resource Claim

This step will manually create a claim for your Redis instance and make it available to other workload namespaces.  Run the following command to create the resource claim (this assumes that you will deploy workloads to a namespace named `workloads`):

```
ytt -f redisResourceClaimTemplate.yaml -v service_namespace=service-instances -v instance_name=redis-standalone-test -v workload_namespace=workloads | kubectl apply -f-
```

Verify that the resource claim is available with the following command:

```
tanzu service claims list -n workloads
```

You should see something similar to the following:

```
  NAME                   READY  REASON  
  ...  
  redis-standalone-test  True   Ready   
```

You are now ready to start deploying workloads using this service claim.