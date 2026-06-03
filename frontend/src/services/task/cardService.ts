import axiosClient from "@/lib/axiosClient"

export const getCards = async (columnId: string, cardId: string) => {
    const res = await axiosClient.get(`/api/column/${columnId}/card/${cardId}`)
    return res.data;
}

export const createCard = async (spaceId: string, data: { columnId: string; title: string; description: string }) => {
    const res = await axiosClient.post(`/api/space/${spaceId}/card`, data);
    return res.data;
}

export const updateCard = async (spaceId: string | null, cardId: string | null, data: { title: string | null; description: string | null; assigneeIds?: string[], dueDate?: string }) => {
    const res = await axiosClient.put(`/api/space/${spaceId}/card/${cardId}`, data);
    return res.data;
}

export const deleteCard = async (spaceId: string, cardId: string) => {
    const res = await axiosClient.delete(`/api/space/${spaceId}/card/${cardId}`);
    return res.data;
}

export const moveCard = async (spaceId: string, cardId: string, moveData: { targetColumnId: string; newPosition: number }) => {
    const res = await axiosClient.patch(`/api/space/${spaceId}/card/${cardId}/move`, moveData);
    return res.data;
}

export const getSpaceMembers = async (spaceId: string) => {
    const res = await axiosClient.get(`/api/space/${spaceId}/members`);
    return res.data;
}

export const archiveCard = async (spaceId: string, cardId: string) => {
    const res = await axiosClient.patch(`/api/space/${spaceId}/card/${cardId}/archive`);
    return res.data;
}

export const unarchiveCard = async (spaceId: string, cardId: string) => {
    const res = await axiosClient.patch(`/api/space/${spaceId}/card/${cardId}/unarchive`);
    return res.data;
}

export const getArchivedCards = async (spaceId: string) => {
    const res = await axiosClient.get(`/api/space/${spaceId}/card/archived`);
    return res.data;
}

export const deleteAllArchivedCards = async (spaceId: string) => {
    const res = await axiosClient.delete(`/api/space/${spaceId}/card/archived/all`);
    return res.data;
}
