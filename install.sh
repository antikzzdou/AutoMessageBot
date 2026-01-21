#!/bin/bash

################################################################################
#                                                                              #
#         ZONE AUTO MESSAGE - Script de Instalación de Estructura             #
#                                                                              #
#  Este script crea automáticamente toda la estructura de carpetas y          #
#  archivos necesarios para el proyecto Minecraft Fabric.                     #
#                                                                              #
#  Uso: bash setup_project.sh  O  chmod +x setup_project.sh && ./setup_project.sh
#                                                                              #
#  Autor: NeoKey                                                              #
#  Versión: 1.0.0                                                             #
#  Fecha: Enero 2025                                                          #
#                                                                              #
################################################################################

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Variables
PROJECT_NAME="zoneautomessage"
PROJECT_PATH="."
PACKAGE_NAME="com/neokey/zoneautomessage"
JAVA_PATH="src/main/java/${PACKAGE_NAME}"
RESOURCES_PATH="src/main/resources"

# Contador de archivos creados
FILES_CREATED=0
DIRS_CREATED=0

################################################################################
# FUNCIONES AUXILIARES
################################################################################

print_header() {
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}║${NC} $1"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
}

print_section() {
    echo -e "${CYAN}▶ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

create_directory() {
    if [ ! -d "$1" ]; then
        mkdir -p "$1"
        print_success "Carpeta creada: $1"
        ((DIRS_CREATED++))
    else
        print_warning "Carpeta ya existe: $1"
    fi
}

create_placeholder_file() {
    local file_path="$1"
    local class_name="$2"
    local package_name="$3"
    
    if [ ! -f "$file_path" ]; then
        cat > "$file_path" << 'EOF'
// ════════════════════════════════════════════════════════════════════════════
// PLACEHOLDER - Reemplazar con contenido real
// ════════════════════════════════════════════════════════════════════════════
// 
// Este archivo es un placeholder. Copia el contenido desde la documentación
// y reemplaza todo el archivo.
//
// Clase: CLASS_NAME
// Paquete: PACKAGE_NAME
//
// ════════════════════════════════════════════════════════════════════════════

package PACKAGE_NAME;

/**
 * PLACEHOLDER - Reemplazar con clase real
 * Nombre: CLASS_NAME
 */
public class CLASS_NAME {
    // TODO: Copiar contenido desde la documentación
}
EOF
        
        # Reemplazar placeholders
        sed -i "s/CLASS_NAME/$class_name/g" "$file_path"
        sed -i "s|PACKAGE_NAME|$package_name|g" "$file_path"
        
        print_success "Placeholder creado: $file_path"
        ((FILES_CREATED++))
    else
        print_warning "Archivo ya existe: $file_path (no se sobrescribió)"
    fi
}

create_json_placeholder() {
    local file_path="$1"
    local description="$2"
    
    if [ ! -f "$file_path" ]; then
        cat > "$file_path" << 'EOF'
{
  "_comment": "PLACEHOLDER - JSON file",
  "description": "DESCRIPTION_PLACEHOLDER",
  "status": "pending",
  "note": "Reemplaza todo el contenido de este archivo con el contenido correcto desde la documentación"
}
EOF
        
        sed -i "s|DESCRIPTION_PLACEHOLDER|$description|g" "$file_path"
        
        print_success "Placeholder JSON creado: $file_path"
        ((FILES_CREATED++))
    else
        print_warning "Archivo ya existe: $file_path (no se sobrescribió)"
    fi
}

create_text_placeholder() {
    local file_path="$1"
    local description="$2"
    
    if [ ! -f "$file_path" ]; then
        cat > "$file_path" << EOF
================================================================================
PLACEHOLDER - Archivo de configuración
================================================================================

Descripción: $description

Estado: PENDIENTE - Reemplazar con contenido real

Instrucciones:
1. Abre este archivo
2. Copia todo el contenido desde la documentación
3. Reemplaza este contenido completamente
4. Guarda el archivo

================================================================================
EOF
        
        print_success "Placeholder creado: $file_path"
        ((FILES_CREATED++))
    else
        print_warning "Archivo ya existe: $file_path (no se sobrescribió)"
    fi
}

################################################################################
# INICIO DEL SCRIPT
################################################################################

print_header "INSTALADOR DE ESTRUCTURA - Zone Auto Message Mod"

echo -e "${YELLOW}Este script creará toda la estructura de carpetas necesaria${NC}"
echo -e "${YELLOW}para el desarrollo del mod Minecraft Fabric.${NC}"
echo ""
echo -e "Proyecto: ${BLUE}$PROJECT_NAME${NC}"
echo -e "Ruta: ${BLUE}$PROJECT_PATH${NC}"
echo ""

# Confirmar
read -p "¿Deseas continuar? (s/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Ss]$ ]]; then
    print_error "Instalación cancelada"
    exit 1
fi

echo ""

################################################################################
# 1. CREAR ESTRUCTURA DE CARPETAS
################################################################################

print_header "PASO 1: Creando estructura de carpetas"

print_section "Carpetas raíz"
create_directory "gradle/wrapper"
create_directory "src"
create_directory "src/main"
create_directory "src/test"

print_section "Carpetas Java (src/main/java)"
create_directory "$JAVA_PATH"
create_directory "$JAVA_PATH/zone"
create_directory "$JAVA_PATH/manager"
create_directory "$JAVA_PATH/event"
create_directory "$JAVA_PATH/util"

print_section "Carpetas Resources (src/main/resources)"
create_directory "$RESOURCES_PATH"
create_directory "$RESOURCES_PATH/assets/$PROJECT_NAME"
create_directory "$RESOURCES_PATH/assets/$PROJECT_NAME/icon"
create_directory "$RESOURCES_PATH/assets/$PROJECT_NAME/textures"
create_directory "$RESOURCES_PATH/lang"

print_section "Carpetas Build"
create_directory "build"
create_directory "build/libs"

echo ""

################################################################################
# 2. CREAR ARCHIVOS JAVA CON PLACEHOLDERS
################################################################################

print_header "PASO 2: Creando archivos Java con placeholders"

print_section "Clase Principal"
create_placeholder_file "$JAVA_PATH/ZoneAutoMessageMod.java" "ZoneAutoMessageMod" "com.neokey.zoneautomessage"

print_section "Paquete zone"
create_placeholder_file "$JAVA_PATH/zone/Zone.java" "Zone" "com.neokey.zoneautomessage.zone"

print_section "Paquete manager"
create_placeholder_file "$JAVA_PATH/manager/ZoneManager.java" "ZoneManager" "com.neokey.zoneautomessage.manager"
create_placeholder_file "$JAVA_PATH/manager/ConfigManager.java" "ConfigManager" "com.neokey.zoneautomessage.manager"
create_placeholder_file "$JAVA_PATH/manager/MessageManager.java" "MessageManager" "com.neokey.zoneautomessage.manager"

print_section "Paquete event"
create_placeholder_file "$JAVA_PATH/event/PlayerTickHandler.java" "PlayerTickHandler" "com.neokey.zoneautomessage.event"

print_section "Paquete util"
create_placeholder_file "$JAVA_PATH/util/Utilities.java" "Utilities" "com.neokey.zoneautomessage.util"

echo ""

################################################################################
# 3. CREAR ARCHIVOS DE CONFIGURACIÓN JSON
################################################################################

print_header "PASO 3: Creando archivos JSON con placeholders"

print_section "Configuración del Mod"
create_json_placeholder "$RESOURCES_PATH/fabric.mod.json" "Metadata del mod Fabric"
create_json_placeholder "$RESOURCES_PATH/lang/en_us.json" "Strings de idioma (Inglés)"

echo ""

################################################################################
# 4. CREAR ARCHIVOS DE CONFIGURACIÓN GRADLE
################################################################################

print_header "PASO 4: Creando archivos Gradle"

print_section "build.gradle"
if [ ! -f "build.gradle" ]; then
    cat > "build.gradle" << 'EOF'
// PLACEHOLDER - Reemplazar con configuración real desde la documentación

plugins {
    id 'fabric-loom' version '1.14.+'
    id 'maven-publish'
}

sourceCompatibility = JavaVersion.VERSION_21
targetCompatibility = JavaVersion.VERSION_21

// TODO: Copiar contenido completo desde documentación
EOF
    print_success "Placeholder creado: build.gradle"
    ((FILES_CREATED++))
else
    print_warning "Archivo ya existe: build.gradle"
fi

print_section "gradle.properties"
if [ ! -f "gradle.properties" ]; then
    cat > "gradle.properties" << 'EOF'
# PLACEHOLDER - Reemplazar con propiedades reales desde la documentación

minecraft_version=1.21.8
yarn_mappings=1.21.8+build.1
loader_version=0.15.11
fabric_version=0.100.8+1.21.8
modmenu_version=10.1.0

archives_base_name=zoneautomessage
mod_version=1.0.0
maven_group=com.neokey.zoneautomessage

# TODO: Verificar todas las versiones
EOF
    print_success "Placeholder creado: gradle.properties"
    ((FILES_CREATED++))
else
    print_warning "Archivo ya existe: gradle.properties"
fi

print_section "settings.gradle"
if [ ! -f "settings.gradle" ]; then
    cat > "settings.gradle" << 'EOF'
pluginManagement {
    repositories {
        maven { url = 'https://maven.fabricmc.net/' }
        gradlePluginPortal()
    }
}
EOF
    print_success "Placeholder creado: settings.gradle"
    ((FILES_CREATED++))
else
    print_warning "Archivo ya existe: settings.gradle"
fi

echo ""

################################################################################
# 5. CREAR ARCHIVOS DE DOCUMENTACIÓN
################################################################################

print_header "PASO 5: Creando archivos de documentación"

print_section "README.md"
if [ ! -f "README.md" ]; then
    cat > "README.md" << 'EOF'
# Zone Auto Message Mod

## Estado: 🟡 EN CONSTRUCCIÓN

Este proyecto está siendo desarrollado. Los archivos placeholder deben ser reemplazados con el contenido real.

### Próximos pasos:

1. [ ] Reemplazar archivos Java con implementación real
2. [ ] Reemplazar fabric.mod.json
3. [ ] Reemplazar build.gradle
4. [ ] Compilar con `gradle build`
5. [ ] Instalar en `.minecraft/mods/`

### Instrucciones:

Consulta la documentación para copiar y pegar cada archivo en su ubicación correcta.

### Estructura de Carpetas:

```
zoneautomessage/
├── src/main/java/com/neokey/zoneautomessage/
│   ├── ZoneAutoMessageMod.java
│   ├── zone/Zone.java
│   ├── manager/
│   │   ├── ZoneManager.java
│   │   ├── ConfigManager.java
│   │   └── MessageManager.java
│   ├── event/PlayerTickHandler.java
│   └── util/Utilities.java
├── src/main/resources/
│   ├── fabric.mod.json
│   ├── assets/
│   └── lang/en_us.json
├── build.gradle
├── gradle.properties
└── settings.gradle
```

---

**Generado con**: setup_project.sh  
**Fecha**: $(date)
EOF
    print_success "Placeholder creado: README.md"
    ((FILES_CREATED++))
else
    print_warning "Archivo ya existe: README.md"
fi

print_section "LICENSE"
if [ ! -f "LICENSE" ]; then
    cat > "LICENSE" << 'EOF'
MIT License

Copyright (c) 2025 NeoKey

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.

---

Este archivo MIT debe ser mantenido en la raíz del proyecto.
EOF
    print_success "Placeholder creado: LICENSE"
    ((FILES_CREATED++))
else
    print_warning "Archivo ya existe: LICENSE"
fi

echo ""

################################################################################
# 6. CREAR ARCHIVO .gitignore
################################################################################

print_header "PASO 6: Creando archivo .gitignore"

if [ ! -f ".gitignore" ]; then
    cat > ".gitignore" << 'EOF'
# Gradle
.gradle/
build/
out/
gradle-app.setting
!gradle-wrapper.jar
.gradletasknamecache

# IDE
.idea/
.vscode/
*.iml
*.iws
*.ipr
*.swp
*.swo
*~

# OS
.DS_Store
Thumbs.db

# Project specific
*.jar
*.class

# Dependencies
libs/

# Config files
config/
EOF
    print_success "Creado: .gitignore"
    ((FILES_CREATED++))
else
    print_warning "Archivo ya existe: .gitignore"
fi

echo ""

################################################################################
# 7. CREAR ARCHIVO CHECKLIST
################################################################################

print_header "PASO 7: Creando checklist de configuración"

if [ ! -f "SETUP_CHECKLIST.md" ]; then
    cat > "SETUP_CHECKLIST.md" << 'EOF'
# Checklist de Configuración - Zone Auto Message Mod

## ✅ Estructura Creada

- [x] Carpetas de proyecto
- [x] Estructura Java (paquetes)
- [x] Carpetas de recursos
- [x] Archivos con placeholders

## 📝 Pasos Siguientes

### 1. Reemplazar Archivos Java

Cada archivo Java contiene un comentario PLACEHOLDER. Necesitas copiar el contenido real desde la documentación:

- [ ] **ZoneAutoMessageMod.java** - `src/main/java/com/neokey/zoneautomessage/`
- [ ] **Zone.java** - `src/main/java/com/neokey/zoneautomessage/zone/`
- [ ] **ZoneManager.java** - `src/main/java/com/neokey/zoneautomessage/manager/`
- [ ] **ConfigManager.java** - `src/main/java/com/neokey/zoneautomessage/manager/`
- [ ] **MessageManager.java** - `src/main/java/com/neokey/zoneautomessage/manager/`
- [ ] **PlayerTickHandler.java** - `src/main/java/com/neokey/zoneautomessage/event/`
- [ ] **Utilities.java** - `src/main/java/com/neokey/zoneautomessage/util/`

### 2. Reemplazar Archivos de Configuración

- [ ] **build.gradle** - Copia desde documentación
- [ ] **gradle.properties** - Copia desde documentación
- [ ] **fabric.mod.json** - Copia desde documentación
- [ ] **en_us.json** - Copia desde documentación (idioma)

### 3. Compilación y Testing

```bash
# Generar workspace
./gradlew genSources

# Compilar
./gradlew build

# JAR generado estará en: build/libs/zoneautomessage-1.0.0-client.jar
```

### 4. Instalación

```bash
# Copiar a mods
cp build/libs/zoneautomessage-1.0.0-client.jar ~/.minecraft/mods/
```

## 📋 Referencias

- Documentación Java: Ver archivos adjuntos
- Documentación JSON: Ver archivos adjuntos
- Guía de Instalación: guia_completa.md
- Ejemplos: ejemplos_practicos.md

## 🚀 Comandos Útiles

```bash
# Limpiar y compilar
./gradlew clean build

# Ejecutar con cliente de Minecraft
./gradlew runClient

# Validar JSON
python3 -m json.tool src/main/resources/fabric.mod.json

# Ver estructura de carpetas
tree -I 'build|.gradle' -L 3
```

## ⚠️ Notas Importantes

1. Java 21+ es REQUERIDO
2. No olvides validar archivos JSON
3. Mantener estructura de carpetas
4. Usar UTF-8 para encoding

---

**Fecha de creación**: $(date)
**Estado**: Estructura lista para desarrollo
EOF
    print_success "Checklist creado: SETUP_CHECKLIST.md"
    ((FILES_CREATED++))
else
    print_warning "Archivo ya existe: SETUP_CHECKLIST.md"
fi

echo ""

################################################################################
# 8. RESUMEN FINAL
################################################################################

print_header "INSTALACIÓN COMPLETADA"

echo ""
echo -e "${GREEN}Estadísticas:${NC}"
echo -e "  Directorios creados: ${BLUE}$DIRS_CREATED${NC}"
echo -e "  Archivos creados: ${BLUE}$FILES_CREATED${NC}"
echo ""

echo -e "${CYAN}Estructura de proyecto:${NC}"
echo ""
cat << 'EOF'
zoneautomessage/
├── 📁 gradle/wrapper/
├── 📁 src/
│   └── 📁 main/
│       ├── 📁 java/com/neokey/zoneautomessage/
│       │   ├── 📄 ZoneAutoMessageMod.java [PLACEHOLDER]
│       │   ├── 📁 zone/
│       │   │   └── 📄 Zone.java [PLACEHOLDER]
│       │   ├── 📁 manager/
│       │   │   ├── 📄 ZoneManager.java [PLACEHOLDER]
│       │   │   ├── 📄 ConfigManager.java [PLACEHOLDER]
│       │   │   └── 📄 MessageManager.java [PLACEHOLDER]
│       │   ├── 📁 event/
│       │   │   └── 📄 PlayerTickHandler.java [PLACEHOLDER]
│       │   └── 📁 util/
│       │       └── 📄 Utilities.java [PLACEHOLDER]
│       └── 📁 resources/
│           ├── 📄 fabric.mod.json [PLACEHOLDER]
│           ├── 📁 assets/zoneautomessage/
│           │   ├── 📁 icon/
│           │   └── 📁 textures/
│           └── 📁 lang/
│               └── 📄 en_us.json [PLACEHOLDER]
├── 📄 build.gradle [PLACEHOLDER]
├── 📄 gradle.properties [PLACEHOLDER]
├── 📄 settings.gradle
├── 📄 README.md
├── 📄 LICENSE
├── 📄 .gitignore
└── 📄 SETUP_CHECKLIST.md

EOF

echo ""
echo -e "${YELLOW}Próximos pasos:${NC}"
echo ""
echo "1. Lee el archivo SETUP_CHECKLIST.md"
echo ""
echo "2. Para cada archivo [PLACEHOLDER]:"
echo "   a) Abre el archivo"
echo "   b) Copia el contenido real desde la documentación"
echo "   c) Reemplaza TODO el contenido del archivo"
echo ""
echo "3. Valida que no hay errores:"
echo ""
echo -e "   ${CYAN}# Validar JSON${NC}"
echo "   python3 -m json.tool src/main/resources/fabric.mod.json"
echo ""
echo "4. Compila el proyecto:"
echo ""
echo -e "   ${CYAN}./gradlew clean build${NC}"
echo ""
echo "5. Instala en Minecraft:"
echo ""
echo -e "   ${CYAN}cp build/libs/*.jar ~/.minecraft/mods/${NC}"
echo ""

echo -e "${GREEN}═══════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✓ Instalación completada correctamente${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════════════${NC}"
echo ""

exit 0
