# Calendar App - Brazilian Holidays

A modern Android application built with Jetpack Compose to visualize national, state, and municipal holidays in Brazil based on the user's location.

## 🇧🇷 Português

### Descrição
Aplicativo de calendário que identifica automaticamente a localização do usuário para exibir feriados específicos (Estaduais e Municipais), além dos feriados nacionais brasileiros.

### Tecnologias
- **Kotlin** & **Jetpack Compose** (UI)
- **Material 3**
- **ViewModel & StateFlow** (Gerenciamento de estado)
- **Google Play Services Location** (GPS)
- **Geocoder API** (Reverse Geocoding para identificar UF/Cidade)
- **Accompanist Permissions** (Gestão de permissões em Compose)

### Recursos
- Detecção automática de Estado (UF) e Cidade via GPS.
- Seleção manual de Estado e Ano (2000-2050).
- Cálculo dinâmico de feriados móveis (Carnaval, Páscoa, etc).
- Suporte a feriados estaduais de todos os 26 estados + DF.

---

## 🇺🇸 English

### Description
A calendar application that automatically identifies the user's location to display specific regional holidays (State and Municipal) in addition to Brazilian national holidays.

### Tech Stack
- **Kotlin** & **Jetpack Compose** (UI)
- **Material 3**
- **ViewModel & StateFlow** (State management)
- **Google Play Services Location** (GPS)
- **Geocoder API** (Reverse Geocoding to identify State/City)
- **Accompanist Permissions** (Permission handling in Compose)

### Features
- Automatic detection of State (UF) and City via GPS.
- Manual State and Year selection (Range: 2000-2050).
- Dynamic calculation of mobile holidays (Carnival, Easter, etc).
- Support for state holidays across all 26 Brazilian states + DF.

## Build Requirements
- Android Studio Ladybug or newer.
- JDK 17+.
- Android SDK 34 (API Level).
