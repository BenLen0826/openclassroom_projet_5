/** Thème tel que renvoyé par `GET /api/topics` (aligné sur `TopicDto` côté back). */
export interface Topic {
  id: number;
  name: string;
  description: string;
  /** `true` si l'utilisateur connecté suit ce thème. */
  subscribed: boolean;
}
