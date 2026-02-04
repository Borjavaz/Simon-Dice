# Contexto del Proyecto: Android DAM - Borja

Este archivo es la guía de estilo para Gemini dentro de este repositorio.

## Stack Tecnológico
- **Lenguaje:** Kotlin
- **Arquitectura:** MVVM (Model-View-ViewModel) con Clean Architecture.
- **Base de Datos:** Room, SQLite, SahredPrefernce

## Reglas de Oro para Respuestas
1. **Idioma:** Háblame siempre en **español**.
2. **Nivel:** Soy Borja, estudiante de 2º de DAM. Explícame los conceptos técnicos pero ve al grano.
3. **Estructura MVVM:** - Los `ViewModels`.
4. **Explicación obligatoria:** Antes de darme el código, dime qué vas a hacer. Después del código, explícame qué has cambiado.

## Prohibiciones:**
- No mezclar lógica de negocio.
- No usar sintaxis antigua de Java a menos que Borja lo pida específicamente.


## Estructura de mi Carpeta .gemini
- Tengo prompts personalizados en `./.gemini/prompts/`.

1.   Si te pido que me generes un readme quiero que lo hagas basndote en `./.gemini/prompts/readme_gen.md`
2.   Si te pido que me generes una planificacion de un proyecto quiero que lo hagas basndote en `./.gemini/prompts/planification_ia.md`
