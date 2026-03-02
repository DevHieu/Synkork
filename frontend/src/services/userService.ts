import axiosClient from "@/lib/axiosClient";

export const getUserInfo = async () => {
    try {
        const response = await axiosClient.get("/api/users/me");
        console.log(response);
        
        return response;
    } catch (error) {
        console.error("Error fetching user info:", error);
        throw error;
    }
};