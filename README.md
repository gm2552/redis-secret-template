# redis-secret-template
A demonstration of using Tanzu SecretTemplate to create a K8s compatible service binding secret along side a Redis instance.

This is POC demonstrating:

* Using a Redis distribution and Operator to deploy either a standalone or clustered instance of Redis. 
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
* helm>3.0.0 (for use with the Opstree Solutions operator).

It also assumes `kubectl`, and `tanzu cli` are configured to access a TAP cluster described above.

## Install and Run POC

You will need to create namespace to run the operator and Redis instances and namespace to deploy the sample application/workload.  For this POC, you will use the namespace `service-instances` for you Redis instances and a namespace of your choice for the workloads.  Create the namespace for the Redis instances if it doesn't already exist using the following command:

```
kubectl create ns service-instances
```

### Checkout/Clone the Code

All of the configuration as well as the sample application are located in a central GitRepositoy.  Clone the Git Repo to your workstating running the following command:

```
git clone https://github.com/gm2552/redis-secret-template
```

### Install a Redis Operator

You will now need to install a Redis operator as well the appropriate TAP services toolkit resources.  You will also deploy an instance of Redis using the operator.  Instruction for each supported operator are at the links below.  

* [Opstree Solution OSS Operator](docs/opsTreeOperator.md)
* [Redis Cluster Enterprise Operator](docs/redisClusterEnterprisOperator.md)

**NOTE**
In either operator selective above, you will give your Redis instance a name.  You will need that instance name for the next section when deploying the workload.

### Deploy Workload

Navigate to the directory where you cloned the repo and deploy the sample workload to your TAP by running the following command substituting <INSTANCE_NAME> with the redis instance name you used the previous section.

```
cd templates
ytt -f workloadTemplate.yaml -v instance_name=<INSTANCE_NAME> -v workload_namespace=workloads | kubectl apply -f-
```

After a few minutes (depending on network latency and caching), you can validate the application deploy successfully running the following command:

```
tanzu apps workloads get student-redis-sample -n workloads
```

If the application was deployed successfully, you should see an output similar to the following:

```
---
# student-redis-sample: Ready
.
.
.
Pods
NAME                                                     STATUS      RESTARTS   AGE
student-redis-sample-00001-deployment-6846f988b-r98mb    Running     4          2m45s
student-redis-sample-00002-deployment-7cc94b89cc-xtkzp   Running     0          2m45s
student-redis-sample-build-1-build-pod                   Succeeded   0          15m
student-redis-sample-config-writer-4kgjw-pod             Succeeded   0          3m20s

Knative Services
NAME                   READY   URL
student-redis-sample   Ready   https://student-redis-sample.perfect300rock.com
---
```

You can navigate to the URL above appending `/student` to the end of the URL to ensure that application is communication successfully with the Redis instance.  You should see something similar to below:

```
[{"id":"Eng2015001","name":"John Doe","gender":"MALE","grade":1}]
```
