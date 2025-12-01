package com.springbootTemplate.univ.soa.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class DotenvConfig implements EnvironmentPostProcessor {

    private static final Logger logger = Logger.getLogger(DotenvConfig.class.getName());

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            // Essayer plusieurs emplacements possibles
            String[] possiblePaths = {
                    "./", // Racine du projet (pour mvn spring-boot:run)
                    ".",  // Répertoire courant
                    "src/main/resources", // Dans les ressources
                    ""    // Répertoire de travail courant
            };

            Dotenv dotenv = null;
            String foundPath = null;

            for (String path : possiblePaths) {
                try {
                    dotenv = Dotenv.configure()
                            .directory(path)
                            .ignoreIfMissing()
                            .load();

                    // Vérifier si au moins une variable est chargée
                    if (!dotenv.entries().isEmpty()) {
                        foundPath = path.isEmpty() ? "current directory" : path;
                        break;
                    }
                } catch (Exception e) {
                    // Continuer avec le chemin suivant
                    logger.fine("Tentative échouée pour le chemin: " + path);
                }
            }

            if (dotenv == null || dotenv.entries().isEmpty()) {
                logger.warning("⚠️ Aucun fichier .env trouvé dans les emplacements standards");
                return;
            }

            Map<String, Object> envProperties = new HashMap<>();
            dotenv.entries().forEach(entry -> envProperties.put(entry.getKey(), entry.getValue()));

            MapPropertySource propertySource = new MapPropertySource("dotenv-properties", envProperties);
            environment.getPropertySources().addFirst(propertySource);

            logger.info("✅ Fichier .env chargé depuis: " + foundPath + " - " + envProperties.size() + " variables trouvées");

        } catch (Exception e) {
            logger.severe("❌ Erreur lors du chargement du fichier .env : " + e.getMessage());
        }
    }
}