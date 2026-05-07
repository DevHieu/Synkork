export interface CardEvent {
  id: string;
  title: string;
  description: string;
  columnId: string;
  position: number;
  createdAt?: string;
  date?: string;
  user?: { name: string };
  
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