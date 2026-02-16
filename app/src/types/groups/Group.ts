export type Group = {
  id: number;
  groupId: string;
  ownerId: number;
  title: string;
  summary: string;
  isPrivate: boolean;
  href: string;
  joinHref: string;
  postsHref: string;
};
