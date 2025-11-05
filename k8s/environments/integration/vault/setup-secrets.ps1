# Configuration Vault pour l'environnement Integration

Write-Host "🔐 Configuration des secrets Vault - INTEGRATION" -ForegroundColor Cyan
Write-Host ""

$env:VAULT_ADDR = 'http://localhost:8200'
$env:VAULT_TOKEN = 'root'

Write-Host "📝 Configuration des secrets pour Integration..." -ForegroundColor Yellow

# MySQL Integration
kubectl exec -n vault vault-0 -- vault kv put secret/integration/mysql `
  host="mysql-service.databases.svc.cluster.local" `
  port="3306" `
  database="recipeyoulove_integration" `
  username="integration_user" `
  password="integration_password_2024" `
  root_password="root_integration_2024"

Write-Host "✅ Secrets MySQL Integration configurés" -ForegroundColor Green

# MongoDB Integration
kubectl exec -n vault vault-0 -- vault kv put secret/integration/mongodb `
  host="mongodb-service.databases.svc.cluster.local" `
  port="27017" `
  database="recipeyoulove_integration" `
  username="integration_mongo_user" `
  password="mongo_integration_2024" `
  root_username="admin" `
  root_password="mongo_root_integration_2024"

Write-Host "✅ Secrets MongoDB Integration configurés" -ForegroundColor Green

# Application Integration
kubectl exec -n vault vault-0 -- vault kv put secret/integration/application `
  server_port="8080" `
  jpa_ddl_auto="update" `
  jpa_show_sql="true" `
  jpa_format_sql="true" `
  jpa_use_sql_comments="true" `
  log_level_jdbc="DEBUG" `
  log_level_hibernate_sql="DEBUG" `
  log_level_hibernate_binder="TRACE" `
  actuator_endpoints="health,info,metrics,prometheus" `
  actuator_health_details="always" `
  environment="integration"

Write-Host "✅ Secrets Application Integration configurés" -ForegroundColor Green
Write-Host ""
Write-Host "🎉 Tous les secrets Integration sont configurés!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Secrets créés:" -ForegroundColor Cyan
Write-Host "  - secret/integration/mysql" -ForegroundColor Gray
Write-Host "  - secret/integration/mongodb" -ForegroundColor Gray
Write-Host "  - secret/integration/application" -ForegroundColor Gray

