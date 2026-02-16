export type GroupMember = {
  id: number;
  groupId: number;
  group: Group;
  userId: number;
  user: User;
  isAdmitted: boolean;
};
