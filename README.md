# Application de Gestion des Cultures

Cette application Android permet de gérer et surveiller les cultures agricoles en enregistrant les informations essentielles et en utilisant les capteurs de l'appareil pour suivre les conditions environnementales.

## Fonctionnalités principales

1. **Gestion des données**
   - Affichage des cultures enregistrées avec leurs informations essentielles
   - Ajout, modification et suppression de cultures
   - Interface utilisateur intuitive avec des icônes pour chaque type de culture

2. **Vérification des champs**
   - Validation des données saisies
   - Vérification des formats (date, valeurs numériques)
   - Messages d'erreur explicites

3. **Base de données locale**
   - Stockage des données avec SQLite
   - Persistance des données même hors ligne

4. **Synchronisation avec Firebase**
   - Sauvegarde des données sur un serveur distant
   - Synchronisation automatique des modifications

5. **Capteurs intégrés**
   - Surveillance en temps réel de la température
   - Surveillance en temps réel de l'humidité
   - Affichage des conditions actuelles

6. **GPS et Géocodage**
   - Localisation précise des parcelles
   - Conversion des coordonnées GPS en adresses lisibles

## Prérequis

- Android Studio Arctic Fox ou plus récent
- Un appareil Android avec :
  - Capteurs de température et d'humidité
  - GPS
  - Connexion Internet (pour la synchronisation)

## Installation

1. Clonez le dépôt
2. Ouvrez le projet dans Android Studio
3. Configurez votre projet Firebase :
   - Créez un projet Firebase
   - Téléchargez le fichier `google-services.json`
   - Placez-le dans le dossier `app/`
4. Ajoutez votre clé API Google Maps dans `AndroidManifest.xml`
5. Compilez et installez l'application

## Utilisation

1. **Ajouter une culture**
   - Appuyez sur le bouton "+" en bas à droite
   - Remplissez le formulaire avec les informations requises
   - L'application utilisera automatiquement le GPS pour localiser la parcelle

2. **Modifier une culture**
   - Appuyez sur une culture dans la liste
   - Modifiez les informations souhaitées
   - Sauvegardez les modifications

3. **Supprimer une culture**
   - Appuyez longuement sur une culture dans la liste
   - Confirmez la suppression

4. **Surveiller les conditions**
   - Les conditions actuelles sont affichées en haut de l'écran
   - Les données sont mises à jour en temps réel

## Structure du projet

- `app/src/main/java/com/example/projetsameh/`
  - `data/` : Classes de données et DAO
  - `firebase/` : Gestion de la synchronisation Firebase
  - `location/` : Gestion de la géolocalisation
  - `viewmodel/` : ViewModels pour la gestion des données
  - `adapter/` : Adaptateurs pour les listes
  - `MainActivity.kt` : Activité principale
  - `CultureDialog.kt` : Dialogue d'ajout/modification

## Contribution

Les contributions sont les bienvenues ! N'hésitez pas à :
1. Fork le projet
2. Créer une branche pour votre fonctionnalité
3. Commiter vos changements
4. Pousser vers la branche
5. Ouvrir une Pull Request

## Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails. 