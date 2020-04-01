#!/usr/bin/env bash

https://hub.kubeapps.com/charts/stable/kube-ops-view/1.1.4

kubectl apply -f ./metric-server/.
helm repo add stable https://kubernetes-charts.storage.googleapis.com
helm install stable/kube-ops-view --version 1.1.4 --generate-name

echo "Waiting for installation to complete"
sleep 10

kubectl port-forward service/kube-ops-view-1585712975 8080:80
