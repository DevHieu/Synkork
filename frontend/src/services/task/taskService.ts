import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_BACKEND_URL + '/api'
});

export const taskApi = {
    // Lấy toàn bộ Board
    getBoard: (spaceId: string) => api.get(`/spaces/${spaceId}/board`),

    // Columns
    createColumn: (spaceId: string, name: string) => 
        api.post(`/spaces/${spaceId}/columns`, { name }),
    
    updateColumn: (spaceId: string, colId: string, name: string) => 
        api.put(`/spaces/${spaceId}/columns/${colId}`, { name }),
    
    deleteColumn: (spaceId: string, colId: string) => 
        api.delete(`/spaces/${spaceId}/columns/${colId}`),

    // Tasks
    createTask: (spaceId: string, task: any) => 
        api.post(`/spaces/${spaceId}/tasks`, task),
    
    updateTask: (spaceId: string, taskId: string, task: any) => 
        api.put(`/spaces/${spaceId}/tasks/${taskId}`, task),
    
    deleteTask: (spaceId: string, taskId: string) => 
        api.delete(`/spaces/${spaceId}/tasks/${taskId}`)
};