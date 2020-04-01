#!/usr/bin/env bash

kubectl apply -f ./namespace.yaml
kubectl apply -f ./configmaps.yaml
kubectl apply -f ./serving-core.yaml
kubectl apply -f ./serving-istio.yaml
#kubectl apply -f ./serving-default-domain.yaml

