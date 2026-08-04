import axiosClient from "@/lib/axiosClient"
import type { ColumnEvent, ColumnRequest } from "@/types/Task"
import axios from "axios"

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

export const updateColumn = async (spaceId: string, columnId: string, data : ColumnRequest) => {
    try {
        const res = await axiosClient.put(`/api/space/${spaceId}/column/${columnId}`, data);
        return res;
    } catch (e: any) {
        if(axios.isAxiosError(e) && e.response?.status === 409) {
           throw new ColumnVersionConflictError(e.response.data?.latest)
        }
        throw e;
    } 
}

export const deleteColumn = async (spaceId: string, columnId: string) => {
    const res = await axiosClient.delete(`/api/space/${spaceId}/column/${columnId}`);
    return res;
}

export const moveColumn = async (spaceId: string, columnId: string, newPosition: number) => {
    const res = await axiosClient.patch(`/api/space/${spaceId}/column/${columnId}/move`, { newPosition });
    return res;
}

export const archiveColumn = (spaceId: string, columnId: string) => {
    const res = axiosClient.patch(`/api/space/${spaceId}/column/${columnId}/archive`);
    return res;
}

export const unarchiveColumn = (spaceId: string, columnId: string) => {
    const res = axiosClient.patch(`/api/space/${spaceId}/column/${columnId}/unarchive`);
    return res;
}

export const getArchivedColumns = (spaceId: string) => {
    const res = axiosClient.get(`/api/space/${spaceId}/column/archived`);
    return res;
}

export const deleteAllArchivedColumns = (spaceId: string) => {
    const res = axiosClient.delete(`/api/space/${spaceId}/column/archived/all`);
    return res;
}

export class ColumnVersionConflictError extends Error {
    public latest: ColumnEvent
    constructor(latest: ColumnEvent) {
        super("VERSION_CONFLICT")
        this.name = "ColumnVersionConflictError"
        this.latest = latest
        Object.setPrototypeOf(this, ColumnVersionConflictError.prototype)
    }
}

