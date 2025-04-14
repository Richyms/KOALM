# KOALM - Aplicación de Seguimiento de Hábitos

KOALM es una aplicación móvil desarrollada en Kotlin para Android que ayuda a los usuarios a realizar un seguimiento de sus hábitos y rutinas diarias.

## 📋 Requisitos Previos

Antes de comenzar, necesitarás:

1. **Android Studio** (versión recomendada: 2023.1.1 o superior)
   - Descarga desde: [Android Studio](https://developer.android.com/studio)
   - Instalación mínima: 8GB RAM, 8GB espacio en disco

2. **Java Development Kit (JDK)** 11 o superior
   - Descarga desde: [JDK](https://www.oracle.com/java/technologies/downloads/)

3. **Dispositivo Android** o emulador
   - Mínimo Android 12 (API 31)
   - Recomendado: Android 13 o superior

## 🚀 Instalación

### Para usuarios sin experiencia en Android Studio:

1. **Instalar Android Studio**
   - Descarga el instalador desde el enlace proporcionado
   - Ejecuta el instalador y sigue las instrucciones
   - Durante la instalación, selecciona "Standard" cuando se te pregunte

2. **Configurar el proyecto**
   - Abre Android Studio
   - Selecciona "Open an existing project"
   - Navega hasta la carpeta del proyecto y selecciónala
   - Espera a que Android Studio sincronice el proyecto (puede tomar varios minutos)

3. **Configurar el emulador**
   - En Android Studio, ve a "Tools" > "Device Manager"
   - Haz clic en "Create Device"
   - Selecciona un teléfono (por ejemplo, Pixel 6)
   - Selecciona una imagen del sistema (API 31 o superior)
   - Completa la configuración del emulador

### Para usuarios con experiencia:

```bash
# Clonar el repositorio
git clone https://github.com/Richyms/KOALM.git

# Abrir el proyecto en Android Studio
# O usar Gradle directamente:
./gradlew build
```

## 🔧 Compilación y Ejecución

1. **Compilar el proyecto**
   - En Android Studio, haz clic en el botón "Make Project" (martillo)
   - O usa el comando: `./gradlew assembleDebug`

2. **Ejecutar la aplicación**
   - Conecta tu dispositivo Android o inicia el emulador
   - Haz clic en el botón "Run" (triángulo verde)
   - Selecciona tu dispositivo o emulador
   - La aplicación se instalará y ejecutará automáticamente

## 📱 Características de la Aplicación

- Personalización de perfil de usuario
- Seguimiento de hábitos
- Interfaz intuitiva con Material Design 3
- Navegación entre pantallas
- Almacenamiento local de datos

## 🛠️ Estructura del Proyecto

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/koalm/
│   │   │   ├── PersonalizarPerfil.kt
│   │   │   └── ... (otros archivos)
│   │   └── res/
│   │       ├── drawable/
│   │       ├── layout/
│   │       └── values/
├── build.gradle.kts
└── ...
```


## 📞 Soporte

Si encuentras algún problema o tienes preguntas:
- Abre un issue en el repositorio
- Contacta al equipo de desarrollo
