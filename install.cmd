minikube.exe addons enable ingress

kubectl.exe apply -f .\kubernetes\tekton.yaml

kubectl.exe apply -f .\kubernetes\tekton-dashboard.yaml

kubectl.exe get pod -n tekton-pipelines --watch