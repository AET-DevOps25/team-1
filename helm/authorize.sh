# dev 环境
kubectl create namespace aihr-dev        # 已存在会提示 AlreadyExists，可忽略
kubectl create rolebinding aihr-dev-admin \
  --clusterrole=admin \
  --user=$(kubectl config view --minify -o jsonpath='{.contexts[0].context.user}') \
  --namespace=aihr-dev

# prod 环境（以后真正发布时再做）
kubectl create namespace aihr
kubectl create rolebinding aihr-admin \
  --clusterrole=admin \
  --user=$(kubectl config view --minify -o jsonpath='{.contexts[0].context.user}') \
  --namespace=aihr