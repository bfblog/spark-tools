kubectl.exe apply -f tekton/tasks
kubectl.exe apply -f tekton/pipelines

tkn.exe tasks list -o name
tkn.exe pipelines list -o name
