import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';

// HttpClient est fourni par défaut en Angular 22 (avec l'API fetch) : provideHttpClient()
// ne sera ajouté qu'à l'étape 5, pour brancher les intercepteurs (JWT, erreurs).
export const appConfig: ApplicationConfig = {
  providers: [provideBrowserGlobalErrorListeners(), provideRouter(routes)],
};
