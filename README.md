# Sims 3 Extended – Android Natif (Kotlin)

Prototype de simulation de vie type **The Sims 3**, en **Android natif Kotlin + Gradle**.

## Pourquoi ce projet ?

Plus de Godot → plus d’erreurs d’export template.  
Structure classique Android = GitHub Actions fiable (comme ton exemple ZERNEX).

## Modules conservés (logique GDAD)

| Module | Status |
|--------|--------|
| TimeManager (horloge 1s = 1 min jeu) | ✅ |
| WorldManager (registre global) | ✅ |
| Sim Core (8 besoins, traits, actions) | ✅ |
| SmartObjects (lit, frigo, canapé…) | ✅ |
| Utility AI | ✅ |
| Rendu 2.5D (Canvas) | ✅ |
| UI besoins en temps réel | ✅ |
| APK 32-bit + 64-bit via GHA | ✅ |

## Contrôles
- **Touche** un objet → le Sim interagit
- **Touche** le sol → le Sim se déplace
- Les besoins baissent avec le temps

## GitHub Actions

À chaque push / Run workflow :
1. `./gradlew assembleDebug`
2. Upload artifact **Sims3Extended-debug**

Télécharge l’APK depuis l’onglet **Actions** → Artifacts.

## Build local

```bash
./gradlew assembleDebug
# APK : app/build/outputs/apk/debug/app-debug.apk
```

## Structure

```
Sims3-Native/
├── app/src/main/java/com/sims3/extended/
│   ├── core/       (Sim, TimeManager, WorldManager)
│   ├── ai/         (UtilityAI)
│   ├── objects/    (SmartObject)
│   ├── render/     (GameView)
│   └── MainActivity.kt
├── .github/workflows/build-android.yml
└── gradlew
```

## Licence
Prototype éducatif. « The Sims » est une marque d’Electronic Arts.
