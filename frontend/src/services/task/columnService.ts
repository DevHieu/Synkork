import axiosClient from "@/lib/axiosClient"

export const getColumns = async (spaceId: string, columnId: string) => {
    const res = await axiosClient.get(`/api/space/${spaceId}/column/${columnId}`);
    return res;
}

export const getAllColumns = async (spaceId: string) => {
    const res = await axiosClient.get(`/api/space/${spaceId}/column`);
    return res;
}

export const getAllColumnsWithoutCard = async (spaceId: string) => {
    const res = await axiosClient.get(`/api/space/${spaceId}/column?includeCards=false`);
    return res;
}

export const createColumn = async (spaceId: string, columnName: string) => {
    const res = await axiosClient.post(`/api/space/${spaceId}/column`, {
        name: columnName
    });
    return res;
}

export const updateColumn = async (spaceId: string, columnId: string, title: string) => {
    const res = await axiosClient.put(`/api/space/${spaceId}/column/${columnId}`, {
        name: title
    });
    return res;
}

export const deleteColumn = async (spaceId: string, columnId: string) => {
    const res = await axiosClient.delete(`/api/space/${spaceId}/column/${columnId}`);
    return res;
}

export const moveColumn = async (spaceId: string, columnId: string, newPosition: number) => {
    const res = await axiosClient.patch(`/api/space/${spaceId}/column/${columnId}/move`, { newPosition });
    return res;
}

