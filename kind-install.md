# Run this app with KIND (Kubernetes in Local)

I am trying to set this app in Kubernetes. Deploy with KNative/ Istio and showcase how VertX can work well
with Serverless Architecture.


## Steps Followed

1. Create a KIND Cluster

```sh
kind create cluster --name istio-test
```

Setup Kubernetes Dashboard

```sh
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.0-beta8/aio/deploy/recommended.yaml

# Cluster Role Binding
kubectl create clusterrolebinding default-admin --clusterrole cluster-admin --serviceaccount=default:default

# Get Token
token=$(kubectl get secrets -o \
jsonpath="{.items[?(@.metadata.annotations['kubernetes\.io/service-account\.name']=='default')].data.token}" \
|base64 --decode)

echo $token

k proxy
```

2. Install Istio - Minimal Installation for now

Install Istioctl

```sh
brew install istioctl
istioctl manifest apply --set profile=default --set values.gateways.istio-ingressgateway.type=NodePort

```


3. Install KNative
ls
```sh
cd ./knative
./apply.sh
```




https://istio.io/docs/setup/platform-setup/kind/
