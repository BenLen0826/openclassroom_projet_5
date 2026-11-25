import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Accueil d'un visiteur non connecté : accès à la connexion et à l'inscription. */
@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {}
