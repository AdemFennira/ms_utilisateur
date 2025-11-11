# Script d'installation des composants Kubernetes pour SmartDish
# À exécuter dans PowerShell

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Installation Kubernetes - SmartDish" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Créer les namespaces
Write-Host "1. Creation des namespaces..." -ForegroundColor Yellow
kubectl apply -f k8s/oci/namespace.yaml
Write-Host "   Namespaces crees" -ForegroundColor Green
Write-Host ""

# 2. Installer NGINX Ingress Controller
Write-Host "2. Installation de NGINX Ingress Controller..." -ForegroundColor Yellow
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.9.4/deploy/static/provider/cloud/deploy.yaml
Write-Host "   NGINX Ingress installe" -ForegroundColor Green
Write-Host ""

# 3. Attendre que NGINX soit prêt
Write-Host "3. Attente que NGINX soit pret (peut prendre 2-3 minutes)..." -ForegroundColor Yellow
kubectl wait --namespace ingress-nginx --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=300s
Write-Host "   NGINX pret" -ForegroundColor Green
Write-Host ""

# 4. Créer les ConfigMaps
Write-Host "4. Creation des ConfigMaps..." -ForegroundColor Yellow
kubectl apply -f k8s/oci/configmap.yaml
Write-Host "   ConfigMaps crees" -ForegroundColor Green
Write-Host ""

# 5. Déployer MySQL
Write-Host "5. Deploiement de MySQL dans Kubernetes..." -ForegroundColor Yellow
kubectl apply -f k8s/oci/mysql-deployment.yaml
Write-Host "   MySQL deploye" -ForegroundColor Green
Write-Host ""

# 6. Créer les secrets MySQL pour smartdish
Write-Host "6. Creation des secrets MySQL..." -ForegroundColor Yellow
kubectl create secret generic mysql-secrets `
  --from-literal=MYSQL_HOST=mysql.smartdish.svc.cluster.local `
  --from-literal=MYSQL_PORT=3306 `
  --from-literal=MYSQL_DATABASE=smartdish `
  --from-literal=MYSQL_USER=admin `
  --from-literal=MYSQL_PASSWORD=SecurePass123! `
  --from-literal=MYSQL_ROOT_PASSWORD=RootPass123! `
  --namespace=smartdish `
  --dry-run=client -o yaml | kubectl apply -f -
Write-Host "   Secret cree pour namespace smartdish" -ForegroundColor Green

# 7. Créer les secrets MySQL pour smartdish-prod
kubectl create secret generic mysql-secrets `
  --from-literal=MYSQL_HOST=mysql.smartdish.svc.cluster.local `
  --from-literal=MYSQL_PORT=3306 `
  --from-literal=MYSQL_DATABASE=smartdish `
  --from-literal=MYSQL_USER=admin `
  --from-literal=MYSQL_PASSWORD=SecurePass123! `
  --from-literal=MYSQL_ROOT_PASSWORD=RootPass123! `
  --namespace=smartdish-prod `
  --dry-run=client -o yaml | kubectl apply -f -
Write-Host "   Secret cree pour namespace smartdish-prod" -ForegroundColor Green
Write-Host ""

# 8. Vérifier l'installation
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Verification de l'installation" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Namespaces:" -ForegroundColor Yellow
kubectl get namespaces | Select-String "smartdish"
Write-Host ""

Write-Host "Pods dans smartdish:" -ForegroundColor Yellow
kubectl get pods -n smartdish
Write-Host ""

Write-Host "Services dans smartdish:" -ForegroundColor Yellow
kubectl get svc -n smartdish
Write-Host ""

Write-Host "NGINX Ingress Controller:" -ForegroundColor Yellow
kubectl get pods -n ingress-nginx
Write-Host ""

Write-Host "Load Balancer IP:" -ForegroundColor Yellow
kubectl get svc -n ingress-nginx ingress-nginx-controller
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Installation terminee avec succes !" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Prochaine etape:" -ForegroundColor Yellow
Write-Host "  Testez votre premier deploiement avec:" -ForegroundColor White
Write-Host "  git checkout -b feat/test-deployment" -ForegroundColor White
Write-Host "  echo '# Test' >> TEST.md" -ForegroundColor White
Write-Host "  git add TEST.md" -ForegroundColor White
Write-Host "  git commit -m 'test: Premier deploiement'" -ForegroundColor White
Write-Host "  git push origin feat/test-deployment" -ForegroundColor White
Write-Host ""

