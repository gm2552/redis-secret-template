# redis-secret-template
A demonstration of using Tanzu SecretTemplate to create a K8s compatible service binding secret along side a Redis instance.

This is POC demonstrating:

* Using an open source Redis distribution and Operator to deploy either a standalone or clustered instance of Redis
* Creeating a Kubernetes Service Binding compliant secret using a SecretTemplate
* Claiming the Redis instance with the service toolkit.
* Consumer the Redis instance from a workload.

## Prerequisites

These instructions assume that you have a TAP 1.2.x or greater iterate cluster (or some variant similar to an iterate cluster) up and running with the following packages installed and kubectl and the Tanzu CLI installed and configured to access your TAP cluster:

* Tanzu Build Services
* Tanzu Cloud Native Runtimes
* Tanzu Service Bindings
* Tanzu Services Toolkit
* Tanzu Out of the Box Supply Chains
* Tanzu Out of the Box Templates
* Tanzu Source Controller

You also need the following tools installed on your local workstation:

* git
* ytt
* kubectl
* tanzu cli
* helm>3.0.0

It also assumes `kubectl`, and `tanzu cli` are configured to access a TAP cluster described above.

## Install and Run POC

### Install the Redis Operator

This POC uses a Redis operator from [OperatorHub.io](https://operatorhub.io/operator/redis-operator).  To install the operator, you will need to run the following help commands:

```
kubectl create ns service-instances

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

### Checkout/Clone the Code

Clone the Git Repo to your workstating running the following command:

```
git clone https://github.com/gm2552/redis-secret-template
```

### Deploy a Standalone Redis Instance

Navigate to the directory where you cloned the repo, and run the following commands to deploy an instance of a standalone Redis server to the "service-instances" namespace.

```
cd templates
ytt -f redisStandaloneTemplate.yaml -v service_namespace=service-instances -v instance_name=redis-standalone-test | kb apply -f-
```

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
kubectl apply -f redisInstanceClasses.yaml
```

Validate that the class exists and that you can see your new standalone Redis instance exists.  Run the following command:

```
tanzu service class list
```

You should see something similar to the following:

```
  NAME              DESCRIPTION                                     
  ...                        
  redis-cluster     Redis Cluster (Multi Instance Leader/Follower)  
  redis-standalone  Standalone Redis (Single Instance)  
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

This step will manually create a claim for Redis instance and make it available to other workload namespaces.  Run the following command to create the resource claim (this assumes that you deploy workloads to a namespace named `workloads`):

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

### Deploy Workload

Deploy the sample workload to your TAP by running the following command:

```
ytt -f workloadTemplate.yaml -v instance_name=redis-standalone-test -v workload_namespace=workloads | kubectl apply -f-
```

After a few minutes (depending on network latency and caching), you can validate the application deploy successfully running the following command:

```
tanzu apps workloads get student-redis-sample -n workloads
```