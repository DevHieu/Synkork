export interface CardEvent {
  id: string;
  title: string;
  description: string;
  columnId: string;
  position: number;
  createdAt?: string;
  
  createdBy: MemberSummary 
  assignees: MemberSummary[]
  dueDate?: string | undefined
  
}

export interface UserSummary {
  id: string
  name: string
  avatarUrl: string | undefined
}

export interface MemberSummary {
  id: string
  name: string
  avatarUrl?: string
}

export interface SpaceMemberDTO {
    id: string
    name: string
    avatarUrl: string | null
    role: string
}

export interface ColumnEvent {
  id: string;
  name: string;
  position: number;
  cards: CardEvent[];
}

export interface TaskMoveEvent {
  moved?: {
    element: ColumnEvent | CardEvent;
    newIndex: number;
    oldIndex: number;
  };
  added?: {
    element: CardEvent;
    newIndex: number;
  };
  removed?: {
    element: CardEvent;
    oldIndex: number;
  };
}

// Thêm type
export interface CardMovePayload {
    targetColumnId: string
    sourceColumnId: string | null
    targetCards: CardEvent[]
    sourceCards: CardEvent[] | null
}