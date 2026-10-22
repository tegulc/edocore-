/**
 * Le contrat d'API EDUCORE (Volume 6, Documentation API) impose le snake_case
 * sur le fil (mot_de_passe, jeton_rafraichissement...). Le code frontend reste
 * en camelCase idiomatique TypeScript ; ces utilitaires font la conversion
 * automatiquement aux frontieres (intercepteurs axios, voir lib/api.ts), pour
 * qu'aucune page/composant n'ait a s'en soucier.
 */

function estObjetSimple(valeur: unknown): valeur is Record<string, unknown> {
  return typeof valeur === 'object' && valeur !== null && !Array.isArray(valeur) && !(valeur instanceof Date) && !(valeur instanceof File);
}

export function versSnakeCase(valeur: unknown): unknown {
  if (Array.isArray(valeur)) return valeur.map(versSnakeCase);
  if (estObjetSimple(valeur)) {
    return Object.fromEntries(
      Object.entries(valeur).map(([cle, v]) => [
        cle.replace(/[A-Z]/g, (lettre) => `_${lettre.toLowerCase()}`),
        versSnakeCase(v),
      ])
    );
  }
  return valeur;
}

export function versCamelCase(valeur: unknown): unknown {
  if (Array.isArray(valeur)) return valeur.map(versCamelCase);
  if (estObjetSimple(valeur)) {
    return Object.fromEntries(
      Object.entries(valeur).map(([cle, v]) => [
        cle.replace(/_([a-z0-9])/g, (_, lettre: string) => lettre.toUpperCase()),
        versCamelCase(v),
      ])
    );
  }
  return valeur;
}
