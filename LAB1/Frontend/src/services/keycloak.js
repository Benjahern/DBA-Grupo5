import Keycloak from 'keycloak-js';

const keycloakUrl = import.meta.env.VITE_KEYCLOAK_URL;

const keycloak = new Keycloak({
  url: keycloakUrl,
  realm: 'host-usach',
  clientId: 'usach-cloud-frontend',
});

export default keycloak;
