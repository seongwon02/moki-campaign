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
