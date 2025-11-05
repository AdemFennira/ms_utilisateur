# Configuration Vault pour l'environnement Production

Write-Host "🚀 Configuration des secrets Vault - PRODUCTION" -ForegroundColor Green
Write-Host ""
Write-Host "⚠️  ATTENTION: Configuration des secrets PRODUCTION" -ForegroundColor Red
Write-Host ""

$confirmation = Read-Host "Voulez-vous continuer? (oui/non)"
if ($confirmation -ne "oui") {
    Write-Host "❌ Configuration annulée" -ForegroundColor Red
    exit 1
}

$env:VAULT_ADDR = 'http://localhost:8200'
$env:VAULT_TOKEN = 'root'

Write-Host ""
Write-Host "📝 Configuration des secrets pour Production..." -ForegroundColor Yellow

# MySQL Production
kubectl exec -n vault vault-0 -- vault kv put secret/production/mysql `
  host="mysql-service.databases.svc.cluster.local" `
  port="3306" `
  database="recipeyoulove_production" `
  username="production_user" `
  password="Pr0d_P@ssw0rd_2024!" `
  root_password="R00t_Pr0d_P@ssw0rd_2024!"

Write-Host "✅ Secrets MySQL Production configurés" -ForegroundColor Green

# MongoDB Production
kubectl exec -n vault vault-0 -- vault kv put secret/production/mongodb `
  host="mongodb-service.databases.svc.cluster.local" `
  port="27017" `
  database="recipeyoulove_production" `
  username="production_mongo_user" `
  password="M0ng0_Pr0d_P@ssw0rd_2024!" `
  root_username="admin" `
  root_password="M0ng0_R00t_Pr0d_2024!"

Write-Host "✅ Secrets MongoDB Production configurés" -ForegroundColor Green

# Application Production
kubectl exec -n vault vault-0 -- vault kv put secret/production/application `
  server_port="8080" `
  jpa_ddl_auto="validate" `
  jpa_show_sql="false" `
  jpa_format_sql="false" `
  jpa_use_sql_comments="false" `
  log_level_jdbc="INFO" `
  log_level_hibernate_sql="WARN" `
  log_level_hibernate_binder="WARN" `
  actuator_endpoints="health,info,metrics,prometheus" `
  actuator_health_details="when-authorized" `
  environment="production"

Write-Host "✅ Secrets Application Production configurés" -ForegroundColor Green
Write-Host ""
Write-Host "🎉 Tous les secrets Production sont configurés!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Secrets créés:" -ForegroundColor Cyan
Write-Host "  - secret/production/mysql" -ForegroundColor Gray
Write-Host "  - secret/production/mongodb" -ForegroundColor Gray
Write-Host "  - secret/production/application" -ForegroundColor Gray

