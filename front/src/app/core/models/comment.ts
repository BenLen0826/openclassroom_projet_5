/** Commentaire tel que renvoyé par l'API (aligné sur `CommentDto` côté back). */
export interface Comment {
  id: number;
  content: string;
  /** Date ISO 8601 en UTC. */
  createdAt: string;
  authorUsername: string;
}
