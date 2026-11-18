/** Article tel que renvoyé par l'API (aligné sur `PostDto` côté back). */
export interface Post {
  id: number;
  title: string;
  content: string;
  /** Date ISO 8601 en UTC, affichée dans le fuseau du navigateur. */
  createdAt: string;
  authorUsername: string;
  topicId: number;
  topicName: string;
}

/** Données du formulaire de création (l'auteur et la date sont définis par le serveur). */
export interface CreatePostRequest {
  topicId: number;
  title: string;
  content: string;
}
