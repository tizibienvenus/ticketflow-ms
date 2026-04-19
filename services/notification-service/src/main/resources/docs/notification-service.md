# Notification Service

Le service `notification-service` centralise l'envoi des notifications CamerGo sur plusieurs canaux et propose une entree unique pour declencher les traitements synchrones et asynchrones.

## Responsabilites

- envoyer des emails bases sur des templates Thymeleaf;
- envoyer des notifications push via Firebase Cloud Messaging;
- envoyer des SMS via Twilio;
- publier des messages WebSocket vers les utilisateurs connectes;
- traiter des notifications unitaires en HTTP et des lots via Kafka.

## Endpoints REST

### `POST /api/v1/notifications/send`

Accepte une notification unitaire et la traite immediatement via le canal correspondant au champ `type`.

### `POST /api/v1/notifications/bulk`

Accepte une liste de notifications, puis publie chaque demande sur le topic Kafka `notifications` pour un traitement asynchrone.

### `GET /documentation/guide.md`

Retourne ce guide au format `text/markdown`.

## OpenAPI

- specification OpenAPI: `/api/v3/api-docs`
- Swagger UI local: desactive dans la configuration actuelle (`springdoc.swagger-ui.enabled=false`)
- cette description Markdown reste visible dans les consommateurs de la specification OpenAPI, notamment l'aggregateur de documentation

## Canaux supportes

| Type | Canal | Description |
| --- | --- | --- |
| `EMAIL` | SMTP + Thymeleaf | Envoi d'emails HTML avec variables et pieces jointes |
| `PUSH` | Firebase Cloud Messaging | Envoi vers un token mobile avec titre, corps, image et data |
| `SMS` | Twilio | Envoi de messages texte a un numero valide |
| `WEBSOCKET` | WebSocket interne | Push temps reel vers un utilisateur et une destination cible |

## Flux de traitement

1. Le controller REST recoit un `NotificationRequestDto`.
2. `NotificationRequestFactory` convertit le DTO vers un `NotificationRequest` specialise.
3. `NotificationServiceImpl` choisit le canal en fonction du `NotificationType`.
4. Pour les envois bulk, chaque requete est publiee sur Kafka dans le topic `notifications`.
5. `NotificationKafkaConsumer` consomme ces messages avec retry et redirige les echecs finaux vers un topic DLT.

## Exemple de payload

```json
{
  "type": "EMAIL",
  "recipient": "user@example.com",
  "subject": "Bienvenue sur CamerGo",
  "template": "WELCOME_PLATFORM",
  "variables": {
    "firstName": "Amina",
    "activationCode": "123456"
  },
  "attachments": [],
  "metadata": {
    "source": "identity-service"
  }
}
```

## Templates emails disponibles

- `invoice-ride`
- `kyc-not-validated`
- `kyc-validated`
- `loyalty-points`
- `order-cancelled`
- `order-confirmation`
- `order-delivered`
- `order-on-the-way`
- `order-preparation`
- `otp-email`
- `payment-failed`
- `payment-success`
- `promo-offer`
- `referral-notification`
- `security-alert`
- `system-update`
- `ticket-received`
- `wallet-topup`
- `welcome-platform`

## Configuration utile

```yaml
server:
  port: 8040

swagger:
  enabled: true

springdoc:
  api-docs:
    path: /api/v3/api-docs
  swagger-ui:
    enabled: false

spring:
  mail:
    host: smtp.gmail.com
    port: 587
```

## Points d'attention

- en bulk, la reponse HTTP `202 Accepted` confirme la prise en charge, pas la livraison finale;
- le choix du canal depend strictement du `type`, sinon une erreur est levee;
- les templates emails doivent rester coherents avec les variables fournies dans `variables`;
- les messages en echec apres les retries Kafka sont rediriges vers le topic dead-letter associe.
