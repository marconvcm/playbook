export type Post = {
  id: number;
  userId: number;
  parentId: number;
  groupId: number;
  title: string;
  content: string;
  requestHash: string;
  createdAt: string;
  updatedAt: string;
  personalPost: boolean;
  groupPost: boolean;
  groupTopLevelPost: boolean;
  answerPost: boolean;
};
