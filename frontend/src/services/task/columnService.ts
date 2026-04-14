import axiosClient from "@/lib/axiosClient"

export const getColumns = async (boardId: string, columnId: string) => {
    const res = await axiosClient.get(`/api/board/${boardId}/columns/${columnId}`);
    return res;
}

