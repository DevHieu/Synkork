import axios from "axios";

const CLOUDINARY_UPLOAD_URL = import.meta.env.VITE_CLOUDINARY_UPLOAD_URL;
const CLOUDINARY_UPLOAD_PRESET = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET;

export const uploadImage = async (file: any) => {
  const cloudinaryFormData = new FormData();
  cloudinaryFormData.append("file", file);
  cloudinaryFormData.append("upload_preset", CLOUDINARY_UPLOAD_PRESET);

  try {
    console.log("Uploading file to Cloudinary...");
    const uploadRes = await axios.post(
      CLOUDINARY_UPLOAD_URL,
      cloudinaryFormData,
      { withCredentials: false }
    );

    console.log(uploadRes.data);

    const finalAvatarUrl = uploadRes.data.secure_url;
    console.log("Upload thành công. URL:", finalAvatarUrl);
    return finalAvatarUrl;
  } catch (error) {
    console.error("Lỗi upload Cloudinary:", error);
    return;
  }
};
