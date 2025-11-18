export const calculateLastVisitDate = (
  visitDayAgo: number,
  baseDateString?: string
) => {
  const today = baseDateString ? new Date(baseDateString) : new Date();
  const lastVisitDate = new Date(today);
  lastVisitDate.setDate(today.getDate() - visitDayAgo);
  return `${lastVisitDate.getFullYear()}.${(lastVisitDate.getMonth() + 1)
    .toString()
    .padStart(2, "0")}.${lastVisitDate
    .getDate()
    .toString()
    .padStart(2, "0")}`;
};

export const formatPhoneNumber = (phoneNumber: string): string => {
  if (!phoneNumber) return "";
  const cleaned = phoneNumber.replace(/\D/g, ""); // Remove all non-digit characters
  const len = cleaned.length;

  if (len === 11) { // e.g., 01012345678
    return cleaned.replace(/(\d{3})(\d{4})(\d{4})/, "$1-$2-$3");
  } else if (len === 10) { // e.g., 0101234567 (older 01x numbers or some landlines)
    if (cleaned.startsWith("02")) { // Seoul landline
      return cleaned.replace(/(\d{2})(\d{3,4})(\d{4})/, "$1-$2-$3");
    }
    return cleaned.replace(/(\d{3})(\d{3})(\d{4})/, "$1-$2-$3");
  } else if (len === 9 && cleaned.startsWith("02")) { // Seoul landline 9 digits
    return cleaned.replace(/(\d{2})(\d{3})(\d{4})/, "$1-$2-$3");
  }
  // Return as is if not a standard format
  return phoneNumber;
};
