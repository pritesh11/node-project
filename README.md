docker build -t node-project:latest .

kubectl create -f kubernetes/node-project.yml
kubectl create -f kubernetes/Ingress.yml
