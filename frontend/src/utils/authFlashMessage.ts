const AUTH_FLASH_MESSAGE_KEY = "synkork_auth_flash_message";

export const setAuthFlashMessage = (message: string) => {
  if (typeof window === "undefined") return;

  const normalizedMessage = message.trim();
  if (!normalizedMessage) return;

  localStorage.setItem(AUTH_FLASH_MESSAGE_KEY, normalizedMessage);
};

export const consumeAuthFlashMessage = () => {
  if (typeof window === "undefined") return "";

  const message = localStorage.getItem(AUTH_FLASH_MESSAGE_KEY) ?? "";
  localStorage.removeItem(AUTH_FLASH_MESSAGE_KEY);

  return message;
};
