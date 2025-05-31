# dev 环境
#kubectl create namespace ai-hr-dev        # 已存在会提示 AlreadyExists，可忽略
kubectl create rolebinding ai-hr-dev-admin \
  --clusterrole=admin \
  --user=$(kubectl config view --minify -o jsonpath='{.contexts[0].context.user}') \
  --namespace=ai-hr-dev

# prod 环境（以后真正发布时再做）
#kubectl create namespace ai-hr
kubectl create rolebinding ai-hr-admin \
  --clusterrole=admin \
  --user=$(kubectl config view --minify -o jsonpath='{.contexts[0].context.user}') \
  --namespace=ai-hr