rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Règles pour la collection users
    match /users/{userId} {
      // Permettre la lecture et l'écriture uniquement si l'utilisateur est authentifié
      // et qu'il accède à ses propres données
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Permettre la création si l'utilisateur est authentifié
      allow create: if request.auth != null;
    }
    
    // Par défaut, refuser tout accès aux autres collections
    match /{document=**} {
      allow read, write: if false;
    }
  }
}

