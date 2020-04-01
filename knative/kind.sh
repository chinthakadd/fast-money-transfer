kind create cluster --name istio-test --config=./kind-config.yaml

kubectl apply -f ./kube-dashboard.yaml

# Cluster Role Binding
kubectl create clusterrolebinding default-admin --clusterrole cluster-admin --serviceaccount=default:default

# Get Token
token=$(kubectl get secrets -o \
jsonpath="{.items[?(@.metadata.annotations['kubernetes\.io/service-account\.name']=='default')].data.token}" \
|base64 --decode)

echo $token

sleep 5

istioctl manifest apply --set profile=default --set values.gateways.istio-ingressgateway.type=NodePort
export NODE_PORT=$(kubectl --namespace istio-system get service/istio-ingressgateway -o json | jq -c '.spec.ports[] | select(.name=="http2") | .nodePort')
