import type { NoteRequest } from '@/types/NoteType'

import axiosClient from "@/lib/axiosClient";

export const getAll = async (spaceId: string) => {
  const res = await axiosClient.get(`/api/spaces/${spaceId}/notes`);

  return res.data;
}

export const getById = async (spaceId: string, noteId: string) => {
 const res = await axiosClient.get(`/api/spaces/${spaceId}/notes/${noteId}`);

 return res.data;
}

export const create = async (spaceId: string, request: NoteRequest ) =>{
 const res = await axiosClient.post(`/api/spaces/${spaceId}/notes`, request);

 return res.data;
}

export async function archiveNote(
  spaceId: string,
  noteId: string
) {
  const res = await axiosClient.patch(
    `/api/spaces/${spaceId}/notes/${noteId}/archive`
  )

  return res.data
}

export const update = async (spaceId: string, id: string, request: NoteRequest  ) => {
  const res = await axiosClient.put(`/api/spaces/${spaceId}/notes/${id}`, request);

  return res.data;
}

export const deleteNote = async ( spaceId: string, id: string   ) => {
  const res = await axiosClient.delete(`/api/spaces/${spaceId}/notes/${id}`);

  return res.data;
}

export const togglePin = async ( spaceId: string, id: string  ) => {
  const res = await axiosClient.patch(`/api/spaces/${spaceId}/notes/${id}/pin`);

  return res.data;
}

export const search = async ( spaceId: string, keyword: string  ) => {
  const res = await axiosClient.get(`/api/spaces/${spaceId}/notes/search?keyword=${keyword}`);

  return res.data;
}

export const updatePosition = async (
  spaceId: string,
  id: string,
  data: { posX: number; posY: number; width: number; height: number }
) => {
  const res = await axiosClient.patch(
    `/api/spaces/${spaceId}/notes/${id}/position`,
    data
  )
  return res.data
}
export const setReminder = async (
  spaceId: string,
  id: string,
  reminderAt: string | null
) => {
  const res = await axiosClient.patch(
    `/api/spaces/${spaceId}/notes/${id}/reminder`,
    { reminderAt }
  )
  return res.data
}