kind create cluster --name istio-test --config=./kind-config.yaml

kubectl apply -f ./kube-dashboard.yaml

# Cluster Role Binding
kubectl create clusterrolebinding default-admin --clusterrole cluster-admin --serviceaccount=default:default

# Get Token
token=$(kubectl get secrets -o \
jsonpath="{.items[?(@.metadata.annotations['kubernetes\.io/service-account\.name']=='default')].data.token}" \
|base64 --decode)

echo $token

