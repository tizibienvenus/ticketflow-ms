# Identity Service Authentication Guide

Ce guide documente uniquement les parcours d'authentification de `identity-service`.

La gestion des utilisateurs, des roles, des groupes et les operations d'administration Keycloak ne font pas partie de ce document.

## Perimetre

- authentification par OTP;
- rafraichissement des tokens;
- enregistrement WebAuthn pour un utilisateur deja authentifie;
- connexion WebAuthn / passkey;
- emission de tokens JWT via Keycloak.

## Endpoints REST d'authentification

### `POST /api/v1/auth/otp/request`

Demande l'envoi d'un code OTP vers un identifiant.

- si `identifier` contient `@`, le service prepare un email;
- sinon, le service prepare un SMS;
- l'envoi est delegue au systeme de notification via Kafka.

Parametre:

- `identifier`: email ou numero de telephone.

### `POST /api/v1/auth/login`

Valide un OTP et retourne des tokens d'acces et de rafraichissement.

Parametres:

- `identifier`: email ou numero de telephone;
- `code`: OTP recu par email ou SMS.

### `POST /api/v1/auth/refresh`

Retourne un nouveau couple de tokens a partir d'un refresh token valide.

Payload:

```json
{
  "token": "refresh-token-value"
}
```

## Endpoints WebAuthn

### `POST /api/v1/auth/webauthn/register/start`

Genere les options de creation d'un credential WebAuthn pour l'utilisateur authentifie courant.

Ce endpoint necessite un token Bearer valide.

### `POST /api/v1/auth/webauthn/register/complete`

Finalise l'enregistrement du credential WebAuthn et l'associe a l'utilisateur authentifie.

Ce endpoint necessite un token Bearer valide.

### `POST /api/v1/auth/webauthn/login/start`

Genere un challenge de connexion WebAuthn.

- avec `identifier`, la requete est restreinte aux credentials de cet utilisateur;
- sans `identifier`, l'authenticator peut identifier l'utilisateur pendant l'assertion.

### `POST /api/v1/auth/webauthn/login/complete`

Verifie la reponse de l'authenticator, met a jour le compteur de signature, puis genere des tokens JWT via Keycloak.

## Flux OTP

1. Le client appelle `/api/v1/auth/otp/request`.
2. Le service genere un OTP a 6 chiffres avec une expiration courte.
3. L'OTP est stocke cote service sous forme chiffree.
4. Une notification est emise via Kafka.
5. Le client soumet `identifier` et `code` sur `/api/v1/auth/login`.
6. Si le code est valide, le service obtient des tokens Keycloak et retourne un `TokenResponse`.

## Flux WebAuthn / Passkey

1. Un utilisateur deja authentifie lance `/api/v1/auth/webauthn/register/start`.
2. Le front cree un credential WebAuthn a partir des options renvoyees.
3. Le front poste le credential sur `/api/v1/auth/webauthn/register/complete`.
4. Pour se reconnecter, le client appelle `/api/v1/auth/webauthn/login/start`.
5. Le front signe le challenge avec le passkey.
6. Le credential signe est envoye a `/api/v1/auth/webauthn/login/complete`.
7. Si la verification reussit, le service genere les tokens applicatifs.

## Reponse token

Les endpoints de login OTP, refresh et login WebAuthn retournent un objet `TokenResponse` contenant notamment:

- `access_token`
- `refresh_token`
- `token_type`
- `expires_in`
- `refresh_expires_in`
- `issued_at`
- `expires_at`

## Exemple OTP request

```http
POST /api/v1/auth/otp/request?identifier=user@example.com
```

## Exemple login OTP

```http
POST /api/v1/auth/login?identifier=user@example.com&code=123456
```

## Exemple WebAuthn login start

```http
POST /api/v1/auth/webauthn/login/start?identifier=user@example.com
```

## Configuration utile

```yaml
server:
  port: 8081

swagger:
  enabled: true

springdoc:
  api-docs:
    path: /api/v3/api-docs

keycloak:
  realm: camergo

webauthn:
  relying-party:
    id: localhost
```

## Documentation exposee

- OpenAPI: `/api/v3/api-docs`
- Swagger UI: `/swagger-ui.html`
- guide Markdown brut: `/documentation/guide.md`

## Notes importantes

- ce guide exclut volontairement les endpoints de gestion des utilisateurs et l'administration Keycloak;
- l'OTP est route par email ou SMS selon le format de l'identifiant;
- l'enregistrement WebAuthn suppose qu'un utilisateur soit deja authentifie;
- la connexion WebAuthn se termine par une emission de tokens via Keycloak, pas par un token local genere manuellement.
