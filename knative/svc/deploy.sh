#!/usr/bin/env bash

kind load docker-image --name istio-test dev.local/fmt:0.0.1
kubectl apply -f ./fmt-svc.yaml


#curl -v -i -H "Host: knative-vertx-fmt.default.example.com" http://10.244.0.7:80/accounts

#curl -v -i -H "Host: knative-vertx-fmt.default.example.com" http://localhost:${NODE_PORT}/accounts

#curl -v -i -H "Host: knative-vertx-fmt.default.example.com" http://10.101.98.186:80/accounts
