# Serveur XMPP `contacts.chaumont.me`

## Observations publiques du 21 août 2026

Ces vérifications ne nécessitent ni compte ni mot de passe.

| Élément | Observation |
|---|---|
| Domaine XMPP | `contacts.chaumont.me` |
| IPv4 | `82.67.146.209` |
| IPv6 | aucune adresse AAAA annoncée |
| SRV client STARTTLS | aucun enregistrement `_xmpp-client._tcp` |
| SRV client TLS direct | aucun enregistrement `_xmpps-client._tcp` |
| Repli utilisé | domaine direct, port 5222, STARTTLS |
| TLS | TLS 1.3, `TLS_AES_256_GCM_SHA384` lors du contrôle |
| Certificat | valide pour `contacts.chaumont.me`, émis par Let’s Encrypt YR2 |
| Validité observée | 6 août 2026 au 4 novembre 2026 |
| SASL pré-authentification | SCRAM-SHA-512/256/1, DIGEST-MD5, PLAIN, X-OAUTH2 |
| Création de compte | `jabber:iq:register` annoncé avant authentification |

L’absence actuelle de SRV n’empêche pas la connexion : Conversations retombe
sur le domaine et le port XMPP standard. Il reste recommandé de publier un SRV,
par exemple :

```dns
_xmpp-client._tcp.contacts.chaumont.me. 3600 IN SRV 0 5 5222 contacts.chaumont.me.
```

Ne publiez cette valeur qu’après vérification qu’elle correspond réellement au
service exploité.

## Capacités après authentification

MAM, PEP/OMEMO, HTTP Upload, Carbons, Stream Management, blocage, CSI, Push,
Bind 2, SASL 2 et découverte des services externes ne peuvent pas être confirmés
honnêtement sans compte de test. Maer Chat interroge les capacités Disco du
compte connecté et les affiche dans « Compte > Informations serveur ».

Les fonctions restent conditionnelles : l’absence d’une capacité masque ou
désactive l’action concernée. Les principales correspondances sont :

| Fonction | Extension ou service attendu |
|---|---|
| Historique synchronisé | XEP-0313 MAM |
| Multi-appareils cohérents | XEP-0280 Carbons, XEP-0198 SM |
| OMEMO | PEP/XEP-0163 et nœuds OMEMO compatibles Conversations |
| Pièces jointes asynchrones | XEP-0363 HTTP File Upload en HTTPS |
| Blocage | XEP-0191 |
| Notifications push | XEP-0357 + XEP-0198 + relais propre à Maer Chat |
| Appels fiables hors LAN | XEP-0215 et services STUN/TURN valides |

## Appels audio/vidéo

Le moteur Jingle/WebRTC reste présent, mais un bouton d’appel ne doit être
proposé que si le correspondant annonce les capacités nécessaires. Pour une
connectivité réelle à travers NAT et réseaux mobiles, configurez au minimum un
service STUN et, de préférence, TURN authentifié annoncé par XEP-0215. Sans
compte de test et sans résultat de découverte externe, les appels ne sont pas
considérés validés pour ce serveur.

## Tests authentifiés à prévoir

Utiliser deux comptes de test dédiés, saisis sur les appareils ou fournis par un
coffre CI, pour vérifier MAM, OMEMO multi-appareil, upload HTTPS, MUC, marqueurs,
réactions, correction, reprise XEP-0198, arrière-plan/Doze et STUN/TURN. Aucun
identifiant ne doit être ajouté au dépôt.
