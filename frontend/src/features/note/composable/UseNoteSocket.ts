import { useNoteStore } from '@/features/note/stores/noteStore'
import { noteSocket } from '@/features/note/services/noteSocket'
import { socketService } from '@/services/websocket/socketService'

export function useNoteSocket() {
  const store = useNoteStore()

  async function connect(spaceId: string) {
    await socketService.connect()

    noteSocket.subscribeCreateNote(spaceId, (payload) => store.addNote(payload))
    noteSocket.subscribeDeleteNote(spaceId, (payload) => store.removeNote(payload))
    noteSocket.subscribeUpdateNote(spaceId, (payload) => store.replaceNote(payload))
    noteSocket.subscribetogglePin(spaceId, (payload) => {
      store.replaceNote(payload)
      store.sortByPinned()
    })
  }

  function disconnect(spaceId: string) {
    noteSocket.unsubscribeAll(spaceId)
  }

  return { connect, disconnect }
}